package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.text.DecimalFormat;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * A statement found in a natural language sentence.
 */
public class Statement implements StatementComponent {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    private Subject subject;
    private Verb verb;
    private DirectObject directObject;
    private IndirectObject indirectObject;
    private Statement embeddedStatement;
    private Set<AbstractComponent> pureComponents;
    private Set<IndexedWord> governors;
    private Set<IndexedWord> embeddingGovernors;
    private CoreMap origin;
    private Set<String> labels = new HashSet<>();

    @Override
    public boolean equals(Object object) {
        if (object instanceof Statement) {
            Statement otherStatement = (Statement) object;
            Set<StatementComponent> otherComponents = otherStatement.getComponents();
            Statement otherEmeddedStatement = otherStatement.getEmbeddedStatement();
            boolean sameEmbeddedStatement = getEmbeddedStatement() != null? getEmbeddedStatement().equals(otherEmeddedStatement) : otherEmeddedStatement == null;
            return otherComponents.equals(getComponents()) && sameEmbeddedStatement;
        }

        return false;
    }
    
    public CoreMap getOrigin() {
        return origin;
    }

    public void setOrigin(CoreMap origin) {
        this.origin = origin;
    }

    public void addLabel(String label) {
        this.labels.add(label);
    }

    public Statement getEmbeddedStatement() {
        return embeddedStatement;
    }

    public static Statement merge(Set<Statement> statements) {
        Set<StatementComponent> components = new HashSet<>();

        for (Statement statement : statements) {
            components.addAll(statement.getComponents());
        }

        return new Statement(components);
    }

    public Statement(Set<StatementComponent> components) {
        pureComponents = new HashSet<>();  // TODO: do away with pure components entirely
        embeddedStatement = null;

        for (StatementComponent component : components) {
            add(component);
        }
    }

    public Statement(AbstractComponent... components) {
        pureComponents = new HashSet<>();  // TODO: do away with pure components entirely
        embeddedStatement = null;

        for (StatementComponent component : components) {
            add(component);
        }
    }

