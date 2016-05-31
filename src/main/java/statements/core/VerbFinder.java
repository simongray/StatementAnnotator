package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import edu.stanford.nlp.util.Index;
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
        Map<IndexedWord, Set<IndexedWord>> conjunctions = new HashMap<>();
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


        // TODO: add multiple verbs to Verb object if found in "conj"
        // find verb conjunctions
        for (IndexedWord simpleVerb : simpleVerbs) {
            for (IndexedWord child : graph.getChildren(simpleVerb)) {
                logger.info("verb: " + simpleVerb + " --- child: " + child);
                logger.info("relation: " + graph.reln(simpleVerb, child).getShortName());
                if (graph.reln(simpleVerb, child).getShortName().equals(CONJ_RELATION)) {
                    Set<IndexedWord> conjuctedVerbs = conjunctions.getOrDefault(simpleVerb, new HashSet<>());
                    conjuctedVerbs.add(child);
                    conjunctions.put(simpleVerb, conjuctedVerbs);
                }
            }
        }

        Set<IndexedWord> allConjunctedVerbs = new HashSet<>();
        for (Set<IndexedWord> conjuctedVerbs : conjunctions.values()) {
            allConjunctedVerbs.addAll(conjuctedVerbs);
        }

        // remaining verbs are only included if not connected to the main verb
        for (IndexedWord simpleVerb : simpleVerbs) {
            IndexedWord parent = graph.getParent(simpleVerb);
            if (!simpleVerbs.contains(parent) && !allConjunctedVerbs.contains(simpleVerb) && !conjunctions.keySet().contains(simpleVerb)) {
                conjunctions.put(simpleVerb, new HashSet<>());
            }
        }

        for (IndexedWord simpleVerb : conjunctions.keySet()) {
            verbs.add(new Verb(simpleVerb, conjunctions.get(simpleVerb), graph));
        }

        logger.info("simple verbs: " + simpleVerbs);
        logger.info("verb conjunctions: " + conjunctions);

        return verbs;
    }
}
