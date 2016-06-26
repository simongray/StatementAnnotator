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
    private final Map<IndexedWord, Set<IndexedWord>> markerMap;
    private final Map<IndexedWord, Set<IndexedWord>> ccMap;

    public Verb(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);

        // TODO: verbs find conjunctions by way of common governor, not conj relations!
        // find markers (only relevant for COP)
        ccMap = StatementUtils.makeRelationsMap(entries, Relations.CC, graph);

        // find markers (only relevant for COP)
        markerMap = StatementUtils.makeRelationsMap(entries, Relations.MARK, graph);
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
     * Markers for all contained entries.
     *
     * @return copulas
     */
    public Set<IndexedWord> getMarkers() {
        Set<IndexedWord> markers = new HashSet<>();
        for (Set<IndexedWord> markerSet : markerMap.values()) {
            markers.addAll(markerSet);  // TODO: needs to search though descendants too
        }
        return markers;
    }

    /**
     * CC for all contained entries.
     *
     * @return
     */
    public Set<IndexedWord> getCC() {
        Set<IndexedWord> cc = new HashSet<>();
        for (Set<IndexedWord> ccSet : ccMap.values()) {
            cc.addAll(ccSet);  // TODO: needs to search though descendants too
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
