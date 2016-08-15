package statements.profile;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statements.core.*;
import statements.patterns.*;

import java.io.IOException;
import java.text.DecimalFormat;
import java.util.*;

public class Profile {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Set<Statement> statements;
    private Set<Statement> interestingStatements = new HashSet<>();

    // entities found in statements using pattern matching
    private Set<String> locations = new HashSet<>();
    private Set<String> possessions = new HashSet<>();
    private Set<String> studies = new HashSet<>();
    private Set<String> work = new HashSet<>();
    private Set<String> identities = new HashSet<>();
    private Set<String> properNouns = new HashSet<>();
    private Set<String> likes = new HashSet<>();
    private Set<String> wants = new HashSet<>();
    private Set<String> feelings = new HashSet<>();
    private Map<String, Set<String>> activities = new HashMap<>();

    private Map<Statement, Integer> qualityPointsMap = new HashMap<>();
    private Map<Statement, Double> qualityMap = new HashMap<>();

    private static DecimalFormat df = new DecimalFormat("#.##");

    /**
     * This set of nouns comprises second and third person pronouns, but not first person.
     * It also adds other words that do not carry any information without context.
     */
    public static final Set<String> UNINTERESTING_NOUNS = new HashSet<>();
    static {
        UNINTERESTING_NOUNS.add("yours");
        UNINTERESTING_NOUNS.add("he");
        UNINTERESTING_NOUNS.add("she");
        UNINTERESTING_NOUNS.add("it");
        UNINTERESTING_NOUNS.add("they");
        UNINTERESTING_NOUNS.add("him");
        UNINTERESTING_NOUNS.add("his");
        UNINTERESTING_NOUNS.add("her");
        UNINTERESTING_NOUNS.add("them");
        UNINTERESTING_NOUNS.add("this");
        UNINTERESTING_NOUNS.add("that");
        UNINTERESTING_NOUNS.add("these");
        UNINTERESTING_NOUNS.add("those");
        UNINTERESTING_NOUNS.add("their");
        UNINTERESTING_NOUNS.add("theirs");
        UNINTERESTING_NOUNS.add("here");
        UNINTERESTING_NOUNS.add("there");
        UNINTERESTING_NOUNS.add("who");
        UNINTERESTING_NOUNS.add("what");
        UNINTERESTING_NOUNS.add("which");
        UNINTERESTING_NOUNS.add("all");
        UNINTERESTING_NOUNS.add("thing");
        UNINTERESTING_NOUNS.add("one");
        UNINTERESTING_NOUNS.add("some");
        UNINTERESTING_NOUNS.add("someone");
        UNINTERESTING_NOUNS.add("here");
        UNINTERESTING_NOUNS.add("there");
    }

    /**
     * Matches statements that are deemed interesting (or uninteresting for the anti-patterns).
     * Used to limit statements for further processing based on a couple of heuristics.
     */
    private final StatementPattern EMBEDDED_INTERESTING_PATTERN = new StatementPattern(
            new VerbPattern().negated(null),
            new NonVerbPattern().person(Person.first, Person.third).local(false).notWords(UNINTERESTING_NOUNS).all()
    ).optional().minSize(2);

    private final StatementPattern INTERESTING_PATTERN = new StatementPattern(
            new SubjectPattern(),
            new VerbPattern().negated(null),
            new NonVerbPattern().person(Person.first, Person.third).local(false).notWords(UNINTERESTING_NOUNS).all(),
            EMBEDDED_INTERESTING_PATTERN  // for embedded statements
    ).question(false).minSize(3);

    private final StatementPattern INTERESTING_ANTIPATTERN_1 = new StatementPattern(
            new VerbPattern().copula().negated(null)
    ).size(2);

    /**
     * Matches statements that came from a question.
     */
    private final StatementPattern CITATION_ANTIPATTERN = new StatementPattern().citation();

    /**
     * Matches anything that is personal in nature, i.e. referring to first person or first person possessions.
     */
    private final MultiPattern PERSONAL_PATTERN = new MultiPattern(
            new NonVerbPattern().firstPerson(),
            new NonVerbPattern().noun().firstPersonPossessive()
    );

