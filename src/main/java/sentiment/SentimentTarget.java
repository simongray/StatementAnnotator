package sentiment;

import java.util.Set;

/**
 * A SentimentTarget is a mention of an entity in a context (i.e. a sentence) taken from a larger piece of text.
 * The mention can be both direct and using pronouns (he, she, it, they, etc).
 */
public class SentimentTarget {
    private String name;
    private Set<String> anaphora;
    private String tag;
    private String gender;  // obviously only applicable to persons
    private int sentenceIndex;  // ... in the list of sentences for the comment
    private int sentiment = -1;  // for the sentiment score, -1 means "unset"

    public SentimentTarget(String name, String tag, String gender, int index) {
        this.name = name;
        this.tag = tag;
        this.gender = gender;
        this.sentenceIndex = index;
    }

    public SentimentTarget getAnaphor(Set<String> anaphora, int index) {
        SentimentTarget anaphor = new SentimentTarget(name, tag, gender, index); // note: using argument for index
        anaphor.setAnaphora(anaphora);
        return anaphor;
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

    public Set<String> getAnaphora() {  // TODO: included mainly for future bugtesting, figure out if this is necessary
        return anaphora;
    }

    public int getSentiment() {
        return sentiment;
    }

    public boolean hasSentiment() {
        return sentiment != -1;
    }

    public boolean hasGender() {
        return gender != null;
    }

    public boolean isMale() {
        if (hasGender()) {
            return gender.equals("MALE");
        } else {
            return false;
        }
    }

    public boolean isFemale() {
        if (hasGender()) {
            return gender.equals("FEMALE");
        } else {
            return false;
        }
    }

    public boolean isPerson() {
        return tag.equals("PERSON");
    }

    public boolean isOrganization() {
        return tag.equals("ORGANIZATION");
    }

    public boolean isLocation() {
        return tag.equals("LOCATION");
    }

    public boolean isMisc() {
        return tag.equals("MISC");
    }

    public boolean isAnaphor() {
        return this.anaphora != null && !this.anaphora.isEmpty();
    }

    private void setAnaphora(Set<String> anaphora) {
        this.anaphora = anaphora;
    }

    public void setSentiment(int sentiment) throws SentimentOutOfBoundsException {
        if (sentiment >= 0 && sentiment < 5) {
            this.sentiment = sentiment;
        } else {
            throw new SentimentOutOfBoundsException("sentiment must integer from 0 to 4");
        }
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
