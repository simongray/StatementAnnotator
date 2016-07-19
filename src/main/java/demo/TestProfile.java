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
import statements.matching.StatementPattern;
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

        MarkdownStripper stripper = new MarkdownStripper();
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        String otherContent = RedditCommentProcessor.readFile("src/main/java/demo/data/mark_comment_history.json", Charset.defaultCharset());
        JSONArray firstUserJsonArray = new JSONArray(content);
        JSONArray secondUserJsonArray = new JSONArray(otherContent);


        Map<CoreMap, Set<Statement>> firstStatements = new HashMap<>();
        Map<CoreMap, Set<Statement>> secondStatements = new HashMap<>();

        List<String> firstComments = RedditCommentProcessor.getComments(firstUserJsonArray, RedditCommentProcessor.ENGLISH);
        List<String> secondComments = RedditCommentProcessor.getComments(secondUserJsonArray, RedditCommentProcessor.ENGLISH);


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

        for (String comment : secondComments) {
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                System.out.println(sentence);
                StatementUtils.printStatements(sentenceStatements);
                secondStatements.put(sentence, sentenceStatements);
            }
        }

        Profile firstProfile = new Profile(firstStatements);
        Map<CoreMap, Set<Statement>> firstResult = firstProfile.getInteresting();

        Profile secondProfile = new Profile(secondStatements);
        Map<CoreMap, Set<Statement>> secondResult = secondProfile.getInteresting();
        Set<Statement> firstValues = new HashSet<>();
        Set<Statement> secondValues = new HashSet<>();

        for (CoreMap sentence : firstResult.keySet()) {
            System.out.println(sentence);
            Set<Statement> sentenceStatements = firstResult.get(sentence);
            StatementUtils.printStatements(sentenceStatements);
            firstValues.addAll(sentenceStatements);
        }

        for (CoreMap sentence : secondResult.keySet()) {
            System.out.println(sentence);
            Set<Statement> sentenceStatements = secondResult.get(sentence);
            StatementUtils.printStatements(sentenceStatements);
            secondValues.addAll(sentenceStatements);
        }

        System.out.println();
        System.out.println();
        System.out.println("MATCHES");
        System.out.println("#######");

        Map<String, Set<Statement>> matchingSubjectStatements = new HashMap<>();

        for (Statement statement : firstValues) {
            StatementPattern pattern = new StatementPattern(statement);
            for (Statement otherStatement : secondValues) {
                if (statement != otherStatement && pattern.matches(otherStatement)) {
                    String statementSubjectLemma = statement.getSubject().getBasicWord();
                    Set<Statement> matchingSubjects = matchingSubjectStatements.getOrDefault(statementSubjectLemma, new HashSet<>());
                    matchingSubjects.add(statement);
                    matchingSubjects.add(otherStatement);
                    matchingSubjectStatements.put(statementSubjectLemma, matchingSubjects);
                }
            }
        }

        for (String subject : matchingSubjectStatements.keySet()) {
            System.out.println(subject + ": " + matchingSubjectStatements.get(subject).size());
        }


//        ComponentSearchString predicate = new ComponentSearchString(Subject.class, "I");
//        Map<CoreMap, Set<Statement>> result = testProfile.filter(predicate);
//        Set<Statement> statementsWithGaps = new HashSet<>();
//
//        for (CoreMap sentence : result.keySet()) {
//            Set<Statement> sentenceStatements = result.get(sentence);
//            for (Statement statement : sentenceStatements) {
//                if (statement.gaps() == 0) {
//                    System.out.println(statement + " ----> " + sentence);
//                } else {
//                    statementsWithGaps.add(statement);
//                }
//            }
//        }
//
//        System.out.println();
//        System.out.println("Statements with gaps: " + statementsWithGaps.size());
//        for (Statement statement : statementsWithGaps) {
//            System.out.println(statement);
//        }
    }
}
