package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds direct objects in sentences.
 */
public class DirectObjectFinder extends AbstractFinder<DirectObject> {
    private Map<IndexedWord, IndexedWord> dobjMapping ;
    private Set<IndexedWord> csubjVerbs;// for verbs that act as subjects
    private Set<IndexedWord> copObjects;
    private Set<DirectObject> directObjects;

    @Override
    protected void init() {
        dobjMapping = new HashMap<>();
        csubjVerbs = new HashSet<>();
        copObjects = new HashSet<>();
        directObjects = new HashSet<>();
    }

    @Override
    protected void check(TypedDependency dependency) {
        // mapping from object to verb, defined by the dobj relation
        if (dependency.reln().getShortName().equals(Relations.DOBJ)) {
            if (!ignoredWords.contains(dependency.dep())) dobjMapping.put(dependency.dep(), dependency.gov());
        }

        // direct objects defined by the cop relation
        // when a is the governor in both an nsubj and a cop relation, then it's a direct object
        if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
            if (hasCopula(dependency.gov(), graph)) {
                if (!ignoredWords.contains(dependency.dep())) copObjects.add(dependency.gov());
            }
        }

        addDependent(csubjVerbs, dependency, Relations.CSUBJ);
    }

    @Override
    protected Set<DirectObject> get() {
        Set<IndexedWord> objectsToRemove = new HashSet<>();

        logger.info("dobj objects mapping: " + dobjMapping);
        logger.info("cop objects found: " + copObjects);
        logger.info("csubj objects found: " + csubjVerbs);

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

        Set<IndexedWord> entries = new HashSet<>();
        entries.addAll(dobjMapping.keySet());
        entries.addAll(copObjects);

        // build complete objects
        for (IndexedWord entry : entries) {
            logger.info("creating new direct object based on: " + entry);
            directObjects.add(new DirectObject(entry, graph));
        }

//        Set<IndexedWord> conjunctions = new HashSet<>();
//
//        // TODO: revise, very hacky, created to fix this example: Central Copenhagen is for tourists and people from Jutland.
//        // check for missing components in conjunctions
//        for (DirectObject object : directObjects) {
//            conjunctions.addAll(object.getConjunction());
//        }
//        conjunctions.removeAll(entries);
//
//        // create direct objects from conjunctions
//        for (IndexedWord word : conjunctions) {
//            logger.info("creating new direct object based on: " + word);
//            directObjects.add(new DirectObject(word, graph));
//        }

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
