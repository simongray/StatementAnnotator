import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;

import java.util.Set;

public class SentimentTargetAnnotator implements Annotator {
    @Override
    public void annotate(Annotation annotation) {

    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        return null;
    }

    @Override
    public Set<Requirement> requires() {
        return null;
    }
}
