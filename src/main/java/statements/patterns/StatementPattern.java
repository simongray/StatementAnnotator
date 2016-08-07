package statements.patterns;

import statements.core.Statement;
import statements.core.StatementComponent;

import java.util.Set;

public class StatementPattern implements Pattern {
    private final Pattern[] patterns;

    public StatementPattern(Pattern... patterns) {
        this.patterns = patterns;
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
