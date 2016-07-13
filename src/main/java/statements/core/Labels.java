package statements.core;


public class Labels {
    // component labels
    public static final String CSUBJ_VERB = "CsubjVerb";  // clausal subject Verb found using the csubj relation
    public static final String COP_VERB = "CopVerb";  // clausal subject Verb found using the csubj relation
    public static final String XCOMP_VERB = "XcompVerb";
    public static final String CONJ_PARENT_VERB = "ConjParentVerb";
    public static final String CONJ_CHILD_VERB = "ConjChildVerb";
    public static final String CONJ_INDIRECT_OBJECT = "ConjIndirectObject";  // fake conjunction, based on shared governance and sequences

    // statement labels
    // these labels are more simple than the component labels
    // they are designed to replace the label "Statement" with the component they act like
    public static final String SUBJECT = "Subject";  // statements that act as subjects
    public static final String DIRECT_OBJECT = "DirectObject";  // statements that act as subjects
    public static final String QUESTION = "Question";  // statements that act as subjects
}
