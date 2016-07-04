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
public class DirectObjectFinder extends AbstractFinder<DirectObject> {
    private static final Logger logger = LoggerFactory.getLogger(DirectObjectFinder.class);

    /**
     * The direct objects that are found in a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return direct objects
     */
    @Override
    public Set<DirectObject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Map<IndexedWord, IndexedWord> dobjMapping = new HashMap<>();
        Set<IndexedWord> csubjVerbs = new HashSet<>();  // for verbs that act as subjects
        Set<IndexedWord> copObjects = new HashSet<>();
        Set<DirectObject> directObjects = new HashSet<>();

        Set<IndexedWord> ignoredWords = getIgnoredWords(graph);
        logger.info("ignored words: " + ignoredWords);

        // find simple objects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(Relations.DOBJ)) {
                if (!ignoredWords.contains(dependency.dep())) dobjMapping.put(dependency.dep(), dependency.gov());
            }
            if (dependency.reln().getShortName().equals(Relations.CSUBJ)) {
                if (!ignoredWords.contains(dependency.dep())) csubjVerbs.add(dependency.dep());
            }
            if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
                if (hasCopula(dependency.gov(), graph)) {
                    if (!ignoredWords.contains(dependency.dep())) copObjects.add(dependency.gov());
                }
            }
        }

        logger.info("dobj objects mapping: " + dobjMapping);
        logger.info("cop objects found: " + copObjects);
        logger.info("csubj objects found: " + csubjVerbs);

        // find possible xcomp+dobj (V+O constructions)
        Set<IndexedWord> voContstructionObjects = new HashSet<>();
        Set<IndexedWord> objectsToRemove = new HashSet<>();

        // remove dobj objects part of clausal verb subjects (through csubj relation)
        for (IndexedWord csubjVerb : csubjVerbs) {
            for (IndexedWord obj : dobjMapping.keySet()) {
                if (dobjMapping.get(obj) == csubjVerb) {
                    objectsToRemove.add(obj);
                }
            }
        }

        logger.info("objects to remove: " + objectsToRemove);

        // remove the objects that don't qualify
        for (IndexedWord object : objectsToRemove) {
            dobjMapping.remove(object);
        }

        // create direct object mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> dobjObjectMapping = StatementUtils.makeChildMap(dobjMapping.keySet(), Relations.CONJ, graph);

        // create copula object mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> copObjectMapping = StatementUtils.makeChildMap(copObjects, Relations.CONJ, graph);

        // build complete objects from mappings
        for (IndexedWord object : dobjObjectMapping.keySet()) {
            directObjects.add(new DirectObject(object, dobjObjectMapping.get(object), DirectObject.Type.DOBJ, graph));
        }
        for (IndexedWord object : copObjectMapping.keySet()) {
            directObjects.add(new DirectObject(object, copObjectMapping.get(object), DirectObject.Type.COP, graph));
        }

        logger.info("dobjObjectMapping: " + dobjObjectMapping);
        logger.info("copObjectMapping: " + copObjectMapping);
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
