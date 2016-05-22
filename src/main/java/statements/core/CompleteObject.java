package statements.core;

/**
 * The complete object of a natural language statement.
 */
public class CompleteObject implements Resembling<CompleteObject> {
    /**
     * The name of the complete verb.
     * @return the longest subject possible
     */
    public String getName() {
        return null;  // TODO: implement full version
    }

    /**
     * The resemblance of another object to this object.
     * @param otherObject object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(CompleteObject otherObject) {
        return null;  // TODO
    }
}
