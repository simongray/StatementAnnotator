package statements.patterns;

import statements.core.Statement;
import statements.core.StatementComponent;

import java.util.HashSet;
import java.util.Set;

public class StatementPattern implements Pattern {
    private final Pattern[] patterns;
    private final Class[] types = new Class[] { Statement.class };

    /**
     * Used to capture certain components (similar to how it works in regex).
     */
    private boolean capture;
    private Statement captured;

    public StatementPattern(Pattern... patterns) {
        this.patterns = patterns;
    }

    public Class[] getTypes() {
        return types;
    }

    /**
     * Capture this statement for processing.
     */
    public StatementPattern capture() {
        this.capture = true;
        return this;
    }

    /**
     * Get the Statement captured during the latest match.
     * Note: used to capture embedded statements.
     *
     * @return captured component
     */
    public Statement getCaptured() {
        return captured;
    }

    /**
     * Get everything captured by this the components of this pattern (when entire pattern matches).
     * Note: different from getCaptured(...) which is used to capture embedded statements.
     *
     * @param componentTypes the component types to get
     * @return the captured components
     */
    public Set<StatementComponent> getCaptures(Class... componentTypes) {
        Set<StatementComponent> captures = new HashSet<>();

        for (Pattern pattern : patterns) {
            if (pattern.getCaptured() != null) {
                if (componentTypes.length == 0) {
                    captures.add(pattern.getCaptured());
                } else if (containsTypes(pattern, componentTypes)) {
                    captures.add(pattern.getCaptured());
                }
            }
        }

        return captures;
    }

    /**
     * The inverse of the getCaptures(...) method.
     *
     * @param statement the statement that the captures are from
     * @param componentTypes the component types that limit the captures
     * @return the non-captured components
     */
    public Set<StatementComponent> getNonCaptures(Statement statement, Class... componentTypes) {
        Set<StatementComponent> components = statement.getComponents();
        components.removeAll(getCaptures(componentTypes));
        return components;
    }

    /**
     * Whether or not a pattern contains one of a number of types.
     * Used to find captured components.
     *
     * @param pattern pattern to test
     * @param componentTypes types to find
     * @return true if one of the types was found
     */
    private boolean containsTypes(Pattern pattern, Class[] componentTypes) {
        for (Class type : pattern.getTypes()) {
            for (Class componentType : componentTypes) {
                if (type.equals(componentType)) return true;
            }
        }

        return false;
    }

    /**
     * Match this StatementPattern against a component.
     *
     * @param statementComponent the component to test
     * @return true if the pattern matches the component
     */
    public boolean matches(StatementComponent statementComponent) {
        if (statementComponent == null) return false;

        // this pattern can only match statements!
        if (statementComponent instanceof Statement) {
            Statement statement = (Statement) statementComponent;
            Set<StatementComponent> components = statement.getComponents();
            if (components == null) return false;

            // for the entire statement to match, every contained pattern must match
            for (Pattern pattern : patterns) {
                if (!matches(pattern, components)) return false;
            }

            // note: must always be final step before returning true!
            if (capture) captured = statement;

            return true;
        }

        return false;
    }

    /**
     * Whether or not a contained pattern matches a component in a set of components.
     *
     * @param pattern the pattern to match against
     * @param components the set of components to test
     * @return true if one of the components matches
     */
    private boolean matches(Pattern pattern, Set<StatementComponent> components) {
        for (StatementComponent component : components) {
            if (pattern.matches(component)) return true;
        }

        return false;
    }
}
