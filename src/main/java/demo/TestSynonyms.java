package demo;

import edu.mit.jwi.item.POS;
import statements.matching.WordnetDictionary;

import java.io.IOException;

public class TestSynonyms {
    public static void main(String[] args) throws IOException {
        WordnetDictionary wordnetDictionary = new WordnetDictionary();
        for (String word : wordnetDictionary.getSynonyms(POS.VERB, "think", "reckon", "believe")) {
            System.out.println(word);
        }
    }
}
