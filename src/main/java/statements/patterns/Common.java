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
    public final static String[] POSSESS_VERB = getDictionary().getSynonyms(POS.VERB, "have", "own", "possess").stream().toArray(String[]::new);
    public final static String[] LIKE_VERB = getDictionary().getSynonyms(POS.VERB, "like", "love", "prefer").stream().toArray(String[]::new);
    public final static String[] DISLIKE_VERB = getDictionary().getSynonyms(POS.VERB, "dislike", "hate", "detest").stream().toArray(String[]::new);
    public final static String[] WANT_VERB = getDictionary().getSynonyms(POS.VERB, "want", "would like", "desire").stream().toArray(String[]::new);

    public final static String[] OPINION_NOUN = getDictionary().getSynonyms(POS.NOUN, "opinion", "thought", "mind").stream().toArray(String[]::new);
    public final static String[] POSITIVE_ADJECTIVE = getDictionary().getSynonyms(POS.ADJECTIVE, "good", "nice", "great", "fantastic", "awesome", "perfect", "fun").stream().toArray(String[]::new);
    public final static String[] NEGATIVE_ADJECTIVE = getDictionary().getSynonyms(POS.ADJECTIVE, "bad", "horrible", "terrible", "scary").stream().toArray(String[]::new);

    public final static String[] LOCATION_PREPOSITION = new String[] { "in", "from", "to", "by", "at", "around", "on" };
}
