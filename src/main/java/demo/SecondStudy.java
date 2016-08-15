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
import java.nio.charset.Charset;
import java.util.*;


public class SecondStudy {
    public static void main(String[] args) throws IOException {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statement");  // short pipeline
        props.setProperty("customAnnotatorClass.statement", "statements.StatementAnnotator");
        props.setProperty("ssplit.newlineIsSentenceBreak", "always");  // IMPORTANT!!
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // load comments
        String content = RedditCommentProcessor.readFile("src/main/java/demo/data/GryphonGuitar_comment_history.json", Charset.defaultCharset());
        JSONArray jsonArray = new JSONArray(content);
        List<String> comments = RedditCommentProcessor.getComments(jsonArray, RedditCommentProcessor.ENGLISH);
        Set<Statement> statements = new HashSet<>();

        List<CoreMap> sentenceDataset = new ArrayList<>();

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
                        if (sentenceString.length() > 20) {

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
                System.out.println("reached 50 sentences!");
                break;
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
        System.out.println("identities: " + profile.getIdentities());
        System.out.println("proper nouns: " + profile.getProperNouns());
        System.out.println("likes: " + profile.getLikes());
        System.out.println("wants: " + profile.getWants());
        System.out.println("activities: " + profile.getActitivies());
        List<Statement> statementsByLexicalDensity = profile.getStatementsByLexicalDensity();
        List<Statement> statementsByQuality = profile.getStatementsByQuality();
        for (int i = 0; i < statementsByLexicalDensity.size(); i++) {
            System.out.println("quality: " + profile.getStatementInfo(statementsByQuality.get(i)));
            System.out.println();
        }

        System.out.println();
        System.out.println("sentences: " + sentenceDataset.size());
        for (CoreMap sentence : sentenceDataset) {
            System.out.print(sentence + "  -->  ");
            for (CoreLabel token : sentence.get(CoreAnnotations.TokensAnnotation.class)) {
                System.out.print(token.word() + "-" + token.tag() + " ");
            }
            System.out.println();
        }
    }
}
