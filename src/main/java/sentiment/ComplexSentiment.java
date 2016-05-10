package sentiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class ComplexSentiment {
    final Logger logger = LoggerFactory.getLogger(ComplexSentiment.class);
    List<SentimentTarget> mentions = new ArrayList<>();

    public void addMentions(List<SentimentTarget> mentions) {
        this.mentions.addAll(mentions);
    }

    /**
     * Get the composed sentiment of a list of mentions to the same entity.
     * @return integer from 0 through 4
     */
    public int getComposedSentiment() {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget mention : mentions) {
            if (mention.hasSentiment()) {
                int sentiment = mention.getSentiment();

                // TODO: figure out if including (and weighting) the neutral sentiment slightly might be more precise
                // TODO: another option is to make more complicated sentiment assessments, e.g. pos+neg=mixed
                if (sentiment != 2) {  // only use non-neutral sentiment to calculate composed sentiment
                    sum += mention.getSentiment();
                    n++;
                }
            } else {
                logger.error("mention has no sentiment: " + mention);
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
        return getComposedSentiment() + ":" + mentions.size();
    }
}
