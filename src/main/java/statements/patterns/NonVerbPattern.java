package statements.patterns;

/**
 * A builder for a proxy Subject/DirectObject/IndirectObject.
 */
public class NonVerbPattern extends ComponentPattern {
    public NonVerbPattern() {
        super(statements.core.Subject.class,statements.core.DirectObject.class, statements.core.IndirectObject.class);
    }
}
