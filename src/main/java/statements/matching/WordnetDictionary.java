package statements.matching;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.Set;

/**
 * A dictionary class for interfacing with Wordnet using JWI.
 */
public class WordnetDictionary {
    IDictionary dict;

    public WordnetDictionary() throws IOException {
        String path = "wordnet/dict/";
        URL url = new URL("file", null, path);

        dict = new Dictionary(url);
        dict.open();
    }

    /**
     * Returns the set of synonyms for some word.
     * Useful for pattern matching.
     *
     * @param word the word to find synonyms for
     * @param pos the part-of-speech tag of the word
     * @return the synonyms, including the original word
     */
    public Set<String> getSynonyms(String word, POS pos) {
        word = word.replaceAll(" ", "_");
        IIndexWord indexWord = dict.getIndexWord(word, pos);
        Set<String> synonyms = new HashSet<>();

        for (IWordID wordID : indexWord.getWordIDs()) {
            IWord entry = dict.getWord(wordID);
            synonyms.add(entry.getLemma().replaceAll("_", " "));

            for (IWord iWord : entry.getSynset().getWords()) {
                synonyms.add(iWord.getLemma().replaceAll("_", " "));
            }
        }

        return synonyms;
    }
}
