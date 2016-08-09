package statements.patterns;


import edu.mit.jwi.item.POS;

import java.io.IOException;

public class Common {
    public static WordnetDictionary dictionary;

    public static WordnetDictionary getDictionary() {
        if (dictionary == null) {
            try {
                dictionary = new WordnetDictionary();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        return dictionary;
    }

    public final static String[] OPINION_VERB = getDictionary().getSynonyms(POS.VERB, "think", "reckon", "believe", "know").stream().toArray(String[]::new);
    public final static String[] LOCATION_VERB = getDictionary().getSynonyms(POS.VERB, "be", "come", "go", "live", "stay", "visit", "travel").stream().toArray(String[]::new);

    public final static String[] OPINION_NOUN = getDictionary().getSynonyms(POS.NOUN, "opinion", "thought", "mind").stream().toArray(String[]::new);

    public final static String[] LOCATION_PREPOSITION = new String[] { "in", "from", "to", "by", "at", "around", "on" };
}
