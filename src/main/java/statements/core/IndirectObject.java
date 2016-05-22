package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject implements Resembling<IndirectObject> {
    /**
     * The name of the complete verb.
     * @return the longest subject possible
     */
    public String getName() {
        return null;  // TODO: implement full version
    }

    /**
     * The compound objects.
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
//        return new HashSet<>(compounds);
        return null;  // TODO: implement
    }

    /**
     * The resemblance of another object to this object.
     * @param otherObject object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(IndirectObject otherObject) {
        return null;  // TODO
    }
}
