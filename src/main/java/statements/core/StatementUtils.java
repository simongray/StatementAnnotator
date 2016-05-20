package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.Comparator;
import java.util.List;

/**
 * Utilities for Statement and related classes.
 */
public class StatementUtils {
    /**
     * Joins a list of IndexedWords together in the right order without putting spaces before commas.
     * @param words the list of words to be joined
     * @return the string representing the words
     */
    public static String join(List<IndexedWord> words) {
        words.sort(new IndexComparator());

        // since IndexedWords are not joinable in a pretty way
        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < words.size(); i++) {
            if (i != 0 || !words.get(i).tag().equals(",")) buffer.append(" ");
            buffer.append(words.get(i).word());
        }

        return buffer.toString();
    }

    /**
     * Used to sort the sets when strings are needed.
     */
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
