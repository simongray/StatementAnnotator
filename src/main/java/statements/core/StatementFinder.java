package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds Statements in sentences.
 */
public class StatementFinder {
    private static final Logger logger = LoggerFactory.getLogger(StatementFinder.class);
    private static SubjectFinder subjectFinder = new SubjectFinder();
    private static VerbFinder verbFinder = new VerbFinder();
    private static DirectObjectFinder directObjectFinder = new DirectObjectFinder();
    private static IndirectObjectFinder indirectObjectFinder = new IndirectObjectFinder();

    /**
     * Find statements in a sentence.
     *
     * @param sentence the sentence to look in
     * @return statements
     */
    public static Set<Statement> find(CoreMap sentence) {
        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);
        logger.info("finding statements based on dependencies: " + graph.typedDependencies());
        graph.prettyPrint();  // TODO: remove when done debugging

        // components are found independently through their own finder classes
        Set<AbstractComponent> components = new HashSet<>();
        components.addAll(subjectFinder.find(graph));
        components.addAll(verbFinder.find(graph));
        components.addAll(directObjectFinder.find(graph));
        components.addAll(indirectObjectFinder.find(graph));

        // components are not allowed to overlap
        // this is sometimes caused by errors in the dependency graph (or bugs in this algorithm)
        components = removeOverlappingComponents(components);
        logger.info("components for linking: " + components);

        Map<IndexedWord, Set<IndexedWord>> nestedStatementMapping = findNestedStatementMapping(graph);

        Set<Set<AbstractComponent>> componentsByNestingLevel = partitionByLevel(components, nestedStatementMapping);

        // statements are produced by discovering connection between the components
        Set<Statement> statements = link(componentsByNestingLevel);

        // annotate with origin
        // TODO: better way to do this?
        for (Statement statement : statements) {
            statement.setOrigin(sentence);
        }

