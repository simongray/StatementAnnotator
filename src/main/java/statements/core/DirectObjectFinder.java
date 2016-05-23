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
        Set<IndexedWord> dobjObjects = new HashSet<>();
        Set<IndexedWord> copObjects = new HashSet<>();
        Map<IndexedWord, Set<IndexedWord>> dobjObjectMapping = new HashMap<>();
        Map<IndexedWord, Set<IndexedWord>> copObjectMapping = new HashMap<>();
        Set<DirectObject> directObjects = new HashSet<>();

        // find simple objects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(DOBJ_RELATION)) {
                dobjObjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(NSUBJ_RELATION)) {
                if (hasCopula(dependency.gov(), graph)) {
                    copObjects.add(dependency.dep());
                }
            }
        }

        // create direct object mapping based on relations
        for (IndexedWord object : dobjObjects) {
            IndexedWord parent = graph.getParent(object);
            if (dobjObjects.contains(parent)) {
                Set<IndexedWord> objects = dobjObjectMapping.getOrDefault(parent, new HashSet<>());
                objects.add(object);
                dobjObjectMapping.put(parent, objects);
            } else {
                dobjObjectMapping.putIfAbsent(object, new HashSet<>());
            }
        }

        // create copula object mapping based on relations
        for (IndexedWord object : copObjects) {
            IndexedWord parent = graph.getParent(object);
            if (copObjects.contains(parent)) {
                Set<IndexedWord> objects = copObjectMapping.getOrDefault(parent, new HashSet<>());
                objects.add(object);
                copObjectMapping.put(parent, objects);
            } else {
                copObjectMapping.putIfAbsent(object, new HashSet<>());
            }
        }

        // build complete objects from mappings
        for (IndexedWord object : dobjObjectMapping.keySet()) {
            directObjects.add(new DirectObject(object, dobjObjectMapping.get(object), false, graph));
        }
        for (IndexedWord object : copObjectMapping.keySet()) {
            directObjects.add(new DirectObject(object, copObjectMapping.get(object), true, graph));
        }

        return directObjects;
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
