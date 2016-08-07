package demo;


import edu.mit.jwi.item.POS;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.*;
import statements.patterns.Pattern;
import statements.patterns.Proxy;
import statements.patterns.WordnetDictionary;

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

        WordnetDictionary dict = new WordnetDictionary();

        Pattern bePattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb(dict.getSynonyms(POS.VERB, "be", "become")),
                Proxy.DirectObject()
        );
        Pattern locationPattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb(dict.getSynonyms(POS.VERB, "be", "come", "go", "move", "live", "travel")),
                Proxy.IndirectObject()
        );
        Pattern likeHatePattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb(dict.getSynonyms(POS.VERB, "like", "love", "enjoy", "prefer", "want", "sure", "hate", "dislike"))
        );
        Pattern thinkPattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb(dict.getSynonyms(POS.VERB, "think", "reckon", "believe")),
                Proxy.Statement()
        );

        for (Statement statement : allStatements) {
            if (bePattern.matches(statement)) {
                System.out.println("be: " + statement);
            }
            if (locationPattern.matches(statement)) {
                System.out.println("come: " + statement);
            }
            if (likeHatePattern.matches(statement)) {
                System.out.println("like: " + statement);
            }
            if (thinkPattern.matches(statement)) {
                System.out.println("think: " + statement);
            }
        }
    }
}
