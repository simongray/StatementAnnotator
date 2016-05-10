package sentiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

/**
 * Encapsulates a list of SentimentTargets for the same entity
 * Includes various methods to query the sentiment expressed in the SentimentTargets.
 * The purpose is to handle sentiment in a way that is more complex than simply e.g. average polarity score.
 */
public class ComplexSentiment {
    final Logger logger = LoggerFactory.getLogger(ComplexSentiment.class);
    List<SentimentTarget> sentimentTargets = new ArrayList<>();

    /**
     * Add a list of SentimentTargets to the ComplexSentiment.
     * @param sentimentTargets
     */
    public void addAll(List<SentimentTarget> sentimentTargets) {
        this.sentimentTargets.addAll(sentimentTargets);
    }

    /**
     * Get the average sentiment rounded to nearest integer.
     * @return average sentiment
     */
    public int getAverageSentiment() {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            sum += sentimentTarget.getSentiment();
            n++;
        }

        // return neutral (= 2) in case the list contained no sentiment
        if (n == 0) {
            return 2;
        }

        return (int) Math.round(sum / n);
    }

    /**
     * Get the composed sentiment of a list of sentimentTargets to the same entity.
     * @return integer from 0 through 4
     */
    public int getComposedSentiment() {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            int sentiment = sentimentTarget.getSentiment();

            // TODO: figure out if including (and weighting) the neutral sentiment slightly might be more precise
            // TODO: another option is to make more complicated sentiment assessments, e.g. pos+neg=mixed
            if (sentiment != 2) {  // only use non-neutral sentiment to calculate composed sentiment
                sum += sentimentTarget.getSentiment();
                n++;
            }
        }

        // return neutral (= 2) in case the list contained no sentiment
        if (n == 0) {
            return 2;
        }

        return (int) Math.round(sum / n);
    }

    @Override
    public String toString() {
        return getAverageSentiment() + ":" + getComposedSentiment() + ":" + sentimentTargets.size();
    }
}
