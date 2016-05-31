package statements.core;

/**
 * Defines a way to compare objects using resemblance rather than strict equality or ordering.
 * It is useful for comparing natural language, which may express the same idea in different ways.
 *
 * @param <T> the base class for the object of comparison.
 */
public interface Resembling<T> {
    /**
     * The resemblance of this object to another object.
     *
     * @param otherObject subject to be compared with
     * @return resemblance
     */
    Resemblance resemble(T otherObject);
}
