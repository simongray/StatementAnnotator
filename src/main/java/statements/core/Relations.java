package statements.core;

/**
 * The relations used for the various finders.
 */
public class Relations {
    public static final String NSUBJ = "nsubj";  // for subjects
    public static final String NSUBJPASS = "nsubjpass";  // for passives
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
}
