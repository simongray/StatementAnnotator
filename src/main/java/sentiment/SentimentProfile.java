package sentiment;

import edu.stanford.nlp.pipeline.Annotation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

public class SentimentProfile {
    final Logger logger = LoggerFactory.getLogger(SentimentProfile.class);
    private final String name;
    private List<Annotation> annotations = new ArrayList<>();
    private Map<String, List<SentimentTarget>> mentionsPerEntity = new HashMap<>();
    private Map<String, Integer> scorePerEntity = new HashMap<>();

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
        return name + " " + Arrays.asList(scorePerEntity);
    }

    /**
     * Get the composed scored for all entities.
     * @return map of entity to sentiment score
     */
    public Map<String, Integer> getEntityScores() {
        return new HashMap<>(scorePerEntity);
    }

    /**
     * Add new annotations to this SentimentProfile.
     * Entity scores will be re-calculated based on new mentions.
     * @param annotations
     */
    public void addAnnotations(List<Annotation> annotations) {
        logger.info("adding " + annotations.size() + " annotations to the profile of " + name);
        this.annotations.addAll(annotations);

        // add new mentions to global mentions map
        for (Annotation annotation : annotations) {
            Map<String, List<SentimentTarget>> localMentionsPerEntity = annotation.get(MergedSentimentTargetsAnnotation.class);

            for (Map.Entry<String, List<SentimentTarget>> entry : localMentionsPerEntity.entrySet()) {
                List<SentimentTarget> mentions = mentionsPerEntity.getOrDefault(entry.getKey(), new ArrayList<>());
                mentions.addAll(entry.getValue());
                mentionsPerEntity.put(entry.getKey(), mentions);
            }
        }

        // (re-)calculate composed scores based on global map
        calculateSentimentScores();
    }

    /**
     * Go through all mentions and calculate composed sentiment scores.
     */
    private void calculateSentimentScores() {
        logger.info("calculating sentiment scores for the profile of " + name);
        for (Map.Entry<String, List<SentimentTarget>> entry : mentionsPerEntity.entrySet()) {
            int score = getComposedSentiment(entry.getValue());
            scorePerEntity.put(entry.getKey(), score);
        }
    }

    /**
     * Get the composed sentiment of a list of mentions to the same entity.
     * @param targets
     */
    private int getComposedSentiment(List<SentimentTarget> targets) {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget target : targets) {
            if (target.hasSentiment()) {
                int sentiment = target.getSentiment();

                // TODO: figure out if including (and weighting) the neutral sentiment slightly might be more precise
                // TODO: another option is to make more complicated sentiment assessments, e.g. pos+neg=mixed
                if (sentiment != 2) {  // only use non-neutral sentiment to calculate composed sentiment
                    sum += target.getSentiment();
                    n++;
                }
            } else {
                logger.error("target has no sentiment: " + target);
            }
        }

        // return neutral (= 2) in case the list contained no sentiment
        if (n == 0) {
            return 2;
        }

        return (int) Math.round(sum / n);
    }
}
