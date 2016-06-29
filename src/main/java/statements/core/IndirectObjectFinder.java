package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds indirect objects in sentences.
 */
public class IndirectObjectFinder extends AbstractFinder<IndirectObject> {
    private static final Logger logger = LoggerFactory.getLogger(IndirectObjectFinder.class);

    /**
     * The indirect objects that are found in a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return indirect objects
     */
    public Set<IndirectObject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Map<IndexedWord, IndexedWord> nmodMapping = new HashMap<>();
        Set<IndexedWord> subjects = new HashSet<>();
        Set<IndexedWord> subjectRelated = new HashSet<>();
        Set<IndirectObject> indirectObjects = new HashSet<>();

        Set<IndexedWord> ignoredWords = getIgnoredWords(graph);
        logger.info("ignored words: " + ignoredWords);

        // find simple objects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(Relations.NMOD)) {
                if (!ignoredWords.contains(dependency.dep())) nmodMapping.put(dependency.dep(), dependency.gov());
            }
            if (dependency.reln().getShortName().equals(Relations.NSUBJ)) {
                if (!ignoredWords.contains(dependency.dep())) subjects.add(dependency.dep());
            }
        }

        // TODO: consider whether nsubjpass, csubj should also be considered
        // remove indirect object that are a part of subjects (through nsubj relation)
        // this is done to fix cases such as "of Sweden" being seen as an indirect object in "Henry Larsson of Sweden"
        for (IndexedWord subject : subjects) {
            for (IndexedWord obj : nmodMapping.keySet()) {
                if (nmodMapping.get(obj).equals(subject)) {
                    subjectRelated.add(obj);
                }
            }
        }
        for (IndexedWord obj : subjectRelated) {
            nmodMapping.remove(obj);
        }

        // create indirect object mapping based on relations
        Map<IndexedWord, Set<IndexedWord>> nmodObjectMapping = StatementUtils.makeChildMap(nmodMapping.keySet(), Relations.CONJ, graph);

        // build complete objects from mapping
        for (IndexedWord object : nmodObjectMapping.keySet()) {
            indirectObjects.add(new IndirectObject(object, nmodObjectMapping.get(object), graph));
        }

        logger.info("indirect objects found: " + indirectObjects);

        return indirectObjects;
    }
}
