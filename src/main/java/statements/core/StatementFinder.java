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
public  class StatementFinder {
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
        Set<Subject> subjects = SubjectFinder.find(graph);
        Set<Verb> verbs = VerbFinder.find(graph);
        Set<DirectObject> directObjects = DirectObjectFinder.find(graph);
        Set<IndirectObject> indirectObjects = IndirectObjectFinder.find(graph);

        // merge all components into same set before linking
        Set<AbstractComponent> components = new HashSet<>();
        components.addAll(subjects);
        components.addAll(verbs);
        components.addAll(directObjects);
        components.addAll(indirectObjects);

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
        Map<AbstractComponent, AbstractComponent> childToParentMapping = new HashMap<>();
        Map<AbstractComponent, AbstractComponent> interStatementMapping = new HashMap<>();
        Map<AbstractComponent, AbstractComponent> conjVerbMapping = new HashMap<>();  // verb conjunctions to split into separate statements
        Set<Set<AbstractComponent>> componentSets = new HashSet<>();
        Set<Statement> statements = new HashSet<>();

        logger.info("components for linking: " + components);

        // find the direct parent for each component based on its primary word
        // this relation will be used to link components
        for (AbstractComponent component : components) {
            IndexedWord parentPrimary = graph.getParent(component.getPrimary());
            AbstractComponent parent = getFromPrimary(parentPrimary, components);

            // keep track of linked statements
            if (parentPrimary != null && graph.reln(parentPrimary, component.getPrimary()).getShortName().equals(Relations.CCOMP)) {
                interStatementMapping.put(component, parent);
            } else {
                childToParentMapping.put(component, parent);
            }
        }

        // remove connections between identical component types
        for (AbstractComponent component : components) {
            AbstractComponent parent = childToParentMapping.getOrDefault(component, null);

            if (parent instanceof Subject && component instanceof Subject
                    || parent instanceof Verb && component instanceof Verb
                    || parent instanceof DirectObject && component instanceof DirectObject
                    || parent instanceof IndirectObject && component instanceof IndirectObject) {
                logger.info("removing identical component type relation: " + component);
                childToParentMapping.remove(component);
            }
        }

        // create sets of components by following all dependencies
        for (AbstractComponent child : childToParentMapping.keySet()) {
            AbstractComponent parent = childToParentMapping.get(child);
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
        for (AbstractComponent childComponent : interStatementMapping.keySet()) {
            AbstractComponent parentComponent = interStatementMapping.get(childComponent);
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

        logger.info("component mapping: " + childToParentMapping);
        logger.info("based on dependencies: " + graph.typedDependencies());
        logger.info("links between statements: " + interStatementMapping);
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
}
