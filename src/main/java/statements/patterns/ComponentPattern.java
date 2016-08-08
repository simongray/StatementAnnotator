package statements.patterns;

import edu.stanford.nlp.ling.IndexedWord;
import statements.core.AbstractComponent;
import statements.core.StatementComponent;
import statements.core.Verb;


/**
 * This class must be instantiated using one of the derived classes.
 *
 * Note: the Boolean wrapper class is used instead of the primitive.
 * This is necessary to be able to have unknown/null states.
 */
public class ComponentPattern implements Pattern {
    private Class[] types;
    private String[] words;
    private Boolean negated;
    private Boolean plural;
    private Boolean specific;
    private Boolean local;
    private Boolean copula;
    private Boolean capitalised;
    private Boolean interesting;
    private Boolean wellFormed;
    private Tag[] partsOfSpeech;
    private Person[] pointsOfView;
    private String[] prepositions;

    /**
     * Used to capture certain components (similar to how it works in regex).
     */
    private boolean capture;
    private AbstractComponent captured;

    public ComponentPattern(Class... types) {
        this.types = types;
    }

    public Class[] getTypes() {
        return types;
    }

    public ComponentPattern words(String... words) {
        for (int i = 0; i < words.length; i++) words[i] = words[i].toLowerCase();
        this.words = words;
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

    /*
        Analysis section.
     */

    public ComponentPattern interesting(Boolean state) {
        this.interesting = state;
        return this;
    }

    public ComponentPattern interesting() {
        return interesting(true);
    }

    public ComponentPattern wellFormed(Boolean state) {
        this.wellFormed = state;
        return this;
    }

    public ComponentPattern wellFormed() {
        return wellFormed(true);
    }

    /**
     * Capture this component for processing.
     */
    public ComponentPattern capture() {
        this.capture = true;
        return this;
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

            if (interesting != null && abstractComponent.isInteresting() != interesting) return false;

            if (wellFormed != null && abstractComponent.isWellFormed() != wellFormed) return false;

            if (partsOfSpeech != null && !matchesPartOfSpeech(abstractComponent)) return false;

            // 1st, 2nd, and/or 3rd person
            if (pointsOfView != null && !matchesPointOfView(abstractComponent)) return false;

            // = "to be" verb
            if (copula != null && abstractComponent instanceof Verb && !((Verb) abstractComponent).isCopula()) return false;

            // such as "to", "from", "by", ...
            if (prepositions != null && !matchesPrepositions(abstractComponent)) return false;

            // matches words to compound
            if (words != null && !matchesWords(abstractComponent.getNormalCompound())) return false;

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
    private boolean matchesTypes(AbstractComponent abstractComponent) {
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
     * Whether the normal compound of a component matches with one of the words of this pattern.
     * If the pattern contains no words, then this method will always return true;
     *
     * @param normalCompound the normal compound to test
     * @return true if one of the words matches
     */
    private boolean matchesWords(String normalCompound) {
        if (words.length == 0) return true;

        for (String word : words) {
            if (word.equals(normalCompound)) return true;
        }

        return false;
    }
}