    // inspired by paper
    private final StatementPattern ADVERB_ADJECTIVE_PATTERN = new StatementPattern(
            new NonVerbPattern().compoundTags(Tag.adverb, Tag.adjective)
    );

    /**
     * Captures likes/loves (and wants in the second case).
     */
    private final StatementPattern EMBEDDED_ACTIVITY_PATTERN = new StatementPattern(
            new VerbPattern(),
            new DirectObjectPattern(),
            new NonVerbPattern().person(Person.first, Person.third).local(false).notWords(UNINTERESTING_NOUNS).all()
    ).capture().optional();

    private final StatementPattern LIKE_PATTERN_1 = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words("like", "love", "prefer"),
            new DirectObjectPattern().notWords(UNINTERESTING_NOUNS).capture().optional(),
            EMBEDDED_ACTIVITY_PATTERN
    );

    private final StatementPattern LIKE_PATTERN_2 = new StatementPattern(
            new SubjectPattern().thirdPerson().notWords(UNINTERESTING_NOUNS).capture(),
            new VerbPattern().copula(),
            new DirectObjectPattern().adjective().words(Common.POSITIVE_ADJECTIVE).description(false)
    );

    private final StatementPattern WANT_PATTERN = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words("want"),
            new DirectObjectPattern().notWords(UNINTERESTING_NOUNS).capture().optional(),
            EMBEDDED_ACTIVITY_PATTERN
    );

    /**
     * Captures feelings.
     */
    private final StatementPattern FEEL_PATTERN = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words("feel"),
            new DirectObjectPattern().capture()
    );

    /**
     * Captures proper nouns.
     */
    private final StatementPattern PROPER_NOUN_PATTERN = new StatementPattern(
            new NonVerbPattern().properNoun().notWords(UNINTERESTING_NOUNS).capture()
    );

    /**
     * Captures objects that indicate the occupation of the author
     */
    private final StatementPattern STUDY_PATTERN = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words("study"),
            new ObjectPattern().capture().optional().notWords(UNINTERESTING_NOUNS)
    );

    private final StatementPattern WORK_PATTERN = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words("work"),
            new IndirectObjectPattern().capture().optional().notWords(UNINTERESTING_NOUNS)
    );

    private final StatementPattern IDENTITY_PATTERN_1 = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().copula(),
            new DirectObjectPattern().partsOfSpeech(Tag.noun, Tag.properNoun).capture().optional().notWords(UNINTERESTING_NOUNS)
    );

    private final StatementPattern IDENTITY_PATTERN_2 = new StatementPattern(
            new StatementPattern(
                    new IndirectObjectPattern().preposition("as").capture()
            )
    );

    /**
     * Captures objects that indicate the location of the author.
     */
    private final StatementPattern LOCATION_PATTERN = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words(Common.LOCATION_VERB),
            new ObjectPattern().preposition(Common.LOCATION_PREPOSITION).partsOfSpeech(Tag.noun, Tag.properNoun).capture()
    );

    /**
     * Captures objects that indicate the possessions of the author.
     */
    private final StatementPattern POSSESSION_PATTERN_1 = new StatementPattern(
            new NonVerbPattern().noun().firstPersonPossessive().capture()
    );

    private final StatementPattern POSSESSION_PATTERN_2 = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words("have", "own", "possess"),
            new DirectObjectPattern().noun().capture().notWords(UNINTERESTING_NOUNS)
    );

    /**
     * Matches statements that indicate the opinion of the author.
     */
    private final StatementPattern OPINION_PATTERN_1 = new StatementPattern(
            new SubjectPattern().firstPerson(),
            new VerbPattern().words(Common.OPINION_VERB),
            new StatementPattern().capture()
    );

    // Note: use the non-captures!
    private final StatementPattern OPINION_PATTERN_2 = new StatementPattern(
            new ObjectPattern().firstPersonPossessive().words(Common.OPINION_NOUN).preposition().capture()
    );

    public Profile(Set<Statement> statements) throws IOException {
        this.statements = statements;

        // citations do not represent the user's own opinions
        int statementCount = statements.size();
        statements.removeIf(CITATION_ANTIPATTERN::matches);
        logger.info("removed citations: " + (statementCount - statements.size()));

        // unpack embedded statements according to a pattern
        // the original statements are replaced with the embedded statements based on the pattern
        unpackEmbeddedStatements();

        // various quality adjustments that do not capture entities
        for (Statement statement : getInterestingStatements()) {
            if (PERSONAL_PATTERN.matches(statement)) {
                addQualityPoint(statement);
            }
            if (ADVERB_ADJECTIVE_PATTERN.matches(statement)) {
                addQualityPoint(statement);
            }
        }

        // find locations that the author has been to
        registerLocations();

        // find possessions of the author
        registerPossessions();

        // find occupations of the author
        registerOccupations();

        // find pronouns mentioned by the author
        registerProperNouns();

        // find emotional content by the author
        registerEmotionalContent();
    }

    /**
     * Find entities that have something in common between this profile and another one.
     * Entities are identified by their common relationship to the author.
     *
     * @param otherProfile the other profile to search in
     * @return the commonalities
     */
    public Set<String> getCommonEntities(Profile otherProfile) {
        Set<String> commonLocations = new HashSet<>(otherProfile.getLocations());
        commonLocations.retainAll(getLocations());

        Set<String> commonPossessions = new HashSet<>(otherProfile.getPossessions());
        commonPossessions.retainAll(getPossessions());

        Set<String> commonStudies = new HashSet<>(otherProfile.getStudies());
        commonStudies.retainAll(getStudies());

        Set<String> commonWork = new HashSet<>(otherProfile.getWork());
        commonWork.retainAll(getWork());

        Set<String> commonIdentities = new HashSet<>(otherProfile.getIdentities());
        commonIdentities.retainAll(getIdentities());

        Set<String> commonProperNouns = new HashSet<>(otherProfile.getProperNouns());
        commonProperNouns.retainAll(getProperNouns());

        Set<String> commonLikes = new HashSet<>(otherProfile.getLikes());
        commonLikes.retainAll(getLikes());

        Set<String> commonWants = new HashSet<>(otherProfile.getWants());
        commonWants.retainAll(getWants());

        Set<String> commonFeelings = new HashSet<>(otherProfile.getFeelings());
        commonWants.retainAll(getFeelings());

        // add all together as commonalities
        Set<String> commonEntities = new HashSet<>();
        commonEntities.addAll(commonLocations);
        commonEntities.addAll(commonPossessions);
        commonEntities.addAll(commonStudies);
        commonEntities.addAll(commonWork);
        commonEntities.addAll(commonIdentities);
        commonEntities.addAll(commonProperNouns);
        commonEntities.addAll(commonLikes);
        commonEntities.addAll(commonWants);
        commonEntities.addAll(commonFeelings);

        return  commonEntities;
    }

    /**
     * Find activities in common between this prifle and another one.
     *
     * @param otherProfile the other profile to search in
     * @return the common activities
     */
    public Map<String, Set<String>> getCommonActivities(Profile otherProfile) {
        Map<String, Set<String>> commonActivities = new HashMap<>();
        Map<String, Set<String>> otherActivities = otherProfile.getActitivies();

        for (String activityVerb : activities.keySet()) {
            if (otherActivities.containsKey(activityVerb)) {
                Set<String> objects = activities.get(activityVerb);
                Set<String> otherObjects = new HashSet<>(otherActivities.get(activityVerb));
                otherObjects.retainAll(objects);
                Set<String> commonObjects = commonActivities.getOrDefault(activityVerb, new HashSet<>());
                commonObjects.addAll(otherObjects);
                commonActivities.put(activityVerb, commonObjects);
            }
        }

        return commonActivities;
    }

    /**
     * Adds one quality point to this statement.
     * Quality points are used to rank statements together with the Lexical Density.
     *
     * @param statement the statement to add a quality point to
     */
    private void addQualityPoint(Statement statement) {
        qualityPointsMap.put(statement, qualityPointsMap.getOrDefault(statement, 0) + 1);
    }

    /**
     * Unpack statements according to certain patterns to replace them with their embedded statements.
     */
    private void unpackEmbeddedStatements() {
        Set<Statement> embeddingStatements = new HashSet<>();
        Set<Statement> embeddedStatements = new HashSet<>();

        for (Statement statement : statements) {
            // TODO: unpacked statements do not carry over negation, e.g. "I think ..." and "I don't think ..."
            if (OPINION_PATTERN_1.matches(statement)) {
                for (StatementComponent capture : OPINION_PATTERN_1.getCaptures()) {
                    Statement embeddedStatement = (Statement) capture;
                    embeddedStatement.setOrigin(statement.getOrigin());
                    embeddedStatements.add(embeddedStatement);
                    embeddingStatements.add(statement);
                    qualityPointsMap.put(embeddedStatement, qualityPointsMap.getOrDefault(statement, 0) + 1);
                    logger.info("unpacked " + embeddedStatement + " from " + statement);
                }
            }

            if (OPINION_PATTERN_2.matches(statement)) {
                Set<StatementComponent> opinionComponents = OPINION_PATTERN_2.getNonCaptures(statement);
                Statement embeddedStatement = new Statement(opinionComponents);
                embeddedStatement.setOrigin(statement.getOrigin());
                embeddedStatements.add(embeddedStatement);
                embeddingStatements.add(statement);
                qualityPointsMap.put(embeddedStatement, qualityPointsMap.getOrDefault(statement, 0) + 1);
                logger.info("unpacked " + embeddedStatement + " from " + statement);
            }
        }

        statements.removeAll(embeddingStatements);
        statements.addAll(embeddedStatements);
        logger.info("total statements unpacked: " + embeddingStatements.size());
    }

    /**
     * Unpack statements according to certain patterns to replace them with their embedded statements.
     */
    private void registerEmotionalContent() {
        for (Statement statement : statements) {
            if (LIKE_PATTERN_1.matches(statement)) {
                for (StatementComponent capture : LIKE_PATTERN_1.getCaptures()) {
                    if (capture instanceof  AbstractComponent) {
                        AbstractComponent abstractComponent = (AbstractComponent) capture;
                        if (!abstractComponent.isVerb()) {
                            likes.add(abstractComponent.getNormalCompound());
                            logger.info("found like " + abstractComponent + " in " + statement);
                        } else {
                            String activityVerb = abstractComponent.getNormalCompound();
                            Set<String> objects = activities.getOrDefault(activityVerb, new HashSet<>());
                            objects.add("");
                            activities.put(activityVerb, objects);
                            logger.info("found liked activity " + abstractComponent + " in " + statement);
                        }
                    } else  if (capture instanceof Statement) {
                        Statement embeddedStatement = (Statement) capture;
                        String activityVerb = embeddedStatement.getVerb().getNormalCompound();
                        String activityObject = embeddedStatement.getDirectObject() != null? " " + embeddedStatement.getDirectObject().getNormalCompound() : "";
                        Set<String> objects = activities.getOrDefault(activityVerb, new HashSet<>());
                        objects.add(activityObject);

                        activities.put(activityVerb, objects);
                        logger.info("found liked activity " + embeddedStatement + " in " + statement);
                    }
                }

                addQualityPoint(statement);
            }

            if (LIKE_PATTERN_2.matches(statement)) {
                for (StatementComponent capture : LIKE_PATTERN_2.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    likes.add(abstractComponent.getNormalCompound());
                    logger.info("found like " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }

            if (WANT_PATTERN.matches(statement)) {
                for (StatementComponent capture : WANT_PATTERN.getCaptures()) {
                    if (capture instanceof  AbstractComponent) {
                        AbstractComponent abstractComponent = (AbstractComponent) capture;
                        if (!abstractComponent.isVerb()) {
                            wants.add(abstractComponent.getNormalCompound());
                            logger.info("found want " + abstractComponent + " in " + statement);
                        } else {
                            String activityVerb = abstractComponent.getNormalCompound();
                            Set<String> objects = activities.getOrDefault(activityVerb, new HashSet<>());
                            objects.add("");
                            activities.put(activityVerb, objects);
                            logger.info("found wanted activity " + abstractComponent + " in " + statement);
                        }
                    } else  if (capture instanceof Statement) {
                        Statement embeddedStatement = (Statement) capture;
                        String activityVerb = embeddedStatement.getVerb().getNormalCompound();
                        String activityObject = embeddedStatement.getDirectObject() != null? " " + embeddedStatement.getDirectObject().getNormalCompound() : "";
                        Set<String> objects = activities.getOrDefault(activityVerb, new HashSet<>());
                        objects.add(activityObject);

                        activities.put(activityVerb, objects);
                        logger.info("found wanted activity " + embeddedStatement + " in " + statement);
                    }
                }

                addQualityPoint(statement);
            }

            if (FEEL_PATTERN.matches(statement)) {
                for (StatementComponent capture : FEEL_PATTERN.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    feelings.add(abstractComponent.getNormalCompound());
                    logger.info("found feeling " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }
        }

        logger.info("total likes found: " + likes.size());
        logger.info("total wants found: " + wants.size());
        logger.info("total activities found: " + activities.size());
        logger.info("total feelings found: " + feelings.size());
    }

    /**
     * Unpack statements according to certain patterns to replace them with their embedded statements.
     */
    private void registerProperNouns() {
        for (Statement statement : statements) {
            if (PROPER_NOUN_PATTERN.matches(statement)) {
                for (StatementComponent capture : PROPER_NOUN_PATTERN.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    properNouns.add(abstractComponent.getNormalCompound());
                    logger.info("found proper noun " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }
        }

        logger.info("total proper nouns found: " + properNouns.size());
    }

    /**
     * Unpack statements according to certain patterns to replace them with their embedded statements.
     */
    private void registerOccupations() {
        for (Statement statement : statements) {
            if (STUDY_PATTERN.matches(statement)) {
                for (StatementComponent capture : STUDY_PATTERN.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    studies.add(abstractComponent.getNormalCompound());
                    logger.info("found study " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }

            if (WORK_PATTERN.matches(statement)) {
                for (StatementComponent capture : WORK_PATTERN.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    work.add(abstractComponent.getNormalCompound());
                    logger.info("found work " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }

            if (IDENTITY_PATTERN_1.matches(statement)) {
                for (StatementComponent capture : IDENTITY_PATTERN_1.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    identities.add(abstractComponent.getNormalCompound());
                    logger.info("found identity " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }

            if (IDENTITY_PATTERN_2.matches(statement)) {
                for (StatementComponent capture : IDENTITY_PATTERN_2.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    identities.add(abstractComponent.getNormalCompound());
                    logger.info("found identity " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }
        }

        logger.info("total studies found: " + studies.size());
        logger.info("total work found: " + work.size());
        logger.info("total identities found: " + identities.size());
    }

    /**
     * Unpack statements according to certain patterns to replace them with their embedded statements.
     */
    private void registerLocations() {
        for (Statement statement : statements) {
            if (LOCATION_PATTERN.matches(statement)) {
                for (StatementComponent capture : LOCATION_PATTERN.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    locations.add(abstractComponent.getNormalCompound());
                    logger.info("found location " + abstractComponent + " in " + statement);
                }

                addQualityPoint(statement);
            }
        }

        logger.info("total locations found: " + locations.size());
    }

    /**
     * Unpack statements according to certain patterns to replace them with their embedded statements.
     */
    private void registerPossessions() {
        for (Statement statement : statements) {
            if (POSSESSION_PATTERN_1.matches(statement)) {
                for (StatementComponent capture : POSSESSION_PATTERN_1.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    possessions.add(abstractComponent.getNormalCompound());
                    logger.info("found possession " + abstractComponent + " in " + statement + " using POSSESSION_PATTERN_1");
                }

                addQualityPoint(statement);
            }

            if (POSSESSION_PATTERN_2.matches(statement)) {
                for (StatementComponent capture : POSSESSION_PATTERN_2.getCaptures()) {
                    AbstractComponent abstractComponent = (AbstractComponent) capture;
                    possessions.add(abstractComponent.getNormalCompound());
                    logger.info("found possession " + abstractComponent + " in " + statement + " using POSSESSION_PATTERN_2");
                }

                addQualityPoint(statement);
            }
        }

        logger.info("total possessions found: " + possessions.size());
    }

    /**
     * Get the statements that evaluate as interesting according to their own internal measure.
     *
     * @return interesting statements
     */
    public Set<Statement> getInterestingStatements() {
        if (interestingStatements.isEmpty()) {
            for (Statement statement : getStatements()) {
                if (INTERESTING_PATTERN.matches(statement) && !INTERESTING_ANTIPATTERN_1.matches(statement)) interestingStatements.add(statement);
            }
        }

        return interestingStatements;
    }

    /**
     * Returns statements in order of diminishing lexical density.
     *
     * @return statements
     */
    public List<Statement> getStatementsByLexicalDensity() {
        List<Statement> rankedStatements = new ArrayList<>(getInterestingStatements());
        rankedStatements.sort(new LexicalDensityComparator());
        return rankedStatements;
    }

    /**
     * Returns statements in order of diminishing quality.
     *
     * @return statements
     */
    public List<Statement> getStatementsByQuality() {
        List<Statement> rankedStatements = new ArrayList<>(getInterestingStatements());
        rankedStatements.sort(new QualityComparator());
        return rankedStatements;
    }

    /**
     * Returns statements in order of diminishing relevance from the perspective of another profile.
     *
     * @param relevanceComparator the comparator used to rank the statements by relevance
     * @return relevant statements
     */
    public List<Statement> getStatementsByRelevance(RelevanceComparator relevanceComparator) {
        List<Statement> rankedStatements = new ArrayList<>(getInterestingStatements());
        rankedStatements.sort(relevanceComparator);
        return rankedStatements;
    }

    public Set<Statement> getStatements() {
        return statements;
    }

    public Set<String> getLocations() {
        return locations;
    }

    public Set<String> getPossessions() {
        return possessions;
    }

    public Set<String> getStudies() {
        return studies;
    }

    public Set<String> getWork() {
        return work;
    }

    public Set<String> getIdentities() {
        return identities;
    }

    public Set<String> getProperNouns() {
        return properNouns;
    }

    public Set<String> getLikes() {
        return likes;
    }

    public Set<String> getWants() {
        return wants;
    }

    public Map<String, Set<String>> getActitivies() {
        return activities;
    }

    public Set<String> getFeelings() {
        return feelings;
    }

    /**
     * Calculates the quality for a statement.
     * Used for rankings statements.
     *
     * @param statement statement to assess
     * @return quality of statement
     */
    private double getStatementQuality(Statement statement) {
        if (!qualityMap.containsKey(statement)) {
            // retrieve the baseline value, in this case lexical density
            double baseline = statement.getLexicalDensity();
            double multiplier = baseline * 0.25;
            double adjustment = qualityPointsMap.getOrDefault(statement, 0) * multiplier;
            double quality = baseline + adjustment;

            // save to map for later lazy-loading
            qualityMap.put(statement, quality);

            return quality;
        } else {
            return qualityMap.get(statement);
        }
    }

    /**
     * Used to sort Statements by lexical density.
     */
    public static class LexicalDensityComparator implements Comparator<Statement> {
        @Override
        public int compare(Statement x, Statement y) {
            double xn = x.getLexicalDensity();
            double yn = y.getLexicalDensity();

            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    /**
     * Used to sort Statements by quality.
     */
    public class QualityComparator implements Comparator<Statement> {
        @Override
        public int compare(Statement x, Statement y) {
            double xn = getStatementQuality(x);
            double yn = getStatementQuality(y);

            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    /**
     * Used to sort Statements by quality relative to other profiles.
     */
    public static class RelevanceComparator implements Comparator<Statement> {
        protected final Logger logger = LoggerFactory.getLogger(getClass());

        private final Profile statementProfile;
        private final Set<StatementPattern> relevancePatterns;
        private final Map<Statement, Integer> relevancePointsMap = new HashMap<>();
        private final Map<Statement, Double> relevanceMap = new HashMap<>();

        /**
         *
         *
         * @param testProfile the profile to check for commonalities
         * @param statementProfile the profile to get ranked statements from
         */
        public RelevanceComparator(Profile testProfile, Profile statementProfile) {
            this.statementProfile = statementProfile;
            this.relevancePatterns = new HashSet<>();

            // for discovering relevant entities
            Set<String> commonEntities = testProfile.getCommonEntities(statementProfile);
            for (String entity : commonEntities) {
                this.relevancePatterns.add(
                        new StatementPattern(
                                new NonVerbPattern().words(entity)
                        )
                );
            }

            // for discovering relevant activities
            Map<String, Set<String>> commonActivities = testProfile.getCommonActivities(statementProfile);
            for (String activityVerb : commonActivities.keySet()) {
                this.relevancePatterns.add(
                        new StatementPattern(
                                new VerbPattern().words(activityVerb),
                                new NonVerbPattern().words(commonActivities.get(activityVerb))
                        )
                );
            }

            // assign relevance points based on the different patterns
            for (Statement statement : statementProfile.getInterestingStatements()) {
                for (StatementPattern pattern : relevancePatterns) {
                    if (pattern.matches(statement)) {
                        logger.info("adding point for: " + statement);
                        addRelevancePoint(statement);
                    }
                }
            }

            logger.info("commonEntities: " + commonEntities);
            logger.info("commonActivities: " + commonActivities);
            logger.info("relevancePatterns: " + relevancePatterns);
            logger.info("relevancePointsMap: " + relevancePointsMap);
        }

        public Map<Statement, Double> getRelevanceMap() {
            return relevanceMap;
        }

        /**
         * Adds one relevance point to this statement.
         *
         * @param statement the statement to add a quality point to
         */
        private void addRelevancePoint(Statement statement) {
            relevancePointsMap.put(statement, relevancePointsMap.getOrDefault(statement, 0) + 1);
        }

        /**
         * Calculates the quality for a statement.
         * Used for rankings statements.
         *
         * @param statement statement to assess
         * @return quality of statement
         */
        private double getStatementRelevance(Statement statement) {
            if (!relevanceMap.containsKey(statement)) {
                // retrieve the baseline value, in this case quality
                double baseline = statementProfile.getStatementQuality(statement);
                double multiplier = baseline * 0.25;
                double adjustment = relevancePointsMap.getOrDefault(statement, 0) * multiplier;
                double relevance = baseline + adjustment;

                // save to map for later lazy-loading
                relevanceMap.put(statement, relevance);
                if (relevance > baseline) {
                    logger.info("assigned relevance score (" + baseline + " -> " + relevance + "): " + statement);
                }

                return relevance;
            } else {
                return relevanceMap.get(statement);
            }
        }

        @Override
        public int compare(Statement x, Statement y) {
            double xn = getStatementRelevance(x);
            double yn = getStatementRelevance(y);

            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }

    public String getStatementInfo(Statement statement) {
        return "{" +
                statement.getSummary() +
                ": \"" + statement.getSentence() + "\"" +
                ", density: " + df.format(statement.getLexicalDensity()) +
                ", quality: " + df.format(qualityMap.get(statement)) +
                "}" + " " + statement.getOrigin();
    }


    public String getStatementInfo(Statement statement, RelevanceComparator comparator) {
        return "{" +
                statement.getSummary() +
                ": \"" + statement.getSentence() + "\"" +
                ", density: " + df.format(statement.getLexicalDensity()) +
                ", quality: " + df.format(qualityMap.get(statement)) +
                ", relevance: " + df.format(comparator.getRelevanceMap().get(statement)) +
                "}" + " " + statement.getOrigin();
    }
}
