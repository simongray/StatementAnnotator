package statements.patterns;

/**
 * A builder for a proxy Subject.
 */
public class SubjectPattern extends ComponentPattern.PatternBuilder {
    public SubjectPattern() {
        super(statements.core.Subject.class);
    }
}
