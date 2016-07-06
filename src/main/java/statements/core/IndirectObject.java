package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.Set;

/**
 * The complete indirect object of a natural language statement.
 */
public class IndirectObject extends AbstractComponent implements Resembling<IndirectObject> {
    
    public IndirectObject(IndexedWord primary, SemanticGraph graph) {
        super(primary, null, graph);

        // COPY-PASTED FROM VERB CLASS
        // temporary hotfix, needed because sequences of indirect objects are now treated as conjunctions
        // TODO: review and revise this stanza
        // in some special cases (e.g. with verb conjunctions) it is necessary to add the other compounds back in
        // the reason this is only performed when count > 1 (apart from saving resources)
        // is that verbs with cc relations that are NOT in verb conjunctions, would display as "loves and" or "says or"
        if (count() > 1) {
            for (Set<IndexedWord> compound : getCompounds()) {
                complete.addAll(compound);
            }
        }

        // TODO: remove when done debugging
        System.out.println(getCompounds());
        System.out.println(getComplete());

        words.addAll(complete);  // needed since verb conjunctions are different from other components
    }

    /**
     * Describes which relations are ignored when producing the complete indirect object.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_INDIRECT_OBJECT_RELATIONS;
    }

    /**
     * Describes which relations are ignored when producing compound indirect objects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return Relations.IGNORED_INDIRECT_OBJECT_COMPOUND_RELATIONS;
    }


    /**
     * The resemblance of another indirect object to this indirect object.
     *
     * @param otherObject object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(IndirectObject otherObject) {
        return null;  // TODO
    }
}
