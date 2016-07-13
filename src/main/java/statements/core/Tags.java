package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.HashSet;
import java.util.Set;


public class Tags {
    public static final String VB = "VB";
    public static final String VBP = "VBP";

    /**
     * Outgoing relations which are stored internally in any AbstractComponent, but not shown/used by default.
     *
     * An example is the NEG relation, which is separated out from the other words,
     * but used to adjust results when doing comparisons.
     */
    public static final Set<String> VERBS = new HashSet<>();
    static {
        VERBS.add(VB);
        VERBS.add(VBP);
    }

    /**
     * Reduce a set of words based on their part-of-speech tags.
     *
     * @param words
     * @param allowedtags
     * @return
     */
    public static Set<IndexedWord> reduce(Set<IndexedWord> words, Set<String> allowedtags) {
        Set<IndexedWord> badWords = new HashSet<>();
        Set<IndexedWord> reducedWords = new HashSet<>(words);

        for (IndexedWord word : words) {
            if (!allowedtags.contains(word.tag())) badWords.add(word);
        }

        reducedWords.removeAll(badWords);

        return reducedWords;
    }
}
