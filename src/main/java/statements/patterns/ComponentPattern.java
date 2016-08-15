package statements.patterns;

import edu.stanford.nlp.ling.IndexedWord;
import statements.core.AbstractComponent;
import statements.core.PartsOfSpeech;
import statements.core.StatementComponent;
import statements.core.Verb;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;


/**
 * This class must be instantiated using one of the derived classes.
 *
 * Note: the Boolean wrapper class is used instead of the primitive.
 * This is necessary to be able to have unknown/null states.
 */
public class ComponentPattern implements Pattern {
    private Class[] types;
    private Set<String> words;
    private Set<String> notWords;
    private Boolean negated;
    private Boolean plural;
    private Boolean specific;
    private Boolean local;
    private Boolean copula;
    private Boolean capitalised;
    private Boolean description;
    private Tag[] partsOfSpeech;
    private Tag[] compoundTags;
    private Set<Set<String>> tagGroups;
    private Person[] pointsOfView;
    private Person[] possessivePointsOfView;
    private String[] prepositions;

    /**
     * Used to capture certain components (similar to how it works in regex).
     */
    private boolean capture;
    private AbstractComponent captured;
    private boolean optional;

    /**
     * Does the pattern need to match all components of that type?
     */
    private boolean mustMatchAll;

    public ComponentPattern(Class... types) {
        this.types = types;
    }

    public Class[] getTypes() {
        return types;
    }

    public ComponentPattern words(Set<String> words) {
        if (this.words == null) this.words = new HashSet<>();
        this.words = words;
        return this;
    }

    public ComponentPattern words(String... words) {
        if (this.words == null) this.words = new HashSet<>();
        for (int i = 0; i < words.length; i++) words[i] = words[i].toLowerCase();
        Collections.addAll(this.words, words);
        return this;
    }

    public ComponentPattern notWords(String... words) {
        if (this.notWords == null) this.notWords = new HashSet<>();
        for (int i = 0; i < words.length; i++) words[i] = words[i].toLowerCase();
        Collections.addAll(this.notWords, words);
        return this;
    }

    public ComponentPattern notWords(Set<String> words) {
        if (this.notWords == null) this.notWords = new HashSet<>();
        this.notWords = words;
        return this;
    }

    public ComponentPattern negated(Boolean state) {
        this.negated = state;
        return this;
    }

    public ComponentPattern negated() {
        return negated(false);
    }

    public ComponentPattern plural(Boolean state) {
        this.plural = state;
        return this;
    }

    public ComponentPattern plural() {
        return plural(true);
    }

    public ComponentPattern singular() {
        return plural(false);
    }

    public ComponentPattern specific(Boolean state) {
        this.specific = state;
        return this;
    }

    public ComponentPattern specific() {
        return specific(true);
    }

    public ComponentPattern general() {
        return specific(false);
    }

    public ComponentPattern local(Boolean state) {
        this.local = state;
        return this;
    }

    public ComponentPattern local() {
        return local(true);
    }

    public ComponentPattern copula(Boolean state) {
        this.copula = state;
        return this;
    }

    public ComponentPattern capitalised(Boolean state) {
        this.capitalised = state;
        return this;
    }

    public ComponentPattern capitalised() {
        return capitalised(true);
    }

    public ComponentPattern copula() {
        return copula(true);
    }

    public ComponentPattern preposition(String... prepositions) {
        for (int i = 0; i < prepositions.length; i++) prepositions[i] = prepositions[i].toLowerCase();
        this.prepositions = prepositions;
        return this;
    }

    public ComponentPattern preposition() {
        preposition(new String[0]);  // empty array will match any preposition
        return this;
    }

    public ComponentPattern description(Boolean state) {
        this.description = state;
        return this;
    }

    public ComponentPattern description() {
        return description(true);
    }

    /*
        Point-of-view section.
     */
    public ComponentPattern person(Person... states) {
        this.pointsOfView = states;
        return this;
    }

    public ComponentPattern firstPerson() {
        return person(Person.first);
    }

    public ComponentPattern secondPerson() {
        return person(Person.second);
    }

    public ComponentPattern thirdPerson() {
        return person(Person.third);
    }

    public ComponentPattern possessive(Person... states) {
        this.possessivePointsOfView = states;
        return this;
    }

    public ComponentPattern firstPersonPossessive() {
        return possessive(Person.first);
    }

    public ComponentPattern secondPersonPossessive() {
        return possessive(Person.second);
    }

