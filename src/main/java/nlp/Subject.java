package nlp;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.*;

/**
 * Takes a flexible view of what constitutes a sentence subject.
 * Using as reference: http://partofspeech.org/subject/
 */
public class Subject {
    private final IndexedWord simpleSubject;
    private final Set<IndexedWord> completeSubject;
    private final Set<IndexedWord> compoundParts;

    /**
     * A Subject in a sentence as based on the nsubj relation.
     * @param simpleSubject the dependant in an nsubj relation
     * @param graph dependency graph containing the dependant
     */
    public Subject(IndexedWord simpleSubject, SemanticGraph graph) {
        this.simpleSubject = simpleSubject;
        this.completeSubject = getCompleteSubject(simpleSubject, graph);
        this.compoundParts = getCompoundParts(simpleSubject, graph);
    }

    /**
     * Retrieves the words making up the complete subject based on the simple subject and its relations in the graph.
     * @param simpleSubject
     * @param graph
     * @return
     */
    private Set<IndexedWord> getCompleteSubject(IndexedWord simpleSubject, SemanticGraph graph) {
        // TODO: create full name using relations such as compound and det (or perhaps by ignoring relations such as cc)
        return null;
    }

    /**
     * Retrieves simple subjects for the compound subject if applicable.
     * Useful for keeping track of the entire compound subject from any of its parts.
     * @param vertex
     * @param graph
     * @return
     */
    private Set<IndexedWord> getCompoundParts(IndexedWord vertex, SemanticGraph graph) {
        // TODO: create links to other Subject parts from conj:and and conj:or
        return null;
    }

    /**
     * Get the name of the simple subject.
     * @return
     */
    public String getSimpleName() {
        return simpleSubject.word();
    }

    /**
     * Get the name of the complete subject.
     * @return
     */
    public String getCompleteName() {
        List<IndexedWord> indexedWords = new ArrayList<>(completeSubject);
        indexedWords.sort(new IndexComparator());

        // since IndexedWords are not joinable in a pretty way
        List<String> words = new ArrayList<>();
        for (IndexedWord indexedWord : indexedWords) {
            words.add(indexedWord.word());
        }

        return String.join(" ", words);
    }

    /**
     * Get the other simple subjects making up the compound subject.
     * @return
     */
    public Set<IndexedWord> getCompoundParts() {
        return compoundParts;
    }

    @Override
    public String toString() {
        return getSimpleName() + " (" + getCompleteName() + ")";
    }

    public static class IndexComparator implements Comparator<IndexedWord> {
        @Override
        public int compare(IndexedWord x,IndexedWord y) {
            int xn = x.index();
            int yn = y.index();
            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return 1;
                } else {
                    return -1;
                }
            }
        }
    }
}
