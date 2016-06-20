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
}
