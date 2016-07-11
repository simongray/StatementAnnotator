package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.*;

/**
 * Finds indirect objects in sentences.
 */
public class IndirectObjectFinder extends AbstractFinder<IndirectObject> {
    private Map<IndexedWord, IndexedWord> nmodMapping;
    private Set<IndexedWord> nsubjSubjects;
    private Set<IndirectObject> indirectObjects;

    @Override
    protected void init() {
        nmodMapping = new HashMap<>();
        nsubjSubjects = new HashSet<>();
        indirectObjects = new HashSet<>();
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

        // find sequences (ex: "in a chair in a house in Copenhagen") and shared governance
        // these are used for creating "conjunctions"
        Set<Set<IndexedWord>> sequences = StatementUtils.findSequences(nmodMapping.keySet(), Relations.NMOD, graph);
        Collection<Set<IndexedWord>> governance = StatementUtils.flip(nmodMapping).values();
        logger.info("sequences: " + sequences);
        logger.info("governance: " + governance);

        // find all objects that are part of "conjunctions"
        Set<Set<IndexedWord>> conjunctions = new HashSet<>(governance);
        conjunctions.addAll(sequences);
        logger.info("unmerged conjunctions: " + conjunctions);

        conjunctions = StatementUtils.merge(conjunctions);
        logger.info("merged conjunctions: " + conjunctions);

        // build complete objects from conjunctions
        for (Set<IndexedWord> conjunction : conjunctions) {
            if (conjunction.size() > 1) {
                for (IndexedWord word : conjunction) {
                    Set<IndexedWord> otherWords = new HashSet<>(conjunction);
                    otherWords.remove(word);

                    logger.info("added new indirect object from conjunction: " + word);
                    logger.info("conjunction words: " + otherWords);
                    indirectObjects.add(new IndirectObject(word, graph, getLabels(word, Labels.CONJ_INDIRECT_OBJECT), otherWords));
                }
            } else {
                IndexedWord word = conjunction.iterator().next();
                logger.info("added new indirect object: " + word);
                indirectObjects.add(new IndirectObject(word, graph, getLabels(word, Labels.CONJ_INDIRECT_OBJECT)));
            }
        }

        return indirectObjects;
    }
}
