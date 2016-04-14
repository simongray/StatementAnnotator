package sentiment;

import java.util.HashSet;
import java.util.Set;

/**
 * A SentimentTarget is a reference to a named entity, as well as the contexts (i.e. sentences) in which it appears in.
 * The overall sentiment of a SentimentTarget is based on the sentiment of each context.
 */
public class SentimentTarget {
    private String name;
    private Type type;
    private Set<SentimentContext> contexts;

    public SentimentTarget(String name, Type type) {
        this.name = name;
        this.type = type;
        contexts = new HashSet<>();
    }

    /**
     * Get the sentiment across all contexts for this sentiment target.
     * @return sentiment
     */
    public int getSentiment() {
        double sum = 0;

        for (SentimentContext context : contexts) {
            sum += (double) context.getSentiment();
        }

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
     * Add another context to this sentiment target.
     * @param context
     */
    public void addContext(SentimentContext context) {
        contexts.add(context);
    }

    @Override
    public String toString() {
        return name + ": " + getSentiment();
    }

    /**
     * Corresponding to entity types created by CoreNLP NER annotator.
     */
    public enum Type {
        PERSON,
        ORGANIZATION,
        MISC  // TODO: figure out whether to include MISC at all
    }
}
