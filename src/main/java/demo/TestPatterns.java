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
//        Pattern pattern = new Pattern(Proxy.Subject("I"), Proxy.IndirectObject());
        Pattern bePattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb("be", "become"),
                Proxy.DirectObject()
        );
        Pattern locationPattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb("come", "be", "live", "go", "move"),
                Proxy.IndirectObject()
        );
        Pattern likeHatePattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb("like", "love", "prefer", "want", "sure", "hate", "dislike")
        );
        Pattern thinkPattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb("think", "know", "believe", "imagine", "guess", "consider", "reckon", "suppose")
        );

        for (Statement statement : firstValues) {
            if (bePattern.matches(statement)) {
//                matchingStatements.add(statement);
                System.out.println("be: " + statement);
            }
            if (locationPattern.matches(statement)) {
                System.out.println("come: " + statement);
            }
            if (likeHatePattern.matches(statement)) {
//                matchingStatements.add(statement);
                System.out.println("like: " + statement);
            }
            if (thinkPattern.matches(statement)) {
//                matchingStatements.add(statement);
                System.out.println("think: " + statement);
            }
        }

        //  some personal information
//        Set<Statement> nonMatchingParts = new HashSet<>();
//        for (Statement statement : matchingStatements) {
//            Set<StatementComponent> components = new HashSet<>(statement.getComponents());
//            Set<StatementComponent> matchingComponents = bePattern.getMatchingComponents(statement);
//            components.removeAll(matchingComponents);
//
//            Statement embeddedStatement = null;
//            Set<AbstractComponent> abstractComponents = new HashSet<>();
//
//            for (StatementComponent component : components) {
//                if (component instanceof Statement) {
//                    embeddedStatement = (Statement) component;
//                } else {
//                    abstractComponents.add((AbstractComponent) component);
//                }
//            }
//
//            if (embeddedStatement == null && !components.isEmpty()) {
//                nonMatchingParts.add(new Statement(components));
//            } else {
//                nonMatchingParts.add(new Statement(abstractComponents, embeddedStatement));
//            }
//        }
//        System.out.println("Simon is");
//        for (Statement statement : nonMatchingParts) {
//            System.out.println("  * " + statement.getSentence());
//        }

    }
}
