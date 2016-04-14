package sentiment;

/**
 * A SentimentContext is a reference to a context (i.e. a sentence) which contains a mention to a named entity.
 * The mention can be both direct and using pronouns (he, she, it, they, etc).
 */
public class SentimentContext {
    private String name;
    private int sentiment = 2; // neutral by default
    private int index;  // sentence index in list of sentences

    public SentimentContext(String name, int index) {
        this.name = name;
        this.index = index;
    }

    /**
     *
     * @return the name, e.g. pronoun, of the entity
     */
    public String getName() {
        return name;
    }

    /**
     * Get the sentiment for this sentence.
     * @return
     */
    public int getSentiment() {
        return sentiment;
    }

    /**
     * Get the index of the sentence in the list of sentences.
     * @return sentence index
     */
    public int getIndex() {
        return index;
    }
}
