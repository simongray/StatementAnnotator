package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * The complete direct object of a natural language statement.
 */
public class DirectObject extends AbstractComponent implements Resembling<DirectObject> {
    /**
     * Different kinds of direct objects.
     */
    public enum Type {
        DOBJ,
        XCOMP,
        COP
    }

    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    private static final Set<String> IGNORED_COMPOUND_RELATIONS = new HashSet<>();
    static {
        IGNORED_RELATIONS.add(Relations.NSUBJ);
        IGNORED_RELATIONS.add(Relations.NMOD);

        IGNORED_COMPOUND_RELATIONS.addAll(IGNORED_RELATIONS);
        IGNORED_COMPOUND_RELATIONS.add(Relations.CONJ);
        IGNORED_COMPOUND_RELATIONS.add(Relations.CC);
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

    private Type type;
    private final Map<IndexedWord, Set<IndexedWord>> negationMap;
    private final Map<IndexedWord, Set<IndexedWord>> copulaMap;  // only applicable to copula objects

    // TODO: shouldn't be necessary to set type manually
    public DirectObject(IndexedWord primary, Set<IndexedWord> secondary, Type type, SemanticGraph graph) {
        super(primary, secondary, graph);
        this.type = type;

        // find negations (relevant for COP (adjectives) and XCOMP (verb) types)
        negationMap = StatementUtils.makeRelationsMap(entries, Relations.NEG, graph);

        // find copulas (only relevant for COP)
        copulaMap = StatementUtils.makeRelationsMap(entries, Relations.COP, graph);
    }

    /**
     * The string of the complete indirect object.
     *
     * @return the longest string possible
     */
    public String getString() {
        return StatementUtils.join(StatementUtils.without(getCopulas(), complete));
    }

    /**
     * Whether this direct object has a copula.
     *
     * @return true if has
     */
    public Type getType() {
        return type;
    }  // TODO: is it relevant?

    /**
     * The negations for a specific contained object.
     *
     * @param object simple direct object
     * @return negations
     */
    public Set<IndexedWord> getNegations(IndexedWord object) {
        return new HashSet<>(negationMap.get(object));
    }

    /**
     * Whether a certain contained object is negated.
     *
     * @param object simple direct object
     * @return true if negated
     */
    public boolean isNegated(IndexedWord object) {
        return StatementUtils.isNegated(negationMap.get(object));
    }


    /**
     * The copula(s) for a specific contained object.
     *
     * @param object simple direct object
     * @return copulas
     */
    public Set<IndexedWord> getCopulas(IndexedWord object) {
        return new HashSet<>(copulaMap.get(object));
    }

    /**
     * Copulas for all contained objects.
     *
     * @return copulas
     */
    public Set<IndexedWord> getCopulas() {
        Set<IndexedWord> copulas = new HashSet<>();
        for (Set<IndexedWord> copulaSet : copulaMap.values()) {
            copulas.addAll(copulaSet);
        }
        return copulas;
    }

    /**
     * The resemblance of another direct object to this direct object.
     *
     * @param otherObject direct object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(DirectObject otherObject) {
        return null;  // TODO
    }
}
