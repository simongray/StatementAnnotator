package statements.patterns;

import statements.core.AbstractComponent;
import statements.core.StatementComponent;

/**
 * ComponentPatterns are instantiated using the builder classes.
 */
public class ComponentPattern implements Pattern {
    private final Class type;
    private final String[] words;
    private final Boolean negated;
    private final Boolean plural;
    private final Boolean specific;
    private final Boolean local;
    private final Boolean properNoun;
    private final int[] person;

    private ComponentPattern(PatternBuilder builder) {
        this.type = builder.type;
        this.words = builder.words;
        this.negated = builder.negated;
        this.plural = builder.plural;
        this.specific = builder.specific;
        this.local = builder.local;
        this.properNoun = builder.properNoun;
        this.person = builder.person;
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

            // local state (if applicable)
            if (properNoun != null && abstractComponent.isProperNoun() != properNoun) return false;

            // matches against 1st, 2nd, and/or 3rd person
            if (person != null && !matchesPerson(abstractComponent)) return false;

            // compound (if applicable)
            if (words != null && !matchesNormalCompound(abstractComponent.getNormalCompound())) return false;

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
    private boolean matchesPerson(AbstractComponent abstractComponent) {
        int componentState;

        if (abstractComponent.isFirstPerson()) {
            componentState = 1;
        } else if (abstractComponent.isSecondPerson()) {
            componentState = 2;
        } else {
            componentState = 3;
        }

        for (int state : person) {
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
    private boolean matchesNormalCompound(String normalCompound) {
        if (words.length == 0) return true;

        for (String word : words) {
            if (word.equals(normalCompound)) return true;
        }

        return false;
    }

    /**
     * This class must be instantiated using one of the derived classes.
     *
     * Note: the Boolean wrapper class is used instead of the primitive.
     * This is necessary to be able to have unknown/null states.
     */
    public abstract static class PatternBuilder {
        public final Class type;
        public String[] words = null;
        public Boolean negated = false;  // note: defaults to false - not null - unlike other attributes
        public Boolean plural = null;
        public Boolean specific = null;
        public Boolean local = null;
        public Boolean properNoun = null;
        public int[] person = null;

        protected PatternBuilder(Class type) {
            this.type = type;
        }

        public ComponentPattern build() {
            return new ComponentPattern(this);
        }

        public PatternBuilder words(String... words) {
            for (int i = 0; i < words.length; i++) words[i] = words[i].toLowerCase();
            this.words = words;
            return this;
        }

        public PatternBuilder negated(Boolean state) {
            this.negated = state;
            return this;
        }

        public PatternBuilder negated() {
            this.negated = true;
            return this;
        }

        public PatternBuilder plural(Boolean state) {
            this.plural = state;
            return this;
        }

        public PatternBuilder plural() {
            this.plural = true;
            return this;
        }

        public PatternBuilder specific(Boolean state) {
            this.specific = state;
            return this;
        }

        public PatternBuilder specific() {
            this.specific = true;
            return this;
        }

        public PatternBuilder local(Boolean state) {
            this.local = state;
            return this;
        }

        public PatternBuilder local() {
            this.local = true;
            return this;
        }

        public PatternBuilder properNoun(Boolean state) {
            this.properNoun = state;
            return this;
        }

        public PatternBuilder properNoun() {
            this.properNoun = true;
            return this;
        }

        public PatternBuilder person(int... states) {
            this.person = states;
            return this;
        }

        public PatternBuilder firstPerson() {
            this.person = new int[] {1};
            return this;
        }

        public PatternBuilder secondPerson() {
            this.person = new int[] {2};
            return this;
        }

        public PatternBuilder thirdPerson() {
            this.person = new int[] {3};
            return this;
        }
    }
}
