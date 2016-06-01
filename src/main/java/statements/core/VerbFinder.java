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
    private static final String NSUBJPASS_RELATION = "nsubjpass";  // for passives
    private static final String DOBJ_RELATION = "dobj";
    private static final String XCOMP_RELATION = "xcomp";  // e.g "she <verb>s to <verb>" or "she <verb>ed <verb>ing"
    private static final Set<String> RELATIONS = new HashSet<>();
    static {
        RELATIONS.add(NSUBJ_RELATION);
        RELATIONS.add(NSUBJPASS_RELATION);
        RELATIONS.add(DOBJ_RELATION);
    }
    private static final String COP_RELATION = "cop";  // copula, ex: in "they're pretty" the "'re" would be copula
    private static final String CONJ_RELATION = "conj";  // conjunctions

    /**
     * The verbs that are found in a sentence.
     *
     * @param graph the dependency graph of a sentence
     * @return verbs
     */
    public static Set<Verb> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> simpleVerbs = new HashSet<>();
        Set<IndexedWord> adjectives = new HashSet<>();
        Set<IndexedWord> xcompVerbs = new HashSet<>();  // for verbs that act as objects of another verb
        Set<Verb> verbs = new HashSet<>();

        // find candidate verbs and adjectives from relations
        for (TypedDependency dependency : dependencies) {
            if (RELATIONS.contains(dependency.reln().getShortName())) {
                simpleVerbs.add(dependency.gov());
            }
            if (dependency.reln().getShortName().equals(COP_RELATION)) {
                adjectives.add(dependency.gov());
            }
            if (dependency.reln().getShortName().equals(XCOMP_RELATION)) {
                xcompVerbs.add(dependency.dep());
            }
        }

        // remove adjectives from candidate verbs
        simpleVerbs.removeAll(adjectives);

        // remove verbs that act as objects
        simpleVerbs.removeAll(xcompVerbs);

        for (IndexedWord simpleVerb : simpleVerbs) {
            verbs.add(new Verb(simpleVerb, null, graph));
        }

        logger.info("simple verbs: " + simpleVerbs);

        return verbs;
    }
}
