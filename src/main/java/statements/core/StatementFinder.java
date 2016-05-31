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
public  class StatementFinder {
    private static final Logger logger = LoggerFactory.getLogger(StatementFinder.class);

    private static final String CCOMP_RELATION = "ccomp"; // "we have found <that ....>"

    /**
     * Find statements in a sentence.
     *
     * @param sentence the sentence to look in
     * @return statements
     */
    public static List<Statement> find(CoreMap sentence) {
        SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

        // find statement components
        Set<Subject> subjects = SubjectFinder.find(graph);
        Set<Verb> verbs = VerbFinder.find(graph);
        Set<DirectObject> directObjects = DirectObjectFinder.find(graph);
        Set<IndirectObject> indirectObjects = IndirectObjectFinder.find(graph);

        // merge all components into same set before linking
        Set<StatementComponent> components = new HashSet<>();
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
    private static List<Statement> link(SemanticGraph graph, Set<StatementComponent> components) {
        Map<StatementComponent, StatementComponent> childToParentMapping = new HashMap<>();
        Map<StatementComponent, StatementComponent> interStatementMapping = new HashMap<>();
        Set<Set<StatementComponent>> componentSets = new HashSet<>();
        List<Statement> statements = new ArrayList<>();

        // find the direct parent for each component based on its primary word
        for (StatementComponent component : components) {
            IndexedWord parentPrimary = graph.getParent(component.getPrimary());
            StatementComponent parent = getFromPrimary(parentPrimary, components);

            // keep track of linked statements
            if (parentPrimary != null && graph.reln(parentPrimary, component.getPrimary()).getShortName().equals(CCOMP_RELATION)) {
                interStatementMapping.put(component, parent);
            } else {
                childToParentMapping.put(component, parent);
            }
        }

        // remove connections between identical component types
        for (StatementComponent component : components) {
            StatementComponent parent = childToParentMapping.getOrDefault(component, null);

            if (parent instanceof Subject && component instanceof Subject
                    || parent instanceof Verb && component instanceof Verb
                    || parent instanceof DirectObject && component instanceof DirectObject
                    || parent instanceof IndirectObject && component instanceof IndirectObject) {
                childToParentMapping.remove(component);
            }
        }

        // create sets of components by following all dependencies
        for (StatementComponent child : childToParentMapping.keySet()) {
            StatementComponent parent = childToParentMapping.get(child);
            boolean found = false;

            // find existing component set to link up to
            for (Set<StatementComponent> componentSet : componentSets) {
                if (componentSet.contains(parent) || componentSet.contains(child)) {
                    if (parent != null) componentSet.add(parent);
                    componentSet.add(child);
                    found = true;
                    break;
                }
            }

            // create new component set if no parent was found
            if (!found) {
                Set<StatementComponent> newComponentSet = new HashSet<>();
                if (parent != null) newComponentSet.add(parent);
                newComponentSet.add(child);
                componentSets.add(newComponentSet);
            }
        }

        // merge any component sets that intersect
        Set<Set<StatementComponent>> mergedComponentSets = new HashSet<>();
        for (Set<StatementComponent> componentSet : componentSets) {
            Set<StatementComponent> mergedComponentSet = new HashSet<>(componentSet);

            for (Set<StatementComponent> otherComponentSet : componentSets) {
                if (intersect(mergedComponentSet, otherComponentSet)) {
                    mergedComponentSet.addAll(otherComponentSet);
                }
            }

            mergedComponentSets.add(mergedComponentSet);  // re-adding identical merged sets has no effect
        }

        // create statements from component sets
        for (Set<StatementComponent> componentSet : mergedComponentSets) {
            Subject subject = null;
            Verb verb = null;
            DirectObject directObject = null;
            IndirectObject indirectObject = null;

            for (StatementComponent component : componentSet) {
                if (component instanceof Subject) {
                    subject = (Subject) component;
                } else if (component instanceof Verb) {
                    verb = (Verb) component;
                } else if (component instanceof DirectObject) {
                    directObject = (DirectObject) component;
                } else if (component instanceof IndirectObject) {
                    indirectObject = (IndirectObject) component;
                }
            }

            logger.info("linked statement from components: " + componentSet);
            statements.add(new Statement(subject, verb, directObject, indirectObject));
        }

        logger.info("component mapping: " + childToParentMapping);
        logger.info("based on dependencies: " + graph.typedDependencies());
        logger.info("links between statements: " + interStatementMapping);

        return statements;
    }

    /**
     * Test whether two component sets intersect.
     *
     * @param set1
     * @param set2
     * @return
     */
    private static boolean intersect(Set<StatementComponent> set1, Set<StatementComponent> set2) {
        for (StatementComponent component : set1) {
            if (set2.contains(component)) {
                return true;
            }
        }

        return false;
    }

    /**
     * The StatementComponent corresponding to a primary word.
     *
     * @param primary primary word of component
     * @param components set of components to search in
     * @return component
     */
    private static StatementComponent getFromPrimary(IndexedWord primary, Set<StatementComponent> components) {
        for (StatementComponent component : components) {
            if (component.getPrimary().equals(primary)) return component;
        }

        return null;
    }
}
