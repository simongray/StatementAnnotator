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

        // find simple indirect objects from relations
        // NOTE: the reason for the mapping in nmodMapping is so that the cases described in the stanza below can be filtered
        // the key set of the map are the real nmod entries!
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

        // find sequences (ex: "in a chair in a house in Copenhagen")
        Set<Set<IndexedWord>> sequences = StatementUtils.findSequences(nmodMapping.keySet(), Relations.NMOD, graph);
        logger.info("sequences: " + sequences);

        Set<IndexedWord> entries = new HashSet<>();

        // find out which indirect objects are already part of sequences
        for (IndexedWord entry : nmodMapping.keySet()) {
            boolean notInSequence = true;

            for (Set<IndexedWord> sequence : sequences) {
                if (sequence.contains(entry)) {
                    notInSequence = false;
                    break;
                }
            }

            if (notInSequence) entries.add(entry);
        }

        // build complete objects from single entries
        for (IndexedWord entry : entries) {
            indirectObjects.add(new IndirectObject(entry, graph));
            logger.info("new indirect object, primary: " + entry);
        }

        // build complete objects from sequences
        for (Set<IndexedWord> sequence : sequences) {
            IndexedWord primary = null;

            // treat lowest indexed word as primary entry
            for (IndexedWord entry : sequence) {
                if (primary == null || primary.index() > entry.index()) {
                    primary = entry;
                }
            }

            logger.info("new indirect object with " + sequence.size() + " entries, primary: " + primary);

            indirectObjects.add(new IndirectObject(primary, graph));
        }

        logger.info("nmodMapping: " + nmodMapping);
        logger.info("indirect objects found: " + indirectObjects);

        return indirectObjects;
    }
}
