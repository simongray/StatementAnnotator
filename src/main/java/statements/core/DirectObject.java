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
     * The name of the complete indirect object.
     * @return the longest name possible
     */
    public String getName() {
        return directObject.word();  // TODO: implement full version
    }

    /**
     * The compound direct objects.
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
//        return new HashSet<>(compounds);
        return null;  // TODO: implement
    }

    /**
     * The resemblance of another direct object to this direct object.
     * @param otherObject direct object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(DirectObject otherDirectObject) {
        return null;  // TODO
    }
}
