package sentiment;

import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;


public class SentimentTargetsAnnotator implements Annotator {
    public final static String SENTIMENT_TARGETS = "sentimenttargets";
    public final static Class SENTIMENT_TARGET_ANNOTATION_CLASS = SentimentTargetsAnnotation.class;

    // anaphora resolution
    public Set<String> trackedKeywords = new HashSet<>();
    public Set<String> trackedMaleKeywords = new HashSet<>();
    public Set<String> trackedFemaleKeywords = new HashSet<>();
    public Set<String> trackedPluralKeywords = new HashSet<>();
    {
        trackedMaleKeywords.add("he");
        trackedMaleKeywords.add("him");
        trackedMaleKeywords.add("his");
        trackedFemaleKeywords.add("she");
        trackedFemaleKeywords.add("her");
        trackedFemaleKeywords.add("hers");
        trackedPluralKeywords.add("they");
        trackedPluralKeywords.add("them");
        trackedPluralKeywords.add("their");
        trackedPluralKeywords.add("theirs");
        trackedKeywords.addAll(trackedMaleKeywords);
        trackedKeywords.addAll(trackedFemaleKeywords);
        trackedKeywords.addAll(trackedPluralKeywords);
    }

    @Override
    public void annotate(Annotation annotation) {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        // get all mentions mapped to the indexes of the sentences they appear in
        Map<Integer, List<SentimentTarget>> mentionsPerSentence = getMentions(sentences);

        // compile a list of mentions from the map
        List<SentimentTarget> mentions = new ArrayList<>();
        for (List<SentimentTarget> sentenceMentions : mentionsPerSentence.values()) {
            mentions.addAll(sentenceMentions);
        }

        // merge the mentions referring to the same entity, producing map of full entity name to mentions
        Map<String, List<SentimentTarget>> mentionsPerEntity = getMergedEntities(mentions);

        // attach sentiment to each mention
        for (Integer i : mentionsPerSentence.keySet()) {
            List<SentimentTarget> sentenceMentions = mentionsPerSentence.get(i);
            CoreMap sentence = sentences.get(i);
            try {
                attachSentiment(sentenceMentions, sentence);
            } catch (SentimentOutOfBoundsException e) {
                System.out.println("could not attach sentiment to mentions " + sentenceMentions + " in sentence: " + sentence);
            }
        }

        // produce a map of full entity name to sentiment by composing the sentiment sores attached in the previous step
        Map<String, Integer> scorePerEntity = new HashMap<>();
        for (String entity : mentionsPerEntity.keySet()) {
            List<SentimentTarget> entityMentions = mentionsPerEntity.get(entity);
            scorePerEntity.put(entity, getComposedSentiment(entityMentions));
        }

        // the final annotation object now includes each map produced as well as the list of mentions
        annotation.set(SentimentTargetsAnnotation.class, mentions);
        annotation.set(SentenceSentimentTargetsAnnotation.class, mentionsPerSentence);
        annotation.set(MergedSentimentTargetsAnnotation.class, mentionsPerEntity);
        annotation.set(MergedSentimentTargetsScoreAnnotation.class, scorePerEntity);
    }

    @Override
    public Set<Requirement> requirementsSatisfied() {
        Set<Requirement> requirementsSatisfied = new HashSet<>();
        requirementsSatisfied.add(new Requirement(SENTIMENT_TARGETS));
        return requirementsSatisfied;
    }

    @Override
    public Set<Requirement> requires() {
        Set<Requirement> requirements = new HashSet<>();
        // TODO: find out why it fails when requirements are set
//        requirements.add(new Requirement(Annotator.STANFORD_NER));
//        requirements.add(new Requirement(Annotator.STANFORD_SENTIMENT));
        return requirements;
    }

    /**
     * Get the composed sentiment a list of (assumed identical) targets.
     * @param targets
     */
    private int getComposedSentiment(List<SentimentTarget> targets) {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget target : targets) {
            if (target.hasSentiment()) {
                sum += target.getSentiment();
                n++;
            } // TODO: should probably log targets without sentiment here
        }

        // return neutral (= 2) in case the list contained no sentiment
        if (n == 0) {
            return 2;
        }

