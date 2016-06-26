package statements.core;

import java.util.HashSet;
import java.util.Set;

/**
 * The relations used for the various finders.
 *
 * The various ignore lists are used to determine the exact boundaries of components in the dependency graph,
 * as well as the boundaries of the compounds within the components.
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
    public static final String MARK = "mark";  // for markers, e.g. "whether"

    public static final Set<String> IGNORED_RELATIONS = new HashSet<>();
    public static final Set<String> IGNORED_VERB_RELATIONS = new HashSet<>();
    public static final Set<String> IGNORED_SUBJECT_RELATIONS = new HashSet<>();
    public static final Set<String> IGNORED_DIRECT_OBJECT_RELATIONS = new HashSet<>();

    public static final Set<String> IGNORED_COMPOUND_RELATIONS = new HashSet<>();
    public static final Set<String> IGNORED_VERB_COMPOUND_RELATIONS = new HashSet<>();
    public static final Set<String> IGNORED_SUBJECT_COMPOUND_RELATIONS = new HashSet<>();
    public static final Set<String> IGNORED_DIRECT_OBJECT_COMPOUND_RELATIONS = new HashSet<>();

    static {
        /**
         * Relations that are ignored when constructing ANY component.
         */
        IGNORED_RELATIONS.add(Relations.DEP);  // ignoring all unknown dependencies
        IGNORED_RELATIONS.add(Relations.PUNCT);  // ignoring all punctuation (can still be accessed for recomposing as text)
        IGNORED_SUBJECT_RELATIONS.addAll(IGNORED_RELATIONS);
        IGNORED_VERB_RELATIONS.addAll(IGNORED_RELATIONS);
        IGNORED_DIRECT_OBJECT_RELATIONS.addAll(IGNORED_RELATIONS);

        /**
         * Relations that are ignored when constructing subjects.
         */
        IGNORED_SUBJECT_RELATIONS.add(Relations.NMOD);

        /**
         * Relations that are ignored when constructing verbs.
         */
        IGNORED_VERB_RELATIONS.add(Relations.NSUBJ);
        IGNORED_VERB_RELATIONS.add(Relations.NSUBJPASS);
        IGNORED_VERB_RELATIONS.add(Relations.DOBJ);
        IGNORED_VERB_RELATIONS.add(Relations.NMOD);
        IGNORED_VERB_RELATIONS.add(Relations.XCOMP);
        IGNORED_VERB_RELATIONS.add(Relations.CCOMP);
        IGNORED_VERB_RELATIONS.add(Relations.CONJ);  // conjunct verbs are found using common governor, not conj relation!
        IGNORED_VERB_RELATIONS.add(Relations.CC);  // conjunct verbs are found using common governor, not conj relation!
        // TODO: figure out whether this needs to be adjusted in light of verb troubles

        /**
         * Relations that are ignored when constructing direct objects.
         */
        IGNORED_DIRECT_OBJECT_RELATIONS.add(Relations.NSUBJ);
        IGNORED_DIRECT_OBJECT_RELATIONS.add(Relations.NMOD);

        /**
         * Relations that are ignored when determining ANY compound boundaries.
         */
        IGNORED_COMPOUND_RELATIONS.add(Relations.CONJ);   // ignoring all links to conjunct verbs, nouns, etc.
        IGNORED_COMPOUND_RELATIONS.add(Relations.CC);     // ignoring all conjunction words (like "and", "or", etc.)

        /**
         * Relations that are ignored when determining subject compound boundaries.
         */
        IGNORED_SUBJECT_COMPOUND_RELATIONS.addAll(IGNORED_SUBJECT_RELATIONS);
        IGNORED_SUBJECT_COMPOUND_RELATIONS.addAll(IGNORED_COMPOUND_RELATIONS);

        /**
         * Relations that are ignored when determining verb compound boundaries.
         */
        IGNORED_VERB_COMPOUND_RELATIONS.addAll(IGNORED_VERB_RELATIONS);
        IGNORED_VERB_COMPOUND_RELATIONS.addAll(IGNORED_COMPOUND_RELATIONS);

        /**
         * Relations that are ignored when determining direct object compound boundaries.
         */
        IGNORED_DIRECT_OBJECT_COMPOUND_RELATIONS.addAll(IGNORED_DIRECT_OBJECT_RELATIONS);
        IGNORED_DIRECT_OBJECT_COMPOUND_RELATIONS.addAll(IGNORED_COMPOUND_RELATIONS);
    }
}
