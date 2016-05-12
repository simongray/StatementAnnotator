package reddit;


import java.io.IOException;

import org.apache.tika.exception.TikaException;
import org.apache.tika.language.LanguageIdentifier;

import org.xml.sax.SAXException;

/**
 * Note: this will ultimately not be a part of this project!
 * Including it here for debugging purposes.
 */
public class RedditCommentDownloader {
    public static void main(String args[])throws IOException, SAXException, TikaException {
        LanguageIdentifier identifier = new LanguageIdentifier("this is english ");
        String language = identifier.getLanguage();
        System.out.println("Language of the given content is : " + language);
    }
}
