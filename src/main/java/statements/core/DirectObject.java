package statements.core;

import edu.stanford.nlp.ling.IndexedWord;

import java.util.Set;

/**
 * The complete direct object of a natural language statement.
 */
public class DirectObject implements Resembling<DirectObject> {
    private IndexedWord directObject;
    private boolean copula;

    public DirectObject(IndexedWord directObject, boolean copula) {
        this.directObject = directObject;
        this.copula = copula;
    }

    /**
     * The name of the complete verb.
     * @return the longest subject possible
     */
    public String getName() {
        return directObject.word();  // TODO: implement full version
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
    public Resemblance resemble(DirectObject otherObject) {
        return null;  // TODO
    }
}
