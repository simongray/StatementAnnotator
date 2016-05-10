package sentiment;

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
    public final static String DEFAULT_COMPOSE_STYLE = "polar_mean";  // the default way to compose sentiment
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

    /**
     * This constructor allows for the annotator to accept different properties to alter its behaviour.
     *
     * It doesn't seem to be documented anywhere, but a method in AnnotatorImplementations.java with signature
     *      public Annotator custom(Properties properties, String property) { ... }
     * allows for various constructor signatures to be implemented for a custom annotator.
     * @param properties
     */
    public SentimentTargetsAnnotator(String name, Properties properties) {
        String prefix = (name != null && !name.isEmpty())? name + ".":"";
        String composeStyle = properties.getProperty(prefix + "composestyle", DEFAULT_COMPOSE_STYLE);
        logger.info("sentiment compose style: " + composeStyle);
    }

    @Override
    public void annotate(Annotation annotation)  {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        try {
            // get all targets mapped to the indexes of the sentences they appear in
            Map<Integer, List<SentimentTarget>> targetsPerSentence = null;
            targetsPerSentence = getTargets(sentences);

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

            // the final annotation object now includes each map produced as well as the list of targets
            annotation.set(SentimentTargetsAnnotation.class, targets);
            annotation.set(SentenceSentimentTargetsAnnotation.class, targetsPerSentence);
            annotation.set(MergedSentimentTargetsAnnotation.class, targetsPerEntity);
        } catch (SentimentTargetTokensMissingException e) {
            logger.error("sentiment target tokens were missing, aborting annotation");
            e.printStackTrace();
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

    private void attachSentiment(List<SentimentTarget> targets, CoreMap sentence) throws InvalidSentimentException {
        logger.info("attaching sentiment to targets in sentence: " + sentence);
        Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);

        if (targets.size() == 1) {
            SentimentTarget target = targets.get(0);
            int sentiment = RNNCoreAnnotations.getPredictedClass(tree);
            target.setSentiment(sentiment);
            logger.info(target + " had its sentiment score set to " + sentiment);
        } else if (targets.size() > 1) {
            logger.info("multiple entities in sentence (" + targets + "), finding context for each");

            // get all possible paths from ROOT to every leaf
            List<List<Tree>> paths = new ArrayList<>();
            attachPaths(tree, new ArrayList<>(), paths);

            // reduce to only relevant paths
            removeIrrelevantPaths(targets, paths);

            // remove shared paths (= context) for each sentiment target
            removeSharedSections(paths);

            // set each targets sentiment score based on its own local context
            for (SentimentTarget target : targets) {
                List<Tree> relevantPath = paths.get(target.getTokenIndex() - 1);  // note: CoreNLP token indexes start at 1
                Tree localTree = relevantPath.get(0);
                int sentiment = RNNCoreAnnotations.getPredictedClass(localTree);
                target.setSentiment(sentiment);
                logger.info(target + " had its sentiment score set to " + sentiment);
            }

        }
    }

    /**
     * Removes any shared sections from a list of paths.
     * The paths should come from the same sentiment-annotated parse tree.
     * @param paths
     */
    private void removeSharedSections(List<List<Tree>> paths) {
        // make a deep copy of each relevant List inside paths
        List<List<Tree>> otherPaths = new ArrayList<>();
        for (List<Tree> path : paths) {
            if (!path.isEmpty()) {
                otherPaths.add(new ArrayList<>(path));  // note: this part is shallow
            }
        }

        // use the copy to locate shared sections
        for (List<Tree> path : paths) {
            if (!path.isEmpty()) {
                int n = -1;  // = nothing should be removed

                for (List<Tree> otherPath : otherPaths) {
                    if (!otherPath.isEmpty()) {
                        if (path.get(path.size() - 1) != otherPath.get(otherPath.size() - 1)) {  // should not be same path
                            for (int i = 0; i < path.size() && i < otherPath.size(); i++) {

                                // find lowest common denominator
                                if (path.get(i) == otherPath.get(i)) {
                                    n = i;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns the parse tree paths relevant to the stated sentiment targets.
     * @param targets
     * @param paths
     * @return
     */
    private void removeIrrelevantPaths(List<SentimentTarget> targets, List<List<Tree>> paths) {
        Set<Integer> relevantPathIndexes = new HashSet<>();

        for (SentimentTarget target : targets) {
            relevantPathIndexes.add(target.getTokenIndex() - 1);  // note: CoreNLP token indexes start at 1
        }

        for (int i = 0; i < paths.size(); i++) {
            if (!relevantPathIndexes.contains(i)) {
                paths.set(i, new ArrayList<>());
            }
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
    private Map<Integer, List<SentimentTarget>> getTargets(List<CoreMap> sentences) throws SentimentTargetTokensMissingException {
        Map<Integer, List<SentimentTarget>> targetsPerSentence = new HashMap<>();

        for (int i = 0; i < sentences.size(); i++) {
            CoreMap sentence = sentences.get(i);
            List<SentimentTarget> sentenceTargets = getEntities(sentence);

            if (sentenceTargets.isEmpty()) {
                List<SentimentTarget> previousSentenceTargets = targetsPerSentence.getOrDefault(i - 1, new ArrayList<>());
                SentimentTarget anaphor = getAnaphor(previousSentenceTargets, sentence);
                if (anaphor != null) sentenceTargets.add(anaphor);
            }

            targetsPerSentence.put(i, sentenceTargets);
        }

        return targetsPerSentence;
    }

    /**
     * Returns the anaphor based on an antecedent found in a previous sentence.
     * @param previousSentenceTargets
     * @param sentence
     * @return
     */
    private SentimentTarget getAnaphor(List<SentimentTarget> previousSentenceTargets, CoreMap sentence) throws SentimentTargetTokensMissingException {
        List<CoreLabel> anaphoraTokens = getAnaphoraTokens(sentence);

        logger.info("finding antecedents for anaphora " + anaphoraTokens + " using targets " + previousSentenceTargets);
        SentimentTarget antecedent = getAntecedent(anaphoraTokens, previousSentenceTargets);
        logger.info("antecedent found: " + antecedent);

        if (antecedent != null) {
            SentimentTarget anaphor = antecedent.getAnaphor(anaphoraTokens);
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
    private List<CoreLabel> getAnaphoraTokens(CoreMap sentence) {
        // track keywords for anaphora resolution
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        List<CoreLabel> anaphoraTokens = new ArrayList<>();

        for (CoreLabel token : tokens) {
            String posTag = token.get(CoreAnnotations.PartOfSpeechAnnotation.class);
            String name = token.get(CoreAnnotations.TextAnnotation.class);

            if (posTag.equals("PRP")) {  // PRP = proper nouns
                logger.info("found candidate anaphora: " + name);
                anaphoraTokens.add(token);
            }
        }

        return anaphoraTokens;
    }

    /**
     * Uses the CoreNLP NER tagger to find relevant entities in a sentence.
     * @return a list of entities
     */
    private List<SentimentTarget> getEntities(CoreMap sentence) throws SentimentTargetTokensMissingException {
        List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
        List<CoreLabel> entityTokenBuffer = new ArrayList<>();
        List<SentimentTarget> entities = new ArrayList<>();
        logger.info("finding entities in sentence: " + sentence);

        for (CoreLabel token : tokens) {
            String nerTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

            if (trackedNerTags.contains(nerTag)) {
                if (entityTokenBuffer.isEmpty()) {
                    entityTokenBuffer.add(token);
                } else {
                    CoreLabel previousToken = entityTokenBuffer.get(entityTokenBuffer.size()-1);
                    String previousNerTag = previousToken.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                    // flush the buffer when encountering another entity
                    if (!nerTag.equals(previousNerTag)) {
                        SentimentTarget entity = new SentimentTarget(entityTokenBuffer);
                        logger.info("found new entity: " + entity);
                        entities.add(entity);
                        entityTokenBuffer = new ArrayList<>();
                    }

                    entityTokenBuffer.add(token);
                }
            } else {
                // flush buffer when encountering a non-entity
                if (!entityTokenBuffer.isEmpty()) {
                    SentimentTarget entity = new SentimentTarget(entityTokenBuffer);
                    logger.info("found new entity: " + entity);
                    entities.add(entity);
                    entityTokenBuffer = new ArrayList<>();
                }
            }
        }

        // make sure to flush any remaining tokens in the buffer, too
        if (!entityTokenBuffer.isEmpty()) {
            SentimentTarget entity = new SentimentTarget(entityTokenBuffer);
            logger.info("found new entity: " + entity);
            entities.add(entity);
        }

        return entities;
    }

    /**
     * Perform basic anaphora resolution to establish an antecedent for the anaphora of a sentence.
     * The antecedent entity is found among a list of previous entity targets.
     * To simplify things, only a single antecedent can be found in a sentence.
     * @param anaphoraTokens
     * @param entities
     * @return
     */
    private SentimentTarget getAntecedent(List<CoreLabel> anaphoraTokens, List<SentimentTarget> entities) {
        Set<AnaphoraType> anaphoraTypes = getAnaphoraTypes(anaphoraTokens);

        // determine whether to perform single or multiple resolution
        if (entities.size() == 1) {
            return getSingleMentionAntecedent(anaphoraTypes, entities.get(0));
        } else if (anaphoraTypes.size() == 1) {
            AnaphoraType anaphoraType = anaphoraTypes.iterator().next();  // shitty Java syntax
            return getSingleTypeAntecedent(anaphoraType, entities);
        } else {
            logger.error("multiple resolution not implemented yet: " + entities + ", " + anaphoraTokens);
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
     * @param anaphoraTokens
     * @return
     */
    private Set<AnaphoraType> getAnaphoraTypes(List<CoreLabel> anaphoraTokens) {
        Set<AnaphoraType> anaphoraTypes = new HashSet<>();

        for (CoreLabel token : anaphoraTokens) {
            String keyword = token.word().toLowerCase();

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
