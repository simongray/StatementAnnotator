package sentiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
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
}
