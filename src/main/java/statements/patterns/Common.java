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

    public final static String[] THINK = getDictionary().getSynonyms(POS.VERB, "think", "reckon", "believe").stream().toArray(String[]::new);
}
