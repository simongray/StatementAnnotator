package statements.patterns;

import statements.core.StatementComponent;

import java.util.Set;

public interface Pattern {
    boolean matches(StatementComponent statementComponent);
    StatementComponent getCaptured();
    Class[] getTypes();
}
