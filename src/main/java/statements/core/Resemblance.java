package statements.core;

/**
 * Resemblance is non-boolean, non-ordered equality.
 *
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