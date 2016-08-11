package statements.patterns;

import statements.core.AbstractComponent;
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
    private boolean optional;
    private Integer minSize;
    private Integer maxSize;
    private Boolean question;
    private Boolean citation;

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

    public StatementPattern question(Boolean state) {
        this.question = state;
        return this;
    }

    public StatementPattern question() {
        return question(true);
    }

    public StatementPattern citation(Boolean state) {
        this.citation = state;
        return this;
    }

    public StatementPattern citation() {
        return citation(true);
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
     * Mark this statement as optional.
     */
    public StatementPattern optional() {
        this.optional = true;
        return this;
    }

    public StatementPattern minSize(Integer amount) {
        this.minSize = amount;
        return this;
    }

    public StatementPattern maxSize(Integer amount) {
        this.maxSize = amount;
        return this;
    }

    public StatementPattern size(Integer amount) {
        this.minSize = amount;
        this.maxSize = amount;
        return this;
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
                } else if (containsTypes(componentTypes)) {
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

            // check statement booleans first
            if (question != null && statement.isQuestion() != question) return false;
            if (citation != null && statement.isCitation() != citation) return false;

            // it is possible to also state a preferred size
            if (minSize != null && components.size() < minSize) return false;
            if (maxSize != null && components.size() > maxSize) return false;

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
        // additional step taken if this pattern must match all of its specified type
        // all components of the relevant types are checked and if one doesn't match, the entire match fails
        if (pattern.mustMatchAll() && pattern instanceof ComponentPattern) {
            ComponentPattern componentPattern = (ComponentPattern) pattern;

            for (StatementComponent component : components) {
                if (component != null && component instanceof AbstractComponent) {
                    if (componentPattern.matchesTypes((AbstractComponent) component) && !pattern.matches(component)) return false;
                }
            }
        }

        // optional patterns are only matched if their types exist among the components tested
        // very useful for ensuring components have a specific form (if they ever appear)
        if (pattern.isOptional() && !pattern.containsTypes(components)) return true;

        // regular matching
        // only one match is needed
        for (StatementComponent component : components) {
            if (pattern.matches(component)) return true;
        }

        return false;
    }

    @Override
    public boolean mustMatchAll() {
        return false;  // doesn't matter since a Statement can only contain a single directly embedded statement anyway
    }

    @Override
    public boolean isOptional() {
        return optional;
    }
}
