package statements.patterns;

/**
 * A builder for a proxy DirectObject.
 */
public class DirectObjectPattern extends ComponentPattern.PatternBuilder {
    public DirectObjectPattern() {
        super(statements.core.DirectObject.class);
    }
}
