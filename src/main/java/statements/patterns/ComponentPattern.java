package statements.patterns;

import statements.core.AbstractComponent;
import statements.core.StatementComponent;

/**
 * ComponentPatterns are instantiated using the builder classes.
 */
public class ComponentPattern implements Pattern {
    private final Class type;
    private final String[] words;
    private final boolean negated;
    private final boolean ignoreNegation;

    private ComponentPattern(PatternBuilder builder) {
        this.type = builder.type;
        this.words = builder.words;
        this.negated = builder.negated;
        this.ignoreNegation = builder.ignoreNegation;
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
            if (!ignoreNegation && abstractComponent.isNegated() != negated) return false;

            // compound (if applicable)
            if (!matchesNormalCompound(abstractComponent.getNormalCompound())) return false;

            return true;
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
        if (words == null || words.length == 0) return true;

        for (String word : words) {
            if (word.equals(normalCompound)) return true;
        }

        return false;
    }

    /**
     * This class must be instantiated using one of the derived classes.
     */
    public abstract static class PatternBuilder {
        public final Class type;
        public String[] words = null;
        public boolean negated = false;
        public boolean ignoreNegation = false;

        private PatternBuilder(Class type) {
            this.type = type;
        }

        public ComponentPattern build() {
            return new ComponentPattern(this);
        }

        public PatternBuilder words(String... words) {
            // all equality comparisons are made without looking at case
            for (int i = 0; i < words.length; i++) words[i] = words[i].toLowerCase();

            this.words = words;
            return this;
        }

        public PatternBuilder negated() {
            this.negated = true;
            return this;
        }

        public PatternBuilder ignoreNegation() {
            this.ignoreNegation = true;
            return this;
        }
    }

    /**
     * A builder for a proxy Subject.
     */
    public static class Subject extends PatternBuilder {
        public Subject() {
            super(statements.core.Subject.class);
        }
    }

    /**
     * A builder for a proxy Verb.
     */
    public static class Verb extends PatternBuilder {
        public Verb() {
            super(statements.core.Verb.class);
        }
    }

    /**
     * A builder for a proxy DirectObject.
     */
    public static class DirectObject extends PatternBuilder {
        public DirectObject() {
            super(statements.core.DirectObject.class);
        }
    }

    /**
     * A builder for a proxy IndirectObject.
     */
    public static class IndirectObject extends PatternBuilder {
        public IndirectObject() {
            super(statements.core.IndirectObject.class);
        }
    }
}
