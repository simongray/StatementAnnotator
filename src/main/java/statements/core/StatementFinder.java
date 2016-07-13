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
        graph.prettyPrint();  // TODO: remove when done debugging

        // find statement components
        Set<AbstractComponent> components = new HashSet<>();
        components.addAll(subjectFinder.find(graph));
        components.addAll(verbFinder.find(graph));
        components.addAll(directObjectFinder.find(graph));
        components.addAll(indirectObjectFinder.find(graph));

        // link components to produce statements
        return link(graph, components);
    }

    /**
     * Produces statements by linking together statement components.
     *
     * @param graph the graph of the sentence
     * @param components the various statement components found in sentence
     * @return statements
     */
    private static Set<Statement> link(SemanticGraph graph, Set<AbstractComponent> components) {
        logger.info("components for linking: " + components);
        logger.info("based on dependencies: " + graph.typedDependencies());

        Map<IndexedWord, Set<IndexedWord>> nestedStatementMapping = findNestedStatementMapping(Relations.EMBEDDED_STATEMENT_SCOPES, graph);
        Map<IndexedWord, Set<AbstractComponent>> nestedComponentMapping = new HashMap<>();
        Set<AbstractComponent> nestedComponents = new HashSet<>();
        logger.info("nested statement mapping: " + nestedStatementMapping);

        // map components into root statement and nested statements based on their primary entry
        for (AbstractComponent component : components) {
            for (IndexedWord statementEntry : nestedStatementMapping.keySet()) {
                Set<IndexedWord> statementWords = nestedStatementMapping.get(statementEntry);
                if (statementWords.contains(component.getPrimary())) {
                    Set<AbstractComponent> statementComponents = nestedComponentMapping.getOrDefault(statementEntry, new HashSet<>());
                    statementComponents.add(component);
                    nestedComponentMapping.put(statementEntry, statementComponents);
                    nestedComponents.add(component);  // keeping track of components not in root
                    logger.info("non-root component: " + component);
                }
            }
        }

        // the remaining components may be considered components of the root statement
        components.removeAll(nestedComponents);

        // TODO: just use set from beginning, skip mapping
        Set<Set<AbstractComponent>> componentsByNestingLevel = new HashSet<>();
        componentsByNestingLevel.add(components);
        for (Set<AbstractComponent> componentSet : nestedComponentMapping.values()) {
            componentsByNestingLevel.add(componentSet);
        }

        logger.info("componentsByNestingLevel: " + componentsByNestingLevel);
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

    private static Map<IndexedWord, Set<IndexedWord>> findNestedStatementMapping(Set<String> nestedStatementScopes, SemanticGraph graph) {
        Map<IndexedWord, Set<IndexedWord>> nestedStatementMapping = new HashMap<>();

        // which words are entry points for nested statements?
        // use these to find the exact scope of the statements
        for (TypedDependency dependency : graph.typedDependencies()) {
            if (Relations.EMBEDDED_STATEMENT_SCOPES.contains(dependency.reln().getShortName())) {
                nestedStatementMapping.put(
                        dependency.dep(),
                        StatementUtils.findCompound(dependency.dep(), graph, Relations.EMBEDDED_STATEMENT_SCOPES, null)
                );
            }
        }

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
     *
     * @param components the components to connect
     * @return Statements consisting of connected components + leftover unconnected AbstractComponents
     */
    private static Set<StatementComponent> connect(Set<StatementComponent> components) {
        Set<StatementComponent> connectedComponents = new HashSet<>();
        logger.info("recursively connecting components: " + components);

        for (StatementComponent component : components) {
            // all connections to this component are containd in this set (including the component itself)
            Set<StatementComponent> connections = new HashSet<>();
            connections.add(component);
            boolean representedElsewhere = false;

            // check against all the other components
            // if a component is fully contained within another, then there is no reason to keep it
            // if a component is the parent of another component, then they are merged
            for (StatementComponent otherComponent : components) {
                if (!component.equals(otherComponent)) {
                    if (component.parentOf(otherComponent)) {
                        connections.add(otherComponent);
                        logger.info(component + " is the parent of " + otherComponent);
                    } else if (otherComponent.contains(component)) {
                        representedElsewhere = true;
                        logger.info(component + " is contained by " + otherComponent);
                    } else if (component instanceof Statement && otherComponent instanceof Statement) {
                        if (((Statement) otherComponent).contains(((Statement) component).getComponents())) {
                            representedElsewhere = true;
                            logger.info(component + " is contained by " + otherComponent);
                        }
                    }
                }
            }

            // in case the component had any connections, these are used to construct a new Statement
            // otherwise the component is preserved as it is (unless it was completely contained)
            if (!representedElsewhere) {
                if (connections.size() > 1) {
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
}
