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

        // the scopes of dependent clauses are separated from the root scope into levels
        Set<Set<IndexedWord>> levels = findLevels(graph);

        // these levels are then used to partition the components by levels
        Set<Set<AbstractComponent>> componentLevels = partitionByLevel(components, levels);

        // statements are produced by discovering connection between the components
        Set<Statement> statements = link(componentLevels);

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
     * Find levels of dependent clauses in the graph.
     * This is used to partition components by level to create a hierarchy of statements embedding other statements.
     *
     * @param graph the dependency graph of the sentence
     * @return levels of dependent clauses
     */
    private static Set<Set<IndexedWord>> findLevels(SemanticGraph graph) {
        Set<Set<IndexedWord>> levels = new HashSet<>();

        // which words are entry points for nested statements?
        // use these to find the exact scope of the statements
        for (TypedDependency dependency : graph.typedDependencies()) {
            if (Relations.EMBEDDED_STATEMENT_SCOPES.contains(dependency.reln().getShortName())) {
                // TODO: also ignore dependent clauses within dependent clauses
                levels.add(StatementUtils.findCompound(dependency.dep(), graph, Relations.EMBEDDED_STATEMENT_SCOPES, null)
                );
            }
        }

        // the remaining vertices in the graph comprise the root level (= non-embedded statements) of the sentence
        Set<IndexedWord> rootLevel = new HashSet<>(graph.vertexSet());
        for (Set<IndexedWord> level : levels) {
            rootLevel.removeAll(level);
        }
        levels.add(rootLevel);
        logger.info("levels before applying changes: " + levels);

        Set<Set<IndexedWord>> fullyContainedLevels = new HashSet<>();

        // remove fully intersecting scopes from mapping
        // if slightly intersecting, remove the relevant words as part of the super scope
        // this is done in order to get perfectly separated scopes
        for (Set<IndexedWord> level : levels) {
            for (Set<IndexedWord> otherLevel : levels) {
                if (!level.equals(otherLevel) && StatementUtils.intersects(level, otherLevel)) {
                    if (level.containsAll(otherLevel)) {
                        logger.info("removing fully contained level: " + otherLevel);
                        fullyContainedLevels.add(otherLevel);
                    } else {
                        if (!otherLevel.containsAll(level)) {
                            logger.info("removing overlapping portion from: " + level);
                            level.removeAll(otherLevel); // TODO: this might be/become a source of errors
                            logger.info("level after changes: " + level);
                        }
                    }
                }
            }
        }

        // apply removals of fully contained levels
        for (Set<IndexedWord> fullyContainedLevel : fullyContainedLevels) {
            levels.remove(fullyContainedLevel);
        }

        logger.info("levels found: " + levels.size());
        for (Set<IndexedWord> level : levels) {
            logger.info("level: " + level);
        }

        return levels;
    }

    /**
     * Partition the components of a sentence into sets of components by their level within the sentence.
     * Levels are defined by relations indicating dependent clauses (such as ccomp and xcomp).
     * Partitioning allows embedded statements to be clearly separated from root statements.
     *
     * @param components
     * @param levels
     * @return
     */
    private static Set<Set<AbstractComponent>> partitionByLevel(Set<AbstractComponent> components, Set<Set<IndexedWord>> levels)  {
        Set<Set<AbstractComponent>> componentLevels = new HashSet<>();

        for (Set<IndexedWord> level : levels) {
            Set<AbstractComponent> componentLevel = new HashSet<>();

            // components are partitioned into levels based on where their primary word appears
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
     * @param componentLevel the components to connect
     * @return Statements consisting of connected components + leftover unconnected AbstractComponents
     */
    private static Set<Statement> connectLevel(Set<AbstractComponent> componentLevel) {
        Set<Statement> statements = new HashSet<>();
        for (AbstractComponent component : componentLevel) {
            statements.add(new Statement(component));
        }

        return connect(statements);
    }

    /**
     * Recursively connect components based on their parent-child relations.
     *
     * @param statements the components to connect
     * @return Statements consisting of connected components + leftover unconnected AbstractComponents
     */
    private static Set<Statement> connect(Set<Statement> statements) {
        Set<Statement> connectedStatements = new HashSet<>();
        Set<Statement> representedElsewhere = new HashSet<>();
        logger.info("recursively connecting statements: " + statements.size());
        for (Statement statement : statements) {
            logger.info("statement: " + statement);
        }

        for (Statement statement : statements) {
            // all connections to this component are contained in this set (including the component itself)
            Set<Statement> connections = new HashSet<>();

            // check against all the other components
            // if a component is fully contained within another, then there is no reason to keep it
            // if a component is the parent of another component, then they are merged
            for (Statement otherStatement : statements) {
                if (!statement.equals(otherStatement)) {
                    if (statement.parentOf(otherStatement)) {
                        connections.add(otherStatement);
                        representedElsewhere.add(otherStatement);
                        logger.info(statement + " is the parent of " + otherStatement);
                    } else if (otherStatement.contains(statement)) {
                        representedElsewhere.add(statement);
                        logger.info(statement + " is contained by " + otherStatement);
                    } else if (otherStatement.contains(statement.getComponents())) {
                        representedElsewhere.add(statement);
                        logger.info(statement + " is contained by " + otherStatement);
                    }
                }
            }

            // in case the component had any connections, these are used to construct a new Statement
            // otherwise the component is preserved as it is (unless it was completely contained)
            if (!representedElsewhere.contains(statement)) {
                if (!connections.isEmpty()) {
                    connections.add(statement);
                    logger.info("made new statement from components: " + connections);
                    Statement newStatement = Statement.merge(connections);
                    logger.info("new statement: " + newStatement);
                    connectedStatements.add(newStatement);
                } else {
                    logger.info("preserving component: " + statement);
                    connectedStatements.add(statement);
                }
            } else {
                logger.info("not preserving component: " + statement);
            }
        }

        // recursively call connect(...) until there are no further changes
        if (!connectedStatements.equals(statements)) {
            connectedStatements = connect(connectedStatements);
        } else {
            logger.info("done connecting components");
        }

        return connectedStatements;
    }

    /**
     * Find overlap between statements.
     *
     * @param statements statements to base mapping on
     * @return map with overlap as key and the overlapping statements as value
     */
    private static Map<Set<StatementComponent>, Set<Statement>> getOverlaps(Set<Statement> statements) {
        Map<Set<StatementComponent>, Set<Statement>> overlapMapping = new HashMap<>();

        for (Statement statement : statements) {
            for (Statement otherStatement : statements) {
                if (statement != otherStatement) {
                    Set<StatementComponent> overlap = statement.getOverlap(otherStatement);

                    if (overlap.size() > 0) {
                        Set<Statement> overlappingStatements = overlapMapping.getOrDefault(overlap, new HashSet<>());
                        overlappingStatements.add(statement);
                        overlappingStatements.add(otherStatement);
                        overlapMapping.put(overlap, overlappingStatements);
                    }
                }
            }
        }

        return overlapMapping;
    }

    /**
     * Resolves overlap between a set of overlapping components by choosing the combination of statements
     * with the lowest total count of duplicate component types.
     *
     * @param overlap the overlapping components
     * @param overlappingStatements the statements with overlapping components
     * @return the best combination of statements without overlap
     */
    private static Set<Statement> getBestCombination(Set<StatementComponent> overlap, Set<Statement> overlappingStatements) {
        int smallestTotalDuplicateCount = -1;
        Set<Statement> optimalCombination = null;

        logger.info("finding best combination for overlap: " + overlap);
        logger.info("between statements: " + overlappingStatements);

        for (Statement overlappingStatement : overlappingStatements) {
            Set<Statement> combination = new HashSet<>();
            combination.add(overlappingStatement);

            for (Statement otherOverlappingStatement : overlappingStatements) {
                if (otherOverlappingStatement != overlappingStatement) {
                    combination.add(otherOverlappingStatement.withoutComponents(overlap));
                }
            }

            int totalDuplicateCount = 0;
            for (Statement statement : combination) {
                totalDuplicateCount += statement.duplicateCount();
            }

            if (smallestTotalDuplicateCount == -1 || totalDuplicateCount < smallestTotalDuplicateCount) {
                logger.info("found smaller duplicate count with combination: " + combination);
                smallestTotalDuplicateCount = totalDuplicateCount;
                optimalCombination = combination;
            }
        }

        logger.info("smallest total duplicate count for " + overlap + ": " + smallestTotalDuplicateCount);

        return optimalCombination;
    }

    /**
     * Recursively resolve overlap between statements.
     * Overlap is removed based on minimising
     *
     * @param statements
     * @return
     */
    private static Set<Statement> resolveOverlap(Set<Statement> statements) {
        Map<Set<StatementComponent>, Set<Statement>> overlapMapping = getOverlaps(statements);

        if (!overlapMapping.isEmpty()) {
            logger.info("recursively resolving overlaps: " + overlapMapping.keySet());

            // the remainder do not overlap and are preserved as before
            Set<Statement> shiftedStatements = new HashSet<>(statements);
            for (Set<Statement> overlappingStatements : overlapMapping.values()) {
                shiftedStatements.removeAll(overlappingStatements);
            }

            // different combinations are attempted to remove the overlap
            // the combination that produces the smallest total Statement.duplicateCount() is used (#57)
            for (Set<StatementComponent> overlap : overlapMapping.keySet()) {
                Set<Statement> overlappingStatements = overlapMapping.get(overlap);
                shiftedStatements.addAll(getBestCombination(overlap, overlappingStatements));
            }

            logger.info("shifted statements: " + shiftedStatements);

            statements = resolveOverlap(shiftedStatements);
        }

        return statements;
    }

    private static Map<Statement, Statement> getEmbeddingMap(Set<Statement> statements) {
        // discover links between unconnected statements and embed nested statements
        Map<Statement, Statement> embeddingMap = new HashMap<>();
        for (Statement statement : statements) {
            for (Statement otherStatement : statements) {
                if (statement.embeddingParentOf(otherStatement)) {
                    logger.info(statement + " embeds " + otherStatement);
                    embeddingMap.put(otherStatement, statement);
                    // TODO: possible issue if a statement embeds two statements...
                }
            }
        }

        return embeddingMap;
    }

    private static Set<List<Statement>> getEmbeddingChains(Map<Statement, Statement> embeddingMap) {
        Set<List<Statement>> chains = new HashSet<>();

        for (Statement child : embeddingMap.keySet()) {
            List<Statement> chain = new ArrayList<>();
            chain.add(child);

            // follow chain of parent statements until parent is no longer itself a child
            while (embeddingMap.containsKey(child)) {
                child = embeddingMap.get(child);
                chain.add(child);
            }

            chains.add(chain);
        }

        // remove chains that are contained by other chains
        // this simply ensures that no duplicate statements are created
        Set<List<Statement>> containedChains = new HashSet<>();
        for (List<Statement> chain : chains) {
            for (List<Statement> otherChain : chains) {
                if (chain != otherChain && otherChain.containsAll(chain)) {
                    containedChains.add(chain);
                }
            }
        }

        chains.removeAll(containedChains);

        return chains;
    }

    /**
     * Produces statements by linking together statement components.
     *
     * @return statements
     */
    private static Set<Statement> link(Set<Set<AbstractComponent>> componentLevels) {
        Set<Statement> unsplitStatements = new HashSet<>();

        // connect components into statements by level
        for (Set<AbstractComponent> componentLevel : componentLevels) {
            unsplitStatements.addAll(connectLevel(componentLevel));
        }

        logger.info("unsplitStatements: " + unsplitStatements);

        // fixing issue #57
        unsplitStatements = resolveOverlap(unsplitStatements);

        // TODO: is splitting even necessary? what if everything is handled in connect(...)?
        // split in case of duplicate roles in the component sets (e.g. multiple Subject components)
        Set<Statement> splitStatements = new HashSet<>();
        for (Statement statement : unsplitStatements) {
            splitStatements.addAll(split(statement, getDuplicateComponentClasses(statement)));
        }

        logger.info("splitStatements: " + splitStatements);
        Set<Statement> statements = new HashSet<>();

        // discover links between unconnected statements and embed nested statements
        Map<Statement, Statement> embeddingMap = getEmbeddingMap(splitStatements);
        logger.info("embeddingMap: " + embeddingMap);
        Set<List<Statement>> chains = getEmbeddingChains(embeddingMap);
        logger.info("chains: " + chains);

        // create embedding statements from the chain of embeddings
        Set<Statement> embeddingStatements = new HashSet<>();
        Statement embeddingStatement = null;
        for (List<Statement> chain : chains) {
            for (Statement statement : chain) {
                if (embeddingStatement == null) {
                    embeddingStatement = statement;
                } else {
                    logger.info("embedded " + embeddingStatement + " within " + statement);
                    embeddingStatement = statement.embed(embeddingStatement);
                }
            }
            embeddingStatements.add(embeddingStatement);

            // remove chain from other statements to avoid duplicates
            splitStatements.removeAll(chain);
        }

        // remove the modified statements and add remaining to final list
        statements.addAll(splitStatements);
        statements.addAll(embeddingStatements);

        return statements;
    }
}
