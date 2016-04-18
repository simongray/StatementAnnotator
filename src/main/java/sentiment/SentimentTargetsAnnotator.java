package sentiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;

/*
    (excerpt from "Entity-Specific Sentiment Classification of Yahoo News Comments")

    The context of an entity contains the words,
    phrases or sen- tences that refer to the entity.
    We use several heuristics to extract the contexts.
    Following are the three main modules of our
    context extraction algorithm:

    1. Preprocessing, where the number of entities in
    a com- ment is checked. For single entity
    comments, the entire com- ment is taken as the
    context for the entity. If a comment con- tains
    multiple entities, it is segmented into sentences
    and is given as input to the anaphora resolution
    module.

    2. Anaphora Resolution: We use a rule based
    approach to anaphora resolution. We check the type
    of entity: PER- SON (P) vs. NON-PERSON (NP) and
    assign sentences to the context of the entity if
    they have explicit mentions of that entity or
    compatible anaphoric references. For exam- ple,
    pronouns such as he, she, her, him can only be
    used to refer to a P entity, whereas they, their,
    them can be used to refer to both P and NP
    entities and it can only be used for NP entities.
    If a sentence does not have references to any
    entity, then it is added to the context of all the
    entities. Also, if a sentence has explicit
    mentions of multiple entities, then it is given as
    input to the local context extraction module.

    3. Local Context Extraction: If entities occur in
    clauses that are connected with “but” (in the
    sentence), then the re- spective clauses are
    returned as local contexts for the enti- ties. If
    the sentence contains a comparison between
    entities, then it is split at the comparative term
    (adjective or adverb), with the comparative term
    added to the left part, and the two parts are
    returned as local contexts for the respective
    enti- ties. If none of the two conditions is
    satisfied, then a window of ±3 tokens around
    entities is taken as their local context.
    Identifying the Sentiment of Contexts

    After obtaining the contexts of entities, we
    classify their sen- timent into positive, negative
    or neutral sentiment classes. We model the task of
    identifying sentiment as two step clas-
    sification. In the first step, we classify the
    context of an en- tity into polar versus neutral
    sentiment classes. Next, we classify the polar
    entities into positive or negative sentiment
    classes. Next, we describe the features used in
    our classifi- cation models and our reasoning
    behind using them. Neutral vs. Polar
    Classification As already discussed, comments
    posted on news sites contain entities that are
    irrel- evant with respect to sentiment analysis
    (see Example 1 in Section ). These entities have
    no sentiment associated with

    them and are filtered out before conducting
    sentiment clas- sification of comments. We address
    this problem by classi- fying entities as polar
    vs. neutral. Irrelevant entities are clas- sified
    as neutral.
 */

public class SentimentTargetsAnnotator implements Annotator {
    public final static String SENTIMENT_TARGETS = "sentimenttargets";
    public final static Class SENTIMENT_TARGET_ANNOTATION_CLASS = SentimentTargetsAnnotation.class;

    @Override
    public void annotate(Annotation annotation) {
        List<SentimentTarget> mentions = getInitialEntities(annotation);

        for (SentimentTarget mention : mentions) {
            System.out.println(mention);
        }

        Map<String, List<SentimentTarget>> targets = mergeEntities(mentions);

        System.out.println("final mapping:");
        for (String key : targets.keySet()) {
            System.out.println(key + " -----> " + targets.get(key));
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
     * Uses the CoreNLP NER tagger to find relevant entities.
     * @return a list of entities
     */
    private List<SentimentTarget> getInitialEntities(Annotation annotation) {
        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
        List<SentimentTarget> mentions = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            List<CoreLabel> tokens = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class);
            String previousTag = "";  // keep track of previous tag for multi-word entities
            String fullName = "";

            // retrieve all entities in sentence, keeping track of multi-word entities
            for (CoreLabel token : tokens) {
                String tag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                String name = token.get(CoreAnnotations.TextAnnotation.class);
                System.out.println(tag + " " + name);

                // only allow specific tags (e.g. not DATE, DURATION, NUMBER)
                if (tag.length() > 1 && tag.equals("PERSON") || tag.equals("ORGANIZATION") || tag.equals("LOCATION") || tag.equals("MISC")) {
                    if (tag.equals(previousTag)) {
                        fullName += " " + name;
                    } else {
                        fullName = name;
                    }
                    previousTag = tag;
                } else {
                    if (fullName.length() > 0) {
                        // TODO: figure out whether it makes sense to also have token index in mention
                        mentions.add(new SentimentTarget(fullName, previousTag, i));
                    }

                    // make sure to reset for next token
                    previousTag = "";
                    fullName = "";
                }
            }
        }

        return mentions;
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
    Map<String, List<SentimentTarget>> mergeEntities(List<SentimentTarget> mentions) {
        Map<String, Set<String>> shortToLong = new HashMap<>();

        // reduce to unique set of names
        Set<String> names = new HashSet<>();
        for (SentimentTarget mention : mentions) {
            names.add(mention.getName());
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
        for (String name : fullToShort.keySet()) {
            mergedEntities.put(name, new ArrayList<>());
        }

        // creating the final mapping of String --> list of sentiment targets
        for (SentimentTarget mention : mentions) {
            String entityName = mention.getName();

            if (mergedEntities.containsKey(entityName)) {
                List<SentimentTarget> contexts = mergedEntities.get(entityName);
                contexts.add(mention);
                mergedEntities.put(entityName, contexts);
            } else {
                for (String fullName : fullToShort.keySet()) {
                    Set<String> shortNames = fullToShort.get(fullName);
                    if (shortNames.contains(entityName)) {
                        List<SentimentTarget> contexts = mergedEntities.get(fullName);
                        contexts.add(mention);
                        mergedEntities.put(entityName, contexts);
                    }
                }
            }
        }

        // todo: replace print with log

        return mergedEntities;
    }
}
