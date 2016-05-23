package statements.core;

import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.trees.TypedDependency;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;

/**
 * Finds subjects in sentences.
 */
public class IndirectObjectFinder {
    private static final Logger logger = LoggerFactory.getLogger(IndirectObjectFinder.class);
    private static final String NMOD_RELATION = "nmod";

    /**
     * The subjects that are found in a sentence.
     * @param graph the dependency graph of a sentence
     * @return subjects
     */
    public static Set<IndirectObject> find(SemanticGraph graph) {
        Collection<TypedDependency> dependencies = graph.typedDependencies();
        Set<IndexedWord> indirectObjects = new HashSet<>();
        Set<IndirectObject> objects = new HashSet<>();

        // find simple objects from relations
        for (TypedDependency dependency : dependencies) {
            if (dependency.reln().getShortName().equals(NMOD_RELATION)) {
                indirectObjects.add(dependency.dep());
            }
        }

        return objects;
    }
}
