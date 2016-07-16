package statements.profile;


import edu.stanford.nlp.util.CoreMap;
import statements.core.*;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

public class Profile {
    Map<CoreMap, Set<Statement>> statements;
    static Set<String> uninterestingNouns = new HashSet<>();
    static {
        uninterestingNouns.add("it");
        uninterestingNouns.add("they");
        uninterestingNouns.add("them");
        uninterestingNouns.add("this");
        uninterestingNouns.add("that");
        uninterestingNouns.add("he");
        uninterestingNouns.add("she");
        uninterestingNouns.add("her");
        uninterestingNouns.add("him");
        uninterestingNouns.add("you");
        uninterestingNouns.add("here");
        uninterestingNouns.add("there");
    }


    public Profile(Map<CoreMap, Set<Statement>> statements) {
        this.statements = statements;
    }

    public Map<CoreMap, Set<Statement>> getInteresting() {
        Map<CoreMap, Set<Statement>> filteredStatements = new HashMap<>();

        for (CoreMap sentence : statements.keySet()) {
            Set<Statement> sentenceStatements = statements.get(sentence);
            if (sentenceStatements != null) {
                for (Statement statement : sentenceStatements) {
                    if (isInteresting(statement) && isWellFormed(statement)) {
                        Set<Statement> filteredSentenceStatements = filteredStatements.getOrDefault(sentence, new HashSet<>());
                        filteredSentenceStatements.add(statement);
                        filteredStatements.put(sentence, filteredSentenceStatements);
                    }
                }
            }
        }

        return filteredStatements;
    }

    private boolean isInteresting(Statement statement) {
        for (StatementComponent component : statement.getComponents()) {
            if (component instanceof Subject || component instanceof DirectObject) {
                if (uninterestingNouns.contains(((AbstractComponent) component).getPrimary().word().toLowerCase())) {
                    return false;
                }
            }
            if (component instanceof Statement) {
                return isInteresting((Statement) component);
            }
        }

        return true;
    }

    private boolean isWellFormed(Statement statement) {
        return statement.gaps() == 0;
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
