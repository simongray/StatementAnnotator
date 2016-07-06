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
        Set<Statement> unconnectedStatements = new HashSet<>();

        logger.info("components for linking: " + components);
        logger.info("based on dependencies: " + graph.typedDependencies());

        Map<IndexedWord, Set<IndexedWord>> nestedStatementMapping = findNestedStatementMapping(Relations.NESTED_STATEMENT_SCOPES, graph);
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
        Set<Set<AbstractComponent>> componentSets = new HashSet<>();
        componentSets.add(components);
        for (Set<AbstractComponent> componentSet : nestedComponentMapping.values()) {
            componentSets.add(componentSet);
        }

        Set<Set<AbstractComponent>> connectedComponentSets = new HashSet<>();

        // discover links between components for each set of components
        // the components sets are separated into either nested statement levels or the root level
        for (Set<AbstractComponent> componentSet : componentSets) {
            for (AbstractComponent component : componentSet) {
                Set<AbstractComponent> connectedComponents = new HashSet<>();
                connectedComponents.add(component);

                for (AbstractComponent otherComponent : componentSet) {
                    if (component.parentOf(otherComponent)) {
                        connectedComponents.add(otherComponent);
                    }
                }

                connectedComponentSets.add(connectedComponents);
            }
        }

        // merge all of the connected component sets in order to remove duplicates
        StatementUtils.merge(connectedComponentSets);
        logger.info("connectedComponentSets: " + connectedComponentSets);

        // build statements from the connected component sets
        for (Set<AbstractComponent> connectedComponentSet : connectedComponentSets) {
            unconnectedStatements.add(new Statement(connectedComponentSet));
        }

        Set<Statement> modifiedStatements = new HashSet<>();
        Set<Statement> statements = new HashSet<>();

        // discover links between unconnected statements and embed nested statements
        for (Statement statement : unconnectedStatements) {
            for (Statement otherStatement : unconnectedStatements) {
                if (statement.parentOf(otherStatement)) {
                    logger.info(statement + " embeds " + otherStatement);
                    modifiedStatements.add(statement);
                    modifiedStatements.add(otherStatement);
                    statements.add(statement.embed(otherStatement));
                }
            }
        }

        // remove the modified statements and add remaining to final list
        unconnectedStatements.removeAll(modifiedStatements);
        statements.addAll(unconnectedStatements);

        return statements;
    }

    private static Map<IndexedWord, Set<IndexedWord>> findNestedStatementMapping(Set<String> nestedStatementScopes, SemanticGraph graph) {
        Map<IndexedWord, Set<IndexedWord>> nestedStatementMapping = new HashMap<>();

        // which words are entry points for nested statements?
        // use these to find the exact scope of the statements
        for (TypedDependency dependency : graph.typedDependencies()) {
            if (Relations.NESTED_STATEMENT_SCOPES.contains(dependency.reln().getShortName())) {
                nestedStatementMapping.put(
                        dependency.dep(),
                        StatementUtils.findCompound(dependency.dep(), graph, Relations.NESTED_STATEMENT_SCOPES, null)
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
}
