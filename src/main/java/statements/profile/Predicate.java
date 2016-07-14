package statements.profile;

import statements.core.Statement;

public interface Predicate {
    boolean evaluate(Statement statement);
}
