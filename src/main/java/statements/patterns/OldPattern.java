package statements.patterns;


import statements.core.Statement;

import java.util.HashSet;
import java.util.Set;

public class OldPattern {
    private final Set<OldProxy> proxies;
    private final boolean strict;

    /**
     * Non-strict evaluation (= other components are allowed).
     *
     * @param proxies
     */
    public OldPattern(OldProxy... proxies) {
        this.strict = false;
        this.proxies = new HashSet<>();
        for (OldProxy proxy : proxies) this.proxies.add(proxy);
    }

    /**
     * Strict evaluation (= only the stated components are allowed).
     *
     * @param strict
     * @param proxies
     */
    public OldPattern(boolean strict, OldProxy... proxies) {
        this.strict = true;
        this.proxies = new HashSet<>();
        for (OldProxy proxy : proxies) this.proxies.add(proxy);
    }

    /**
     * Matches statement if all proxies match.
     *
     * @param statement statement to test
     * @return true if statement matches pattern
     */
    public boolean matches(Statement statement) {
        if (strict && statement.getComponents().size() != proxies.size()) return false;

        if (proxies != null) {
            int matchCount = 0;

            for (OldProxy proxy : proxies) {
                if (proxy.matches(statement)) matchCount++;
            }

            if (matchCount == proxies.size()) {
                return true;
            }
        }

        return false;
    }
}
