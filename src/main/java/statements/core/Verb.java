package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The complete primary of a natural language statement.
 */
public class Verb extends AbstractComponent implements Resembling<Verb> {
    private final Map<IndexedWord, Set<IndexedWord>> ccMap;

    public Verb(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);

        ccMap = StatementUtils.makeDescendantMap(entries, Relations.CC, graph);

        // TODO: review and revise this stanza + the one in getComplete()
        // in some special cases (e.g. with verb conjunctions) it is necessary to add the other compounds back in
        // the reason this is only performed when count > 1 (apart from saving resources)
        // is that verbs with cc relations that are NOT in verb conjunctions, would display as "loves and" or "says or"
        if (count() > 1) {
            for (Set<IndexedWord> compound : getCompounds()) {
                complete.addAll(compound);
            }

            // as well as the missing markers for those compounds (CC is ignored for compounds)
            for (Set<IndexedWord> cc : ccMap.values()) {
                complete.addAll(cc);
            }
        }
    }

    @Override
    public Set<IndexedWord> getComplete() {
        // this is done in order to prevent silly cases where the verb is in a subject conjunction
        // i.e. "she loves it and he hates it" should not return "loves and" as the complete verb for "she loves it"
        if (count() == 1) {
            return StatementUtils.without(getCC(), complete);
        } else {
            return complete;
        }
    }

    /**
     * Describes which relations are ignored when producing the complete subject.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_VERB_RELATIONS;
    }

    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return Relations.IGNORED_VERB_COMPOUND_RELATIONS;
    }

    /**
     * The string of the complete verb.
     *
     * @return the longest string possible
     */
    public String getString() {
        return StatementUtils.join(StatementUtils.without(getMarkers(), complete));
    }

    /**
     * CC for all contained entries.
     *
     * @return cc
     */
    public Set<IndexedWord> getCC() {
        Set<IndexedWord> cc = new HashSet<>();
        for (Set<IndexedWord> ccSet : ccMap.values()) {
            cc.addAll(ccSet);
        }
        return cc;
    }

    /**
     * The resemblance of this object to another object.
     *
     * @param otherObject subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Verb otherObject) {
        return null;  // TODO
    }
}
