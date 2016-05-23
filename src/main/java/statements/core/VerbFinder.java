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
    private static final String COP_RELATION = "cop";  // copula, ex: in "they're pretty" the "'re" would be copula
    private static final String CONJ_RELATION = "conj";  // conjunctions

    /**
     * The verbs that are found in a sentence.
     * @param graph the dependency graph of a sentence
     * @return verbs
     */
    public static Set<Verb> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> simpleVerbs = new HashSet<>();
        Set<IndexedWord> adjectives = new HashSet<>();
        Set<Verb> verbs = new HashSet<>();

        // find candidate verbs and adjectives from relations
        for (TypedDependency dependency : dependencies) {
            if (RELATIONS.contains(dependency.reln().getShortName())) {
                simpleVerbs.add(dependency.gov());
            }
            if (dependency.reln().getShortName().equals(COP_RELATION)) {
                adjectives.add(dependency.gov());
            }
        }

        // remove adjectives from candidate verbs
        simpleVerbs.removeAll(adjectives);

        // disregard verbs that are dependents (act as objects) to the other verb (unless part of a conjunction)
        for (IndexedWord simpleVerb : simpleVerbs) {
            IndexedWord parent = graph.getParent(simpleVerb);

            if (!simpleVerbs.contains(parent)) {
                verbs.add(new Verb(simpleVerb, graph));
            } else if (graph.reln(parent, simpleVerb).getShortName().equals(CONJ_RELATION)) {
                verbs.add(new Verb(simpleVerb, graph));
            }
        }

        return verbs;
    }
}
