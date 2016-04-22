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

    private enum AnaphoraTypes {
        MALE,
        FEMALE,
        PLURAL
    }


    @Override
    public void annotate(Annotation annotation) {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        Map<Integer, List<SentimentTarget>> mentionsPerSentence = getMentions(sentences);


        List<SentimentTarget> mentions = new ArrayList<>();
        for (List<SentimentTarget> sentenceMentions : mentionsPerSentence.values()) {
            mentions.addAll(sentenceMentions);
        }

        Map<String, List<SentimentTarget>> mentionsPerEntity = getMergedEntities(mentions);
        Map<String, List<Integer>> scores = new HashMap<>();

        System.out.println("\nfinal mapping:");
        for (String key : mentionsPerEntity.keySet()) {
            System.out.println(key + " -----> " + mentionsPerEntity.get(key));
        }

        // get a list containing all the sentiment target mentions
        for (Integer i : mentionsPerSentence.keySet()) {
            List<SentimentTarget> sentenceMentions = mentionsPerSentence.get(i);
            CoreMap sentence = sentences.get(i);
            attachSentiment(sentenceMentions, sentence);
        }

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
     * Attaches sentiment to the targets of a sentence.
     *
     * @param targets
     * @param sentence
     */

    private void attachSentiment(List<SentimentTarget> targets, CoreMap sentence) {
        // TODO: this method should be simpler and simply take 1 sentence and all of the entities for that sentence as its input
        if (targets.size() == 1) {
            SentimentTarget target = targets.get(0);
            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
            int sentimentScore = RNNCoreAnnotations.getPredictedClass(tree);
            Sentiment sentiment = getSentiment(sentimentScore);

            System.out.println("" + target.getName() + " scored " + sentiment + " in sentence: " + sentence.get(CoreAnnotations.TextAnnotation.class));
        } else {
            System.out.println("multiple entities in sentence (" + targets + "), not implemented yet"); // TODO
        }
    }

    /**
     * Converts a RNN sentiment score into human readable form.
     * @param sentimentScore
     * @return
     */
    private Sentiment getSentiment(int sentimentScore) {
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
     * @param keywords
     * @param mentions
     * @return
     */
    private SentimentTarget getAntecedent(Set<String> keywords, List<SentimentTarget> mentions) {
        Set<AnaphoraTypes> possibleMatches = getPossibleMatches(keywords);

        // determine whether to perform single or multiple resolution
        if (mentions.size() == 1) {
            return getSingleMentionAntecedent(possibleMatches, mentions.get(0));
        } else if (possibleMatches.size() == 1) {
            AnaphoraTypes type = possibleMatches.iterator().next();  // shitty Java syntax
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
    private SentimentTarget getSingleMentionAntecedent(Set<AnaphoraTypes> types, SentimentTarget mention) {
        if (mention.isPerson()) {
            if (mention.hasGender()) {
                if (mention.isMale() && types.contains(AnaphoraTypes.MALE)) {
                    return mention;
                } else if (mention.isFemale() && types.contains(AnaphoraTypes.FEMALE)) {
                    return mention;
                }
            } else if (types.contains(AnaphoraTypes.MALE) || types.contains(AnaphoraTypes.FEMALE)) {
                return mention;
            }
        }

        if (mention.isOrganization() && types.contains(AnaphoraTypes.PLURAL)) {
            return mention;
        }

        if (mention.isLocation() && types.contains(AnaphoraTypes.PLURAL)) {
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
    private SentimentTarget getSingleTypeAntecedent(AnaphoraTypes type, List<SentimentTarget> mentions) {
        if (type == AnaphoraTypes.MALE) {
            return getMaleAnaphor(mentions);
        } else if (type == AnaphoraTypes.FEMALE) {
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
    private Set<AnaphoraTypes> getPossibleMatches(Set<String> keywords) {
        Set<AnaphoraTypes> possibleMatches = new HashSet<>();

        for (String keyword : keywords) {
            if (trackedMaleKeywords.contains(keyword)) {
                possibleMatches.add(AnaphoraTypes.MALE);
            } else if (trackedFemaleKeywords.contains(keyword)) {
                possibleMatches.add(AnaphoraTypes.FEMALE);
            } else if (trackedPluralKeywords.contains(keyword)) {
                possibleMatches.add(AnaphoraTypes.PLURAL);
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
}
