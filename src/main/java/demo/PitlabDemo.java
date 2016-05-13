package demo;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.json.JSONArray;
import reddit.RedditCommentProcessor;
import sentiment.SentimentProfile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Properties;

/**
 * Created by simongray on 13/05/16.
 */
public class PitlabDemo {
    private final static String ENGLISH = "en";
    private final static String DANISH = "da";

    public static void main(String args[]) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, parse, sentiment, sentimenttargets");
        props.setProperty("customAnnotatorClass.sentimenttargets", "sentiment.SentimentTargetsAnnotator");
        props.put("ner.model", "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
        props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");  // fast, more memory usage
//        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");  // slow, less memory usage
        props.setProperty("sentimenttargets.composestyle", "yolo");  // TODO: change into actual style

        DemoTimer.start("pipeline launch");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        DemoTimer.stop();

        List<Annotation> annotations = new ArrayList<>();

        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        JSONArray jsonArray = new JSONArray(content);

        List<String> englishComments = new ArrayList<>();
        List<String> danishComments = new ArrayList<>();
        List<String> otherComments = new ArrayList<>();

        for (Object obj : jsonArray) {
            // identify language
            DemoTimer.start("language identification");
            String comment = (String) obj;
            int endIndex = comment.length() < 30? comment.length() - 1: 30;
            String language = RedditCommentProcessor.getLanguage(comment);
            System.out.println(language + " -> " + comment.substring(0, endIndex));
            DemoTimer.stop();

            if (language.equals(ENGLISH)) {
                // annotate a piece of text
                DemoTimer.start("creating annotation");
                Annotation annotation = new Annotation(comment);
                pipeline.annotate(annotation);
                DemoTimer.stop();
                annotations.add(annotation);

                englishComments.add(comment);
            } else if (language.equals(DANISH)) {
                danishComments.add(comment);
            } else {
                otherComments.add(comment);
            }
        }

        System.out.println("english comments: " + englishComments.size());
        System.out.println("danish comments: " + danishComments.size());
        System.out.println("unknown comments: " + otherComments.size());

        DemoTimer.start("building profile");
        SentimentProfile testProfile = new SentimentProfile(annotations);
        DemoTimer.stop();
        for (Map.Entry entry : testProfile.getSentiments().entrySet()) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }
}
