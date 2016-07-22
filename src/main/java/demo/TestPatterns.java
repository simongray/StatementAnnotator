package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.MarkdownStripper;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.*;
import statements.matching.Pattern;
import statements.matching.Proxy;
import statements.profile.Profile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


public class TestPatterns {
    public static void main(String[] args) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statement");  // short pipeline
        props.setProperty("customAnnotatorClass.statement", "statements.StatementAnnotator");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
//        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/mark_comment_history.json", Charset.defaultCharset());
        JSONArray firstUserJsonArray = new JSONArray(content);


        Map<CoreMap, Set<Statement>> statements = new HashMap<>();

        List<String> comments = RedditCommentProcessor.getComments(firstUserJsonArray, RedditCommentProcessor.ENGLISH);

        Set<Statement> allStatements = new HashSet<>();

        for (String comment : comments) {
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                System.out.println(sentence);
                StatementUtils.printStatements(sentenceStatements);
                statements.put(sentence, sentenceStatements);
                if (sentenceStatements != null) allStatements.addAll(sentenceStatements);
            }
        }

        System.out.println();
        System.out.println();
        System.out.println("MATCHING STATEMENTS");
        System.out.println("#######");

//        Pattern pattern = new Pattern(Proxy.Subject("I"), Proxy.IndirectObject());
//        Pattern bePattern = new Pattern(
//                Proxy.Subject("I", "we"),
//                Proxy.Verb("be", "become"),
//                Proxy.DirectObject()
//        );
//        Pattern locationPattern = new Pattern(
//                Proxy.Subject("I", "we"),
//                Proxy.Verb("come", "be", "live", "go", "move"),
//                Proxy.IndirectObject()
//        );
//        Pattern likeHatePattern = new Pattern(
//                Proxy.Subject("I", "we"),
//                Proxy.Verb("like", "love", "enjoy", "prefer", "want", "sure", "hate", "dislike")
//        );
        Pattern thinkPattern = new Pattern(
                true,
                Proxy.Subject("I", "we"),
                Proxy.Verb("think", "know", "believe", "imagine", "guess", "consider", "reckon", "suppose"),
                Proxy.Statement()
        );

        for (Statement statement : allStatements) {
//            if (bePattern.matches(statement)) {
//                System.out.println("be: " + statement);
//            }
//            if (locationPattern.matches(statement)) {
//                System.out.println("come: " + statement);
//            }
//            if (likeHatePattern.matches(statement)) {
//                System.out.println("like: " + statement);
//            }
            if (thinkPattern.matches(statement)) {
                System.out.println("think: " + statement);
            }
        }
    }
}
