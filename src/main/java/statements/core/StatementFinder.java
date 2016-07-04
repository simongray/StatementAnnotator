package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.GrammaticalRelation;
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
        Set<Statement> statements = new HashSet<>();

        logger.info("components for linking: " + components);
        logger.info("based on dependencies: " + graph.typedDependencies());

        // find the direct child-parent relationships between components
        // this mapping is used to create sets of linked components which can be turned into statements
        Map<AbstractComponent, AbstractComponent> componentMapping = getComponentMapping(components, graph);
        logger.info("component mapping: " + componentMapping);

        // discover inter-statement relationships
        // this mapping is used to compose statements together if they're connected
        // TODO: currently just checking for CCOMP relations, it really is not that clever
        Map<AbstractComponent, AbstractComponent> statementMapping = getStatementMapping(componentMapping, graph);
        logger.info("statement mapping: " + statementMapping);

        // remove component relationships found in the inter-statement mapping
        // these components will be replaced by links to separate statements
        for (AbstractComponent component : statementMapping.keySet()) {
            componentMapping.remove(component);
            logger.info("removed " + component + " from component mapping, available in statement mapping");
        }

        // remove connections between identical component types
        // TODO: this mainly a debugging step, figure out whether to keep it
        for (AbstractComponent component : components) {
            AbstractComponent parent = componentMapping.getOrDefault(component, null);

            if (isSameType(component, parent)) {
                logger.info("removing identical component type relation: " + component);
                componentMapping.remove(component);
            }
        }

        // create sets of components by following all dependencies
        Set<Set<AbstractComponent>> componentSets = findLinks(componentMapping);

        // merge any component sets that intersect
        // TODO: figure out whether this is really necessary
        StatementUtils.merge(componentSets);

        // create statements from component sets
        for (Set<AbstractComponent> componentSet : componentSets) {
            logger.info("linked statement from components: " + componentSet);
            Statement statement = new Statement(componentSet);
            statements.add(statement);
        }

        // attach dependent clauses to the main statements (and remove as independent statements)
        attachNestedStatements(statementMapping, statements);

        logger.info("final statements: " + statements);

        return statements;
    }

    /**
     * The AbstractComponent corresponding to a primary word.
     *
     * @param primary primary word of component
     * @param components set of components to search in
     * @return component
     */
    private static AbstractComponent getFromPrimary(IndexedWord primary, Set<AbstractComponent> components) {
        for (AbstractComponent component : components) {
            if (component.getPrimary().equals(primary)) return component;
        }

        return null;
    }

    /**
     * Find out whether a component is a connected to another through a parent-child relationship.
     * Checks every entry of both components, not just the primary word.
     * Useful when linking components together, especially in case a primary word is not part of the relation.
     *
     * @param component child component
     * @param otherComponent parent component
     * @param graph dependency graph of sentence
     * @return whether the component is a child or not
     */
    private static boolean isChild(AbstractComponent component, AbstractComponent otherComponent, SemanticGraph graph) {
        if (component == otherComponent) return false;

        IndexedWord primary = component.getPrimary();  // TODO: also check other entries, maybe even other words?

        for (IndexedWord word : otherComponent.getWords()) {  // TODO: efficient enough?
            if (graph.reln(word, primary) != null) return true;
        }

        return false;
    }

    /**
     * Returns whether the two components are the same component type.
     *
     * @param component
     * @param otherComponent
     * @return
     */
    private static boolean isSameType(AbstractComponent component, AbstractComponent otherComponent) {
        return (component instanceof Subject && otherComponent instanceof Subject
                || component instanceof Verb && otherComponent instanceof Verb
                || component instanceof DirectObject && otherComponent instanceof DirectObject
                || component instanceof IndirectObject && otherComponent instanceof IndirectObject);
    }

    /**
     * Get the child-parent mapping for a list of statement components.
     *
     * @param components
     * @param graph
     * @return
     */
    private static Map<AbstractComponent, AbstractComponent> getComponentMapping(Set<AbstractComponent> components, SemanticGraph graph) {
        Map<AbstractComponent, AbstractComponent> componentMapping = new HashMap<>();

        // find the direct parent for each component based on its primary word
        // this relation will be used to link components
        for (AbstractComponent component : components) {
            for (AbstractComponent otherComponent : components) {
                if (isChild(component, otherComponent, graph)) {
                    componentMapping.put(component, otherComponent);
                }
            }
        }

        return componentMapping;
    }

    /**
     * Get the inter-statement mapping from a child-parent mapping of components.
     *
     * @param componentMapping
     * @param graph
     * @return
     */
    private static Map<AbstractComponent, AbstractComponent> getStatementMapping(Map<AbstractComponent, AbstractComponent>componentMapping, SemanticGraph graph) {
        Map<AbstractComponent, AbstractComponent> statementMapping = new HashMap<>();

        for (AbstractComponent child : componentMapping.keySet()) {
            AbstractComponent parent = componentMapping.get(child);
            IndexedWord childPrimary = child.getPrimary();
            IndexedWord parentPrimary = parent.getPrimary();

            if (parentPrimary != null && childPrimary != null) {
                GrammaticalRelation relation = graph.reln(parentPrimary, childPrimary);

                // TODO: is there more general way that is not just checking for CCOMP or DEP relations?
                if (relation != null && relation.getShortName().equals(Relations.CCOMP)) statementMapping.put(child, parent);
            }
        }

        return statementMapping;
    }

    /**
     * Get the sets of linked components found in the component mapping.
     *
     * @param componentMapping
     * @return
     */
    private static Set<Set<AbstractComponent>> findLinks(Map<AbstractComponent, AbstractComponent> componentMapping) {
        Set<Set<AbstractComponent>> linkedComponents = new HashSet<>();

        // create sets of components by following all dependencies in component mapping
        for (AbstractComponent child : componentMapping.keySet()) {
            AbstractComponent parent = componentMapping.get(child);
            boolean found = false;

            // find existing component set to link up to
            for (Set<AbstractComponent> componentSet : linkedComponents) {
                if (componentSet.contains(parent) || componentSet.contains(child)) {
                    if (parent != null) componentSet.add(parent);
                    componentSet.add(child);
                    found = true;
                    break;
                }
            }

            // create new component set if no parent was found
            if (!found) {
                Set<AbstractComponent> newComponentSet = new HashSet<>();
                if (parent != null) newComponentSet.add(parent);
                newComponentSet.add(child);
                linkedComponents.add(newComponentSet);
            }
        }

        return linkedComponents;
    }

    /**
     * Attaches nested statements (= dependent clauses) to their parent statements.
     * Then removes them from list of statements.
     *
     * @param statementMapping
     * @param statements
     */
    private static void attachNestedStatements(Map<AbstractComponent, AbstractComponent> statementMapping, Set<Statement> statements) {
        // link dependent clauses together with the main statements
        for (AbstractComponent childComponent : statementMapping.keySet()) {
            AbstractComponent parentComponent = statementMapping.get(childComponent);
            Statement child = null;
            Statement parent = null;

            for (Statement statement : statements) {
                if (statement.contains(childComponent)) {
                    child = statement;
                }
                if (statement.contains(parentComponent)) {
                    parent = statement;
                }
            }

            // compose into parent statements
            if (parent != null && child != null) {
                logger.info("nesting " + child + " within " + parent);
                parent.addChild(child);
                statements.remove(child);
                logger.info("components of parent: "+parent.getComponents());
            }
        }
    }
}
