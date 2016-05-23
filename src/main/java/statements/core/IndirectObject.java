package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject implements Resembling<IndirectObject> {
    private IndexedWord indirectObject;
    private boolean copula;

    public IndirectObject(IndexedWord indirectObject, boolean copula) {
        this.indirectObject = indirectObject;
        this.copula = copula;
    }

    /**
     * The name of the complete indirect object.
     * @return the longest name possible
     */
    public String getName() {
        return indirectObject.word();  // TODO: implement full version
    }

    /**
     * The compound indirect objects.
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
//        return new HashSet<>(compounds);
        return null;  // TODO: implement
    }

    /**
     * The resemblance of another indirect object to this indirect object.
     * @param otherObject object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(IndirectObject otherIndirectObject) {
        return null;  // TODO
    }
}
