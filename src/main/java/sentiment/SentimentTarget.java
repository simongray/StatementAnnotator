package sentiment;

/**
 * A SentimentTarget is a mention of an entity in a context (i.e. a sentence) taken from a larger piece of text.
 * The mention can be both direct and using pronouns (he, she, it, they, etc).
 */
public class SentimentTarget {
    private String name;
    private String tag;
    private String gender;  // obviously only applicable to persons
    private int sentenceIndex;  // ... in the list of sentences for the comment

    public SentimentTarget(String name, String tag, String gender, int index) {
        this.name = name;
        this.tag = tag;
        this.gender = gender;
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
     * Get the gender of the mention.
     * @return tag
     */
    public String getGender() {
        return gender;
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
        return name + ":" + tag + ":" + gender + ":" + sentenceIndex;
    }
}
