package statements.core;

/**
 * This class is a formal way to represent the semantics of natural language.
 */
public abstract class Statement implements Resembling<Statement> {
    private CompleteSubject subject;
    private StatementVerb verb;
    private StatementObject object;

    public Statement(CompleteSubject subject, StatementVerb verb, StatementObject object) {
        this.subject = subject;
        this.verb = verb;
        this.object = object;
    }

    @Override
    public abstract Resemblance resemble(Statement otherStatement);
}
