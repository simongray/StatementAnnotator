package statements.core;

/**
 * This interface defines a way to compare objects using resemblance rather than strict equality or ordering.
 * It is useful for comparing natural language, which may express the same idea in different ways.
 * The base class of a class hierarchy which needs to be
 * @param <T> the base class for the object of comparison.
 */
public interface Resembling<T> {
    /**
     * Returns whether or not the Resembling object resembles another Resembling object.
     * @param otherObject
     * @return
     */
    Resemblance resemble(T otherObject);

    /**
     * Resemblance is not boolean.
     * An object can either:
     *    * fully resemble another (= direct equality of fields)
     *    * closely resemble another (= fuzzy equality of fields)
     *    * slightly resemble another (= partial fuzzy or direct equality of fields)
     *    * not resemble another (= no direct or fuzzy equality)
     */
    enum Resemblance {
        FULL,
        CLOSE,
        SLIGHT,
        NONE
    }
}
