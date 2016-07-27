package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.MarkdownStripper;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.core.StatementUtils;
import statements.matching.Pattern;
import statements.profile.Profile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


public class TestProfile {
    private final static String ENGLISH = "en";
    private final static String DANISH = "da";

    public static void main(String[] args) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statement");  // short pipeline
        props.setProperty("customAnnotatorClass.statement", "statements.StatementAnnotator");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // load
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        String otherContent = RedditCommentProcessor.readFile("src/main/java/demo/data/mark_comment_history.json", Charset.defaultCharset());
        JSONArray firstUserJsonArray = new JSONArray(content);
        JSONArray secondUserJsonArray = new JSONArray(otherContent);

        List<String> firstComments = RedditCommentProcessor.getComments(firstUserJsonArray, RedditCommentProcessor.ENGLISH);
        List<String> secondComments = RedditCommentProcessor.getComments(secondUserJsonArray, RedditCommentProcessor.ENGLISH);

        Set<Statement> firstStatements = new HashSet<>();
        Set<Statement> secondStatements = new HashSet<>();

        // retrieve statements from first data set
        for (String comment : firstComments) {
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                if (sentenceStatements != null) firstStatements.addAll(sentenceStatements);
            }
        }

        // retrieve statements from second data set
        for (String comment : secondComments) {
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                if (sentenceStatements != null) secondStatements.addAll(sentenceStatements);

            }
        }

        // create profiles based on statements from both data sets
        Profile firstProfile = new Profile(firstStatements);
        Profile secondProfile = new Profile(secondStatements);
        System.out.println(firstProfile.getStatements().size() + " statements in first profile");
        System.out.println(secondProfile.getStatements().size() + " statements in second profile");
    }
}
