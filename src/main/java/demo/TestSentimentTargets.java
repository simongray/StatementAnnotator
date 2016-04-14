package demo;

import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import sentiment.SentimentTarget;
import sentiment.SentimentTargetsAnnotation;
import sentiment.SentimentTargetsAnnotator;

import java.util.List;
import java.util.Properties;


public class TestSentimentTargets {
    public static void main(String[] args) {
        // initiate pipeline with properties (i.e. what stages)
        Properties props = new Properties();

        // using the custom SentimentTargetsAnnotator
        props.put("customAnnotatorClass.sentimenttargets", "sentiment.SentimentTargetsAnnotator");

        // final pipeline
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment, sentimenttargets");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // annotate a piece of text
        Annotation annotation = new Annotation("George Bush was quite sentimental in his old days and often thought highly of Clinton. I fucking dislike her guts! I hate her, that piece of shit, I hate her so much.");

        System.out.println("before annotation");
        pipeline.annotate(annotation);

        // using sentence annotation, perhaps another annotation type is better suited (there are A LOT)
//        List<SentimentTarget> targets = annotation.get(SentimentTargetsAnnotation.class);
//
//        for (SentimentTarget target : targets) {
//            System.out.println(target);
//        }
    }
}
