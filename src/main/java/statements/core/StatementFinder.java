package statements.core;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

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

         // TODO: basic algorithm
         /*
            Algorithm sketch:
                1. One-word sentences are discarded since they are not proper sentences, as are sentences without an S.
                2. TODO: The graph is analysed for subjects and these are used to create statements.
                   there is link-up method at the end that produces the actual subjects from the components,
                   i.e. it gets the list of subjects, verbs, and objects and spits out statements
                   // TODO: figure out how to deal with stuff like "she's not pretty"
                3. TODO: If no subjects can be found, the statement is assumed to be self-referencing
                   and the verb phrase is analysed instead (e.g. "hate cycling!").

                   TODO: capitalisation confusion in verb phrases
                   it seems like parser results turn verb phrases like "hate cycling"
                   into compound noun phrases if the first word is capitalised ("Hate") and can also be a noun.
                   "Hated cycling" resolves correctly, though, since there is no way to confuse it.
                   Will probably need to hack around that somehow to get the best results.
         */
         return link(subjects, verbs, null);  // TODO: make not null
    }

    /**
     * Produces statements by linking together statement components.
     * @param subjects subjects found in the sentence
     * @param verbs verbs found in the sentence
     * @param objects objects found in the sentence
     * @return statements
     */
    private static List<Statement> link(Set<Subject> subjects, Set<Verb> verbs, Set<IndirectObject> objects) {
        return null;  // TODO: implement
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
