package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.trees.TypedDependency;

import java.util.*;

/**
 * Finds indirect objects in sentences.
 */
public class IndirectObjectFinder extends AbstractFinder<IndirectObject> {
    private Map<IndexedWord, IndexedWord> nmodMapping;
    private Set<IndirectObject> indirectObjects;
    private Set<IndexedWord> iobjObjects;

    @Override
    protected void init() {
        nmodMapping = new HashMap<>();
        iobjObjects = new HashSet<>();
        indirectObjects = new HashSet<>();
    }

    @Override
    protected void check(TypedDependency dependency) {
        // the dep is the potential IndirectObject, while the gov is needed to sort out nmod relations to subjects
        updateMapping(nmodMapping, dependency, Relations.INDIRECT_OBJECT_NMOD);
        addDependent(iobjObjects, dependency, Relations.IOBJ);
    }

    private boolean isValid(IndexedWord nmodDependent) {
        IndexedWord nmodGovernor = nmodMapping.get(nmodDependent);

        // finds the first reference in a sequence (if applicable)
        while (nmodMapping.containsKey(nmodGovernor)) {
            nmodDependent = nmodGovernor;
            nmodGovernor = nmodMapping.get(nmodDependent);
        }

        // check if the nmod relation is to a verb
        if (PartsOfSpeech.VERBS.contains(nmodGovernor.tag())) {
            return true;
        } else {
            return false;
        }
    }

    @Override
    protected Set<IndirectObject> get() {
        Set<IndexedWord> notRelatedToVerbs = new HashSet<>();

        // find and remove any NMOD relation that isn't tied to a verb or another NMOD relation (= in a sequence)
        // other nmod relations (with subject/dirobj governors) are included directly as part of those components!
        for (IndexedWord nmodDependent : nmodMapping.keySet()) {
            if (!isValid(nmodDependent)) notRelatedToVerbs.add(nmodDependent);
        }

        for (IndexedWord obj : notRelatedToVerbs) {
            nmodMapping.remove(obj);
        }

        // find sequences (ex: "in a chair in a house in Copenhagen") and shared governance
        // these are used for creating "conjunctions"
        Set<Set<IndexedWord>> sequences = StatementUtils.findSequences(nmodMapping.keySet(), Relations.NMOD, graph);
        Collection<Set<IndexedWord>> governance = StatementUtils.flip(nmodMapping).values();

        // find all objects that are part of "conjunctions"
        Set<Set<IndexedWord>> conjunctions = new HashSet<>();
        conjunctions.addAll(governance);
        conjunctions.addAll(sequences);

        conjunctions = StatementUtils.merge(conjunctions);

        // build complete objects from conjunctions
        for (Set<IndexedWord> conjunction : conjunctions) {
            if (conjunction.size() > 1) {
                for (IndexedWord word : conjunction) {
                    Set<IndexedWord> otherWords = new HashSet<>(conjunction);
                    otherWords.remove(word);
                    indirectObjects.add(new IndirectObject(word, graph, getLabels(word, Labels.CONJ_INDIRECT_OBJECT), otherWords));
                }
            } else {
                IndexedWord word = conjunction.iterator().next();
                indirectObjects.add(new IndirectObject(word, graph, getLabels(word, Labels.CONJ_INDIRECT_OBJECT)));
            }
        }

        // build indirect objects from the iobj relation (ex: "He gave Frodo the ring")
        for (IndexedWord word : iobjObjects) {
            indirectObjects.add(new IndirectObject(word, graph, getLabels(word)));
        }

        return indirectObjects;
    }
}
