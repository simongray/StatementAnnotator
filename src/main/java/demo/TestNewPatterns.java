package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.core.StatementUtils;
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

        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
//        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/mark_comment_history.json", Charset.defaultCharset());
        JSONArray firstUserJsonArray = new JSONArray(content);


        Map<CoreMap, Set<Statement>> statements = new HashMap<>();

        List<String> comments = RedditCommentProcessor.getComments(firstUserJsonArray, RedditCommentProcessor.ENGLISH);

        Set<Statement> allStatements = new HashSet<>();

        int commentLimit = 50;
//        int commentLimit = comments.size();

        // retrieve statements from first data set
        for (int i = 0; i < commentLimit; i++) {
            String comment = comments.get(i);
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

        // testing the new pattern class
        Pattern thinkPattern = new StatementPattern(
                new SubjectPattern().firstPerson(),
                new VerbPattern().words(Common.THINK_SYNONYMS),
                new StatementPattern()
        );

        Pattern thinkNotPattern = new StatementPattern(
                new SubjectPattern().firstPerson(),
                new VerbPattern().words(Common.THINK_SYNONYMS).negated(),
                new StatementPattern()
        );

        Pattern thinkAndThinkNotPattern = new StatementPattern(
                new SubjectPattern().firstPerson(),
                new VerbPattern().words(Common.THINK_SYNONYMS).negated(null),
                new StatementPattern()
        );

        Pattern pluralPattern = new StatementPattern(
//                new ComponentPattern.DirectObject().plural(true).person(2).build()
                new SubjectPattern().secondPerson(),
                new DirectObjectPattern().plural()
        );

        for (Statement statement : allStatements) {
            if (thinkPattern.matches(statement)) {
                System.out.println("think: " + statement);
            }
            if (thinkNotPattern.matches(statement)) {
                System.out.println("not: " + statement);
            }
//            if (thinkAndThinkNotPattern.matches(statement)) {
//                System.out.println("both: " + statement);
//            }
            if (pluralPattern.matches(statement)) {
                System.out.println("plural: " + statement);
            }
        }
    }
}
