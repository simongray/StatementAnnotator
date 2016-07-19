package statements.matching;


import statements.core.AbstractComponent;
import statements.core.Statement;
import statements.core.StatementComponent;

import java.util.HashSet;
import java.util.Set;

public class Pattern {
    private final Statement statement;
    private final Set<Proxy> proxies;

    public Pattern(Statement statement) {
        this.statement = statement;
        this.proxies = null;
    }

    public Pattern(Proxy... proxies) {
        this.statement = null;
        this.proxies = new HashSet<>();
        for (Proxy proxy : proxies) this.proxies.add(proxy);
    }

    /**
     * Matches statement if all proxies match.
     *
     * @param statement statement to test
     * @return true if statement matches pattern
     */
    public boolean matches(Statement statement) {
        if (proxies != null) {
            int matchCount = 0;

            for (Proxy proxy : proxies) {
                if (proxy.matches(statement)) matchCount++;
            }

            if (matchCount == proxies.size()) {
                return true;
            }
        }

        return false;
    }

    public Set<StatementComponent> getMatchingComponents(Statement statement) {
        Set<StatementComponent> matchingComponents = new HashSet<>();

        for (Proxy proxy : proxies) {
            matchingComponents.addAll(proxy.getMatchingComponents(statement));
        }

        return matchingComponents;
    }

    public boolean test(Statement otherStatement) {
        if (statement.getSubject() != null && otherStatement.getSubject() != null) {
            String statementString = statement.getSubject().getNormalCompound();
            String otherStatementString = otherStatement.getSubject().getNormalCompound();

            if (statementString.equals(otherStatementString)) {
                return true;
            }
        }

        return false;
    }
}
