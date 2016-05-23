package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject implements StatementComponent, Resembling<IndirectObject> {
    private final IndexedWord primary;
    private final Set<IndexedWord> secondary;
    private final Set<IndexedWord> complete;
    private final Set<Set<IndexedWord>> compounds;

    public IndirectObject(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        this.primary = primary;
        this.secondary = secondary;
        this.complete = graph.descendants(primary);

        // recursively discover all compound objects
        compounds = new HashSet<>();
        compounds.add(StatementUtils.findCompoundComponents(primary, graph, null));
        for (IndexedWord object : secondary) {
            compounds.add(StatementUtils.findCompoundComponents(object, graph, null));
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
     * The primary single indirect object contained within the complete indirect object.
     * @return primary object
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * The compound indirect objects.
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
        return compounds;
    }

    /**
     * The strings of all of the compound indirect objects.
     * @return compound indirect object strings
     */
    public Set<String> getCompoundStrings() {
        Set<String> compoundObjectStrings = new HashSet<>();
        for (Set<IndexedWord> compound : compounds) {
            compoundObjectStrings.add(StatementUtils.join(compound));
        }
        return compoundObjectStrings;
    }

    /**
     * The resemblance of another indirect object to this indirect object.
     * @param otherObject object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(IndirectObject otherObject) {
        return null;  // TODO
    }

    /**
     * The amount of individual objects contained within the complete indirect object.
     * @return objects count
     */
    public int size() {
        return secondary.size() + 1;
    }


    @Override
    public String toString() {
        return "IO: " + getName() + " (" + size() + ")";
    }
}
