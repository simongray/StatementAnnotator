package statements.patterns;

/**
 * A builder for a proxy Verb.
 */
public  class VerbPattern extends ComponentPattern.PatternBuilder {
    public VerbPattern() {
        super(statements.core.Verb.class);
    }
}
