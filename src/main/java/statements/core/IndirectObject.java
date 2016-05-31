package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject extends AbstractComponent implements Resembling<IndirectObject> {

    public IndirectObject(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);
    }

    /**
     * The name of the complete indirect object.
     * @return the longest name possible
     */
    public String getName() {
        return StatementUtils.join(complete);
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


    @Override
    public String toString() {
        return "IO: " + getName() + " (" + getPrimary().word() + ", " + size() + ")";
    }
}
