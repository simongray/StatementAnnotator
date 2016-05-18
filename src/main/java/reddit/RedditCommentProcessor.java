package reddit;

import org.apache.tika.language.LanguageIdentifier;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;


/**
 * Note: this will ultimately not be a part of this project!
 * Including it here for debugging purposes.
 */
public class RedditCommentProcessor {
    public static String getLanguage(String text) {
        return new LanguageIdentifier(text).getLanguage();
    }

    public static String readFile(String path, Charset encoding) throws IOException {
        byte[] encoded = Files.readAllBytes(Paths.get(path));
        return new String(encoded, encoding);
    }
}
