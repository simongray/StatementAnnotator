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
    private List<CoreMap> sentences;

    @Override
    public void annotate(Annotation annotation) {
        sentences = annotation.get(CoreAnnotations.SentencesAnnotation.class);
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
    private List<SentimentTargetMention> getInitalMentions() {
        List<SentimentTargetMention> mentions = new ArrayList<>();

        for (int i = 0; i < sentences.size(); i++) {
            List<CoreLabel> tokens = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class);
            String previousTag = "";  // keep track of previous tag for multi-word entities
            String fullName = "";

            // retrieve all entities in sentence, keeping track of multi-word entities
            for (CoreLabel token : tokens) {
                String tag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);
                String name = token.get(CoreAnnotations.TextAnnotation.class);

                // only allow specific tags (e.g. not DURATION or MISC)
                if (tag.length() > 1 && tag.equals("PERSON") || tag.equals("ORGANIZATION") || tag.equals("LOCATION")) {
                    if (tag.equals(previousTag)) {
                        fullName += " " + name;
                    } else {
                        fullName = name;
                    }
                    previousTag = tag;
                } else {
                    if (fullName.length() > 0) {
                        // TODO: figure out whether it makes sense to also have token index in mention
                        mentions.add(new SentimentTargetMention(fullName, tag, i));
                    }

                    // make sure to reset for next token
                    previousTag = "";
                    fullName = "";
                }
            }
        }

        return mentions;
    }

    /**
     * Algorithm to merge entities if the names are found to be shorter versions of the full name.
     * @param mentions
     * @return
     */
    List<SentimentTarget> mergeEntities(List<SentimentTargetMention> mentions) {
        Map<String, List<String>> nameMappings = new HashMap<>();

        // create map of short names to long names
        for (SentimentTargetMention mention : mentions) {
            for (SentimentTargetMention otherMention : mentions) {
                if (!mention.equals(otherMention)) {
                    String name = mention.getName();  // candidate short name
                    String otherName = otherMention.getName();  // candidate long name

                    if (otherName.startsWith(name) || otherName.endsWith(name)) {
                        List<String> names = nameMappings.getOrDefault(name, new ArrayList<>());
                        names.add(otherName);
                        nameMappings.put(name, names);
                    }
                }
            }
        }

        // purge double references and remove conflicts
        for (String shortName : nameMappings.keySet()) {
            List<String> longNames = nameMappings.get(shortName);

            if (longNames.size() > 1) {
                String survivingName = "";

                for (String longName : longNames) {
                    if (nameMappings.containsKey(longName)) {
                        nameMappings.put(longName, new ArrayList<>());  // purge double reference
                    } else if (survivingName.isEmpty()) {
                        survivingName = longName;
                    } else {
                        nameMappings.put(shortName, new ArrayList<>());  // remove conflict
                        break;
                    }
                }
            }
        }

        // populate list of sentiment targets
        for (SentimentTargetMention mention : mentions) {
//            if (nameMappings.get()) // TODO: finish this
        }

        return null;
    }
}
