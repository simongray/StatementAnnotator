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
        Map<IndexedWord, IndexedWord> dobjMapping = new HashMap<>();
        Set<IndexedWord> xcompObjects = new HashSet<>();
        Set<IndexedWord> csubjVerbs = new HashSet<>();  // for verbs that act as subjects
        Set<IndexedWord> copObjects = new HashSet<>();
        Set<DirectObject> directObjects = new HashSet<>();

        // find simple objects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(Relations.DOBJ)) {
                dobjMapping.put(dependency.dep(), dependency.gov());
            }
            if (dependency.reln().getShortName().equals(Relations.XCOMP)) {
                xcompObjects.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(Relations.CSUBJ)) {
                csubjVerbs.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
                if (hasCopula(dependency.gov(), graph)) {
                    copObjects.add(dependency.gov());
                }
            }
        }

        logger.info("dobj objects mapping: " + dobjMapping);
        logger.info("xcomp objects found: " + xcompObjects);
        logger.info("cop objects found: " + copObjects);
        logger.info("csubj objects found: " + csubjVerbs);

        // find possible xcomp+dobj (V+O constructions)
        Set<IndexedWord> voContstructionObjects = new HashSet<>();

        // TODO: review this stanza (an possibly its relation to the one below)
        for (IndexedWord xcompObject : xcompObjects) {
            for (IndexedWord dobjObject : dobjMapping.keySet()) {
                GrammaticalRelation relation = graph.reln(xcompObject, dobjObject);
                if (relation != null && relation.getShortName().equals(Relations.DOBJ)) {
                    dobjMapping.remove(dobjObject);
                }
            }
        }

        // remove dobj objects part of clausal verb subjects (through csubj relation)
        for (IndexedWord csubjVerb : csubjVerbs) {
            for (IndexedWord obj : dobjMapping.keySet()) {
                if (dobjMapping.get(obj) == csubjVerb) dobjMapping.remove(obj);  // TODO: potential concurrency issue
            }
        }

        // create direct object mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> dobjObjectMapping = StatementUtils.makeRelationsMap(dobjMapping.keySet(), Relations.CONJ, graph);

        // create xcomp object mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> xcompObjectMapping = StatementUtils.makeRelationsMap(xcompObjects, Relations.CONJ, graph);

        // create copula object mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> copObjectMapping = StatementUtils.makeRelationsMap(copObjects, Relations.CONJ, graph);

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