        return statements;
    }

    /**
     * Ranks components based on subjective preference/likelihood to not be syntactically the issue.
     *
     * @param component
     * @return
     */
    private static int rank(AbstractComponent component) {
        if (component instanceof Subject) return 1;
        if (component instanceof Verb) return 2;
        if (component instanceof DirectObject) return 3;
        if (component instanceof IndirectObject) return 4;
        return -1;
    }

    /**
     * Removes components that overlap with others.
     * TODO: currently uses "rank" as last instance to decide which components to remove - might be a better way
     *
     * @param components components to modify
     * @return components with overlapping components removed
     */
    private static Set<AbstractComponent> removeOverlappingComponents(Set<AbstractComponent> components) {
        Set<AbstractComponent> reducedComponents = new HashSet<>(components);

        for (AbstractComponent component : components) {
            for (AbstractComponent otherComponent : components) {
                if (component != otherComponent && StatementUtils.intersects(component.getCompound(), otherComponent.getCompound())) {
                    if (component.contains(otherComponent)) {
                        reducedComponents.remove(otherComponent);
                        logger.error("removed " + otherComponent + " since it is contained by " + component);
                    } else if (otherComponent.contains(component)) {
                        reducedComponents.remove(component);
                        logger.error("removed " + component + " since it is contained by " + otherComponent);
                    } else if (rank(component) <= rank(otherComponent)) {
                        reducedComponents.remove(otherComponent);
                        logger.error("removed " + otherComponent + " since it overlaps with " + component);
                    }
                }
            }
        }

        return reducedComponents;
    }

    /**
     * Partition the components of a sentence into sets of components by their level within the sentence.
     * Levels are defined by relations indicating dependent clauses (such as ccomp and xcomp).
     * Partitioning allows embedded statements to be clearly separated from root statements.
     *
     * @param components
     * @param nestedStatementMapping
     * @return
     */
    private static Set<Set<AbstractComponent>> partitionByLevel(Set<AbstractComponent> components, Map<IndexedWord, Set<IndexedWord>> nestedStatementMapping)  {
        // TODO: levels should be defined as param instead of nestedStatementMapping
        logger.info("nested statement mapping: " + nestedStatementMapping);
        Set<Set<IndexedWord>> levels = new HashSet<>();
        for (Set<IndexedWord> level : nestedStatementMapping.values()) {
            levels.add(level);
        }

        // components are partitioned into levels based on where their primary word appears
        Set<Set<AbstractComponent>> componentLevels = new HashSet<>();
        for (Set<IndexedWord> level : levels) {
            Set<AbstractComponent> componentLevel = new HashSet<>();
            for (AbstractComponent component : components) {
                if (level.contains(component.getPrimary())) componentLevel.add(component);
            }
            componentLevels.add(componentLevel);
        }
        logger.info("component levels found: " + componentLevels.size());

        for (Set<AbstractComponent> componentLevel : componentLevels) {
            logger.info("component level: " + componentLevel);
        }

        return componentLevels;
    }

    private static Map<IndexedWord, Set<IndexedWord>> findNestedStatementMapping(SemanticGraph graph) {
        Map<IndexedWord, Set<IndexedWord>> nestedStatementMapping = new HashMap<>();

        // which words are entry points for nested statements?
        // use these to find the exact scope of the statements
        for (TypedDependency dependency : graph.typedDependencies()) {
            if (Relations.EMBEDDED_STATEMENT_SCOPES.contains(dependency.reln().getShortName())) {
                // TODO: ignore dependent clauses
                nestedStatementMapping.put(
                        dependency.dep(),
                        StatementUtils.findCompound(dependency.dep(), graph, Relations.EMBEDDED_STATEMENT_SCOPES, null)
                );
            }
        }

        // the remaining vertices in the graph comprise the root level (= non-embedded statements) of the sentence
        Set<IndexedWord> rootLevel = new HashSet<>(graph.vertexSet());
        for (Set<IndexedWord> level : nestedStatementMapping.values()) {
            rootLevel.removeAll(level);
        }
        nestedStatementMapping.put(rootLevel.iterator().next(), rootLevel);

        logger.info("nested statement mapping before changes: " + nestedStatementMapping);

        Set<IndexedWord> fullyContainedScopes = new HashSet<>();
        Map<IndexedWord, Set<IndexedWord>> mappingChanges = new HashMap<>();

        // remove fully intersecting scopes from mapping
        // if slightly intersecting, remove the relevant words as part of the super scope
        // this is done in order to get perfectly separated scopes
        for (IndexedWord entry : nestedStatementMapping.keySet()) {
            Set<IndexedWord> statementScope = nestedStatementMapping.get(entry);

            for (IndexedWord otherEntry : nestedStatementMapping.keySet()) {
                if (!entry.equals(otherEntry)) {
                    Set<IndexedWord> otherStatementScope = new HashSet<>(nestedStatementMapping.get(otherEntry));

                    if (StatementUtils.intersects(statementScope, otherStatementScope)) {
                        if (statementScope.containsAll(otherStatementScope)) {
                            logger.info("removing fully contained scope: " + otherStatementScope);
                            fullyContainedScopes.add(otherEntry);
                        } else {
                            if (!otherStatementScope.containsAll(statementScope)) {
                                statementScope.removeAll(otherStatementScope);
                                mappingChanges.put(entry, statementScope);
                            }
                        }
                    }
                }
            }
        }

        // apply removals
        for (IndexedWord entry : fullyContainedScopes) {
            nestedStatementMapping.remove(entry);
            mappingChanges.remove(entry);
        }

        logger.info("applying changes to statement mapping: " + mappingChanges);

        // apply the changes from mappingChanges
        for (IndexedWord entry : mappingChanges.keySet()) {
            Set<IndexedWord> newStatementScope = nestedStatementMapping.get(entry);
            nestedStatementMapping.put(entry, newStatementScope);
        }

        return nestedStatementMapping;
    }

    /**
     * Duplicate component classes found within a set of components from a statement.
     * Useful for determining where to split a set of components into multiple sets.
     * Used by the split method.
     *
     * @param statement
     * @return duplicate component classes
     */
    private static Set<Class> getDuplicateComponentClasses(Statement statement) {
        int subjects = 0;
        int verbs = 0;
        int directObjects = 0;
        int indirectObjects = 0;

        for (StatementComponent component : statement.getComponents()) {
            if (component instanceof Subject) {
                subjects++;
            } else if (component instanceof Verb) {
                verbs++;
            } else if (component instanceof DirectObject) {
                directObjects++;
            } else if (component instanceof IndirectObject) {
                indirectObjects++;
            }
        }

        Set<Class> duplicateComponents = new HashSet<>();
        if (subjects > 1) {
            duplicateComponents.add(Subject.class);
        }
        if (verbs > 1) {
            duplicateComponents.add(Verb.class);
        }
        if (directObjects > 1) {
            duplicateComponents.add(DirectObject.class);
        }
        if (indirectObjects > 1) {
            duplicateComponents.add(IndirectObject.class);
        }

        return duplicateComponents;
    }

    /**
     * Recursively split a set of components based on duplicate roles filled out by the components.
     * For example, component sets with duplicate subjects are split into separate sets of components
     * with exactly one subject - each of the duplicates - in them.
     *
     * @param statement the statement to split into multiple statements
     * @param duplicateClasses the duplicate classes that determine the split
     * @return sets of components without duplicate role components
     */
    private static Set<Statement> split(Statement statement, Set<Class> duplicateClasses) {
        Set<Statement> splitStatements = new HashSet<>();
        Set<Class> remainingDuplicateClasses = new HashSet<>(duplicateClasses);
        logger.info("splitting " + statement + " based on duplicates: " + duplicateClasses);

        if (duplicateClasses.isEmpty()) {
            splitStatements.add(statement);
            return splitStatements;
        } else {
            // with each level of recursion one component class is no longer needed
            // remaining duplicate classes will be handled further down the recursive stack
            for (Class componentClass : duplicateClasses) {
                remainingDuplicateClasses.remove(componentClass);
                Set<StatementComponent> duplicateComponents = new HashSet<>();
                Set<StatementComponent> components = statement.getComponents();

                // find duplicate components of the particular class
                for (StatementComponent component : components) {
                    if (component.getClass().equals(componentClass)) {
                        duplicateComponents.add(component);
                    }
                }

                // make recursive calls for further splits using the remaining duplicate component classes
                for (StatementComponent duplicateComponent : duplicateComponents) {
                    Set<StatementComponent> newComponentSet = new HashSet<>(components);
                    newComponentSet.removeAll(duplicateComponents);  // remove all duplicates first
                    newComponentSet.add(duplicateComponent);  // then re-add this particular duplicate
                    logger.info(duplicateComponent + " is of the duplicate class: " + duplicateComponent.getClass().getSimpleName());

                    // only actually perform the recursive call if there's a point (= remaining duplicates)
                    // otherwise just add them directly
                    if (remainingDuplicateClasses.isEmpty()) {
                        splitStatements.add(new Statement(newComponentSet));
                    } else {
                        logger.info("recursive call to split based on duplicate components: " + remainingDuplicateClasses);
                        splitStatements.addAll(split(new Statement(newComponentSet), remainingDuplicateClasses));
                    }
                }
            }
        }

        return splitStatements;
    }
    /**
     * Connect components based on their parent-child relations.
     * Starts a recursive connection operation.
     *
     * @param components the components to connect
     * @return Statements consisting of connected components + leftover unconnected AbstractComponents
     */
    private static Set<StatementComponent> connect(Set<StatementComponent> components) {
        return connect(components, new HashSet<>());
    }

    /**
     * Recursively connect components based on their parent-child relations.
     *
     * @param components the components to connect
     * @return Statements consisting of connected components + leftover unconnected AbstractComponents
     */
    private static Set<StatementComponent> connect(Set<StatementComponent> components, Set<StatementComponent> representedElsewhere) {
        Set<StatementComponent> connectedComponents = new HashSet<>();
        logger.info("recursively connecting components: " + components);

        for (StatementComponent component : components) {
            // all connections to this component are containd in this set (including the component itself)
            Set<StatementComponent> connections = new HashSet<>();

            // check against all the other components
            // if a component is fully contained within another, then there is no reason to keep it
            // if a component is the parent of another component, then they are merged
            for (StatementComponent otherComponent : components) {
                if (!component.equals(otherComponent)) {
                    if (component.parentOf(otherComponent)) {
                        connections.add(otherComponent);
                        representedElsewhere.add(otherComponent);
                        logger.info(component + " is the parent of " + otherComponent);
                    } else if (otherComponent.contains(component)) {
                        representedElsewhere.add(component);
                        logger.info(component + " is contained by " + otherComponent);
                    } else if (component instanceof Statement && otherComponent instanceof Statement) {
                        if (((Statement) otherComponent).contains(((Statement) component).getComponents())) {
                            representedElsewhere.add(component);
                            logger.info(component + " is contained by " + otherComponent);
                        }
                    }
                }
            }

            // in case the component had any connections, these are used to construct a new Statement
            // otherwise the component is preserved as it is (unless it was completely contained)
            if (!representedElsewhere.contains(component)) {
                if (!connections.isEmpty()) {
                    connections.add(component);
                    logger.info("made new statement from components: " + connections);
                    Statement newStatement = new Statement(connections);
                    logger.info("new statement: " + newStatement);
                    connectedComponents.add(newStatement);
                } else {
                    logger.info("preserving component: " + component);
                    connectedComponents.add(component);
                }
            } else {
                logger.info("not preserving component: " + component);
            }
        }

        // recursively call connect(...) until there are no further changes
        if (!connectedComponents.equals(components)) {
            connectedComponents = connect(connectedComponents);
        } else {
            logger.info("done connecting components");
        }

        return connectedComponents;
    }

    /**
     * Produces statements by linking together statement components.
     *
     * @return statements
     */
    private static Set<Statement> link(Set<Set<AbstractComponent>> componentsByNestingLevel) {
        Set<Set<StatementComponent>> connectedComponentSets = new HashSet<>();

        // connect components by nesting level
        // (avoid individual nested statement components being accidentally incorporated into their parent statements)
        for (Set<AbstractComponent> componentLevel : componentsByNestingLevel) {
            Set<StatementComponent> statementComponents = new HashSet<>(componentLevel);  // STUPID JAVA!!
            connectedComponentSets.add(connect(statementComponents));
        }

        logger.info("connectedComponentSets: " + connectedComponentSets);
        Set<Statement> unsplitStatements = new HashSet<>();
        Set<StatementComponent> leftoverComponents = new HashSet<>();

        // partition into full statements and leftover components (for debugging)
        for (Set<StatementComponent> componentSet : connectedComponentSets) {
            for (StatementComponent component : componentSet) {
                if (component instanceof Statement) {
                    unsplitStatements.add((Statement) component);
                } else {
                    leftoverComponents.add(component);
                }
            }
        }

        logger.info("unsplitStatements: " + unsplitStatements);
        logger.info("leftoverComponents: " + leftoverComponents);
        Set<Statement> splitStatements = new HashSet<>();

        // TODO: is splitting even necessary? what if everything is handled in connect(...)?
        // split in case of duplicate roles in the component sets (e.g. multiple Subject components)
        for (Statement statement : unsplitStatements) {
            splitStatements.addAll(split(statement, getDuplicateComponentClasses(statement)));
        }

        logger.info("splitStatements: " + splitStatements);
        Set<Statement> modifiedStatements = new HashSet<>();
        Set<Statement> statements = new HashSet<>();

        // TODO: perform this step before splitting?
        // discover links between unconnected statements and embed nested statements
        for (Statement statement : splitStatements) {
            for (Statement otherStatement : splitStatements) {
                if (statement.parentOf(otherStatement)) {
                    logger.info(statement + " embeds " + otherStatement);
                    modifiedStatements.add(statement);
                    modifiedStatements.add(otherStatement);
                    statements.add(statement.embed(otherStatement));
                }
            }
        }

        // remove the modified statements and add remaining to final list
        logger.info("modifiedStatements: " + modifiedStatements);
        splitStatements.removeAll(modifiedStatements);
        statements.addAll(splitStatements);

        return statements;
    }
}
