package statements.matching;

import edu.mit.jwi.Dictionary;
import edu.mit.jwi.IDictionary;
import edu.mit.jwi.item.IIndexWord;
import edu.mit.jwi.item.IWord;
import edu.mit.jwi.item.IWordID;
import edu.mit.jwi.item.POS;
import java.io.IOException;
import java.net.URL;
import java.util.*;

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
     *
     * @param pos the part-of-speech tag of the word
     * @param word the word to find synonyms for
     * @return the synonyms, including the original word
     */
    public Set<String> getSynonyms(POS pos, String word) {
        // the entry word is always itself part of the synonyms
        Set<String> synonyms = new HashSet<>();
        synonyms.add(word);

        // make sure that multi-word entries are in the Wordnet format
        word = word.replaceAll(" ", "_");

        // find the relevant dictionary entry
        IIndexWord indexWord = dict.getIndexWord(word, pos);

        // find every possible synonym in the dictionary
        if (indexWord != null) {
            for (IWordID wordID : indexWord.getWordIDs()) {
                IWord entry = dict.getWord(wordID);
                synonyms.add(entry.getLemma().replaceAll("_", " "));

                for (IWord iWord : entry.getSynset().getWords()) {
                    synonyms.add(iWord.getLemma().replaceAll("_", " "));
                }
            }
        }

        return synonyms;
    }

    /**
     * Returns a more precise set of synonyms for some word(s).
     * This method will only return synonyms that intersect at least one other entry's synonyms,
     * making it more precise than using all possible synonyms for a single word.
     * Useful for pattern matching using proxies.
     *
     * @param words the word(s) to find synonyms for
     * @param pos the part-of-speech tag of the word
     * @return the synonyms, including the original word
     */
    public Set<String> getSynonyms(POS pos, String... words) {
        if (words.length == 1) return getSynonyms(pos, words[0]);
        Set<IWord> entries = new HashSet<>();
        Set<IWord> intersectingSynonyms = new HashSet<>();
        List<List<IWord>> entrySynonymResults = new ArrayList<>();

        Set<String> synonyms = new HashSet<>();

        // find the relevant synonyms for each word
        for (String word : words) {
            // the entry word is always itself part of the synonyms
            synonyms.add(word);


            // make sure that multi-word entries are in the Wordnet format
            word = word.replaceAll(" ", "_");

            // find the dictionary entry for the word
            IIndexWord indexWord = dict.getIndexWord(word, pos);
            if (indexWord != null) {
                List<IWordID> dictionaryResult = indexWord.getWordIDs();

                // find synonyms from dictionary entry
                if (dictionaryResult != null) {
                    List<IWord> entrySynonyms = new ArrayList<>();

                    for (IWordID wordID : dictionaryResult) {
                        IWord entry = dict.getWord(wordID);
                        entrySynonyms.addAll(entry.getSynset().getWords());
                    }

                    entries.addAll(entrySynonyms);
                    entrySynonymResults.add(entrySynonyms);
                }
            }
        }

        // only preserve synonyms that intersect at least one other entry's synonyms
        for (IWord entry : entries) {
            int existenceCount = 0;

            for (List<IWord> dictionaryResult : entrySynonymResults) {
                if (dictionaryResult.contains(entry)) {
                    existenceCount++;
                    if (existenceCount > 1) {
                        intersectingSynonyms.add(entry);
                        break;
                    }
                }
            }
        }

        // retrieve the lemmas from the remaining entries
        for (IWord intersectingSynonym : intersectingSynonyms) {
            synonyms.add(intersectingSynonym.getLemma().replaceAll("_", " "));
        }

        return synonyms;
    }
}
