package statements.profile;

import edu.mit.jwi.item.POS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statements.core.*;
import statements.matching.Pattern;
import statements.matching.Proxy;
import statements.matching.WordnetDictionary;

import java.io.IOException;
import java.util.*;

public class Profile {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    Set<Statement> statements;
    WordnetDictionary dict;

    public Profile(Set<Statement> statements) throws IOException {
        this.dict = new WordnetDictionary();
        this.statements = statements;

        // unpack embedded statements according to a pattern
        // the original statements are replaced with the embedded statements based on the pattern
        Set<Statement> packedStatements = new HashSet<>();
        Set<Statement> unpackedStatements = new HashSet<>();

        Pattern thinkPattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb(dict.getSynonyms(POS.VERB, "think", "reckon", "believe")),
                Proxy.Statement()
        );

        for (Statement statement : statements) {
            if (thinkPattern.matches(statement)) {
                unpackedStatements.add(statement.getEmbeddedStatement());
                packedStatements.add(statement);
            }
        }

        statements.addAll(unpackedStatements);
        statements.removeAll(packedStatements);
        logger.info("unpacked " + packedStatements.size() + " statements");
    }

    public Set<Statement> getStatements() {
        return statements;
    }
}
