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

    public Verb(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);

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
     * Markers for all contained verbs.
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
