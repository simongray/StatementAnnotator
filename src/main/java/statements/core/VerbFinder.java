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

    private static final Set<String> VERB_RELATIONS = new HashSet<>();
    static {
        VERB_RELATIONS.add(Relations.NSUBJ);
        VERB_RELATIONS.add(Relations.NSUBJPASS);
        VERB_RELATIONS.add(Relations.DOBJ);
    }

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
            if (VERB_RELATIONS.contains(dependency.reln().getShortName())) {
                simpleVerbs.add(dependency.gov());
            }
            if (dependency.reln().getShortName().equals(Relations.COP)) {
                adjectives.add(dependency.gov());
            }
            if (dependency.reln().getShortName().equals(Relations.XCOMP)) {
                xcompVerbs.add(dependency.dep());
            }
        }

        // remove adjectives from candidate verbs
        simpleVerbs.removeAll(adjectives);

        // remove verbs that act as objects
        simpleVerbs.removeAll(xcompVerbs);

        for (IndexedWord simpleVerb : simpleVerbs) {
            verbs.add(new Verb(simpleVerb, graph));
        }

        logger.info("verbs found: " + verbs);

        return verbs;
    }
}