    private void add(StatementComponent component) {
        if (component instanceof AbstractComponent) {
            AbstractComponent abstractComponent = (AbstractComponent) component;

            if (!pureComponents.contains(abstractComponent)) {
                if (abstractComponent instanceof Subject) {
                    subject = (Subject) abstractComponent;
                } else if (abstractComponent instanceof Verb) {
                    verb = (Verb) abstractComponent;
                } else if (abstractComponent instanceof DirectObject) {
                    directObject = (DirectObject) abstractComponent;
                } else if (abstractComponent instanceof IndirectObject) {
                    indirectObject = (IndirectObject) abstractComponent;
                }
                pureComponents.add(abstractComponent);
            }
        } else if (component instanceof Statement) {
            Statement statement = (Statement) component;

            for (StatementComponent containedComponent : statement.getComponents()) {
                add(containedComponent);
            }
        } else {
            // TODO: throw exception?
        }
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
     * Outgoing (= governing) words.
     * Useful for establishing whether this statement is connected to another component.
     *
     * @return governors
     */
    public Set<IndexedWord> getEmbeddingGovernors() {
        if (embeddingGovernors == null) {
            embeddingGovernors = new HashSet<>();
            for (StatementComponent component : getComponents()) {
                embeddingGovernors.addAll(component.getEmbeddingGovernors());
            }
        }

        // remove internal governors from other components
        // otherwise, a statement can be the parent of itself
        embeddingGovernors.removeAll(getCompound());

        return embeddingGovernors;
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
        Set<IndexedWord> compound = new HashSet<>();

        for (StatementComponent component : getComponents()) {
            compound.addAll(component.getCompound());
        }

        return compound;
    }

    /**
     * Every remaining word of the statement.  // TODO: revise
     *
     * @return compound
     */
    public Set<IndexedWord> getRemaining() {
        Set<IndexedWord> remaining = new HashSet<>();

        for (StatementComponent component : getComponents()) {
            remaining.addAll(component.getRemaining());
        }

        return remaining;
    }

    public Set<IndexedWord> getWords() {
        Set<IndexedWord> complete = new HashSet<>();
        complete.addAll(getCompound());
        complete.addAll(getRemaining());

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

    /**
     * The number of duplicate component types in the statement.
     * Useful for determining the quality of the statement,
     * as well as whether a component ought to be moved from this statement to another during statement finding (#57).
     *
     * @return duplicate count
     */
    public int duplicateCount() {
        int verbCount = 0;
        int subjectCount = 0;
        int directObjectCount = 0;
        int indirectObjectCount = 0;
        int embeddedStatementCount = 0;

        for (StatementComponent component : getComponents()) {
            if (component instanceof Verb) verbCount++;
            if (component instanceof Subject) subjectCount++;
            if (component instanceof DirectObject) directObjectCount++;
            if (component instanceof IndirectObject) indirectObjectCount++;
            if (component instanceof Statement) embeddedStatementCount++;
        }

        int duplicateCount = 0;
        duplicateCount += verbCount > 1? verbCount - 1 : 0;
        duplicateCount += subjectCount > 1? subjectCount - 1 : 0;
        duplicateCount += directObjectCount > 1? directObjectCount - 1 : 0;
        duplicateCount += indirectObjectCount > 1? indirectObjectCount - 1: 0;
        duplicateCount += embeddedStatementCount > 1? embeddedStatementCount - 1 : 0;

        return duplicateCount;
    }

    /**
     * The sentence string that can be constructed from this Statement.
     *
     * @return sentence
     */
    public String getSentence() {
        return StatementUtils.join(getWords());
    }

    /**
     * A summary is a string that quickly tells you which components this Statement contains.
     *
     * @return summary
     */
    public String getSummary() {
        List<String> summary = new ArrayList<>();

        if (getSubject() != null) {
            summary.add("S");
        }
        if (getVerb() != null) {
            summary.add("V");
        }
        if (getDirectObject() != null) {
            summary.add("DO");
        }
        if (getIndirectObject() != null) {
            summary.add("IO");
        }
        if (getEmbeddedStatement() != null) {
            summary.add("E");
        }

        return String.join("+", summary);
    }

    /**
     * Whether this statement includes a specific component.
     *
     * @param otherComponent component to search for
     * @return true if contains component
     */
    public boolean contains(StatementComponent otherComponent) {
        return getComponents().contains(otherComponent);
    }

    /**
     * Whether this statement contains a specific type of component.
     * Useful when flattening embedding statements.
     *
     * @param type component type to search for
     * @return true if contains type
     */
    public boolean containsType(Class type) {
        for (StatementComponent component : getComponents()) {
            if (component.getClass().equals(type)) return true;
        }

        return false;
    }

    /**
     * Whether this statement includes a set of components.
     *
     * @param otherComponents components to check
     * @return true if contains component
     */
    public boolean contains(Set<StatementComponent> otherComponents) {
        for (StatementComponent component : otherComponents) {
            if (!contains(component)) return false;
        }

        return true;
    }

    /**
     * Get the overlapping components of this statement and another.
     *
     * @param otherStatement the other statement
     * @return the overlapping components
     */
    public Set<StatementComponent> getOverlap(Statement otherStatement) {
        Set<StatementComponent> overlap = new HashSet<>(getComponents());
        overlap.retainAll(otherStatement.getComponents());
        return overlap;
    }

    /**
     * Produce a new Statement without certain components.
     *
     * @param components the components to remove
     * @return Statement without the specific components
     */
    public Statement withoutComponents(Set<StatementComponent> components) {
        Set<StatementComponent> reducedComponents = getComponents();
        reducedComponents.removeAll(components);
        return new Statement(reducedComponents);
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
     * The labels of a statement.
     * In most cases, statements don't have a label, but in some cases they can be useful.
     * Statement labels can be useful for knowing how to an embedded statement should be treated inside its parent.
     *
     * @return label
     */
    public Set<String> getLabels() {
        Set<String> allLabels = new HashSet<>(labels);

        for (StatementComponent component : getComponents()) {
            Set<String> componentLabels = component.getLabels();

            // find component label combinations that trigger relevant statement labels
            if (componentLabels != null) {
                if (componentLabels.contains(Labels.CSUBJ_VERB)) {
                    allLabels.add(Labels.SUBJECT);
                } else if (componentLabels.contains(Labels.XCOMP_VERB)) {
                    allLabels.add(Labels.DIRECT_OBJECT);
                }
            }
        }

        return allLabels;
    }

    public Set<IndexedWord> getAll() {
        Set<IndexedWord> all = new HashSet<>();
        for (StatementComponent component : getComponents()) {
            all.addAll(component.getAll());
        }

        return all;
    }

    // TODO: figure out if something like this should be used
    public boolean isGoodSize() {
        // Using as guide the number 12 found here: http://www.aje.com/en/arc/editing-tip-sentence-length/
        int preferredSize = 12;
        int upperLimit = preferredSize + preferredSize/4;
        int lowerLimit = preferredSize - preferredSize/4;

        return size() < upperLimit && size() > lowerLimit;
    }

    public boolean isQuestion() {
        return labels.contains(Labels.QUESTION);
    }

    public boolean isCitation() {
        return labels.contains(Labels.CITATION);
    }

    /**
     * Computes the lexical density of the words in the statement.
     * Based on the definitions found on: http://www.sltinfo.com/lexical-density/
     * @return the lexical density of the statement
     */
    public double getLexicalDensity() {
        double lexicalWords = 0.0;
        Set<IndexedWord> words = getWords();

        if (words.size() == 0) {
            logger.error("no words in statement from sentence: " + getOrigin() + ")");
            return 0.0;
        } else {
            for (IndexedWord word : words) {
                if (PartsOfSpeech.LEXICAL_WORDS.contains(word.tag())) lexicalWords += 1.0;
            }
            return lexicalWords / (double) words.size();
        }
    }

    @Override
    public String toString() {
        return "{" +
                (getLabels().isEmpty()? "" : String.join("/", getLabels()) + "/") +
                getSummary() +
                ": \"" + getSentence() + "\"" +
//            ", density: " + df.format(getLexicalDensity()) +  // TODO: remove after done debugging
                "}";
    }
}
