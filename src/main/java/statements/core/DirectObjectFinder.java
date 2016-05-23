package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds subjects in sentences.
 */
public class DirectObjectFinder {
    private static final Logger logger = LoggerFactory.getLogger(DirectObjectFinder.class);
    private static final String NSUBJ_RELATION = "nsubj";
    private static final String DOBJ_RELATION = "dobj";
    private static final String COP_RELATION = "cop";

    /**
     * The direct objects that are found in a sentence.
     * @param graph the dependency graph of a sentence
     * @return direct objects
     */
    public static Set<DirectObject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> directObjects = new HashSet<>();
        Set<IndexedWord> copulaObjects = new HashSet<>();
        Map<IndexedWord, Set<IndexedWord>> subjectMapping = new HashMap<>();
        Set<DirectObject> objects = new HashSet<>();

        // find simple objects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(DOBJ_RELATION)) {
                directObjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(NSUBJ_RELATION)) {
                if (hasCopula(dependency.gov(), graph)) {
                    copulaObjects.add(dependency.dep());
                }
            }
        }

        for (IndexedWord directObject : directObjects) {
            objects.add(new DirectObject(directObject, false));
        }
        for (IndexedWord copulaObject : copulaObjects) {
            objects.add(new DirectObject(copulaObject, true));
        }

        return objects;
    }

    /**
     * Whether or not a word has a copula relation.
     * @param word the word to examine
     * @param graph the graph in which the word appears
     * @return copula existence
     */
    private static boolean hasCopula(IndexedWord word, SemanticGraph graph) {
        for (IndexedWord child : graph.getChildren(word)) {
            if (graph.reln(word, child).getShortName().equals(COP_RELATION)) {
                return true;
            }
        }

        return false;
    }
}
