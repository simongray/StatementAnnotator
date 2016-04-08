package demo;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

public class TestStuff {
    public static void main(String[] args) {
        // initiate pipeline with properties (i.e. what stages)
        Properties props = new Properties();
        props.put("annotators", "tokenize, ssplit, pos, lemma, ner, parse, sentiment"); // required for use fo "ner" and "sentiment"
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // annotate a piece of text
        Annotation annotation = new Annotation("He was quite sentimental in his old days and often thought highly of her. I fucking dislike her guts! I hate her, that piece of shit, I hate her so much.");
        pipeline.annotate(annotation);

        // using sentence annotation, perhaps another annotation type is better suited (there are A LOT)
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        // handle each sentence
        for(CoreMap sentence: sentences) {
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            System.out.println("the overall sentiment rating is " + sentence.get(SentimentCoreAnnotations.SentimentClass.class));
            parse(tree, 0);
        }

    }

    // recursively display scores in tree
    public static void parse(Tree tree, int n) {
        // RNNCoreAnnotations.getPredictedClass() returns the sentiment analysis score from 0 to 4 (with -1 for n/a)
        System.out.println(new String(new char[n]).replace("\0", " ") + tree.value() + " " + RNNCoreAnnotations.getPredictedClass(tree));

        // scores (not confirmed, but 99% sure)
            // very negative: 0
            // negative: 1
            // neutral: 2
            // positive: 3
            // very positive: 4

        for (Tree child : tree.children()) {
            parse(child, n+1);
        }
    }
}
