package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
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
     * @param sentence the sentence to look in
     * @return statements
     */
     public static List<Statement> find(CoreMap sentence) {
         logger.info("finding statements in sentence: " + sentence);

         // saves resources
         if (!isProper(sentence)) {
             logger.info("sentence was not proper, no statements found");
             return null;
         }

         List<Statement> statements = new ArrayList<>();
         SemanticGraph graph = sentence.get(SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class);

         Set<Subject> subjects = SubjectFinder.find(graph);
         logger.info(subjects.toString());
         for (Subject subject : subjects) {
             logger.info("components: " + subject.getCompoundStrings());
             logger.info("complete name: " + subject.getName());
         }

//         for (Subject subject : subjects) {
//             System.out.println();
//             System.out.println(subject + " compares to other subjects in sentence in this way: ");
//             for (Subject otherSubject : subjects) {
//                 System.out.println(subject.resemble(otherSubject) + ": " + otherSubject);
//             }
//         }

         Set<Verb> verbs = VerbFinder.find(graph);
         logger.info("verbs: " + verbs.toString());

         Set<DirectObject> directObjects = DirectObjectFinder.find(graph);
         Set<IndirectObject> indirectObjects = IndirectObjectFinder.find(graph);

         logger.info("direct objects: " + directObjects);
         logger.info("indirect objects: " + indirectObjects);

         /*
            Algorithm sketch:
                   there is link-up method at the end that produces the actual subjects from the components,
                   i.e. it gets the list of subjects, verbs, and objects and spits out statements
                3. TODO: If no subjects can be found, the statement is assumed to be self-referencing
                   and the verb phrase is analysed instead (e.g. "hate cycling!").

                   TODO: capitalisation confusion in verb phrases
                   it seems like parser results turn verb phrases like "hate cycling"
                   into compound noun phrases if the first word is capitalised ("Hate") and can also be a noun.
                   "Hated cycling" resolves correctly, though, since there is no way to confuse it.
                   Will probably need to hack around that somehow to get the best results.
         */

         // merge all components into same set before linking
         Set<StatementComponent> components = new HashSet<>();
         components.addAll(subjects);
         components.addAll(verbs);
         components.addAll(directObjects);
         components.addAll(indirectObjects);

         return link(graph, components);
    }

    /**
     * Produces statements by linking together statement components.
     * @param graph the graph of the sentence
     * @param components the various statement components found in sentence
     * @return statements
     */
    private static List<Statement> link(SemanticGraph graph, Set<StatementComponent> components) {
        Map<StatementComponent, StatementComponent> childToParentMapping = new HashMap<>();
        Set<Set<StatementComponent>> componentSets = new HashSet<>();
        List<Statement> statements = new ArrayList<>();

        // find the direct parent for each component based on its primary word
        for (StatementComponent component : components) {
            IndexedWord parentPrimary = graph.getParent(component.getPrimary());
            StatementComponent parent = getFromPrimary(parentPrimary, components);
            logger.info("connection: " + component + " -> " + parent);
            childToParentMapping.put(component, parent);
        }

        // remove connections between identical component types
        for (StatementComponent component : components) {
            StatementComponent parent = childToParentMapping.getOrDefault(component, null);

            if (parent instanceof Subject && component instanceof Subject
                || parent instanceof Verb && component instanceof Verb
                || parent instanceof DirectObject && component instanceof DirectObject
                || parent instanceof IndirectObject && component instanceof IndirectObject) {
                childToParentMapping.remove(component);
                logger.info("removed connection: " + parent + " -> " + component);
            }
        }

        // create sets of components by following all connections
        for (StatementComponent parent : childToParentMapping.keySet()) {
            StatementComponent child = childToParentMapping.get(parent);
            boolean found = false;

            // find existing component set to link up
            for (Set<StatementComponent> componentSet : componentSets) {
                if (componentSet.contains(parent) || componentSet.contains(child)) {
                    componentSet.add(parent);
                    componentSet.add(child);
                    found = true;
                    break;
                }
            }

            // create new component set if none found
            if (!found) {
                Set<StatementComponent> newComponentSet = new HashSet<>();
                newComponentSet.add(parent);
                newComponentSet.add(child);
                componentSets.add(newComponentSet);
            }
        }

        // create statements from component sets
        for (Set<StatementComponent> componentSet : componentSets) {
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

            logger.info("new statement from: " + componentSet);

            statements.add(new Statement(subject, verb, directObject, indirectObject));
        }

        return statements;
    }

    /**
     * The StatementComponent corresponding to a primary word.
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

    /**
     * Test whether a sentence is a proper sentence.
     * Returns true if the ROOT tag is connected an S tag (= there is a VP).
     * This is useful for determining if a sentence is not just an interjection, e.g. "So cool!" or "OK, great.".
     * @param sentence the sentence to test
     * @return proper or not
     */
     private static boolean isProper(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);  // TODO: do using depparse instead?

        for (Tree child : tree.children()) {
            if (child.value().equals("S")) return true;
        }

        return false;
    }
}
