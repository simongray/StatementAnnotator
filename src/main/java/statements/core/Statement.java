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
    private Statement embeddedStatement;
    private Set<AbstractComponent> pureComponents;
    private Set<IndexedWord> governors;

    public Statement(Set<AbstractComponent> pureComponents) {
        this(pureComponents, null);
    }

    public Statement(Set<AbstractComponent> pureComponents, Statement embeddedStatement) {
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
        this.embeddedStatement = embeddedStatement;
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
     * Outgoing (= governing) words.
     * Useful for establishing whether this statement is connected to another component.
     *
     * @return governors
     */
    public Set<IndexedWord> getGovernors() {
        if (governors == null) {
            governors = new HashSet<>();
            for (StatementComponent component : getComponents()) {
                governors.addAll(component.getGovernors());
            }
        }

        // remove internal governors from other components
        // otherwise, a statement can be the parent of itself
        governors.removeAll(getCompound());

        return governors;
    }

    /**
     * All components making up the statement (including any nested statement).
     *
     * @return components
     */
    public Set<StatementComponent> getComponents() {
        Set<StatementComponent> components = new HashSet<>(pureComponents);
        if (embeddedStatement != null) components.add(embeddedStatement);
        return components;
    }

    /**
     * Every word of the statement.
     *
     * @return compound
     */
    public Set<IndexedWord> getCompound() {
        Set<IndexedWord> complete = new HashSet<>();

        for (StatementComponent component : getComponents()) {
            complete.addAll(component.getCompound());
        }

        return complete;
    }

    /**
     * The size of the statement (number of tokens).
     * Useful for sorting statements.
     *
     * @return size
     */
    public int size() {
        return getCompound().size();
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
        return "{" +
            ((getLabel() != null)? getLabel() + "/": "") +
            getClass().getSimpleName() +
            ": \"" + StatementUtils.join(getCompound()) + "\"" +
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
     *
     * @param childStatement
     */
    public Statement embed(Statement childStatement) {
        return new Statement(pureComponents, childStatement);
    }


    /**
     * The label of a statement.
     * In most cases, statements don't have a label, but in some cases they can be useful.
     * Statement labels can be useful for knowing how to an embedded statement should be treated inside its parent.
     *
     * @return label
     */
    public String getLabel() {
        String label = null;

        for (StatementComponent component : getComponents()) {
            String componentLabel = component.getLabel();

            // find component label combinations that trigger relevant statement labels
            if (componentLabel != null) {
                if (componentLabel.equals(Labels.CSUBJVERB)) {
                    label = Labels.SUBJECT;
                }
            }
        }

        return label;
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
