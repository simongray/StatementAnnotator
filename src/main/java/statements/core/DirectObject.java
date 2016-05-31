package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;

import java.util.HashMap;
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

    /**
     * Describes which relations are ignored when producing compound subjects.
     */
    static {
        IGNORED_RELATIONS.add("nsubj");
        IGNORED_RELATIONS.add("nmod");

        IGNORED_COMPOUND_RELATIONS.addAll(IGNORED_RELATIONS);
        IGNORED_COMPOUND_RELATIONS.add("conj");
        IGNORED_COMPOUND_RELATIONS.add("cc");
    }
    private static final String NEG_RELATION = "neg";
    private static final String COP_RELATION = "cop";
    private Type type;
    private final Map<IndexedWord, Set<IndexedWord>> negationMap = new HashMap<>();
    private final Map<IndexedWord, Set<IndexedWord>> copulaMap = new HashMap<>();  // only applicable to copula objects

    public DirectObject(IndexedWord primary, Set<IndexedWord> secondary, Type type, SemanticGraph graph) {
        super(primary, secondary, graph);
        this.type = type;

        // find negations (relevant for COP (adjectives) and XCOMP (verb) types)
        negationMap.put(primary, StatementUtils.findSpecificDescendants(NEG_RELATION, primary, graph));
        for (IndexedWord object : secondary) {
            negationMap.put(object, StatementUtils.findSpecificDescendants(NEG_RELATION, object, graph));
        }

        // find copulas (only relevant for COP)
        if (type == Type.COP) {
            copulaMap.put(primary, StatementUtils.findSpecificDescendants(COP_RELATION, primary, graph));
            for (IndexedWord object : secondary) {
                copulaMap.put(object, StatementUtils.findSpecificDescendants(COP_RELATION, object, graph));
            }
        }
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
    }

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

    /**
     * The identifier for this component.
     *
     * @return identifier
     */
    @Override
    protected String getIdentifier() {
        return "DO";
    }
}