    public ComponentPattern thirdPersonPossessive() {
        return possessive(Person.third);
    }

    /*
        Part-of-speech tag section.
     */
    public ComponentPattern partsOfSpeech(Tag... state) {
        this.partsOfSpeech = state;
        return this;
    }

    public ComponentPattern pronoun() {
        return partsOfSpeech(Tag.pronoun);
    }

    public ComponentPattern properNoun() {
        return partsOfSpeech(Tag.properNoun);
    }

    public ComponentPattern noun() {
        return partsOfSpeech(Tag.noun);
    }

    public ComponentPattern verb() {
        return partsOfSpeech(Tag.verb);
    }

    public ComponentPattern adjective() {
        return partsOfSpeech(Tag.adjective);
    }

    public ComponentPattern adverb() {
        return partsOfSpeech(Tag.adverb);
    }

    // for the entire compound, not just the primary word!
    public ComponentPattern compoundTags(Tag... tags) {
        this.compoundTags = tags;
        this.tagGroups = new HashSet<>();

        for (Tag tag : tags) {
            if (tag.equals(Tag.adjective)) {
                this.tagGroups.add(PartsOfSpeech.ADJECTIVES);
            } else if (tag.equals(Tag.adverb)) {
                this.tagGroups.add(PartsOfSpeech.ADVERBS);
            } else if (tag.equals(Tag.noun)) {
                this.tagGroups.add(PartsOfSpeech.NOUNS);
            } else if (tag.equals(Tag.pronoun)) {
                this.tagGroups.add(PartsOfSpeech.PRONOUNS);
            } else if (tag.equals(Tag.properNoun)) {
                this.tagGroups.add(PartsOfSpeech.PROPER_NOUNS);
            } else if (tag.equals(Tag.verb)) {
                this.tagGroups.add(PartsOfSpeech.VERBS);
            }
        }

        return this;
    }

    /*
        Meta section.
     */
    public ComponentPattern all() {
        this.mustMatchAll = true;
        return this;
    }

    public ComponentPattern optional() {
        this.optional = true;
        return this;
    }

    /**
     * Capture this component for processing.
     */
    public ComponentPattern capture() {
        this.capture = true;
        return this;
    }

    @Override
    public boolean mustMatchAll() {
        return mustMatchAll;
    }

    @Override
    public boolean isOptional() {
        return optional;
    }

    /**
     * Get the component captured during the latest match.
     * @return captured component
     */
    public AbstractComponent getCaptured() {
        return captured;
    }

    /**
     * Match this ComponentPattern against a component.
     *
     * @param statementComponent the component to test
     * @return true if the pattern matches the component
     */
    public boolean matches(StatementComponent statementComponent) {
        if (statementComponent == null) return false;

        // this pattern can only match abstract components!
        if (statementComponent instanceof AbstractComponent) {
            AbstractComponent abstractComponent = (AbstractComponent) statementComponent;

            // must ALWAYS match component type
            if (!matchesTypes(abstractComponent)) return false;

            // can be ignored if specified, otherwise defaults to matching non-negated
            if (negated != null && abstractComponent.isNegated() != negated) return false;

            if (plural != null && abstractComponent.isPlural() != plural) return false;

            if (specific != null && abstractComponent.isSpecific() != specific) return false;

            if (local != null && abstractComponent.isLocal() != local) return false;

            if (capitalised != null && abstractComponent.isCapitalised() != capitalised) return false;

            if (description != null && abstractComponent.hasDescription() != description) return false;

            if (partsOfSpeech != null && !matchesPartOfSpeech(abstractComponent)) return false;

            if (compoundTags != null && !matchesCompoundTags(abstractComponent)) return false;

            // 1st, 2nd, and/or 3rd person
            if (pointsOfView != null && !matchesPointOfView(abstractComponent)) return false;

            // 1st, 2nd, and/or 3rd person
            if (possessivePointsOfView != null && !matchesPossessivePointOfView(abstractComponent)) return false;

            // = "to be" verb
            if (copula != null && abstractComponent instanceof Verb && !(((Verb) abstractComponent).isCopula() == copula)) return false;

            // such as "to", "from", "by", ...
            if (prepositions != null && !matchesPrepositions(abstractComponent)) return false;

            // matches words to compound
            if (words != null && !matchesWords(abstractComponent, words)) return false;
            if (notWords != null && matchesWords(abstractComponent, notWords)) return false;

            if (capture) captured = abstractComponent;

            return true;
        }

        return false;
    }

