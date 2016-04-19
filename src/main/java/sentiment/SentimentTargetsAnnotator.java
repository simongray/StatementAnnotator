package sentiment;

import edu.stanford.nlp.ie.machinereading.structure.MachineReadingAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.Annotator;
import edu.stanford.nlp.util.CoreMap;

import java.util.*;


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

        // for anaphora resolution
        Map<String, List<SentimentTarget>> anaphoras = new HashMap<>();  // TODO: figure out if this should be called anaphoras
        Set<String> foundKeywords = new HashSet<>();
        Set<String> trackedKeywords = new HashSet<>();
        trackedKeywords.add("he");
        trackedKeywords.add("him");
        trackedKeywords.add("his");
        trackedKeywords.add("she");
        trackedKeywords.add("her");
        trackedKeywords.add("hers");
        trackedKeywords.add("they");
        trackedKeywords.add("them");
        trackedKeywords.add("their");
        trackedKeywords.add("theirs");
        String lastMale = "";
        String lastFemale = "";
        String lastPerson = "";
        String lastOrganization = "";
        String lastLocation = "";
        boolean foundEntity;

        for (int i = 0; i < sentences.size(); i++) {
            List<CoreLabel> tokens = sentences.get(i).get(CoreAnnotations.TokensAnnotation.class);
            String previousTag = "";  // keep track of previous tag for multi-word entities
            String fullName = "";
            String gender = "";
            foundEntity = false;

            // retrieve all entities in sentence
            for (CoreLabel token : tokens) {
                String name = token.get(CoreAnnotations.TextAnnotation.class);
                String nerTag = token.get(CoreAnnotations.NamedEntityTagAnnotation.class);

                // only allow specific tags (e.g. not DATE, DURATION, NUMBER)
                if (nerTag.length() > 1 && nerTag.equals("PERSON") || nerTag.equals("ORGANIZATION") || nerTag.equals("LOCATION") || nerTag.equals("MISC")) {
                    System.out.println(nerTag + " " + name);

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
                        mentions.add(new SentimentTarget(fullName, previousTag, gender, i));
                        foundEntity = true;
                        System.out.println(fullName + ": " + gender);
                        // TODO: figure out whether it makes sense to also have token index in mention

                        if (previousTag.equals("PERSON")) {
                            if (gender != null && gender.equals("MALE")) {
                                lastMale = fullName;
                            } else if (gender != null && gender.equals("FEMALE")) {
                                lastFemale = fullName;
                            } else {
                                lastPerson = fullName;
                            }
                        } else if (previousTag.equals("ORGANIZATION")) {
                            lastOrganization = fullName;
                        } else if (previousTag.equals("LOCATION")) {
                            lastLocation = fullName;
                        }
                    }

                    // track keywords for anaphora resolution
                    if (!foundEntity) {
                        if (trackedKeywords.contains(name.toLowerCase())) {
                            foundKeywords.add(name);  // TODO: apparently not working, figure out why
                        }
                    }

                    // make sure to reset for next token
                    previousTag = "";
                    fullName = "";
                }
            }

            if (!foundEntity) {
                System.out.println("!!!!NO ENTITY FOUND!!!! Assigning to previous entity...");
                System.out.println(lastMale + ", " + lastFemale + ", " + lastPerson + ", " + lastOrganization + ", " + lastLocation);
                // TODO: perform anaphora resolution
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
}
