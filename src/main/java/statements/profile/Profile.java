package statements.profile;


import edu.stanford.nlp.util.CoreMap;
import statements.core.Statement;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Profile {
    Map<CoreMap, Set<Statement>> statements;

    public Profile(Map<CoreMap, Set<Statement>> statements) {
        this.statements = statements;
    }

    public Map<CoreMap, Set<Statement>>  filter(Predicate predicate) {
        Map<CoreMap, Set<Statement>> filteredStatements = new HashMap<>();

        for (CoreMap sentence : statements.keySet()) {
            Set<Statement> sentenceStatements = statements.get(sentence);
            if (sentenceStatements != null) {
                for (Statement statement : sentenceStatements) {
                    if (predicate.evaluate(statement)) {
                        Set<Statement> filteredSentenceStatements = filteredStatements.getOrDefault(sentence, new HashSet<>());
                        filteredSentenceStatements.add(statement);
                        filteredStatements.put(sentence, filteredSentenceStatements);
                    }
                }
            }
        }

        return filteredStatements;
    }
}
