package sentiment;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

/**
 * Encapsulates a list of SentimentTargets for the same entity
 * Includes various methods to query the sentiment expressed in the SentimentTargets.
 * The purpose is to handle sentiment in a way that is more complex than simply e.g. average polarity score.
 */
public class ComplexSentiment {
    final Logger logger = LoggerFactory.getLogger(ComplexSentiment.class);
    List<SentimentTarget> sentimentTargets = new ArrayList<>();
    SentimentProfile profile;

    /**
     * A reference to the parent SentimentProfile is needed in to order to calculate relative polarity.
     * @param profile the parent profile
     */
    public ComplexSentiment(SentimentProfile profile) {
        this.profile = profile;
    }

    /**
     * Add a list of SentimentTargets to the ComplexSentiment.
     * @param sentimentTargets
     */
    public void addAll(List<SentimentTarget> sentimentTargets) {
        this.sentimentTargets.addAll(sentimentTargets);
    }

    /**
     * Get the average sentiment.
     * @return average sentiment
     */
    public double getMean() {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            sum += sentimentTarget.getSentiment();
            n++;
        }

        // return neutral (= 2) in case the list contained no sentiment
        if (n == 0) {
            return 2.0;
        }

        return sum / n;
    }

    /**
     * Get the positive to negative ratio for all polar values.
     * A ratio > 1 is mostly positive, while a ratio < 1 is mostly negative.
     * A ratio that is equal to 1 has as many negative as positive mentions.
     * @return ratio
     */
    public double getPositiveToNegativeRatio() {
        int positiveCount = 0;
        int negativeCount = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            int sentiment = sentimentTarget.getSentiment();

            if (sentiment > 2) {
                positiveCount++;
            } else if (sentiment < 2) {
                negativeCount++;
            }
        }

        // return neutral (= 2) in case the list contained no sentiment
        if (positiveCount + negativeCount == 0) {
            return 1;
        } else {
            return (double) positiveCount / (double) negativeCount;  // TODO: fix possibility of divide by zero
        }
    }

    /**
     * Get the neutral to polar ratio for all values.
     * A ratio > 1 is mostly neutral, while a ratio < 1 is mostly polar.
     * A ratio that is equal to 1 has as many neutral as polar mentions.
     * @return
     */
    public double getNeutralRatio() {
        int neutralCount = 0;
        int polarCount = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            int sentiment = sentimentTarget.getSentiment();

            if (sentiment == 2) {
                neutralCount++;
            } else {
                polarCount++;
            }
        }

        // return neutral (= 2) in case the list contained no sentiment
        if (neutralCount + polarCount == 0) {
            return 1;
        } else {
            return (double) neutralCount / (double) polarCount; // TODO: fix possibility of divide by zero
        }
    }

    /**
     * Get the count of positives.
     * @return
     */
    public int getPositiveCount() {
        int count = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            if (sentimentTarget.getSentiment() > 2) count++;
        }

        return count;
    }

    /**
     * Get the count of negatives.
     * @return
     */
    public int getNegativeCount() {
        int count = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            if (sentimentTarget.getSentiment() < 2) count++;
        }

        return count;
    }

    /**
     * Get the count of neutrals.
     * @return
     */
    public int getNeutralCount() {
        int count = 0;

        for (SentimentTarget sentimentTarget : sentimentTargets) {
            if (sentimentTarget.getSentiment() == 2) count++;
        }

        return count;
    }

    public Evaluation evaluation() {
        if (isNumerous()) {
            if (isRelativelyPositive()) return Evaluation.POSITIVE;
            if (isRelativelyNegative()) return Evaluation.NEGATIVE;
            if (isRelativelyNeutral()) return Evaluation.NEUTRAL;
        }

        return Evaluation.UNCERTAIN;
    }

    private boolean isNumerous() {
        return size() > 3;  // TODO: make this more dynamic
    }

    private boolean isRelativelyPositive() {
        double upperBound = profile.getSentenceMean() + profile.getSentenceStandardDeviation()/2;
        return getMean() > upperBound;
    }

    private boolean isRelativelyNegative() {
        double lowerBound = profile.getSentenceMean() - profile.getSentenceStandardDeviation()/2;
        return getMean() < lowerBound;
    }

    private boolean isRelativelyNeutral() {
        return !(isRelativelyNegative() || isRelativelyPositive());
    }

    /**
     * Determine whether this ComplexSentiment is strong or not using a heuristic.
     * The heuristic depends on data from the parent SentimentProfile.
     * @return
     */
    public boolean isStrong() {
        if (isNumerous() && !isRelativelyNeutral()) {
            return true;
        }

        return false;
    }

    /**
     * The size of the internal sentimentTargets list.
     * Useful for evaluating how a ComplexSentiment should be weighted.
     * @return
     */
    public int size() {
        return sentimentTargets.size();
    }

    @Override
    public String toString() {
        NumberFormat nf = NumberFormat.getNumberInstance(Locale.US);
        DecimalFormat df = (DecimalFormat) nf;
        df.setMaximumFractionDigits(2);
        df.setMinimumFractionDigits(2);
        return    evaluation() + " -> "
                + getPositiveCount() + ":"
                + getNeutralCount() + ":"
                + getNegativeCount() + "::"
                + df.format(getMean()) + ":"
                + df.format(getPositiveToNegativeRatio()) + ":"
                + df.format(getNeutralRatio()) + "::"
                + sentimentTargets.size();
    }

    public enum Evaluation {
        POSITIVE,
        NEGATIVE,
        NEUTRAL,
        UNCERTAIN
    }
}
