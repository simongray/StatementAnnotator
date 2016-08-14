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
import java.util.*;


public class TestProfile {
    private final static String ENGLISH = "en";
    private final static String DANISH = "da";

    public static void main(String[] args) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statement");  // short pipeline
        props.setProperty("customAnnotatorClass.statement", "statements.StatementAnnotator");
        props.setProperty("ssplit.newlineIsSentenceBreak", "always");  // IMPORTANT!!
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

//        int commentLimit = 100;
        int commentLimit = firstComments.size();

        // retrieve statements from first data set
        for (int i = 0; i < commentLimit; i++) {
            String comment = firstComments.get(i);
            Annotation annotation = new Annotation(comment);
            pipeline.annotate(annotation);

            List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

            for (CoreMap sentence : sentences) {
                Set<Statement> sentenceStatements = sentence.get(StatementsAnnotation.class);
                if (sentenceStatements != null) firstStatements.addAll(sentenceStatements);
            }
        }

//        commentLimit = 100;
        commentLimit = secondComments.size();

        // retrieve statements from second data set
        for (int i = 0; i < commentLimit; i++) {
            String comment = secondComments.get(i);
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
        System.out.println("common entities:" + firstProfile.getCommonEntities(secondProfile));
        System.out.println("common activities:" + firstProfile.getCommonActivities(secondProfile));

        int limit = 10;
        Profile.RelevanceComparator relevanceComparator;
        List<Statement> statementsByLexicalDensity;
        List<Statement> statementsByQuality;
        List<Statement> statementsByRelevance;

        // the results presented to Mark
        System.out.println(firstProfile.getInterestingStatements().size() + " interesting statements in first profile");
        statementsByLexicalDensity = firstProfile.getStatementsByLexicalDensity();
        statementsByQuality = firstProfile.getStatementsByQuality();

        relevanceComparator = new Profile.RelevanceComparator(secondProfile, firstProfile);
        statementsByRelevance = firstProfile.getStatementsByRelevance(relevanceComparator);

        for (int i = 0; i < statementsByLexicalDensity.size() && i < limit; i++) {
            System.out.println("relevn.: " + firstProfile.getStatementInfo(statementsByRelevance.get(i), relevanceComparator));
            System.out.println("quality: " + firstProfile.getStatementInfo(statementsByQuality.get(i)));
            System.out.println("density: " + firstProfile.getStatementInfo(statementsByLexicalDensity.get(i)));
            System.out.println();
        }

        // the results presented to me
        System.out.println(secondProfile.getInterestingStatements().size() + " interesting statements in second profile");
        statementsByLexicalDensity = secondProfile.getStatementsByLexicalDensity();
        statementsByQuality = secondProfile.getStatementsByQuality();

        relevanceComparator = new Profile.RelevanceComparator(firstProfile, secondProfile);
        statementsByRelevance = secondProfile.getStatementsByRelevance(relevanceComparator);

        for (int i = 0; i < statementsByLexicalDensity.size() && i < limit; i++) {
            System.out.println("relevn.: " + secondProfile.getStatementInfo(statementsByRelevance.get(i), relevanceComparator));
            System.out.println("quality: " + secondProfile.getStatementInfo(statementsByQuality.get(i)));
            System.out.println("density: " + secondProfile.getStatementInfo(statementsByLexicalDensity.get(i)));
            System.out.println();
        }
    }
}
