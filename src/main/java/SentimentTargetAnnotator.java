import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;

import java.util.HashSet;
import java.util.Set;

public class SentimentTargetAnnotator implements Annotator {
    final String SENTIMENT_TARGETS = "sentimenttargets";

    @Override
    public void annotate(Annotation annotation) {
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
        requirements.add(new Requirement(Annotator.STANFORD_NER));
        requirements.add(new Requirement(Annotator.STANFORD_SENTIMENT));
        return requirements;
    }
}
