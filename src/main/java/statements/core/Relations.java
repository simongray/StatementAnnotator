package statements.core;

import java.util.HashSet;
import java.util.Set;

/**
 * The relations used for the various finders.
 *
 * The various ignore lists are used to determine the exact boundaries of components in the dependency graph,
 * as well as the boundaries of the compounds within the components.
 *
 * The relations here are based on SemanticGraphCoreAnnotations.CollapsedCCProcessedDependenciesAnnotation.class
 * and the default Universal Dependencies English model (edu/stanford/nlp/models/parser/nndep/english_UD.gz).
 */
public class Relations {
    public static final String DEP = "dep";  // unknown dependency
    public static final String NSUBJ = "nsubj";  // for subjects
    public static final String NSUBJPASS = "nsubjpass";  // for passives
    public static final String CSUBJ = "csubj";   // for clausal subjects
    public static final String DOBJ = "dobj";  // direct objects (or verbs acting in their place)
    public static final String COP= "cop";  // copula, ex: in "they're pretty" the "'re" would be copula
    public static final String XCOMP = "xcomp";  // e.g "she <verb>s to <verb>" or "she <verb>ed <verb>ing"
    public static final String NMOD = "nmod";  // indicates that a preposition is present
    public static final String CCOMP = "ccomp"; // "we have found <that ....>"
    public static final String CONJ = "conj"; // connections between words based on "and", "or", etc.
    public static final String CC = "cc";  // the "and", "or", etc. itself
    public static final String NEG = "neg";  // negations such as "not" or "never"
    public static final String PUNCT = "punct";  // punctuation like ","
    public static final String MARK = "mark";  // for markers, e.g. "whether" or "that"
    public static final String ACL = "acl";  // clausal modifiers of nouns, e.g. "a way" --acl--> "to reduce stress"
    public static final String ACL_RELCL = "acl:relcl";  // a subtype of acl
    public static final String COMPOUND = "compound";  // e.g. "European" + "Parliament"
    public static final String ADVCL = "advcl";  // modifies a verb or adjective with additional description
    public static final String PARATAXIS = "parataxis";  // ex: "sometimes <X>, sometimes <Y>"
    public static final String ADVMOD = "advmod";  // ex: the "always" in "always on"
    public static final String AUX = "aux";  // ex: the "always" in "always on"
    public static final String IOBJ = "iobj";  // ex: the "always" in "always on"
    public static final String DISCOURSE = "discourse";  // useless words like "yeah"
    public static final String DET = "det";  // determiners like "a", "the", "some", "very"
    public static final String AMOD = "amod";  // adjectives describing nouns, e.g. "racist person"

    /**
     * Outgoing relations which are stored internally in any AbstractComponent, but not shown/used by default.
     *
     * An example is the NEG relation, which is separated out from the other words,
     * but used to adjust results when doing comparisons.
     */
    public static final Set<String> HIDDEN_INTERNAL_RELATIONS = new HashSet<>();
    static {
        HIDDEN_INTERNAL_RELATIONS.add(NEG);
        HIDDEN_INTERNAL_RELATIONS.add(PUNCT);
        HIDDEN_INTERNAL_RELATIONS.add(MARK);
        HIDDEN_INTERNAL_RELATIONS.add(NEG);
        HIDDEN_INTERNAL_RELATIONS.add(CC);
    }

    /**
     * Outgoing relations and their descendants which form dependent clauses for specific words.
     * These relations are used by the finders to find out which words to ignore when searching for components.
     * These dependent scopes later become internal parts of AbstractComponents,
     * but are not allowed themselves to contain components.
     */
    public static final Set<String> DEPENDENT_CLAUSE_SCOPES = new HashSet<>();
    static {
        DEPENDENT_CLAUSE_SCOPES.add(Relations.ACL);  // scope for description of a noun
        DEPENDENT_CLAUSE_SCOPES.add(Relations.ACL_RELCL);
        DEPENDENT_CLAUSE_SCOPES.add(Relations.ADVCL);  // scope for description of a verb/adjective
    }

