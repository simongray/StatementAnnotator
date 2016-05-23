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
public class DirectObject implements StatementComponent, Resembling<DirectObject> {
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
    private static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    static {
        IGNORED_RELATIONS.add("nsubj");
        IGNORED_RELATIONS.add("nmod");
    }
    private static final Set<String> IGNORED_CHILD_RELATIONS = new HashSet<>();
    static {
        IGNORED_CHILD_RELATIONS.addAll(IGNORED_RELATIONS);
        IGNORED_RELATIONS.add("conj");
        IGNORED_RELATIONS.add("cc");
    }
    private static final String NEG_RELATION = "neg";
    private static final String COP_RELATION = "cop";
    private final IndexedWord primary;
    private Set<IndexedWord> secondary;
    private Type type;
    private final Set<IndexedWord> complete;
    private final Map<IndexedWord, Set<IndexedWord>> negationMap = new HashMap<>();
    private final Map<IndexedWord, Set<IndexedWord>> copulaMap = new HashMap<>();  // only applicable to copula objects
    private final Set<Set<IndexedWord>> compounds;

    public DirectObject(IndexedWord primary, Set<IndexedWord> secondary, Type type, SemanticGraph graph) {
        this.primary = primary;
        this.secondary = secondary;
        this.type = type;
        this.complete = StatementUtils.findCompoundComponents(primary, graph, IGNORED_RELATIONS);

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

        // recursively discover all compound objects
        compounds = new HashSet<>();
        compounds.add(StatementUtils.findCompoundComponents(primary, graph, IGNORED_CHILD_RELATIONS));
        for (IndexedWord object : secondary) {
            compounds.add(StatementUtils.findCompoundComponents(object, graph, IGNORED_CHILD_RELATIONS));
        }
    }

    /**
     * The name of the complete indirect object.
     * @return the longest name possible
     */
    public String getName() {
        return StatementUtils.join(withoutCopula(complete));
    }

    /**
     * Remove copulas from a set of words.
     * @param words words
     * @return words without copulas
     */
    private Set<IndexedWord> withoutCopula(Set<IndexedWord> words) {
        Set<IndexedWord> newWords = new HashSet<>(words);
        newWords.removeIf(indexedWord -> getCopulas().contains(indexedWord));
        return newWords;
    }

    /**
     * The primary single direct object contained within the complete direct object.
     * @return primary object
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * The compound direct objects.
     * @return compounds
     */
    public Set<Set<IndexedWord>> getCompounds() {
        return compounds;
    }

    /**
     * The strings of all of the compound direct objects.
     * @return compound direct object strings
     */
    public Set<String> getCompoundStrings() {
        Set<String> compoundObjectStrings = new HashSet<>();
        for (Set<IndexedWord> compound : compounds) {
            compoundObjectStrings.add(StatementUtils.join(compound));
        }
        return compoundObjectStrings;
    }

    /**
     * Whether this direct object has a copula.
     * @return true if has
     */
    public Type getType() {
        return type;
    }

    /**
     * The negations for a specific contained object.
     * @param object simple direct object
     * @return negations
     */
    public Set<IndexedWord> getNegations(IndexedWord object) {
        return new HashSet<>(negationMap.get(object));
    }

    /**
     * Whether a certain contained object is negated.
     * @param object simple direct object
     * @return true if negated
     */
    public boolean isNegated(IndexedWord object) {
        return StatementUtils.isNegated(negationMap.get(object));
    }


    /**
     * The copula(s) for a specific contained object.
     * @param object simple direct object
     * @return copulas
     */
    public Set<IndexedWord> getCopulas(IndexedWord object) {
        return new HashSet<>(copulaMap.get(object));
    }

    /**
     * Copulas for all contained objects.
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
     * @param otherObject direct object to be compared with
     * @return resemblance
     */
    @Override
    public Resemblance resemble(DirectObject otherObject) {
        return null;  // TODO
    }

    /**
     * The amount of individual objects contained within the complete direct object.
     * @return objects count
     */
    public int size() {
        return secondary.size() + 1;
    }


    @Override
    public String toString() {
        return "DO: " + getName() + " (" + size() + ")";
    }
}
