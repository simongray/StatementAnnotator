package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.HashSet;
import java.util.Set;

/**
 * A statement found in a natural language sentence.
 *
 * It is useful for representing the semantics of natural language in a more formal way.
 *
 */
public abstract class Statement implements Resembling<Statement> {
    private Subject subject;
    private Verb verb;
    private DirectObject directObject;
    private IndirectObject indirectObject;

    public Statement(Subject subject, Verb verb, DirectObject directObject, IndirectObject indirectObject) {
        this.subject = subject;
        this.verb = verb;
        this.directObject = directObject;
        this.indirectObject = indirectObject;
    }

    /**
     * The subject of the statement.
     * @return subject
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * The verb of the statement.
     * @return verb
     */
    public Verb getVerb() {
        return verb;
    }

    /**
     * The direct object of the statement.
     * @return direct object
     */
    public DirectObject getDirectObject() {
        return directObject;
    }

    /**
     * The indirect object of the statement.
     * @return indirect object
     */
    public IndirectObject getIndirectObject() {
        return indirectObject;
    }

    /**
     * The resemblance of another statement to this statement.
     * @param otherStatement statement to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Statement otherStatement) {
        return StatementUtils.reduce(
                subject.resemble(otherStatement.getSubject()),
                verb.resemble(otherStatement.getVerb()),
                directObject.resemble(otherStatement.getDirectObject()),
                indirectObject.resemble(otherStatement.getIndirectObject())
        );
    }

    @Override
    public String toString() {
        Set<IndexedWord> statement = new HashSet<>();

        if (subject != null) {
            for (Set<IndexedWord> compound : subject.getCompounds()) {
                statement.addAll(compound);
            }
        }

        if (verb != null) {
            statement.addAll(verb.getCompound());
        }

        if (directObject != null) {
            for (Set<IndexedWord> compound : directObject.getCompounds()) {
                statement.addAll(compound);
            }
        }

        if (indirectObject != null) {
            for (Set<IndexedWord> compound : indirectObject.getCompounds()) {
                statement.addAll(compound);
            }
        }

        return StatementUtils.join(statement);
    }
}
