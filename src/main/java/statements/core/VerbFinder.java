package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds verbs in sentences.
 */
public class VerbFinder {
    private static final Logger logger = LoggerFactory.getLogger(VerbFinder.class);
    private static final String NSUBJ_RELATION = "nsubj";
    private static final String DOBJ_RELATION = "dobj";
    private static final String XCOMP_RELATION = "xcomp";  // e.g "she <verb>s to <verb>" or "she <verb>ed <verb>ing"
    private static final Set<String> RELATIONS = new HashSet<>();
    static {
        RELATIONS.add(NSUBJ_RELATION);
        RELATIONS.add(DOBJ_RELATION);
        RELATIONS.add(XCOMP_RELATION);
    }

    /**
     * The verbs that are found in a sentence.
     * @param graph the dependency graph of a sentence
     * @return verbs
     */
    public static Set<CompleteVerb> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> simpleVerbs = new HashSet<>();
        Set<CompleteVerb> verbs = new HashSet<>();

        // find verbs from relations
        for (TypedDependency dependency : dependencies) {
            if (RELATIONS.contains(dependency.reln().getShortName())) {
                simpleVerbs.add(dependency.gov());
            }
        }

        // disregard verbs that are dependents (act as objects) to the other verb
        for (IndexedWord simpleVerb : simpleVerbs) {
            IndexedWord parent = graph.getParent(simpleVerb);
            if (!simpleVerbs.contains(parent)) {
                verbs.add(new CompleteVerb(simpleVerb, graph));
            }
        }

        return verbs;
    }
}