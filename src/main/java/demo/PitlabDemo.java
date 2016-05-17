package demo;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import org.json.JSONArray;
import reddit.MarkdownStripper;
import reddit.RedditCommentProcessor;
import sentiment.ComplexSentiment;
import sentiment.SentimentProfile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;
import java.util.Map.Entry;

/**
 * Created by simongray on 13/05/16.
 */
public class PitlabDemo {
    private final static String ENGLISH = "en";
    private final static String DANISH = "da";

    public static void main(String args[]) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, tokensregex, parse, sentiment, sentimenttargets");
//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, regexner, parse, sentiment, sentimenttargets");
        props.setProperty("customAnnotatorClass.sentimenttargets", "sentiment.SentimentTargetsAnnotator");
        props.setProperty("customAnnotatorClass.tokensregex", "edu.stanford.nlp.pipeline.TokensRegexAnnotator");
        props.setProperty("tokensregex.rules", "tokensregex_rules.txt");
        props.put("tokensregex.verbose", "true");
        props.put("ner.model", "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
        props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");  // fast, more memory usage
//        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");  // slow, less memory usage

        DemoTimer.start("pipeline launch");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        DemoTimer.stop();

        MarkdownStripper stripper = new MarkdownStripper();
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        JSONArray jsonArray = new JSONArray(content);
        List<String> englishComments = new ArrayList<>();
        List<String> danishComments = new ArrayList<>();
        List<String> otherComments = new ArrayList<>();
        List<Annotation> annotations = new ArrayList<>();

        for (Object obj : jsonArray) {
            String comment = (String) obj;

            // strip markdown
            comment = stripper.strip(comment);

            // identify language
            DemoTimer.start("language identification");
            String language = RedditCommentProcessor.getLanguage(comment);
            DemoTimer.stop();

            int endIndex = comment.length() < 30? comment.length() - 1: 30;
            System.out.println(language + " -> " + comment.substring(0, endIndex));

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
        List<Entry<String, ComplexSentiment>> sentiments = new ArrayList<>();
        sentiments.addAll(testProfile.getSentiments().entrySet());
//        Collections.sort(sentiments, new SentimentComparer());
        System.out.println();
        System.out.println("top positive");
        Collections.sort(sentiments, new PositiveSentimentComparer());
        for (Entry<String, ComplexSentiment> entry : sentiments) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
        System.out.println();
        System.out.println("top negative");
        Collections.sort(sentiments, new NegativeSentimentComparer());
        for (Entry<String, ComplexSentiment> entry : sentiments) {
            System.out.println(entry.getKey() + " -> " + entry.getValue());
        }
    }

    public static class SentimentComparer implements Comparator<Entry<String, ComplexSentiment>> {
        @Override
        public int compare(Entry<String, ComplexSentiment> x, Entry<String, ComplexSentiment> y) {
            int xn = x.getValue().size();
            int yn = y.getValue().size();
            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    public static class PositiveSentimentComparer implements Comparator<Entry<String, ComplexSentiment>> {
        @Override
        public int compare(Entry<String, ComplexSentiment> x, Entry<String, ComplexSentiment> y) {
            int xn = x.getValue().getPositiveCount();
            int yn = y.getValue().getPositiveCount();
            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    public static class NegativeSentimentComparer implements Comparator<Entry<String, ComplexSentiment>> {
        @Override
        public int compare(Entry<String, ComplexSentiment> x, Entry<String, ComplexSentiment> y) {
            int xn = x.getValue().getNegativeCount();
            int yn = y.getValue().getNegativeCount();
            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }
}
