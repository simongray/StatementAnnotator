package sentiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.trees.Tree;
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
    private List<CoreMap> sentences;

    @Override
    public void annotate(Annotation annotation) {
        sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);

        // locate candidate named entities in text (e.g. a comment)
        Set<SentimentTarget> rawTargets = getRawTargets();

        // (requires NER with anaphora resolution)


        // determine interestingness of entities and purge uninteresting ones
        // (perhaps simply reducing to PERSON and ORGANIZATION classes?)

        // determine entities position in sentiment tree
        // (only relevant in case there are multiple entities in the text)

        // find conflicts between entities by moving up tree to see where each entity meets
        // use heuristic(s) to select dominant entity
        // will need to look at different trees to determine best splitting heuristic
        // (or implement something like the method in "Entity-Specific Sentiment Classification of Yahoo News Comments")

        annotation.set(SENTIMENT_TARGET_ANNOTATION_CLASS, rawTargets);  // TODO: set to final targets var

        // using sentence annotation, perhaps another annotation type is better suited (there are A LOT)
//        List<CoreMap> sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
//
//        // handle each sentence
//        for(CoreMap sentence: sentences) {
//            // what kind of annotations are available
//            System.out.println(sentence.keySet());
//
//            // TokensAnnotation are available for example
//            List<CoreLabel> tokens = sentence.get(CoreAnnotations.TokensAnnotation.class);
//            for (CoreLabel token : tokens) {
//                System.out.println(token + ", " + token.ner());  // actually includes NER in this case!! (days = DURATION)
//            }
//
//            // seems like it's only necessary to parse tree if there is a conflict of entities (i.e. two or more entities in a sentence)
//
//            Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);
//            System.out.println("the overall sentiment rating is " + sentence.get(SentimentCoreAnnotations.SentimentClass.class));
//            parse(tree, 0);
//        }
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
     * The CoreNLP NER tagger finds relevant entities.
     * @return a map of entities and mention indexes
     */
    private Set<SentimentTarget> getRawTargets() {
        Set<SentimentTarget> targets = new HashSet<>();

        for (int i = 0; i < sentences.size(); i++) {
            List<CoreLabel> tokens = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class);
            String previousNerTag = "";  // keep track of previous tag for multi-word entities
            String fullEntityName = "";

            // retrieve all entities in sentence
            for (CoreLabel token : tokens) {
                String nerTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                String entityName = token.get(CoreAnnotations.TextAnnotation.class);

                // keeping track of multi-word entities as well
                // only allow specific tags (e.g. not DURATION or MISC)
                if (nerTag.length() > 1 && nerTag.equals("PERSON") || nerTag.equals("ORGANIZATION") || nerTag.equals("LOCATION")) {
                    if (nerTag.equals(previousNerTag)) {
                        fullEntityName += " " + entityName;
                    } else {
                        fullEntityName = entityName;
                    }
                    previousNerTag = nerTag;
                } else {
                    // update mentions in target when new entity has been found
                    if (fullEntityName.length() > 0) {
                        SentimentTarget target = null;

                        for (SentimentTarget existingTarget : targets) {
                            if (existingTarget.getName().equals(fullEntityName)) {
                                target = existingTarget;
                                break;
                            }
                        }

                        // create new target if no existing target was found
                        if (target == null) {
                            target = new SentimentTarget(fullEntityName, previousNerTag);
                            targets.add(target);
                        }

                        // TODO: figure out whether it makes sense to also have token index in mention
                        target.addMention(new SentimentTargetMention(fullEntityName, i));
                    }

                    // make sure to reset for next token
                    previousNerTag = "";
                    fullEntityName = "";
                }
            }
        }

        return targets;
    }

    /**
     * Algorithm to merge entities if the names are found to be shorter versions of the full name.
     * @param targets
     * @return
     */
    Set<SentimentTarget> mergePersons(Set<SentimentTarget> targets) {
        Map<String, String> prefixes = new HashMap<>();
        Map<String, String> postfixes = new HashMap<>();

        // find short names
        for (SentimentTarget target : targets) {
            if (target.getType() == "PERSON") {
                String candidateShortName = target.getName();
                for (SentimentTarget otherTarget : targets) {
                    if (otherTarget.getName() == "PERSON" && !otherTarget.equals(candidateShortName)) {
                        String candidateFullName = otherTarget.getName();

                        if (candidateFullName.startsWith(candidateShortName)) {
                            if (prefixes.containsKey(candidateShortName)) {
                                prefixes.put(candidateShortName, null); // signals conflict
                            } else {
                                prefixes.put(candidateShortName, candidateFullName);
                            }
                        } else if (candidateFullName.endsWith(candidateShortName)) {
                            if (postfixes.containsKey(candidateShortName)) {
                                postfixes.put(candidateShortName, null);  // signals conflict
                            } else {
                                postfixes.put(candidateShortName, candidateFullName);
                            }
                        }
                    }
                }
            }
        }

        // cannot be both pre- and postfix
        for (String prefix : prefixes.keySet()) {
            if (postfixes.containsKey(prefix)) {
                prefixes.remove(prefix);
                postfixes.remove(prefix);
            }
        }

        // reverse the mappings to get (full name, short names) pairs
        Map<String, Set<String>> mergeMap = new HashMap<>();

        for (String prefix : prefixes.keySet()) {
            String fullName = prefixes.get(prefix);

            if (mergeMap.containsKey(fullName)) {
                mergeMap.get(fullName).add(prefix);
            } else {
                Set<String> shortNames = new HashSet<>();
                shortNames.add(prefix);
                mergeMap.put(fullName, shortNames);
            }
        }

        for (String postfix : prefixes.keySet()) {
            String fullName = prefixes.get(postfix);

            if (mergeMap.containsKey(fullName)) {
                mergeMap.get(fullName).add(postfix);
            } else {
                Set<String> shortNames = new HashSet<>();
                shortNames.add(postfix);
                mergeMap.put(fullName, shortNames);
            }
        }

        Set<SentimentTarget> newTargets = new HashSet<>();

        for (SentimentTarget target : targets) {
            if ()
        }


        return null;
    }

//    /**
//     * Utility function using some heuristics to reduce the number of targets (short names combined with long names).
//     * @param rawTargets the raw targets produced by getRawTargets(...)
//     * @return reduced targets
//     */
//    private Map<String, SentimentTarget> getReducedTargets(Map<String, SentimentTarget> rawTargets) {
//        List<SentimentTarget> names = rawTargets.
//
//
//    }

    // recursively display scores in tree
    public static void parse(Tree tree, int n) {
        // RNNCoreAnnotations.getPredictedClass(tree) returns the sentiment analysis score from 0 to 4 (with -1 for n/a)
        System.out.println(new String(new char[n]).replace("\0", " ")
                + tree.value()
        );

        for (CoreLabel label : tree.taggedLabeledYield()) {
            System.out.println("-----");
            System.out.print("toString= " + label);
            System.out.println();
        }

        // scores (not confirmed, but 99% sure)
        // very negative: 0
        // negative: 1
        // neutral: 2
        // positive: 3
        // very positive: 4

        for (Tree child : tree.children()) {
            parse(child, n+1);
        }
    }
}
