package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds Statements in sentences.
 */
public class StatementFinder {
    private static final Logger logger = LoggerFactory.getLogger(StatementFinder.class);

    /**
     * Find statements in a sentence.
     *
     * @param sentence the sentence to look in
     * @return statements
     */
    public static Set<Statement> find(CoreMap sentence) {
        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

        // find statement components
        Set<AbstractComponent> components = new HashSet<>();
        components.addAll(SubjectFinder.find(graph));
        components.addAll(VerbFinder.find(graph));
        components.addAll(DirectObjectFinder.find(graph));
        components.addAll(IndirectObjectFinder.find(graph));

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
        Map<AbstractComponent, AbstractComponent> conjVerbMapping = new HashMap<>();  // verb conjunctions to split into separate statements
        Set<Set<AbstractComponent>> componentSets = new HashSet<>();
        Set<Statement> statements = new HashSet<>();

        logger.info("components for linking: " + components);

        // find the direct child-parent relationships between components
        Map<AbstractComponent, AbstractComponent> componentMapping = getComponentMapping(components, graph);

        // discover inter-statement relationships
        Map<AbstractComponent, AbstractComponent> statementMapping = getStatementMapping(componentMapping, graph);

        // remove component relationships found in the inter-statement mapping
        // these components will be replaced by links to separate statements
        for (AbstractComponent component : statementMapping.keySet()) {
            componentMapping.remove(component);
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
        for (AbstractComponent child : componentMapping.keySet()) {
            AbstractComponent parent = componentMapping.get(child);
            boolean found = false;

            // find existing component set to link up to
            for (Set<AbstractComponent> componentSet : componentSets) {
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
                componentSets.add(newComponentSet);
            }
        }

        // merge any component sets that intersect
        Set<Set<AbstractComponent>> mergedComponentSets = new HashSet<>();
        for (Set<AbstractComponent> componentSet : componentSets) {
            Set<AbstractComponent> mergedComponentSet = new HashSet<>(componentSet);

            for (Set<AbstractComponent> otherComponentSet : componentSets) {
                if (!mergedComponentSet.equals(otherComponentSet) && StatementUtils.intersects(mergedComponentSet, otherComponentSet)) {
                    logger.info("merging " + otherComponentSet + " into " + mergedComponentSet);
                    mergedComponentSet.addAll(otherComponentSet);
                }
            }

            mergedComponentSets.add(mergedComponentSet);  // re-adding identical merged sets has no effect
        }

        // create statements from component sets
        for (Set<AbstractComponent> componentSet : mergedComponentSets) {
            logger.info("linked statement from components: " + componentSet);
            Statement statement = new Statement(componentSet);
            statements.add(statement);
        }

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

        logger.info("component mapping: " + componentMapping);
        logger.info("based on dependencies: " + graph.typedDependencies());
        logger.info("links between statements: " + statementMapping);
        logger.info("links between verbs: " + conjVerbMapping);
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
     * @param component
     * @param otherComponent
     * @param graph
     * @return whether the component is a child or not
     */
    private static boolean isChild(AbstractComponent component, AbstractComponent otherComponent, SemanticGraph graph) {
        if (component == otherComponent) return false;

        // check if component is a child of otherComponent
        for (IndexedWord childEntry : component.getEntries()) {
            Set<IndexedWord> parents = graph.getParents(childEntry);
            if (StatementUtils.intersects(parents, otherComponent.getEntries())) {
                return true;
            }
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

            if (parentPrimary != null && childPrimary != null && graph.reln(parentPrimary, childPrimary).getShortName().equals(Relations.CCOMP)) {
                statementMapping.put(child, parent);
            }
        }

        return statementMapping;
    }
}
