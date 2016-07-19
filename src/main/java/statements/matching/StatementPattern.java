package statements.matching;


import statements.core.Statement;

public class StatementPattern {
    private final Statement statement;

    public StatementPattern(Statement statement) {
        this.statement = statement;
    }

    public boolean matches(Statement otherStatement) {
        if (statement.getSubject() != null && otherStatement.getSubject() != null) {
            String statementSubjectLemma = statement.getSubject().getNormalCompound();
            String otherStatementSubjectLemma = otherStatement.getSubject().getNormalCompound();

            if (statementSubjectLemma.equals(otherStatementSubjectLemma)) {
                return true;
            }
        }

        return false;
    }
}
