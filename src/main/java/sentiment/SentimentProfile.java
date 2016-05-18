package sentiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statistics.Statistics;

import java.util.*;

/**
 * Represents a person's entire opinion profile.
 * It builds this profile from the annotations created by SentimentTargetsAnnotator.
 */
public class SentimentProfile {
    final Logger logger = LoggerFactory.getLogger(SentimentProfile.class);
    private List<Annotation> annotations = new ArrayList<>();
    private Map<String, ComplexSentiment> entityToComplexSentiment = new HashMap<>();
    private List<Double> sentenceSentiments = new ArrayList<>();
    private Statistics sentenceStatistics;

    public SentimentProfile(List<Annotation> annotations) {
        logger.info("created sentiment profile with " + annotations.size() + annotations);
        update(annotations);
    }

    /**
     * Add new annotations to this profile.
     * Should be used sparingly, as an update resets sentence statistics.
     * @param annotations
     */
    public void update(List<Annotation> annotations) {
        for (Annotation annotation : annotations) {
            add(annotation);
        }
        sentenceStatistics = new Statistics(sentenceSentiments);
    }

    // TODO: perhaps add name merge code in this class too?

    /**
     * Get the ComplexSentiment for every entity.
     * @return a shallow copy of the internal map of entity to sentiment
     */
    public Map<String, ComplexSentiment> getSentiments() {
        return new HashMap<>(entityToComplexSentiment);
    }

    /**
     * Determine whether a complex sentiment is strong or not using a heuristic.
     * @param complexSentiment
     * @return
     */
    private boolean isStrong(ComplexSentiment complexSentiment) {
        double upperBound = getSentenceMean() + getSentenceStandardDeviation()/2;
        double lowerBound = getSentenceMean() - getSentenceStandardDeviation()/2;
        double sentiment = complexSentiment.getMean();
        int size = complexSentiment.size();


        if (size > 3 && (sentiment > upperBound || sentiment < lowerBound)) {
            return true;
        }

        return false;
    }

    /**
     * Get the ComplexSentiment for every entity.
     * @return a shallow copy of the internal map of entity to sentiment
     */
    public Map<String, ComplexSentiment> getStrongSentiments() {
        Map<String, ComplexSentiment> strongSentiments = new HashMap<>();

        for (String entity : entityToComplexSentiment.keySet()) {
            ComplexSentiment complexSentiment = entityToComplexSentiment.get(entity);
            if (isStrong(complexSentiment)) {
                strongSentiments.put(entity, complexSentiment);
            }
        }

        return strongSentiments;
    }

    /**
     * Add new annotation to this SentimentProfile.
     * Adds sentiment targets to the relevant complex sentiment.
     * Establishes a new base sentiment based on the sentiment trees for the full sentences.
     * @param annotation
     */
    private void add(Annotation annotation) {
        this.annotations.add(annotation);

        try {
            updateBaseSentiment(annotation);
        } catch (SentimentMissingException e) {
            logger.error("could not get sentence sentiment(s) for annotation: " + annotation.toShortString());
        }

        Map<String, List<SentimentTarget>> entityToSentimentTargets = annotation.get(MergedSentimentTargetsAnnotation.class);

        for (Map.Entry<String, List<SentimentTarget>> entry : entityToSentimentTargets.entrySet()) {
            ComplexSentiment complexSentiment = entityToComplexSentiment.getOrDefault(entry.getKey(), new ComplexSentiment());
            complexSentiment.addAll(entry.getValue());
            entityToComplexSentiment.put(entry.getKey(), complexSentiment);
        }
    }

    /**
     * Set the base sentiment (= the average sentiment over all sentences).
     * Useful for determining the bias of the sentiment profile.
     * @param annotation
     * @throws SentimentMissingException
     */
    private void updateBaseSentiment(Annotation annotation) throws SentimentMissingException {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        for (CoreMap sentence : sentences) {
            Tree sentenceContext = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentenceSentiment = RNNCoreAnnotations.getPredictedClass(sentenceContext);
            if (sentenceSentiment == -1) throw new SentimentMissingException();
            sentenceSentiments.add((double) sentenceSentiment);
        }
    }

    /**
     * Returns the mean sentiment (= the average sentiment over all sentences).
     * Useful for determining the bias of the sentiment profile.
     * @return
     */
    public double getSentenceMean() {
        if (sentenceSentiments.isEmpty()) {
            return 2.0;
        } else {
            return sentenceStatistics.getMean();
        }
    }

    /**
     * Returns the sentence variance (= the sentiment variance over all sentences).
     * @return
     */
    public double getSentenceVariance() {
        if (sentenceSentiments.isEmpty()) {
            return 0.0;
        } else {
            return sentenceStatistics.getVariance();
        }
    }

    /**
     * Returns the sentence standard deviation (= the sentiment standard deviation over all sentences).
     * @return
     */
    public double getSentenceStandardDeviation() {
        if (sentenceSentiments.isEmpty()) {
            return 0.0;
        } else {
            return sentenceStatistics.getStandardDeviation();
        }
    }

    /**
     * Returns the sentence median (= the sentiment median over all sentences).
     * @return
     */
    public double getSentenceMedian() {
        if (sentenceSentiments.isEmpty()) {
            return 2.0;
        } else {
            return sentenceStatistics.getMedian();
        }
    }

    @Override
    public String toString() {
        return Arrays.asList(entityToComplexSentiment).toString();
    }
}
