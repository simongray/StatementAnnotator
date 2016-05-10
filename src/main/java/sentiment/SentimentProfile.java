package sentiment;

import edu.stanford.nlp.pipeline.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

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

    @Override
    public String toString() {
        return Arrays.asList(entityToSentiment).toString();
    }

    /**
     * Get the ComplexSentiment for every entity.
     * @return map of entity to sentiment score
     */
    public Map<String, ComplexSentiment> getSentiments() {
        return new HashMap<>(entityToSentiment);
    }

    /**
     * Add new annotation to this SentimentProfile.
     * @param annotation
     */
    public void add(Annotation annotation) {
        this.annotations.add(annotation);

        // add new mentions to global map of entity to sentiment
        Map<String, List<SentimentTarget>> entityToMentions = annotation.get(MergedSentimentTargetsAnnotation.class);

        for (Map.Entry<String, List<SentimentTarget>> entry : entityToMentions.entrySet()) {
            ComplexSentiment sentiment = entityToSentiment.getOrDefault(entry.getKey(), new ComplexSentiment());
            sentiment.addMentions(entry.getValue());
            entityToSentiment.put(entry.getKey(), sentiment);
        }
    }
}