    /**
     * Whether the person (1st, 2nd, 3rd) of the component matches.
     *
     * @param abstractComponent the component to test
     * @return true if one of the states matches
     */
    public boolean matchesTypes(AbstractComponent abstractComponent) {
        Class componentType = abstractComponent.getClass();

        // when no specific types have been given, will match any component
        if (types.length == 0) return true;

        for (Class type : types) {
            if (type.equals(componentType)) return true;
        }

        return false;
    }

    /**
     * Whether the person (1st, 2nd, 3rd) of the component matches.
     *
     * @param abstractComponent the component to test
     * @return true if one of the states matches
     */
    private boolean matchesPrepositions(AbstractComponent abstractComponent) {
        // when no specific prepositions have been given, will match any
        if (prepositions.length == 0 && abstractComponent.hasPreposition()) return true;

        for (String preposition : prepositions) {
            for (IndexedWord componentPreposition : abstractComponent.getPrepositions()) {
                if (componentPreposition.word().toLowerCase().equals(preposition)) return true;
            }
        }

        return false;
    }

    /**
     * Whether the person (1st, 2nd, 3rd) of the component matches.
     *
     * @param abstractComponent the component to test
     * @return true if one of the states matches
     */
    private boolean matchesPartOfSpeech(AbstractComponent abstractComponent) {
        Tag partOfSpeechState = null;

        if (abstractComponent.isPronoun()) {
            partOfSpeechState = Tag.pronoun;
        } else if (abstractComponent.isProperNoun()) {
            partOfSpeechState = Tag.properNoun;
        }  else if (abstractComponent.isNoun()) {
            partOfSpeechState = Tag.noun;
        } else if (abstractComponent.isVerb()) {
            partOfSpeechState = Tag.verb;
        } else if (abstractComponent.isAdjective()) {
            partOfSpeechState = Tag.adjective;
        } else if (abstractComponent.isAdverb()) {
            partOfSpeechState = Tag.adverb;
        }

        for (Tag state : partsOfSpeech) {
            if (partOfSpeechState == state) return true;
        }

        return false;
    }

    /**
     * Whether the person (1st, 2nd, 3rd) of the component matches.
     *
     * @param abstractComponent the component to test
     * @return true if one of the states matches
     */
    private boolean matchesCompoundTags(AbstractComponent abstractComponent) {
        for (Set<String> posTagGroup : tagGroups) {
            if (!abstractComponent.hasPosTags(posTagGroup)) return false;
        }

        return true;
    }

    /**
     * Whether the person (1st, 2nd, 3rd) of the component matches.
     *
     * @param abstractComponent the component to test
     * @return true if one of the states matches
     */
    private boolean matchesPointOfView(AbstractComponent abstractComponent) {
        Person componentState;

        if (abstractComponent.isFirstPerson()) {
            componentState = Person.first;
        } else if (abstractComponent.isSecondPerson()) {
            componentState = Person.second;
        } else {
            componentState = Person.third;
        }

        for (Person state : pointsOfView) {
            if (componentState == state) return true;
        }

        return false;
    }

    /**
     * Whether the person (1st, 2nd, 3rd) of the component matches.
     *
     * @param abstractComponent the component to test
     * @return true if one of the states matches
     */
    private boolean matchesPossessivePointOfView(AbstractComponent abstractComponent) {
        Person componentState = null;

        if (abstractComponent.hasFirstPersonPossessive()) {
            componentState = Person.first;
        } else if (abstractComponent.hasSecondPersonPossessive()) {
            componentState = Person.second;
        } else if (abstractComponent.hasThirdPersonPossessive()) {
            componentState = Person.third;
        }

        for (Person state : possessivePointsOfView) {
            if (componentState == state) return true;
        }

        return false;
    }

    /**
     * Whether the normal compound of a component matches with one of the words of this pattern.
     * If the pattern contains no words, then this method will always return true;
     *
     * @param abstractComponent the component to test
     * @return true if one of the words matches
     */
    private boolean matchesWords(AbstractComponent abstractComponent, Set<String> wordsToMatch) {
        if (wordsToMatch.isEmpty()) return true;

        String normalCompound = abstractComponent.getNormalCompound();
        if (wordsToMatch.contains(normalCompound)) return true;
        if (wordsToMatch.contains(abstractComponent.getPrimary().lemma().toLowerCase())) return true;

        return false;
    }
}