    /**
     * Outgoing relations and their descendants which form dependent statements.
     * These scopes should be analysed separately from the root relations
     * and the statements found within them will become embedded into other relations.
     */
    public static final Set<String> EMBEDDED_STATEMENT_SCOPES = new HashSet<>();
    static {
        EMBEDDED_STATEMENT_SCOPES.add(CCOMP);
        EMBEDDED_STATEMENT_SCOPES.add(XCOMP);
        EMBEDDED_STATEMENT_SCOPES.add(CSUBJ);
    }

    /**
     * Outgoing relations which almost certainly lead to other components and should be ignored.
     */
    public static final Set<String> INTER_COMPONENT_RELATIONS = new HashSet<>();
    static {
        INTER_COMPONENT_RELATIONS.add(CONJ);
        INTER_COMPONENT_RELATIONS.add(DEP);  // TODO: is the best place to put this?
        INTER_COMPONENT_RELATIONS.add(Relations.PARATAXIS);  // TODO: is the best place to put this?
        INTER_COMPONENT_RELATIONS.add(Relations.NSUBJ);
        INTER_COMPONENT_RELATIONS.add(Relations.NSUBJPASS);
        INTER_COMPONENT_RELATIONS.add(Relations.DOBJ);
        INTER_COMPONENT_RELATIONS.add(Relations.NMOD);
        INTER_COMPONENT_RELATIONS.add(Relations.COP);
        INTER_COMPONENT_RELATIONS.add(Relations.IOBJ);
    }

    /**
     * Outgoing relations which are completely ignored when building the basic scope of any AbstractComponent.
     * These relations include the HIDDEN_INTERNAL_RELATIONS, DEPENDENT_CLAUSE_SCOPES, EMBEDDED_STATEMENT_SCOPES,
     * COMPONENT_RELATIONS, as well as a few special relations.
     */
    public static final Set<String> IGNORED_OUTGOING_RELATIONS = new HashSet<>();
    static {
        IGNORED_OUTGOING_RELATIONS.addAll(HIDDEN_INTERNAL_RELATIONS);
        IGNORED_OUTGOING_RELATIONS.addAll(DEPENDENT_CLAUSE_SCOPES);
        IGNORED_OUTGOING_RELATIONS.addAll(EMBEDDED_STATEMENT_SCOPES);
        IGNORED_OUTGOING_RELATIONS.addAll(INTER_COMPONENT_RELATIONS);
        IGNORED_OUTGOING_RELATIONS.add(AUX);  // only relevant for verbs, sometimes associated with non-verbs
        IGNORED_OUTGOING_RELATIONS.add(DISCOURSE);  // usually nothing of value in this relation
    }

    /**
     * Incoming relations which are ignored when connecting components in the statement finder.
     */
    public static final Set<String> IGNORED_CONNECTING_RELATIONS = new HashSet<>();
    static {
        IGNORED_CONNECTING_RELATIONS.add(CONJ);
        IGNORED_CONNECTING_RELATIONS.add(DEP);
        IGNORED_CONNECTING_RELATIONS.add(Relations.PARATAXIS);  // TODO: examine closer
        IGNORED_CONNECTING_RELATIONS.addAll(EMBEDDED_STATEMENT_SCOPES);  // TODO: added as part of solution to #57, re-examine
    }

    // TODO: rework everything below this point
    /**
     * The smallest scope for compounds.
     */
    public static final Set<String> SMALL_COMPOUND_SCOPE = new HashSet<>();
    static {
        SMALL_COMPOUND_SCOPE.add(COMPOUND);
        SMALL_COMPOUND_SCOPE.add(ADVMOD);  // TODO: consider whether it should be part of larger compound scope
    }
    public static final String INCLUDING = "including";
    public static final ComplexRelation INDIRECT_OBJECT_NMOD = ComplexRelation.getRelationExcludingSpecifics(NMOD, INCLUDING);
    public static final ComplexRelation DESCRIPTIVE_NMOD = ComplexRelation.getRelationIncludingSpecifics(NMOD, INCLUDING);
}
