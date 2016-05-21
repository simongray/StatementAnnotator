package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;

/**
 * Utilities for Statement and related classes.
 */
public class StatementUtils {
    /**
     * Joins a list of IndexedWords together in the correct order without putting spaces before commas.
     * @param words the list of words to be joined
     * @return the string representing the words
     */
    public static String join(Set<IndexedWord> words) {
        List<IndexedWord> wordsList = new ArrayList<>(words);
        wordsList.sort(new IndexComparator());

        StringBuffer buffer = new StringBuffer();
        for (int i = 0; i < wordsList.size(); i++) {
            if (i != 0 && !wordsList.get(i).tag().equals(",")) buffer.append(" ");
            buffer.append(wordsList.get(i).word());
        }

        return buffer.toString();
    }

    /**
     * Reduce a variable amount of Resemblance objects to a single Resemblance.
     * Useful for
     * @param resemblances
     * @return
     */
    static Resemblance reduce(Resemblance... resemblances) {
        Resemblance lowestCommonDenominator = Resemblance.FULL;  // default

        for (Resemblance resemblance : resemblances) {
            if (resemblance == Resemblance.NONE) {
                lowestCommonDenominator = resemblance; break;
            } else if (resemblance == Resemblance.SLIGHT) {
                lowestCommonDenominator = resemblance;
            } else if (resemblance == Resemblance.CLOSE) {
                lowestCommonDenominator = resemblance;
            }
        }

        return lowestCommonDenominator;
    }

    /**
     * Usd to sort IndexedWords by index.
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
