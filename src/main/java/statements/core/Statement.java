package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.HashSet;
import java.util.Set;

/**
 * A statement found in a natural language sentence.
 */
public class Statement implements StatementComponent, Resembling<Statement> {
    private Subject subject;
    private Verb verb;
    private DirectObject directObject;
    private IndirectObject indirectObject;
    private Statement childStatement;
    private Set<AbstractComponent> pureComponents;

    /**
     * Initialised by setting statement components as parameters. Params can be left null if the component type is N/A.
     * Initialisation should not be performed manually, but by using StatementFinder to build Statement objects.

     */
    public Statement(Set<AbstractComponent> pureComponents) {
        for (StatementComponent component : pureComponents) {
            if (component instanceof Subject) {
                subject = (Subject) component;
            } else if (component instanceof Verb) {
                verb = (Verb) component;
            } else if (component instanceof DirectObject) {
                directObject = (DirectObject) component;
            } else if (component instanceof IndirectObject) {
                indirectObject = (IndirectObject) component;
            }
        }
        this.pureComponents = pureComponents;
    }

    /**
     * The subject of the statement.
     *
     * @return subject
     */
    public Subject getSubject() {
        return subject;
    }

    /**
     * The verb of the statement.
     *
     * @return verb
     */
    public Verb getVerb() {
        return verb;
    }

    /**
     * The direct object of the statement.
     *
     * @return direct object
     */
    public DirectObject getDirectObject() {
        return directObject;
    }

    /**
     * The indirect object of the statement.
     *
     * @return indirect object
     */
    public IndirectObject getIndirectObject() {
        return indirectObject;
    }

    /**
     * The pure components making up the statement (not including any nested statement).
     *
     * @return components
     */
    public Set<AbstractComponent> getPureComponents() {
        // TODO: do away with this eventually
        return new HashSet<>(pureComponents);
    }

    /**
     * All components making up the statement (including any nested statement).
     *
     * @return components
     */
    public Set<StatementComponent> getComponents() {
        Set<StatementComponent> components = new HashSet<>(pureComponents);
        if (childStatement != null) components.add(childStatement);
        return components;
    }

    /**
     * Every word of the statement.
     *
     * @return words
     */
    public Set<IndexedWord> getComplete() {
        Set<IndexedWord> complete = new HashSet<>();

        for (StatementComponent component : getComponents()) {
            // in case of pure components, getWords() is used as it includes ignored words
            if (component instanceof AbstractComponent) {
               complete.addAll(((AbstractComponent) component).getWords());
            } else {
                complete.addAll(component.getComplete());
            }
        }

        return complete;
    }

    @Override
    public boolean connectedTo(StatementComponent otherComponent) {
        return false;  // TODO: implement this based on method in AbstractComponent
    }

    /**
     * The size of the statement (number of tokens).
     * Useful for sorting statements.
     *
     * @return size
     */
    public int size() {
        return getComplete().size();
    }


    /**
     * The number of components in the statement.
     *
     * @return size
     */
    public int count() {
        return getComponents().size();
    }


    @Override
    public String toString() {
        return "{"
            + getClass().getSimpleName() +
            ": \"" + StatementUtils.join(getComplete()) + "\"" +
            (count() > 1? ", components: " + count() : "") +
        "}";
    }

    /**
     * Whether this statement includes a specific component.
     *
     * @param component component to search for
     * @return true if contains component
     */
    public boolean contains(StatementComponent component) {
        return getComponents().contains(component);
    }

    /**
     * Link this statement to a child statement (e.g. a dependent clause).
     * TODO: evaluate whether this is the optimal way of doings things
     *
     * @param childStatement
     */
    public void addChild(Statement childStatement) {
        this.childStatement = childStatement;
    }

    /**
     * The resemblance of another statement to this statement.
     *
     * @param otherStatement statement to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Statement otherStatement) {
        // check that components match
        if (subject == null && otherStatement.getSubject() != null) return Resemblance.NONE;
        if (verb == null && otherStatement.getVerb() != null) return Resemblance.NONE;
        if (directObject == null && otherStatement.getDirectObject() != null) return Resemblance.NONE;
        if (indirectObject == null && otherStatement.getIndirectObject() != null) return Resemblance.NONE;

        // reduce lowest valued resemblance state
        return StatementUtils.reduce(
            subject == null? null : subject.resemble(otherStatement.getSubject()),
            verb == null? null : verb.resemble(otherStatement.getVerb()),
            directObject == null? null : directObject.resemble(otherStatement.getDirectObject()),
            indirectObject == null? null : indirectObject.resemble(otherStatement.getIndirectObject())
        );
    }
}
