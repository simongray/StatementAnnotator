package statements.core;

/**
 * This class is a formal way to represent the semantics of natural language.
 */
public abstract class Statement implements Resembling<Statement> {
    private StatementSubject subject;
    private StatementVerb verb;
    private StatementObject object;

    public Statement(StatementSubject subject, StatementVerb verb, StatementObject object) {
        this.subject = subject;
        this.verb = verb;
        this.object = object;
    }

    @Override
    public abstract Resemblance resemble(Statement otherStatement);
}
