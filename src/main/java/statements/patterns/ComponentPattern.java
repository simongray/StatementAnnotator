package statements.patterns;

import statements.core.AbstractComponent;
import statements.core.StatementComponent;

/**
 * This class must be instantiated using one of the derived classes.
 *
 * Note: the Boolean wrapper class is used instead of the primitive.
 * This is necessary to be able to have unknown/null states.
 */
public class ComponentPattern implements Pattern {
    private Class type;
    private String[] words;
    private Boolean negated;
    private Boolean plural;
    private Boolean specific;
    private Boolean local;
    private Boolean properNoun;
    private Tag[] partsOfSpeech;
    private Person[] pointsOfView;

    protected ComponentPattern(Class type) {
        this.type = type;
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
        this.negated = true;
        return this;
    }

    public ComponentPattern plural(Boolean state) {
        this.plural = state;
        return this;
    }

    public ComponentPattern plural() {
        this.plural = true;
        return this;
    }

    public ComponentPattern specific(Boolean state) {
        this.specific = state;
        return this;
    }

    public ComponentPattern specific() {
        this.specific = true;
        return this;
    }

    public ComponentPattern local(Boolean state) {
        this.local = state;
        return this;
    }

    public ComponentPattern local() {
        this.local = true;
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
        this.pointsOfView = new Person[] {Person.first};
        return this;
    }

    public ComponentPattern secondPerson() {
        this.pointsOfView = new Person[] {Person.second};
        return this;
    }

    public ComponentPattern thirdPerson() {
        this.pointsOfView = new Person[] {Person.third};
        return this;
    }

    /*
        Part-of-speech tag section.
     */
    public ComponentPattern partsOfSpeech(Tag... state) {
        this.partsOfSpeech = state;
        return this;
    }

    public ComponentPattern properNoun() {
        this.partsOfSpeech = new Tag[]{ Tag.properNoun };
        return this;
    }

    public ComponentPattern noun() {
        this.partsOfSpeech = new Tag[]{ Tag.noun };
        return this;
    }

    public ComponentPattern verb() {
        this.partsOfSpeech = new Tag[]{ Tag.verb };
        return this;
    }

    public ComponentPattern adjective() {
        this.partsOfSpeech = new Tag[]{ Tag.adjective };
        return this;
    }

    public ComponentPattern adverb() {
        this.partsOfSpeech = new Tag[]{ Tag.adverb };
        return this;
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
            if (abstractComponent.getClass() != type) return false;

            // negation state (can be ignored if specified, otherwise defaults to matching non-negated)
            if (negated != null && abstractComponent.isNegated() != negated) return false;

            // plural state (if applicable)
            if (plural != null && abstractComponent.isPlural() != plural) return false;

            // specific state (if applicable)
            if (specific != null && abstractComponent.isSpecific() != specific) return false;

            // local state (if applicable)
            if (local != null && abstractComponent.isLocal() != local) return false;

            // part of speech (noun, verb, adjective, ...)
            if (partsOfSpeech != null && !matchesPartOfSpeech(abstractComponent)) return false;

            // matches against 1st, 2nd, and/or 3rd person
            if (pointsOfView != null && !matchesPointOfView(abstractComponent)) return false;

            // compound (if applicable)
            if (words != null && !matchesWords(abstractComponent.getNormalCompound())) return false;

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
    private boolean matchesPartOfSpeech(AbstractComponent abstractComponent) {
        Tag partOfSpeechState = null;

        if (abstractComponent.isProperNoun()) {
            partOfSpeechState = Tag.properNoun;
        } else if (abstractComponent.isNoun()) {
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
