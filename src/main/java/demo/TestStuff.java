package demo;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.List;
import java.util.Properties;

/*
    http://nlp.stanford.edu/nlp/javadoc/javanlp/

    http://stackoverflow.com/questions/19884515/adding-a-new-annotator-in-stanford-corenlp


 */


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

    // Tree.getSpan() = IntPair (beginning word index, end word index) of phrase in sentence
    // Tree.label() = nodeString() = alternates between parser symbol or word, label() returns a Label object
    // Tree.value() = same as label() but with added sentiment analysis score
    // Tree.yield() = list of words and punctuation represented by this node in the tree
    // Tree.dependencies() --> nullpointerexception
    // Tree.pennString() = the indented, lisp-style parsing output
    // Tree.constituents() = not quite sure, it's an array of (x, y) pairs, perhaps to do with dependency grammar?
    // Tree.labeledYield() = array of "word/symbol" strings
    // Tree.isPhrasal() = true for list of words, but not for single words or empty nodes
    // Tree.score() = NaN
    // Tree.labels() = array of either word for single-word nodes or a list of words and symbols in no particular order
    // Tree.taggedLabeledYield() = list of CoreLabels that go "symbol-index" when toString()'d
        // CoreLabel.word() = the word
        // CoreLabel.value() = the parser symbol thingy
        // CoreLabel.lemma() = null, even though I did include that Annotator
        // CoreLabel.tag() = the parser symbol thingy again for some reason
        // CoreLabel.ner() = null, even though I did include that Annotator
    // Tree.isLeaf() = is a leaf, i.e. no children, in so in this case all of them are words (not symbols)
    // Tree.isPreTerminal() = right before a leaf, i.e. the symbol right before a leaf (containing a word)
    // Tree.isUnaryRewrite() = if node only has a single child, seems to be the same as isPreTerminal()
    // Tree.isEmpty() = false in every case, seems useless
    // Tree.isPrePreterminal() = node before a preterminal node, not sure how it can be used





    // recursively display scores in tree
    public static void parse(Tree tree, int n) {
        // RNNCoreAnnotations.getPredictedClass(tree) returns the sentiment analysis score from 0 to 4 (with -1 for n/a)
        System.out.println(new String(new char[n]).replace("\0", " ")
                + tree.value()
        );

        for (CoreLabel label : tree.taggedLabeledYield()) {
            System.out.println("-----");
            System.out.println("toString= " + label.toString());
            System.out.println("word= " + label.word());
            System.out.println("value= " + label.value());
            System.out.println("lemma= " + label.lemma());
            System.out.println("tag= " + label.tag());
            System.out.println("ner= " + label.ner());
            System.out.println();
        }

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
