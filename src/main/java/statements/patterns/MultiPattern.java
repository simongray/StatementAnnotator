package statements.patterns;

import statements.core.StatementComponent;

/**
 * This class is a container for matching multiple Patterns at the same time.
 *
 * A normal StatementPattern requires all components to match (logical AND)
 * while this pattern only requires one component to match (logical OR).
 * Note: it currently does not support capturing components!
 */
public class MultiPattern {
    private final Pattern[] patterns;

    public MultiPattern(Pattern... patterns) {
        for (int i = 0; i < patterns.length; i++) {
            patterns[i] = new StatementPattern(patterns[i]);
        }

        this.patterns = patterns;
    }

    public boolean matches(StatementComponent statementComponent) {
        for (Pattern pattern : patterns) {
            if (pattern.matches(statementComponent)) {
                return true;
            }
        }

        return false;
    }
}
