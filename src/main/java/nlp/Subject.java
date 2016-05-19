package nlp;

import edu.stanford.nlp.ling.IndexedWord;
import sentiment.ComplexSentiment;

import java.util.*;

/**
 * Takes a flexible view of what constitutes a sentence subject.
 */
public class Subject {
    private final IndexedWord entry;
    private final Set<IndexedWord> children;

    public Subject(IndexedWord entry, Set<IndexedWord> children) {
        this.entry = entry;
        this.children = children;
    }

    public String shortName() {
        return entry.word();
    }

    public String longName() {
        List<IndexedWord> indexedWords = new ArrayList<>(children);
        indexedWords.add(entry);
        indexedWords.sort(new IndexComparator());
        List<String> words = new ArrayList<>();
        for (IndexedWord indexedWord : indexedWords) {
            words.add(indexedWord.word());
        }

        return String.join(" ", words);
    }

    @Override
    public String toString() {
        return shortName() + " (" + longName() + ")";
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
