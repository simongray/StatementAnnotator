package sentiment;

/**
 * A SentimentTarget is a reference to a context (i.e. a sentence) which contains a mention to a named entity.
 * The mention can be both direct and using pronouns (he, she, it, they, etc).
 */
public class SentimentTarget {
    private String name;
    private String tag;
    private int sentenceIndex;  // index in list of sentences

    public SentimentTarget(String name, String tag, int index) {
        this.name = name;
        this.tag = tag;
        this.sentenceIndex = index;
    }

    /**
     * Get the name of the mention.
     * @return name
     */
    public String getName() {
        return name;
    }

    /**
     * Get the NER tag of the mention.
     * @return tag
     */
    public String getTag() {
        return tag;
    }

    /**
     * Get the sentence index of the mention in the list of sentences.
     * @return sentenceIndex
     */
    public int getSentenceIndex() {
        return sentenceIndex;
    }

    @Override
    public String toString() {
        return name + ":" + tag + ":" + sentenceIndex;
    }
}
