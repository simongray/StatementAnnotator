import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;

import java.util.HashSet;
import java.util.Set;

public class SentimentTargetAnnotator implements Annotator {
    final String SENTIMENT_TARGETS = "sentimenttargets";

    @Override
    public void annotate(Annotation annotation) {
        // locate candidate entities

        // determine interestingness of entities and purge uninteresting ones

        // determine entities position in sentiment tree

        // find conflicts between entities by moving up tree to see where each entity meets

        // use heuristic(s) to select dominant entity
        // will need to look at different trees to determine best splitting heuristic
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
