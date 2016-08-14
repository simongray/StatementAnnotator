package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.profile.Profile;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


public class TestSingleProfile {
    public static void main(String[] args) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statement");  // short pipeline
        props.setProperty("customAnnotatorClass.statement", "statements.StatementAnnotator");
        props.setProperty("ssplit.newlineIsSentenceBreak", "always");  // IMPORTANT!!
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // load comments
//        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/data.json", Charset.defaultCharset());
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/mark_comment_history.json", Charset.defaultCharset());
        JSONArray jsonArray = new JSONArray(content);
        List<String> comments = RedditCommentProcessor.getComments(jsonArray, RedditCommentProcessor.ENGLISH);
        Set<Statement> statements = new HashSet<>();

        // retrieve statements from comments
        for (String comment : comments) {
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);
            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                if (sentenceStatements != null) statements.addAll(sentenceStatements);
            }
        }

        // create profile based on statements
        Profile profile = new Profile(statements);
        System.out.println("statements: " + profile.getStatements().size());
        System.out.println("interesting statements: " + profile.getInterestingStatements().size());
        System.out.println("locations: " + profile.getLocations());
        System.out.println("possessions: " + profile.getPossessions());
        System.out.println("studies: " + profile.getStudies());
        System.out.println("work: " + profile.getWork());
        System.out.println("proper nouns: " + profile.getProperNouns());
        List<Statement> statementsByLexicalDensity = profile.getStatementsByLexicalDensity();
        List<Statement> statementsByQuality = profile.getStatementsByQuality();
        for (int i = 0; i < statementsByLexicalDensity.size(); i++) {
            System.out.println("quality: " + profile.getStatementInfo(statementsByQuality.get(i)));
            System.out.println("density: " + profile.getStatementInfo(statementsByLexicalDensity.get(i)));
            System.out.println();
        }
    }
}
