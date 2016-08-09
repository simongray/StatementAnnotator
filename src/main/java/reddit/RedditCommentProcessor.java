package reddit;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.util.CoreMap;
import org.apache.tika.language.LanguageIdentifier;
import org.json.JSONArray;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.core.StatementUtils;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;


/**
 * Note: this will ultimately not be a part of this project!
 * Including it here for debugging purposes.
 */
public class RedditCommentProcessor {
    public final static String ENGLISH = "en";
    public final static String DANISH = "da";

    public static MarkdownStripper stripper = new MarkdownStripper();

    public static String getLanguage(String text) {
        return new LanguageIdentifier(text).getLanguage();
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }

    public static List<String> getComments(JSONArray jsonArray, String language) {
        List<String> comments = new ArrayList<>();

        for (Object object : jsonArray) {
            String comment = (String) object;

            comment = clean(comment);

            // identify language
            String commentLanguage = RedditCommentProcessor.getLanguage(comment);

            if (commentLanguage.equals(language)) {
                comments.add(comment);
            }
        }

        return comments;
    }

    public static String clean(String comment) {
        // strip markdown
        comment = stripper.strip(comment);

        // fix common mistakes
        comment = fixMistakes(comment);

        // expand abbreviations
        comment = expandAbbreviations(comment);

        return comment;
    }

    private static String expandAbbreviations(String comment) {
        comment = comment.replaceAll("IMO", "in my opinion");
        comment = comment.replaceAll("AFAIK", "as far as I know");
        comment = comment.replaceAll("TIL", "today I learned");
        comment = comment.replaceAll("IANAL", "I am not a lawyer");

        return comment;
    }

    /**
     * Certain common mistakes are easily fixed and usually improve dependency graph results.
     *
     * @param comment
     * @return
     */
    private static String fixMistakes(String comment) {
        // replace short dash with long dash
        comment = comment.replaceAll(" - ", " – ");

        return comment;
    }
}
