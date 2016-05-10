package sentiment;

import edu.stanford.nlp.pipeline.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SentimentProfile {
    final Logger logger = LoggerFactory.getLogger(SentimentProfile.class);
    private final String name;
    private List<Annotation> annotations = new ArrayList<>();
    private Map<String, ComplexSentiment> entityToSentiment = new HashMap<>();

    public SentimentProfile(String name, List<Annotation> annotations) {
        logger.info("created sentiment profile for " + name);
        this.name = name;
        addAnnotations(annotations);
    }

    public String getName() {
        return name;
    }

    @Override
    public String toString() {
        return name + " " + Arrays.asList(entityToSentiment);
    }

    /**
     * Get the composed scored for all entities.
     * @return map of entity to sentiment score
     */
    public Map<String, ComplexSentiment> getEntityScores() {
        return new HashMap<>(entityToSentiment);
    }

    /**
     * Add new annotations to this SentimentProfile.
     * Entity scores will be re-calculated based on new mentions.
     * @param annotations
     */
    public void addAnnotations(List<Annotation> annotations) {
        logger.info("adding " + annotations.size() + " annotations to the profile of " + name);
        this.annotations.addAll(annotations);

        // add new mentions to global map of entity to sentiment
        for (Annotation annotation : annotations) {
            Map<String, List<SentimentTarget>> entityToMentions = annotation.get(MergedSentimentTargetsAnnotation.class);

            for (Map.Entry<String, List<SentimentTarget>> entry : entityToMentions.entrySet()) {
                ComplexSentiment sentiment = entityToSentiment.getOrDefault(entry.getKey(), new ComplexSentiment());
                sentiment.addMentions(entry.getValue());
                entityToSentiment.put(entry.getKey(), sentiment);
            }
        }
    }
}
