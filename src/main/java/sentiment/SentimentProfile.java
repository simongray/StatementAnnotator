package sentiment;

import edu.stanford.nlp.pipeline.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Represents a person's entire opinion profile.
 * It builds this profile from the annotations created by SentimentTargetsAnnotator.
 */
public class SentimentProfile {
    final Logger logger = LoggerFactory.getLogger(SentimentProfile.class);
    private List<Annotation> annotations = new ArrayList<>();
    private Map<String, ComplexSentiment> entityToSentiment = new HashMap<>();

    public SentimentProfile(List<Annotation> annotations) {
        logger.info("created sentiment profile with " + annotations.size() + annotations);
        for (Annotation annotation : annotations) {
            add(annotation);
        }
    }

    // TODO: perhaps add name merge code in this class too?

    /**
     * Get the ComplexSentiment for every entity.
     * @return a shallow copy of the internal map of entity to sentiment
     */
    public Map<String, ComplexSentiment> getSentiments() {
        return new HashMap<>(entityToSentiment);
    }

    /**
     * Add new annotation to this SentimentProfile.
     * This operation also unpackages and assigns included SentimentTargets to the relevant entities.
     * @param annotation
     */
    public void add(Annotation annotation) {
        this.annotations.add(annotation);

        Map<String, List<SentimentTarget>> entityToSentimentTargets = annotation.get(MergedSentimentTargetsAnnotation.class);

        for (Map.Entry<String, List<SentimentTarget>> entry : entityToSentimentTargets.entrySet()) {
            ComplexSentiment sentiment = entityToSentiment.getOrDefault(entry.getKey(), new ComplexSentiment());
            sentiment.addAll(entry.getValue());
            entityToSentiment.put(entry.getKey(), sentiment);
        }
    }

    @Override
    public String toString() {
        return Arrays.asList(entityToSentiment).toString();
    }
}
