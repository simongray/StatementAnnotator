package statements.patterns;

import statements.core.StatementComponent;

public interface Pattern {
    boolean matches(StatementComponent statementComponent);
}
