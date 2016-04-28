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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;


public class SentimentTargetsAnnotator implements Annotator {
    public final static String SENTIMENT_TARGETS = "sentimenttargets";
    final Logger logger = LoggerFactory.getLogger(SentimentTargetsAnnotator.class);

    // anaphora resolution
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
    }

    // tracked NER tags
    public Set<String> trackedNerTags = new HashSet<>();
    {
        trackedNerTags.add("PERSON");
        trackedNerTags.add("ORGANIZATION");
        trackedNerTags.add("LOCATION");
        trackedNerTags.add("MISC");
    }

    @Override
    public void annotate(Annotation annotation) {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        // get all targets mapped to the indexes of the sentences they appear in
        Map<Integer, List<SentimentTarget>> targetsPerSentence = getTargets(sentences);

        // compile a list of targets from the map
        List<SentimentTarget> targets = new ArrayList<>();
        targetsPerSentence.values().forEach(targets::addAll);

        // merge the targets referring to the same entity, producing map of full entity name to targets
        Map<String, List<SentimentTarget>> targetsPerEntity = getMergedEntities(targets);

        // attach sentiment to each mention
        for (Integer i : targetsPerSentence.keySet()) {
            List<SentimentTarget> sentenceTargets = targetsPerSentence.get(i);
            CoreMap sentence = sentences.get(i);
            try {
                attachSentiment(sentenceTargets, sentence);
            } catch (InvalidSentimentException e) {
                logger.error("could not attach sentiment to targets " + sentenceTargets + " in sentence: " + sentence);
            }
        }

        // produce a map of full entity name to sentiment by composing the sentiment sores attached in the previous step
        Map<String, Integer> scorePerEntity = new HashMap<>();
        for (String entity : targetsPerEntity.keySet()) {
            List<SentimentTarget> entityTargets = targetsPerEntity.get(entity);
            logger.info("finding composed sentiment for entity: " + entity);
            scorePerEntity.put(entity, getComposedSentiment(entityTargets));
        }

        // the final annotation object now includes each map produced as well as the list of targets
        annotation.set(SentimentTargetsAnnotation.class, targets);
        annotation.set(SentenceSentimentTargetsAnnotation.class, targetsPerSentence);
        annotation.set(MergedSentimentTargetsAnnotation.class, targetsPerEntity);
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
     * Get the composed sentiment a list of mentions of the same entity.
     * @param targets
     */
    private int getComposedSentiment(List<SentimentTarget> targets) {
        double sum = 0.0;
        int n = 0;

        for (SentimentTarget target : targets) {
            if (target.hasSentiment()) {
                sum += target.getSentiment();
                n++;
            } else {
                logger.error("target has no sentiment: " + target);
            }
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

    private void attachSentiment(List<SentimentTarget> targets, CoreMap sentence) throws InvalidSentimentException {
        Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);

        if (targets.size() == 1) {
            SentimentTarget target = targets.get(0);
            int sentimentScore = RNNCoreAnnotations.getPredictedClass(tree);
            target.setSentiment(sentimentScore);
        } else {
            // TODO: implement tree traversal for splitting entity contexts
            logger.info("multiple entities in sentence (" + targets + "), cannot attach sentiment, not implemented yet");

            List<List<Tree>> paths = new ArrayList<>();
            attachPaths(tree, new ArrayList<>(), paths);

        }
    }

    /**
     * Recursively attaches all possible paths in the parse tree to a list of paths.
     * @param tree
     * @param path
     * @param paths
     */
    private void attachPaths(Tree tree, List<Tree> path, List<List<Tree>> paths) {
        path.add(tree);

        if (tree.isLeaf()) {
            paths.add(path);
        } else {
            for (Tree child : tree.children()) {
                attachPaths(child, new ArrayList<>(path), paths);
            }
        }
    }

    /**
     * Returns entities and their anaphora in a list of sentences.
     * @return a list of entities
     */
    private Map<Integer, List<SentimentTarget>> getTargets(List<CoreMap> sentences) {
        Map<Integer, List<SentimentTarget>> targetsPerSentence = new HashMap<>();

        for (int i = 0; i < sentences.size(); i++) {
            CoreMap sentence = sentences.get(i);
            List<SentimentTarget> sentenceTargets = getEntities(sentence, i);

            if (sentenceTargets.isEmpty()) {
                List<SentimentTarget> previousSentenceTargets = targetsPerSentence.getOrDefault(i - 1, new ArrayList<>());
                SentimentTarget anaphor = getAnaphor(previousSentenceTargets, sentence, i);
                sentenceTargets.add(anaphor);
            }

            targetsPerSentence.put(i, sentenceTargets);
        }

        return targetsPerSentence;
    }

    /**
     * Returns the anaphor based on an antecedent found in a previous sentence.
     * @param previousSentenceTargets
     * @param sentence
     * @param i
     * @return
     */
    private SentimentTarget getAnaphor(List<SentimentTarget> previousSentenceTargets, CoreMap sentence, int i) {
        Set<String> foundKeywords = getAnaphoraKeywords(sentence);

        logger.info("finding antecedents for anaphora " + foundKeywords + " using targets " + previousSentenceTargets);
        SentimentTarget antecedent = getAntecedent(foundKeywords, previousSentenceTargets);
        logger.info("antecedent found: " + antecedent);

        if (antecedent != null) {
            SentimentTarget anaphor = antecedent.getAnaphor(foundKeywords, i);
            logger.info("added anaphor: " + anaphor);
            return anaphor;
        }

        return null;
    }

    /**
     * Returns the anaphora keywords found in a sentence.
     * @param sentence
     * @return
     */
    private Set<String> getAnaphoraKeywords(CoreMap sentence) {
        // track keywords for anaphora resolution
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        Set<String> foundKeywords = new HashSet<>();

        for (CoreLabel token : tokens) {
            String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String name = token.get(CoreAnnotations.TextAnnotation.class);

            if (posTag.equals("PRP")) {  // PRP = proper nouns
                logger.info("found candidate anaphora: " + name);
                foundKeywords.add(name.toLowerCase());
            }
        }

        return foundKeywords;
    }

    /**
     * Uses the CoreNLP NER tagger to find relevant entities.
     * @return a list of entities
     */
    private List<SentimentTarget> getEntities(CoreMap sentence, int i) {
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        String previousNerTag = "";  // keep track of previous tag for multi-word entities
        String fullName = "";
        String gender = "";
        List<SentimentTarget> sentenceTargets = new ArrayList<>();
        logger.info("finding targets in sentence: " + tokens);

        // retrieve all entities in sentence
        for (CoreLabel token : tokens) {
            String name = token.get(CoreAnnotations.TextAnnotation.class);
            String nerTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

            // only allow specific tags (e.g. not DATE, DURATION, NUMBER)
            if (nerTag.length() > 1 && trackedNerTags.contains(nerTag)) {

                // keeping track of multi-word entities
                if (nerTag.equals(previousNerTag)) {
                    fullName += " " + name;
                } else {
                    fullName = name;

                    // use only first names for gender
                    if (nerTag.equals("PERSON")) {
                        gender = token.get(MachineReadingAnnotations.GenderAnnotation.class);
                    }
                }

                previousNerTag = nerTag;
            } else {
                if (!fullName.isEmpty()) {
                    SentimentTarget target = new SentimentTarget(fullName, previousNerTag, gender, i); // TODO: refactor to use list of tokens instead
                    sentenceTargets.add(target);
                    logger.info("added target: " + target);
                }

                // make sure to reset for next token
                previousNerTag = "";
                fullName = "";
            }
        }

        return sentenceTargets;
    }

    /**
     * Perform basic anaphora resolution to establish an antecedent for the anaphora of a sentence.
     * The antecedent entity is found among a list of previous entity targets.
     * To simplify things, only a single antecedent can be found in a sentence.
     * @param anaphora
     * @param targets
     * @return
     */
    private SentimentTarget getAntecedent(Set<String> anaphora, List<SentimentTarget> targets) {
        Set<AnaphoraType> anaphoraTypes = getAnaphoraTypes(anaphora);

        // determine whether to perform single or multiple resolution
        if (targets.size() == 1) {
            return getSingleMentionAntecedent(anaphoraTypes, targets.get(0));
        } else if (anaphoraTypes.size() == 1) {
            AnaphoraType anaphoraType = anaphoraTypes.iterator().next();  // shitty Java syntax
            return getSingleTypeAntecedent(anaphoraType, targets);
        } else {
            logger.error("multiple resolution not implemented yet: " + targets + ", " + anaphora);
            return null;  // TODO: implement this
        }
    }

    /**
     * Perform anaphora resolution for a single known entity.
     * @param types
     * @param target
     * @return
     */
    private SentimentTarget getSingleMentionAntecedent(Set<AnaphoraType> types, SentimentTarget target) {
        logger.info("looking for anaphora matching sentiment target: " + target);

        if (target.isPerson()) {
            if (target.hasGender()) {
                if (target.isMale() && types.contains(AnaphoraType.MALE)) {
                    return target;
                } else if (target.isFemale() && types.contains(AnaphoraType.FEMALE)) {
                    return target;
                }
            } else if (types.contains(AnaphoraType.MALE) || types.contains(AnaphoraType.FEMALE)) {
                return target;
            }
        }

        if (target.isOrganization() && types.contains(AnaphoraType.PLURAL)) {
            return target;
        }

        if (target.isLocation() && types.contains(AnaphoraType.PLURAL)) {
            return target;
        }

        logger.info("found no anaphora matching sentiment target");
        return null;
    }

    /**
     * Perform anaphora resolution for a single anaphora type (MALE, FEMALE, or PLURAL).
     * @param type
     * @param targets
     * @return
     */
    private SentimentTarget getSingleTypeAntecedent(AnaphoraType type, List<SentimentTarget> targets) {
        if (type == AnaphoraType.MALE) {
            return getMaleAntecedent(targets);
        } else if (type == AnaphoraType.FEMALE) {
            return getFemaleAntecedent(targets);
        } else {
            return getPluralAntecedent(targets);
        }
    }

    /**
     * Perform anaphora resolution for a male.
     * @param targets
     * @return
     */
    private SentimentTarget getMaleAntecedent(List<SentimentTarget> targets) {
        SentimentTarget candidate = null;
        List<SentimentTarget> candidatesWithoutGender = new ArrayList<>();
        logger.info("looking for male antecedent");

        // will return any PERSON if the targets only include a single, non-gendered PERSON
        // if exactly one MALE candidate is found, will return that instead
        for (SentimentTarget target : targets) {
            if (target.isPerson()) {
                if (target.hasGender()) {
                    if (target.isMale()) {  // do not consider females at all!
                        if (candidate == null) {
                            logger.info("found candidate male antecedent: " + target.getName());
                            candidate = target;
                        } else {
                            logger.info("found multiple candidate male antecedents!");
                            return null;
                        }
                    }
                } else {
                    logger.info("found candidate unknown gender antecedent: " + target.getName());
                    candidatesWithoutGender.add(target);
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
     * @param targets
     * @return
     */
    private SentimentTarget getFemaleAntecedent(List<SentimentTarget> targets) {
        SentimentTarget candidate = null;
        List<SentimentTarget> candidatesWithoutGender = new ArrayList<>();
        logger.info("looking for female antecedent");

        // will return any PERSON if the targets only include a single, non-gendered PERSON
        // if exactly one FEMALE candidate is found, will return that instead
        for (SentimentTarget target : targets) {
            if (target.isPerson()) {
                if (target.hasGender()) {
                    if (target.isFemale()) {  // do not consider males at all!
                        if (candidate == null) {
                            logger.info("found candidate female antecedent: " + target.getName());
                            candidate = target;
                        } else {
                            logger.info("found multiple candidate female antecedents!");
                            return null;
                        }
                    }
                } else {
                    logger.info("found candidate unknown gender antecedent: " + target.getName());
                    candidatesWithoutGender.add(target);
                }
            }
        }

        // if no FEMALE candidate is found, use the candidate without gender
        if (candidatesWithoutGender.size() == 1) {
            candidate = candidatesWithoutGender.get(0);
        }

        return candidate;
    }

    /**
     * Perform anaphora resolution for a non-PERSON.
     * @param targets
     * @return
     */
    private SentimentTarget getPluralAntecedent(List<SentimentTarget> targets) {
        SentimentTarget candidate = null;
        logger.info("looking for plural antecedent");

        for (SentimentTarget target : targets) {
            if (target.isOrganization() || target.isLocation()) {
                if (candidate == null) {
                    logger.info("found candidate plural antecedent: " + target.getName());
                    candidate = target;
                } else {
                    logger.info("found multiple candidate plural antecedents!");
                    return null;
                }
            }
        }

       return candidate;
    }

    /**
     * Used to determine which types of anaphora were found in a sentence.
     * @param anaphora
     * @return
     */
    private Set<AnaphoraType> getAnaphoraTypes(Set<String> anaphora) {
        Set<AnaphoraType> anaphoraTypes = new HashSet<>();

        for (String keyword : anaphora) {
            if (trackedMaleKeywords.contains(keyword)) {
                anaphoraTypes.add(AnaphoraType.MALE);
            } else if (trackedFemaleKeywords.contains(keyword)) {
                anaphoraTypes.add(AnaphoraType.FEMALE);
            } else if (trackedPluralKeywords.contains(keyword)) {
                anaphoraTypes.add(AnaphoraType.PLURAL);
            }
        }

        return anaphoraTypes;
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

     * @param targets
     * @return
     */
    Map<String, List<SentimentTarget>> getMergedEntities(List<SentimentTarget> targets) {
        Map<String, Set<String>> shortToLong = new HashMap<>();

        // reduce to unique set of names
        Set<String> names = new HashSet<>();
        for (SentimentTarget target : targets) {
            if (target.isPerson()) {  // TODO: consider whether it should also merge locations or organisations
                names.add(target.getName());
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
        logger.info("short to long mapping: " + shortToLong.entrySet());

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
        logger.info("purged mapping: " + shortToLong.entrySet());

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
        logger.info("reversed mapping: " + fullToShort.entrySet());

        // creating the final mapping of String --> list of sentiment targets
        Map<String, List<SentimentTarget>> mergedEntities = new HashMap<>();

        for (SentimentTarget target : targets) {
            String entityName = target.getName();
            String targetName = "";

            // if target is either a full name or a short name
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

            // target is neither a full name or a short name (most targets will be like this)
            if (targetName.isEmpty()) {
                targetName = entityName;
            }

            List<SentimentTarget> contexts = mergedEntities.getOrDefault(targetName, new ArrayList<>());
            contexts.add(target);
            mergedEntities.put(targetName, contexts);
        }
        logger.info("merged entities: " + mergedEntities.entrySet());

        return mergedEntities;
    }

    private enum AnaphoraType {
        MALE,
        FEMALE,
        PLURAL
    }
}
