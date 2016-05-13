package demo;

import org.json.JSONArray;
import reddit.RedditCommentProcessor;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by simongray on 13/05/16.
 */
public class TestReddit {
    private final static String ENGLISH = "en";
    private final static String DANISH = "da";

    public static void main(String args[]) throws IOException {
        List<String> texts = new ArrayList<>();

        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        JSONArray jsonArray = new JSONArray(content);

        List<String> englishComments = new ArrayList<>();
        List<String> danishComments = new ArrayList<>();
        List<String> otherComments = new ArrayList<>();

        for (Object obj : jsonArray) {
            String comment = (String) obj;
            int endIndex = comment.length() < 20? comment.length() - 1: 20;
            DemoTimer.start("language identification");
//            System.out.println(comment.substring(0, endIndex));
            String language = RedditCommentProcessor.getLanguage(comment);
//            System.out.println("Language: " + language);
            if (language.equals(ENGLISH)) {
                englishComments.add(comment);
            } else if (language.equals(DANISH)) {
                danishComments.add(comment);
            } else {
                otherComments.add(comment);
            }
            DemoTimer.stop();
        }

        System.out.println("english comments: " + englishComments.size());
        System.out.println("danish comments: " + danishComments.size());
        System.out.println("other comments: " + otherComments.size());

        for (String otherComment : otherComments) {
            int endIndex = otherComment.length() < 50? otherComment.length() - 1: 50;
            DemoTimer.start("language identification");
            System.out.println(otherComment.substring(0, endIndex));

            String language = RedditCommentProcessor.getLanguage(otherComment);
            System.out.println("Language: " + language);
        }
    }
}
