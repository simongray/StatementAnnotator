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
public class ObjectFinder {
    private static final Logger logger = LoggerFactory.getLogger(ObjectFinder.class);
    private static final String NSUBJ_RELATION = "nsubj";
    private static final String DOBJ_RELATION = "dobj";
    private static final String COP_RELATION = "cop";
    private static final String NMOD_REALTION = "nmod";


    /**
     * The subjects that are found in a sentence.
     * @param graph the dependency graph of a sentence
     * @return subjects
     */
    public static Set<Subject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> directObjects = new HashSet<>();
        Set<IndexedWord> copulaObjects = new HashSet<>();
        Set<IndexedWord> indirectObjects = new HashSet<>();
        Map<IndexedWord, Set<IndexedWord>> subjectMapping = new HashMap<>();
        Set<Subject> objects = new HashSet<>();

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
            if (dependency.reln().getShortName().equals(NMOD_REALTION)) {
                indirectObjects.add(dependency.dep());
            }
        }

        // TODO: split into direct and indirect object finders

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
