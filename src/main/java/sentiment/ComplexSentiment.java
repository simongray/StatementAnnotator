package sentiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

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
     * Get the composed sentiment of a list of sentimentTargets to the same entity.
     * @return integer from 0 through 4
     */
    public int getComposedSentiment() {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            if (sentimentTarget.hasSentiment()) {
                int sentiment = sentimentTarget.getSentiment();

                // TODO: figure out if including (and weighting) the neutral sentiment slightly might be more precise
                // TODO: another option is to make more complicated sentiment assessments, e.g. pos+neg=mixed
                if (sentiment != 2) {  // only use non-neutral sentiment to calculate composed sentiment
                    sum += sentimentTarget.getSentiment();
                    n++;
                }
            } else {
                logger.error("target has no sentiment: " + sentimentTarget);
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
        return getComposedSentiment() + ":" + sentimentTargets.size();
    }
}
