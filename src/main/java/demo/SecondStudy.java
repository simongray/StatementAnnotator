package demo;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import org.json.JSONArray;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.profile.Profile;

import java.io.IOException;
import java.io.PrintWriter;
import java.nio.charset.Charset;
import java.util.*;
import java.util.logging.LogManager;
import java.util.logging.Logger;


public class SecondStudy {
    static StanfordCoreNLP pipeline;

    public static Profile createProfile(String username, boolean limited, PrintWriter writer) throws IOException {
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/"+username+"_comment_history.json", Charset.defaultCharset());
        JSONArray jsonArray = new JSONArray(content);
        List<String> comments = RedditCommentProcessor.getComments(jsonArray, RedditCommentProcessor.ENGLISH);
        Set<Statement> statements = new HashSet<>();

        List<CoreMap> sentenceDataset = new ArrayList<>();

        if (limited) {
            int sentenceCountdown = 50;

            // retrieve statements from comments
            for (String comment : comments) {
                Annotation annotation = new Annotation(comment);
                pipeline.annotate(annotation);
                List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

                for (int i = 0; i < sentences.size(); i++) {
                    if (i == 0 || i == sentences.size()) {
                        if (sentenceCountdown > 0) {
                            String sentenceString = sentences.get(i).toString();

                            // don't include very short sentences
                            if (sentenceString.length() > 30) {

                                // do not include questions and citations in data set
                                if (!sentenceString.endsWith("?") && !(sentenceString.startsWith("\"") && sentenceString.endsWith("\""))) {
                                    Set<Statement> sentenceStatements = sentences.get(i).get(StatementsAnnotation.class);

                                    // only include if the sentence spawned any sentences
                                    if (sentenceStatements != null) {
                                        sentenceCountdown--;
                                        sentenceDataset.add(sentences.get(i));
                                        statements.addAll(sentenceStatements);
                                    }
                                }
                            }
                        }
                    }
                }

                if (sentenceCountdown == 0) {
                    break;
                }
            }
        } else {
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
        }

        // create profile based on statements
        Profile profile = new Profile(statements);
        writer.println("Profile for: " + username);
        writer.println("statements: " + profile.getStatements().size());
        writer.println("interesting statements: " + profile.getInterestingStatements().size());
        writer.println("locations: " + profile.getLocations());
        writer.println("possessions: " + profile.getPossessions());
        writer.println("studies: " + profile.getStudies());
        writer.println("work: " + profile.getWork());
        writer.println("identities: " + profile.getIdentities());
        writer.println("proper nouns: " + profile.getProperNouns());
        writer.println("likes: " + profile.getLikes());
        writer.println("dislikes: " + profile.getDislikes());
        writer.println("wants: " + profile.getWants());
        writer.println("activities: " + profile.getActivities());
        writer.println("feelings: " + profile.getFeelings());

        if (limited) {
            writer.println();
            writer.println("sentences: " + sentenceDataset.size());
            for (int i = 0; i < 50; i++) {
                writer.println(sentenceDataset.get(i));
            }
        }

        return profile;
    }

    public static void main(String[] args) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statement");  // short pipeline
        props.setProperty("customAnnotatorClass.statement", "statements.StatementAnnotator");
        props.setProperty("ssplit.newlineIsSentenceBreak", "always");  // IMPORTANT!!
        pipeline = new StanfordCoreNLP(props);

        String participant = "sklopnicht";
        String[] users = new String[] {"MagFreakingNeto", "kaspar42", "GryphonGuitar"};
        PrintWriter writer = new PrintWriter(participant + "_result.txt", "UTF-8");
        Profile participantProfile = createProfile("sklopnicht", false, writer);
        writer.println();

        int limit = 50;
        List<Statement> statementsByQuality = participantProfile.getStatementsByQuality();
        writer.println("Top " + limit + " statements from "+ participant + " by quality");

        for (int i = 0; i < statementsByQuality.size() && i < limit; i++) {
            writer.println(statementsByQuality.get(i).getOrigin());
        }

        for (String user : users) {
            writer.println();
            Profile userProfile = createProfile(user, true, writer);

            Profile.RelevanceComparator relevanceComparator = new Profile.RelevanceComparator(participantProfile, userProfile);
            List<Statement> statementsByRelevance = userProfile.getStatementsByRelevance(relevanceComparator);

            writer.println();
            writer.println("common entities:" + participantProfile.getCommonEntities(userProfile));
            writer.println("common activities:" + participantProfile.getCommonActivities(userProfile));
            writer.println("Statements from "+ user + " by relevance to " + participant);

            for (int i = 0; i < statementsByRelevance.size(); i++) {
                writer.println(statementsByRelevance.get(i).getOrigin());
            }
        }

        writer.close();
    }
}