        return (int) Math.round(sum / n);
    }

    /**
     * Attaches sentiment to the targets of a sentence.
     *
     * @param targets
     * @param sentence
     */

    private void attachSentiment(List<SentimentTarget> targets, CoreMap sentence) throws SentimentOutOfBoundsException {
        // TODO: this method should be simpler and simply take 1 sentence and all of the entities for that sentence as its input
        if (targets.size() == 1) {
            SentimentTarget target = targets.get(0);
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentimentScore = RNNCoreAnnotations.getPredictedClass(tree);
            target.setSentiment(sentimentScore);

            System.out.println("" + target.getName() + " scored " + getSentiment(sentimentScore) + " in sentence: " + sentence.get(CoreAnnotations.TextAnnotation.class));
        } else {
            System.out.println("multiple entities in sentence (" + targets + "), not implemented yet"); // TODO
        }
    }

    /**
     * Converts a RNN sentiment score into human readable form.
     * @param sentimentScore
     * @return
     */
    private Sentiment getSentiment(int sentimentScore) {  // TODO: figure out if this makes sense to keep
        switch (sentimentScore) {
            case 0:
                return Sentiment.VERY_NEGATIVE;
            case 1:
                return Sentiment.NEGATIVE;
            case 3:
                return Sentiment.POSITIVE;
            case 4:
                return Sentiment.VERY_POSITIVE;
            default:
                return Sentiment.NEUTRAL;  // i.e. for score = 2
        }
    }

    /**
     * Uses the CoreNLP NER tagger to find relevant entities.
     * @return a list of entities
     */
    private Map<Integer, List<SentimentTarget>> getMentions(List<CoreMap> sentences) {
        Map<Integer, List<SentimentTarget>> mentions = new HashMap<>();

        // for anaphora resolution
        Map<String, List<SentimentTarget>> anaphoras = new HashMap<>();  // TODO: figure out if this should be called anaphoras
        Set<String> foundKeywords = new HashSet<>();

        for (int i = 0; i < sentences.size(); i++) {
            List<CoreLabel> tokens = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class);
            String previousTag = "";  // keep track of previous tag for multi-word entities
            String fullName = "";
            String gender = "";
            mentions.put(i, new ArrayList<>());

            // retrieve all entities in sentence
            for (CoreLabel token : tokens) {
                String name = token.get(CoreAnnotations.TextAnnotation.class);
                String nerTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                // only allow specific tags (e.g. not DATE, DURATION, NUMBER)
                if (nerTag.length() > 1 && nerTag.equals("PERSON") || nerTag.equals("ORGANIZATION") || nerTag.equals("LOCATION") || nerTag.equals("MISC")) {

                    // keeping track of multi-word entities
                    if (nerTag.equals(previousTag)) {
                        fullName += " " + name;
                    } else {
                        fullName = name;

                        // use only first names for gender
                        if (nerTag.equals("PERSON")) {
                            gender = token.get(MachineReadingAnnotations.GenderAnnotation.class);
                        }
                    }

                    previousTag = nerTag;
                } else {
                    if (!fullName.isEmpty()) {
                        SentimentTarget mention = new SentimentTarget(fullName, previousTag, gender, i);
                        mentions.get(i).add(mention);
                        System.out.println("ENTITY: " + fullName + ", " + gender + ", " + i);
                        // TODO: figure out whether it makes sense to also have token index in mention
                    }

                    // track keywords for anaphora resolution
                    if (mentions.get(i).isEmpty() && trackedKeywords.contains(name.toLowerCase())) {
                        foundKeywords.add(name.toLowerCase());
                        System.out.println("KEYWORD: " + name + ", " + i);
                    }

                    // make sure to reset for next token
                    previousTag = "";
                    fullName = "";
                }
            }

            if (mentions.get(i).isEmpty() && !foundKeywords.isEmpty()) {
                SentimentTarget antecedent = getAntecedent(foundKeywords, mentions.getOrDefault(i-1, new ArrayList<>()));

                if (antecedent != null) {
                    System.out.println("ANTECENDENT: " + antecedent + " based on anaphora: " + foundKeywords);
                    mentions.get(i).add(antecedent.getAnaphor(foundKeywords, i));
                }
            }
        }

        return mentions;
    }

    /**
     * Perform basic anaphora resolution for a sentence.
     * The antecedent entity is determined among a list of previous entity mentions and anaphoras found in a sentence.
     * @param keywords
     * @param mentions
     * @return
     */
    private SentimentTarget getAntecedent(Set<String> keywords, List<SentimentTarget> mentions) {
        Set<AnaphoraType> possibleMatches = getPossibleMatches(keywords);

        // determine whether to perform single or multiple resolution
        if (mentions.size() == 1) {
            return getSingleMentionAntecedent(possibleMatches, mentions.get(0));
        } else if (possibleMatches.size() == 1) {
            AnaphoraType type = possibleMatches.iterator().next();  // shitty Java syntax
            return getSingleTypeAntecedent(type, mentions);
        } else {
            System.out.println("multiple resolution not implemented yet...");
            return null;  // TODO: implement this
        }
    }

    /**
     * Perform anaphora resolution with a single known entity.
     * @param types
     * @param mention
     * @return
     */
    private SentimentTarget getSingleMentionAntecedent(Set<AnaphoraType> types, SentimentTarget mention) {
        if (mention.isPerson()) {
            if (mention.hasGender()) {
                if (mention.isMale() && types.contains(AnaphoraType.MALE)) {
                    return mention;
                } else if (mention.isFemale() && types.contains(AnaphoraType.FEMALE)) {
                    return mention;
                }
            } else if (types.contains(AnaphoraType.MALE) || types.contains(AnaphoraType.FEMALE)) {
                return mention;
            }
        }

        if (mention.isOrganization() && types.contains(AnaphoraType.PLURAL)) {
            return mention;
        }

        if (mention.isLocation() && types.contains(AnaphoraType.PLURAL)) {
            return mention;
        }

        return null;
    }

    /**
     * Perform anaphora resolution for a single known type (MALE, FEMALE, or PLURAL).
     * @param type
     * @param mentions
     * @return
     */
    private SentimentTarget getSingleTypeAntecedent(AnaphoraType type, List<SentimentTarget> mentions) {
        if (type == AnaphoraType.MALE) {
            return getMaleAnaphor(mentions);
        } else if (type == AnaphoraType.FEMALE) {
            return getFemaleAnaphor(mentions);
        } else {
            return getPluralAnaphor(mentions);
        }
    }

    /**
     * Perform anaphora resolution for a male.
     * @param mentions
     * @return
     */
    private SentimentTarget getMaleAnaphor(List<SentimentTarget> mentions) {
        SentimentTarget candidate = null;
        List<SentimentTarget> candidatesWithoutGender = new ArrayList<>();

        // will return any PERSON if the mentions only include a single, non-gendered PERSON
        // if exactly one MALE candidate is found, will return that instead
        for (SentimentTarget mention : mentions) {
            if (mention.isPerson()) {
                if (mention.hasGender()) {
                    if (mention.isMale()) {  // do not consider females at all!
                        if (candidate == null) {
                            candidate = mention;
                        } else {
                            return null;
                        }
                    }
                } else {
                    candidatesWithoutGender.add(mention);
                }
            }
        }

        // if no MALE candidate is found, use the candidate without gender
        if (candidatesWithoutGender.size() == 1) {
            candidate = candidatesWithoutGender.get(0);
        }

        return candidate;
    }

    /**
     * Perform anaphora resolution for a female.
     * @param mentions
     * @return
     */
    private SentimentTarget getFemaleAnaphor(List<SentimentTarget> mentions) {
        SentimentTarget candidate = null;
        List<SentimentTarget> candidatesWithoutGender = new ArrayList<>();

        // will return any PERSON if the mentions only include a single, non-gendered PERSON
        // if exactly one FEMALE candidate is found, will return that instead
        for (SentimentTarget mention : mentions) {
            if (mention.isPerson()) {
                if (mention.hasGender()) {
                    if (mention.isFemale()) {  // do not consider males at all!
                        if (candidate == null) {
                            candidate = mention;
                        } else {
                            return null;
                        }
                    }
                } else {
                    candidatesWithoutGender.add(mention);
                }
            }
        }

        // if no MALE candidate is found, use the candidate without gender
        if (candidatesWithoutGender.size() == 1) {
            candidate = candidatesWithoutGender.get(0);
        }

        return candidate;
    }

    /**
     * Perform anaphora resolution for a male.
     * @param mentions
     * @return
     */
    private SentimentTarget getPluralAnaphor(List<SentimentTarget> mentions) {
        SentimentTarget candidate = null;

        for (SentimentTarget mention : mentions) {
            if (mention.isOrganization() || mention.isLocation()) {
                if (candidate == null) {
                    candidate = mention;
                } else {
                    return null;
                }
            }
        }

       return candidate;
    }

    /**
     * Used to determine which types of anaphora were found in a sentence.
     * @param keywords
     * @return
     */
    private Set<AnaphoraType> getPossibleMatches(Set<String> keywords) {
        Set<AnaphoraType> possibleMatches = new HashSet<>();

        for (String keyword : keywords) {
            if (trackedMaleKeywords.contains(keyword)) {
                possibleMatches.add(AnaphoraType.MALE);
            } else if (trackedFemaleKeywords.contains(keyword)) {
                possibleMatches.add(AnaphoraType.FEMALE);
            } else if (trackedPluralKeywords.contains(keyword)) {
                possibleMatches.add(AnaphoraType.PLURAL);
            }
        }

        return possibleMatches;
    }

    // TODO: revisit and revise this horribly inefficient and unreadable method
    /**
     * Algorithm to merge entities if the names are found to be shorter versions of the full name.
     *
     *  Pseudo-code:
     *      Goal: full(est) names should contain references to short(er) names, shorter names eliminated
     *      Result: List of SentimentTargets with name=fullname containing list of SentimentTargetMentions
     *          * determine which names are full names and shorter versions of the full names
     *              * a full name is a name that is NOT a short name
     *              * a short name is a prefix or a suffix of EXACTLY one other name which is not ALSO a short name

     * @param mentions
     * @return
     */
    Map<String, List<SentimentTarget>> getMergedEntities(List<SentimentTarget> mentions) {
        Map<String, Set<String>> shortToLong = new HashMap<>();

        // reduce to unique set of names
        Set<String> names = new HashSet<>();
        for (SentimentTarget mention : mentions) {
            if (mention.isPerson()) {  // TODO: consider whether it should also merge locations or organisations
                names.add(mention.getName());
            }
        }

        // create map of short names to long names
        for (String name : names) {
            for (String otherName : names) {
                if (!name.equals(otherName) && (otherName.startsWith(name) || otherName.endsWith(name))) {
                    Set<String> longNames = shortToLong.getOrDefault(name, new HashSet<>());
                    longNames.add(otherName);
                    shortToLong.put(name, longNames);
                }
            }
        }

        System.out.println();
        System.out.println("after creating short to long mapping");

        for (String name : shortToLong.keySet()) {
            System.out.println(name + " -> " + shortToLong.get(name));
        }

        // if a long name also exists as a short name it should be purged
        for (String shortName : shortToLong.keySet()) {
            Set<String> longNames = shortToLong.get(shortName);
            Set<String> purgedNames = new HashSet<>();

            if (longNames.size() > 1) {
                for (String longName : longNames) {
                    if (shortToLong.containsKey(longName)) {
                        purgedNames.add(longName);
                    }
                }
            }

            longNames.removeAll(purgedNames);
            shortToLong.put(shortName, longNames);
        }

        System.out.println();
        System.out.println("after purging middle-form names");

        for (String name : shortToLong.keySet()) {
            System.out.println(name + " -> " + shortToLong.get(name));
        }

        // of the remaining short names, the ones with only a single long form are preserved
        // the ones with multiple are conflicting, the ones with a null reference are middle-form
        // if a name is a short name, then it needs to be a mention in its long-form sentiment target
        Map<String, Set<String>> fullToShort = new HashMap<>();
        for (String shortName : shortToLong.keySet()) {
            Set<String> longNames = shortToLong.get(shortName);
            if (longNames.size() == 1) {
                // weird syntax since Set does not have a get(0)-method or similar
                String longName = longNames.iterator().next();
                Set<String> shortNames = fullToShort.getOrDefault(longName, new HashSet<>());
                shortNames.add(shortName);
                fullToShort.put(longName, shortNames);
            }
        }

        System.out.println();
        System.out.println("reverse mapping with full names to short names");
        for (String name : fullToShort.keySet()) {
            System.out.println(name + " -> " + fullToShort.get(name));
        }

        System.out.println();
        System.out.println("as map of SentimentTargets");
        Map<String, List<SentimentTarget>> mergedEntities = new HashMap<>();

        // creating the final mapping of String --> list of sentiment targets
        for (SentimentTarget mention : mentions) {
            String entityName = mention.getName();
            String targetName = "";

            // if mention is either a full name or a short name
            if (mergedEntities.containsKey(entityName)) {
                targetName = entityName;
            } else {
                for (String fullName : fullToShort.keySet()) {
                    Set<String> shortNames = fullToShort.get(fullName);
                    if (shortNames.contains(entityName)) {
                       targetName = fullName;
                        break;
                    }
                }
            }

            // mention is neither a full name or a short name (most targets will be like this)
            if (targetName.isEmpty()) {
                targetName = entityName;
            }

            List<SentimentTarget> contexts = mergedEntities.getOrDefault(targetName, new ArrayList<>());
            contexts.add(mention);
            mergedEntities.put(targetName, contexts);
        }

        // todo: replace print with log

        return mergedEntities;
    }

    public enum Sentiment {
        VERY_NEGATIVE,
        NEGATIVE,
        NEUTRAL,
        POSITIVE,
        VERY_POSITIVE
    }


    private enum AnaphoraType {
        MALE,
        FEMALE,
        PLURAL
    }
}
