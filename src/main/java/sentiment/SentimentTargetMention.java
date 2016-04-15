package sentiment;

/**
 * A SentimentTargetMention is a reference to a context (i.e. a sentence) which contains a mention to a named entity.
 * The mention can be both direct and using pronouns (he, she, it, they, etc).
 */
public class SentimentTargetMention {
    private String name;
    private int index;  // sentence index in list of sentences

    public SentimentTargetMention(String name, int index) {
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
     * Get the index of the sentence in the list of sentences.
     * @return sentence index
     */
    public int getIndex() {
        return index;
    }

    @Override
    public String toString() {
        return name + ":" + index;
    }
}
