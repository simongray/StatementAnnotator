package statements.core;

import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.trees.TreeCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This class finds valid Statements in sentences.
 */
public  class StatementFinder {
    private static final Logger logger = LoggerFactory.getLogger(StatementFinder.class);

    /**
     * Find statements in a sentence.
     * @param sentence
     * @return
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

         Set<CompleteSubject> subjects = CompleteSubjectFinder.find(graph);
         logger.info(subjects.toString());
         for (CompleteSubject subject : subjects) {
             logger.info("components: " + subject.getCompoundSubjectNames());
             logger.info("complete name: " + subject.getName());
         }

         // TODO: basic algorithm
         /*
            Algorithm sketch:
                1. One-word sentences are discarded since they are not proper sentences, as are sentences without an S.
                2. TODO: The graph is analysed for subjects and these are used to create statements.
                3. TODO: If no subjects can be found, the statement is assumed to be self-referencing
                   and the verb phrase is analysed instead (e.g. "hate cycling!").

                   TODO: capitalisation confusion in verb phrases
                   it seems like parser results turn verb phrases like "hate cycling"
                   into compound noun phrases if the first word is capitalised ("Hate") and can also be a noun.
                   "Hated cycling" resolves correctly, though, since there is no way to confuse it.
                   Will probably need to hack around that somehow to get the best results.
         */
         return null;
    }

    /**
     * Returns true if the ROOT tag is connected an S tag (= there is a VP).
     * This is useful for determining if a sentence is not just an interjection, e.g. "So cool!" or "OK, great.".
     * @param sentence
     * @return
     */
     private static boolean isProper(CoreMap sentence) {
        Tree tree = sentence.get(TreeCoreAnnotations.TreeAnnotation.class);  // TODO: do using depparse instead?

        for (Tree child : tree.children()) {
            if (child.value().equals("S")) return true;
        }

        return false;
    }
}
