package statements.patterns;


import edu.mit.jwi.item.POS;
import statements.core.DirectObject;
import statements.core.IndirectObject;

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

    /**
     * Matches statements that indicate the personal location of the author.
     */
    public static final StatementPattern PERSONAL_LOCATION_PATTERN = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words("be", "come", "go"),
            new ComponentPattern(DirectObject.class, IndirectObject.class).preposition()
    );
}
