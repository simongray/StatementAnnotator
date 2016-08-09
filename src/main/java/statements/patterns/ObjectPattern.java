package statements.patterns;

/**
 * A builder for a proxy DirectObject/IndirectObject.
 */
public class ObjectPattern extends ComponentPattern {
    public ObjectPattern() {
        super(statements.core.DirectObject.class, statements.core.IndirectObject.class);
    }
}
