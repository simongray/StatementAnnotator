package sentiment;

import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
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
        Map<String, Set<Integer>> rawEntities = getRawEntities();

        System.out.println(rawEntities);


        // (requires NER with anaphora resolution)


        // determine interestingness of entities and purge uninteresting ones
        // (perhaps simply reducing to PERSON and ORGANIZATION classes?)

        // determine entities position in sentiment tree
        // (only relevant in case there are multiple entities in the text)

        // find conflicts between entities by moving up tree to see where each entity meets
        // use heuristic(s) to select dominant entity
        // will need to look at different trees to determine best splitting heuristic
        // (or implement something like the method in "Entity-Specific Sentiment Classification of Yahoo News Comments")

//        annotation.set(SENTIMENT_TARGET_ANNOTATION_CLASS, Arrays.asList(new SentimentTarget(2), new SentimentTarget(0)));

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
    private Map<String, Set<Integer>> getRawEntities() {
        Map<String, Set<Integer>> entities = new HashMap<>();

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
                    // update mentions when new entity has been found
                    if (fullEntityName.length() > 0) {
                        Set<Integer> mentions = entities.get(fullEntityName);

                        // create set if not already created
                        if (mentions == null) {
                            mentions = new HashSet<>();
                        }

                        mentions.add(i);
                        entities.put(fullEntityName, mentions);
                    }

                    // make sure to reset for next token
                    previousNerTag = "";
                    fullEntityName = "";
                }
            }
        }

        return entities;
    }

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
