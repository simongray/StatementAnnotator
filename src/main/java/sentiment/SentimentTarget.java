package sentiment;

import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A SentimentTarget is a mention of an entity in a context (i.e. a phrase) taken from a larger piece of text.
 * The mention can be both direct or an anaphor (he, she, it, they, etc).
 */
public class SentimentTarget {
    private String name;
    private String tag;
    private String gender;
    private int sentenceIndex;
    private int tokenIndex;  // note: CoreNLP token indexes start at 1, not 0!
    private int sentiment = -1;  // for the sentiment score, -1 means "unset"
    private List<CoreLabel> tokens;
    private List<CoreLabel> anaphora;

    public SentimentTarget(List<CoreLabel> tokens) throws SentimentTargetTokensMissingException {
        if (tokens.isEmpty()) throw new SentimentTargetTokensMissingException();

        this.name = tokens.stream().map(CoreLabel::word).collect(Collectors.joining(" "));
        this.tag = tokens.get(0).get(CoreAnnotations.NamedEntityTagAnnotation.class);
        this.gender = tokens.get(0).get(MachineReadingAnnotations.GenderAnnotation.class);
        this.sentenceIndex = tokens.get(0).get(CoreAnnotations.SentenceIndexAnnotation.class);
        this.tokenIndex = tokens.get(0).get(CoreAnnotations.IndexAnnotation.class);
        this.tokens = tokens;
    }

    /**
     * Create anaphor target associated with antecedent.
     */
    public SentimentTarget getAnaphor(List<CoreLabel> tokens) throws SentimentTargetTokensMissingException {
        SentimentTarget anaphor = new SentimentTarget(this.tokens);
        anaphor.setAnaphora(tokens);
        return anaphor;
    }

    /**
     * Get the name of the mention.
     * @return name
     */
    public String getName() {
        return name;
    }

    public List<CoreLabel> getAnaphora() {  // TODO: included mainly for future bugtesting, figure out if this is necessary
        return anaphora;
    }

    public int getSentiment() {
        return sentiment;
    }

    public boolean hasSentiment() {
        return sentiment != -1;
    }

    public boolean isMale() {
        if (gender != null) {
            return gender.equals("MALE");
        } else {
            return false;
        }
    }

    public boolean isFemale() {
        if (gender != null) {
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

    private void setAnaphora(List<CoreLabel> anaphora) {
        this.sentenceIndex = tokens.get(0).get(CoreAnnotations.SentenceIndexAnnotation.class);
        this.anaphora = anaphora;
    }

    public void setSentiment(int sentiment) throws InvalidSentimentException {
        if (sentiment >= 0 && sentiment < 5) {
            this.sentiment = sentiment;
        } else {
            throw new InvalidSentimentException("sentiment must integer from 0 to 4");
        }
    }

    /**
     * Get the sentence index of the mention in the list of sentences.
     * @return sentenceIndex
     */
    public int getSentenceIndex() {
        return sentenceIndex;
    }

    public int getTokenIndex() {
        return tokenIndex;
    }

    @Override
    public String toString() {
        return name + ":" + tag + ":" + gender + ":" + sentenceIndex;
    }
}
