package sentiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class SentimentTargetsAnnotator implements Annotator {
    public final static String SENTIMENT_TARGETS = "sentimenttargets";
    public final static Class SENTIMENT_TARGET_ANNOTATION_CLASS = SentimentTargetsAnnotation.class;

    @Override
    public void annotate(Annotation annotation) {
        // locate candidate entities

        // determine interestingness of entities and purge uninteresting ones

        // determine entities position in sentiment tree

        // find conflicts between entities by moving up tree to see where each entity meets

        // use heuristic(s) to select dominant entity
        // will need to look at different trees to determine best splitting heuristic
        annotation.set(SENTIMENT_TARGET_ANNOTATION_CLASS, Arrays.asList(new SentimentTarget(2), new SentimentTarget(0)));

        // using sentence annotation, perhaps another annotation type is better suited (there are A LOT)
//        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//
//        // handle each sentence
//        for(CoreMap sentence: sentences) {
//            // what kind of annotations are available
//            System.out.println(sentence.keySet());
//
//            // TokensAnnotation are available for example
//            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
//            for (CoreLabel token : tokens) {
//                System.out.println(token + ", " + token.ner());  // actually includes NER in this case!! (days = DURATION)
//            }
//
//            // seems like it's only necessary to parse tree if there is a conflict of entities (i.e. two or more entities in a sentence)
//
//            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
//            System.out.println("the overall sentiment rating is " + sentence.get(SentimentCoreAnnotations.SentimentClass.class));
//            parse(tree, 0);
//        }
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        Set<Requirement> requirementsSatisfied = new HashSet<>();
        requirementsSatisfied.add(new Requirement(SENTIMENT_TARGETS));
        return requirementsSatisfied;
    }

    @Override
    public Set<Requirement> requires() {
        Set<Requirement> requirements = new HashSet<>();
        // TODO: find out why it fails when requirements are set
//        requirements.add(new Requirement(Annotator.STANFORD_NER));
//        requirements.add(new Requirement(Annotator.STANFORD_SENTIMENT));
        return requirements;
    }

    // recursively display scores in tree
    public static void parse(Tree tree, int n) {
        // RNNCoreAnnotations.getPredictedClass(tree) returns the sentiment analysis score from 0 to 4 (with -1 for n/a)
        System.out.println(new String(new char[n]).replace("\0", " ")
                + tree.value()
        );

        for (CoreLabel label : tree.taggedLabeledYield()) {
            System.out.println("-----");
            System.out.print("toString= " + label);
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
