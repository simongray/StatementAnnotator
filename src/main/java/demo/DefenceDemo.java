package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import reddit.RedditCommentProcessor;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.core.StatementComponent;
import statements.core.StatementUtils;
import statements.patterns.StatementPattern;
import statements.patterns.SubjectPattern;
import statements.patterns.VerbPattern;

import java.util.HashSet;
import java.util.List;
import java.util.Properties;
import java.util.Set;


public class DefenceDemo {
    public static void main(String[] args) {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statements");  // short pipeline
        props.setProperty("customAnnotatorClass.statements", "statements.StatementAnnotator");
        props.setProperty("ssplit.newlineIsSentenceBreak", "always");  // IMPORTANT!!
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        String example =
                "The ceremony is happening now. \n" +
                "The wedding wasn't ever not happening. \n" +
                "That kind of stuff doesn't happen to me.\n" +
                "It just never really happened.";

        StatementPattern happenPattern = new StatementPattern(
                new VerbPattern().words("happen").negated(false),
                new SubjectPattern().capture()
        );

        Annotation annotation = new Annotation(RedditCommentProcessor.clean(example));
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);
        Set<StatementComponent> capturedComponents = new HashSet<>();

        System.out.println();

        for (CoreMap sentence : sentences) {
            Set<Statement> statements = sentence.get(StatementsAnnotation.class);
            System.out.println(sentence);
            StatementUtils.printStatements(statements);

            for (Statement statement : statements) {
                if (happenPattern.matches(statement)) {
                    capturedComponents.addAll(happenPattern.getCaptures());
                }
            }
        }

        System.out.println();

        for (StatementComponent component : capturedComponents) {
            System.out.println("captured: " + component);
        }
    }
}
