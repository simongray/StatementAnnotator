package statements.patterns;

/**
 * A builder for a proxy Verb.
 */
public  class VerbPattern extends ComponentPattern {
    public VerbPattern() {
        super(statements.core.Verb.class);

        // note that verbs only match non-negated form by default!
        // I find that this more intuitive than matching negated AND non-negated by default
        negated(false);
    }
}
