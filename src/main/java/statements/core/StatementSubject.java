package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.*;

/**
 * This class represents the subject of a natural language statement.
 */
public class StatementSubject {
    private final IndexedWord simpleSubject = null;
    private final Set<IndexedWord> completeSubject = null;
    private final Set<IndexedWord> compoundParts = null;

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
