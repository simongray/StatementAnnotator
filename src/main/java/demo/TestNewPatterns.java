package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.patterns.*;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.*;


public class TestNewPatterns {
    public static void main(String[] args) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statement");  // short pipeline
        props.setProperty("customAnnotatorClass.statement", "statements.StatementAnnotator");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

//        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/mark_comment_history.json", Charset.defaultCharset());
        JSONArray firstUserJsonArray = new JSONArray(content);


        List<String> comments = RedditCommentProcessor.getComments(firstUserJsonArray, RedditCommentProcessor.ENGLISH);

        Set<Statement> statements = new HashSet<>();

//        int commentLimit = 50;
        int commentLimit = comments.size();

        // retrieve statements from first data set
        for (int i = 0; i < commentLimit; i++) {
            String comment = comments.get(i);
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
//                System.out.println(sentence);
//                StatementUtils.printStatements(sentenceStatements);
                if (sentenceStatements != null) statements.addAll(sentenceStatements);
            }
        }

        System.out.println();
        System.out.println();
        System.out.println("MATCHING STATEMENTS");
        System.out.println("#######");

        // testing the new pattern class
        StatementPattern thinkPattern = new StatementPattern(
                new SubjectPattern().firstPerson(),
                new VerbPattern().words(Common.OPINION_VERB),
                new StatementPattern().interesting().wellFormed().capture()
        );

        StatementPattern thinkNotPattern = new StatementPattern(
                new SubjectPattern().firstPerson(),
                new VerbPattern().words(Common.OPINION_VERB).negated(),
                new StatementPattern()
        );

//        StatementPattern testPattern = new StatementPattern(
//                new SubjectPattern().firstPerson(),
//                new VerbPattern().words("be", "come", "go", "live", "stay", "visit", "travel"),
////                new ComponentPattern(DirectObject.class, IndirectObject.class).preposition("in", "from", "to", "by", "at", "around").capture()
//                new ObjectPattern().preposition().capture()
//        );

        StatementPattern testPattern = new StatementPattern(
                new SubjectPattern().firstPerson(),
                new VerbPattern().words(Common.LOCATION_VERB),
                new ObjectPattern().preposition("in", "from", "to", "by", "at", "around", "on").noun().capture()
        );

        for (Statement statement : statements) {
//            if (thinkPattern.matches(statement)) {
//                System.out.println("think: " + statement);
//            }
//            if (thinkPattern.matches(statement)) {
//                System.out.println("think: " + thinkPattern.getCaptures());
//            }
//            if (thinkNotPattern.matches(statement)) {
//                System.out.println("not: " + statement);
//            }
            if (testPattern.matches(statement)) {
                System.out.println("test: " + statement + " --> " + statement.getComponents());
                System.out.println("      " + testPattern.getCaptures());
                System.out.println("      " + statement.getOrigin());
            }
        }
    }
}
