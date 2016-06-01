package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Set;


/**
 * The complete subject of a natural language statement.
 */
public class Subject extends AbstractComponent implements Resembling<Subject> {

    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    private static final Set<String> IGNORED_COMPOUND_RELATIONS = new HashSet<>();
    static {
        IGNORED_COMPOUND_RELATIONS.add("conj");   // other simple subjects
        IGNORED_COMPOUND_RELATIONS.add("cc");     // words like "and"
        IGNORED_COMPOUND_RELATIONS.add("punct");  // punctuation like ","
    }

    public Subject(IndexedWord primary, Set<IndexedWord> secondary, SemanticGraph graph) {
        super(primary, secondary, graph);
    }

    /**
     * Describes which relations are ignored when producing the complete subject.
     */
    protected Set<String> getIgnoredRelations() {
        return IGNORED_RELATIONS;
    }

    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    protected Set<String> getIgnoredCompoundRelations() {
        return IGNORED_COMPOUND_RELATIONS;
    }

    /**
     * The resemblance of another subject to this subject.
     *
     * @param otherSubject subject to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(Subject otherSubject) {
        if (otherSubject == null) {
            return Resemblance.NONE;
        }

        if (getString().equals(otherSubject.getString())) {
            return Resemblance.FULL;  // identical complete subject
        }

        int resemblanceCount = 0;
//        Set<String> otherSubjectCompounds = otherSubject.getCompoundStrings();
//        for (String compound : getCompoundStrings()) {
//            if (otherSubjectCompounds.contains(compound)) {
//                resemblanceCount++;
//            }
//        }

        if (resemblanceCount == size()) {
            return Resemblance.CLOSE;  // identical subject compounds
        }

        if (resemblanceCount > 0) {
            return Resemblance.SLIGHT;  // at least 1 identical subject compound
        }

        return Resemblance.NONE;
    }
}
