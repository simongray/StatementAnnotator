package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.MarkdownStripper;
import reddit.RedditCommentProcessor;
import sentiment.ComplexSentiment;
import sentiment.SentimentProfile;
import sentiment.SentimentTarget;
import statements.annotations.StatementsAnnotation;
import statements.core.Resemblance;
import statements.core.Statement;
import statements.core.StatementUtils;
import statements.core.Subject;
import statements.profile.ComponentSearchString;
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
//        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/mark_comment_history.json", Charset.defaultCharset());
        JSONArray jsonArray = new JSONArray(content);

        List<String> englishComments = new ArrayList<>();
        List<String> danishComments = new ArrayList<>();
        List<String> otherComments = new ArrayList<>();

        Map<CoreMap, Set<Statement>> statements = new HashMap<>();
        Set<Statement> allStatements = new HashSet<>();

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

                List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

                for (CoreMap sentence : sentences) {
                    Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                    System.out.println(sentence);
                    StatementUtils.printStatements(sentenceStatements);
                    statements.put(sentence, sentenceStatements);
                    if (sentenceStatements != null) allStatements.addAll(sentenceStatements);
                }

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
        System.out.println("statements: " + statements.size());

        DemoTimer.start("building profile...");
        for (Statement statement : allStatements) {
            for (Statement otherStatement : allStatements) {
                if (statement != otherStatement) {
                    Resemblance resemblance = statement.resemble(otherStatement);
                    if (resemblance != Resemblance.NONE) {
                        System.out.println(resemblance + ": " + statement + " <---> " + otherStatement);
                    }
                }
            }
        }
//        Profile testProfile = new Profile(statements);
//        Map<CoreMap, Set<Statement>> result = testProfile.getInteresting();
//        Map<CoreMap, Set<Statement>> result = testProfile.getLikes();
//        Map<CoreMap, Set<Statement>> result = testProfile.getSpecial();
//
//        for (CoreMap sentence : result.keySet()) {
//            Set<Statement> sentenceStatements = result.get(sentence);
//            for (Statement statement : sentenceStatements) {
//                System.out.println(statement + " ----> " + sentence);
//            }
//        }

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
//        DemoTimer.stop();
//
//        System.out.println();
//        System.out.println("Statements with gaps: " + statementsWithGaps.size());
//        for (Statement statement : statementsWithGaps) {
//            System.out.println(statement);
//        }
    }
}
