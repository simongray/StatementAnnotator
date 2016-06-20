package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.GrammaticalRelation;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds direct objects in sentences.
 */
public class DirectObjectFinder {
    private static final Logger logger = LoggerFactory.getLogger(DirectObjectFinder.class);

    /**
     * The direct objects that are found in a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return direct objects
     */
    public static Set<DirectObject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> dobjObjects = new HashSet<>();
        Set<IndexedWord> xcompObjects = new HashSet<>();
        Set<IndexedWord> copObjects = new HashSet<>();
        Map<IndexedWord, Set<IndexedWord>> dobjObjectMapping = new HashMap<>();
        Map<IndexedWord, Set<IndexedWord>> xcompObjectMapping = new HashMap<>();
        Map<IndexedWord, Set<IndexedWord>> copObjectMapping = new HashMap<>();
        Set<DirectObject> directObjects = new HashSet<>();

        // find simple objects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(Relations.DOBJ)) {
                dobjObjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(Relations.XCOMP)) {
                xcompObjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
                if (hasCopula(dependency.gov(), graph)) {
                    copObjects.add(dependency.gov());
                }
            }
        }

        logger.info("dobj objects found: " + dobjObjects);
        logger.info("xcomp objects found: " + xcompObjects);
        logger.info("cop objects found: " + copObjects);

        // find possible xcomp+dobj (V+O constructions)
        Set<IndexedWord> voContstructionObjects = new HashSet<>();

        for (IndexedWord xcompObject : xcompObjects) {
            for (IndexedWord dobjObject : dobjObjects) {
                GrammaticalRelation relation = graph.reln(xcompObject, dobjObject);
                if (relation != null && relation.getShortName().equals(Relations.DOBJ)) {
                    voContstructionObjects.add(dobjObject);
                }
            }
        }

        // remove dobj objects if part of V+O construction
        dobjObjects.removeAll(voContstructionObjects);

        // create direct object mapping based on relations
        dobjObjectMapping = StatementUtils.makeRelationsMap(dobjObjects, Relations.CONJ, graph);

        // create xcomp object mapping based on relations
        xcompObjectMapping = StatementUtils.makeRelationsMap(xcompObjects, Relations.CONJ, graph);

        // create copula object mapping based on relations
        copObjectMapping = StatementUtils.makeRelationsMap(copObjects, Relations.CONJ, graph);

        // build complete objects from mappings
        for (IndexedWord object : dobjObjectMapping.keySet()) {
            directObjects.add(new DirectObject(object, dobjObjectMapping.get(object), DirectObject.Type.DOBJ, graph));
        }
        for (IndexedWord object : xcompObjectMapping.keySet()) {
            directObjects.add(new DirectObject(object, xcompObjectMapping.get(object), DirectObject.Type.XCOMP, graph));
        }
        for (IndexedWord object : copObjectMapping.keySet()) {
            directObjects.add(new DirectObject(object, copObjectMapping.get(object), DirectObject.Type.COP, graph));
        }

        logger.info("direct objects found: " + directObjects);

        return directObjects;
    }

    /**
     * Whether or not a word has a copula relation.
     *
     * @param word the word to examine
     * @param graph the graph in which the word appears
     * @return copula existence
     */
    private static boolean hasCopula(IndexedWord word, SemanticGraph graph) {
        for (IndexedWord child : graph.getChildren(word)) {
            if (graph.reln(word, child).getShortName().equals(Relations.COP)) {
                return true;
            }
        }

        return false;
    }
}
