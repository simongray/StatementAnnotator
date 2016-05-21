package statements.core;

/**
 * This class represents a statement found in a sentence.
 * It is a formal way to represent the semantics of natural language.
 */
public abstract class Statement implements Resembling<Statement> {
    private CompleteSubject subject;
    private CompleteVerb verb;
    private CompleteObject object;

    public Statement(CompleteSubject subject, CompleteVerb verb, CompleteObject object) {
        this.subject = subject;
        this.verb = verb;
        this.object = object;
    }

    /**
     * The subject of the statement.
     * @return subject
     */
    public CompleteSubject getSubject() {
        return subject;
    }

    /**
     * The verb of the statement.
     * @return verb
     */
    public CompleteVerb getVerb() {
        return verb;
    }

    /**
     * The object of the statement.
     * @return object
     */
    public CompleteObject getObject() {
        return object;
    }

    /**
     * The resemblance of this statement to another statement.
     * @param otherStatement statement to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Statement otherStatement) {
        CompleteSubject otherSubject = otherStatement.getSubject();
        CompleteVerb otherVerb = otherStatement.getVerb();
        CompleteObject otherObject = otherStatement.getObject();

        // for S+V+O
        if (subject != null && verb != null && object != null) {
            return StatementUtils.reduce(
                    subject.resemble(otherSubject),
                    verb.resemble(otherVerb),
                    object.resemble(otherObject)
            );
        }

        // for S+V
        if (subject != null && verb != null) {
            return StatementUtils.reduce(
                    subject.resemble(otherSubject),
                    verb.resemble(otherVerb)
            );
        }

        // for V+O
        if (verb != null && object != null) {
            return StatementUtils.reduce(
                    verb.resemble(otherVerb),
                    object.resemble(otherObject)
            );
        }

        return Resemblance.NONE;
    }
}
