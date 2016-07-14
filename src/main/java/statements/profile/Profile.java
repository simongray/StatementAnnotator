package statements.profile;


import statements.core.Statement;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class Profile {
    Collection<Statement> statements;

    public Profile(Collection<Statement> statements) {
        this.statements = statements;
    }

    public Set<Statement> filter(Predicate predicate) {
        Set<Statement> filteredStatements = new HashSet<>();

        for (Statement statement : statements) {
            if (predicate.evaluate(statement)) filteredStatements.add(statement);
        }

        return filteredStatements;
    }
}
