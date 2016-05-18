package sentiment;

import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.stream.Collectors;

/**
 * A SentimentTarget is a mention of an entity in a localContext (i.e. a phrase) taken from a larger piece of text.
 * The target can be both a direct mention or an anaphor (he, she, it, they, etc). referencing an antecedent entity.
 */
public class SentimentTarget {
    final Logger logger = LoggerFactory.getLogger(SentimentTarget.class);
    private String name;
    private String tag;
    private String gender;
    private int sentenceIndex;
    private int tokenIndex;  // note: CoreNLP token indexes start at 1, not 0!
    private List<CoreLabel> tokens;
    private List<CoreLabel> anaphora;
    private Tree localContext;  // TODO: figure out whether it makes sense to save the context after assigning sentiment
    private Tree sentenceContext;
    private int sentiment = 2;  // = neutral by default
    private int sentenceSentiment = 2;

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
    public SentimentTarget createAnaphor(List<CoreLabel> tokens) throws SentimentTargetTokensMissingException {
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

    /**
     * Returns the sentiment score for the local context.
     * @return
     */
    public int getSentiment() {
        return sentiment;
    }

    /**
     * Returns the sentiment score for the sentence.
     * @return
     */
    public int getSentenceSentiment() {
        return sentenceSentiment;
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

    /**
     * Used when creating SentimentTargets that are anaphora.
     * @param anaphora
     */
    private void setAnaphora(List<CoreLabel> anaphora) {
        this.sentenceIndex = tokens.get(0).get(CoreAnnotations.SentenceIndexAnnotation.class);  // TODO: why is this necessary?
        this.anaphora = anaphora;
    }

    /**
     * Set the localContext for this sentiment target.
     * The localContext is a CoreNLP sentiment annotated tree.
     * The sentiment is extracted from the tree when setting the localContext.
     * @param localContext
     */
    public void setContext(Tree localContext, Tree sentenceContext) throws SentimentContextMissingException, SentimentMissingException {
        this.localContext = localContext;
        this.sentenceContext = sentenceContext;

        // make sure at least one type of context is available
        if (sentenceContext == null) throw new SentimentContextMissingException();
        sentenceSentiment = RNNCoreAnnotations.getPredictedClass(sentenceContext);

        // default to sentence sentiment when no local context is available
        if (localContext == null) {
            sentiment = sentenceSentiment;
            logger.info("setting sentiment to " + sentiment + " for " + name + " using sentence context: " + sentenceContext);
        } else {
            sentiment = RNNCoreAnnotations.getPredictedClass(localContext);
            logger.info("setting sentiment to " + sentiment + " for " + name + " using local context: " + localContext);
        }

        if (sentiment == -1) throw new SentimentMissingException();
    }

    public Tree getLocalContext() {
        return localContext;
    }

    public Tree getSentenceContext() {
        return sentenceContext;
    }

    public int getTokenIndex() {
        return tokenIndex;
    }

    @Override
    public String toString() {
        return name + ":" + tag + ":" + gender + ":" + sentenceIndex;
    }
}
