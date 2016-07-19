package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.MarkdownStripper;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.IndirectObject;
import statements.core.Statement;
import statements.core.StatementUtils;
import statements.core.Subject;
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

        MarkdownStripper stripper = new MarkdownStripper();
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        JSONArray firstUserJsonArray = new JSONArray(content);


        Map<CoreMap, Set<Statement>> firstStatements = new HashMap<>();

        List<String> firstComments = RedditCommentProcessor.getComments(firstUserJsonArray, RedditCommentProcessor.ENGLISH);


        for (String comment : firstComments) {
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                System.out.println(sentence);
                StatementUtils.printStatements(sentenceStatements);
                firstStatements.put(sentence, sentenceStatements);
            }
        }

        Profile firstProfile = new Profile(firstStatements);
        Map<CoreMap, Set<Statement>> firstResult = firstProfile.getInteresting();

        Set<Statement> firstValues = new HashSet<>();

        for (CoreMap sentence : firstResult.keySet()) {
            System.out.println(sentence);
            Set<Statement> sentenceStatements = firstResult.get(sentence);
            StatementUtils.printStatements(sentenceStatements);
            firstValues.addAll(sentenceStatements);
        }

        System.out.println();
        System.out.println();
        System.out.println("MATCHING STATEMENTS");
        System.out.println("#######");

        Set<Statement> matchingStatements = new HashSet<>();
        Pattern pattern = new Pattern(Proxy.Subject("I"), Proxy.IndirectObject());

        for (Statement statement : firstValues) {
            if (pattern.matches(statement)) {
                matchingStatements.add(statement);
                System.out.println(statement);
            }
        }
    }
}
