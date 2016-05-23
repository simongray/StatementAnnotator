package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete direct object of a natural language statement.
 */
public class DirectObject implements Resembling<DirectObject> {
    private final IndexedWord primary;
    private Set<IndexedWord> secondary;
    private final Set<IndexedWord> complete;
    private final boolean copula;
    private final Set<Set<IndexedWord>> compounds;

    public DirectObject(IndexedWord primary, Set<IndexedWord> secondary, boolean copula, SemanticGraph graph) {
        this.primary = primary;
        this.secondary = secondary;
        this.copula = copula;
        this.complete = graph.descendants(primary);

        // recursively discover all compound objects
        compounds = new HashSet<>();
        compounds.add(StatementUtils.findCompoundComponents(primary, graph, null));
        for (IndexedWord subject : secondary) {
            compounds.add(StatementUtils.findCompoundComponents(subject, graph, null));
        }
    }

    /**
     * The name of the complete indirect object.
     * @return the longest name possible
     */
    public String getName() {
        return StatementUtils.join(complete);
    }

    /**
     * The primary single direct object contained within the complete direct object.
     * @return primary objct
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * The compound direct objects.
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
        return compounds;
    }

    /**
     * The strings of all of the compound direct objects.
     * @return compound direct object strings
     */
    public Set<String> getCompoundStrings() {
        Set<String> compoundSubjectNames = new HashSet<>();
        for (Set<IndexedWord> compound : compounds) {
            compoundSubjectNames.add(StatementUtils.join(compound));
        }
        return compoundSubjectNames;
    }

    /**
     * The resemblance of another direct object to this direct object.
     * @param otherObject direct object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(DirectObject otherObject) {
        return null;  // TODO
    }

    /**
     * The amount of individual subjects contained within the complete direct object.
     * @return objects count
     */
    public int size() {
        return secondary.size() + 1;
    }


    @Override
    public String toString() {
        return getName() + " (" + size() + ")";
    }
}
