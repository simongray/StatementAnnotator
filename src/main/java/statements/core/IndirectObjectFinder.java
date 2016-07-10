package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds indirect objects in sentences.
 */
public class IndirectObjectFinder extends AbstractFinder<IndirectObject> {
    private final Logger logger = LoggerFactory.getLogger(IndirectObjectFinder.class);

    private Map<IndexedWord, IndexedWord> nmodMapping;
    private Set<IndexedWord> nsubjSubjects;
    private Set<IndirectObject> indirectObjects;

    @Override
    protected void init() {
        nmodMapping = new HashMap<>();
        nsubjSubjects = new HashSet<>();
        indirectObjects = new HashSet<>();
        logger.info("ignored words: " + ignoredWords);
    }

    @Override
    protected void check(TypedDependency dependency) {
        // the dep is the potential IndirectObject, while the gov is needed to sort out nmod relations to subjects
        updateMapping(nmodMapping, dependency, Relations.NMOD);

        // TODO: consider whether nsubjpass, csubj should also be considered
        // used in conjunction with the set of governors in the mapping to remove nmod deps connected to subjects
        // this is done to fix cases such as "of Sweden" being seen as an indirect object in "Henry Larsson of Sweden"
        addDependent(nsubjSubjects, dependency, Relations.NSUBJ);
    }

    @Override
    protected Set<IndirectObject> get() {
        Set<IndexedWord> subjectRelatedNmod = new HashSet<>();

        // find the intersection between the subjects and the nmod governors
        // use this intersection to find any affected nmod dependents
        for (IndexedWord subject : nsubjSubjects) {
            for (IndexedWord nmodGovernor : nmodMapping.keySet()) {
                if (nmodMapping.get(nmodGovernor).equals(subject)) {
                    subjectRelatedNmod.add(nmodGovernor);
                }
            }
        }

        logger.info("nmodMapping: " + nmodMapping);
        logger.info("subjectRelatedNmod: " + subjectRelatedNmod);

        // remove nmod dependents related to subjects of a sentence
        // these are considered extended descriptions of nouns rather than real indirect objects
        for (IndexedWord obj : subjectRelatedNmod) {
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

        logger.info("indirect objects found: " + indirectObjects);

        return indirectObjects;
    }
}
