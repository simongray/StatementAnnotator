package sentiment;

import java.util.HashSet;
import java.util.Set;

/**
 * A SentimentTarget is a reference to a named entity, as well as the contexts (i.e. sentences) in which it appears in.
 * The overall sentiment of a SentimentTarget is based on the sentiment of each context.
 */
public class SentimentTarget {
    private String name;
    private String type;
    private Set<SentimentTargetMention> contexts;

    public SentimentTarget(String name, String type) {
        this.name = name;
        this.type = type;
        contexts = new HashSet<>();
    }

    /**
     * Get the sentiment across all contexts for this sentiment target.
     * @return sentiment
     */
    public int getSentiment() {
        double sum = 2;
//
//        for (SentimentTargetMention context : contexts) {
//            sum += (double) context.getSentiment();
//        }

        return (int) Math.round(sum/contexts.size());
    }

    /**
     *
     * @return name of sentiment target
     */
    public String getName() {
        return name;
    }

    /**
     *
     * @return type (NER tag) of sentiment target
     */
    public String getType() {
        return type;
    }

    /**
     * Add another mention to this sentiment target.
     * @param mention
     */
    public void addMention(SentimentTargetMention mention) {
        contexts.add(mention);
    }

    @Override
    public String toString() {
        return name + ":" + getSentiment() + ":" + contexts;
    }
}
