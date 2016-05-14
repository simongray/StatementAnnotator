package sentiment;

import edu.stanford.nlp.neural.rnn.RNNCoreAnnotations;
import edu.stanford.nlp.sentiment.SentimentCoreAnnotations;
import edu.stanford.nlp.trees.Tree;
import edu.stanford.nlp.util.CoreMap;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class LongestPathResolver implements ContextResolver {
    final Logger logger = LoggerFactory.getLogger(LongestPathResolver.class);
    public final static String NAME = "longestpath";

    @Override
    public void attachContext(List<SentimentTarget> targets, CoreMap sentence)  {
        logger.info("attaching sentiment to targets in sentence: " + sentence);
        Tree tree = sentence.get(SentimentCoreAnnotations.SentimentAnnotatedTree.class);

        parse(tree, 0);  // DEBUGGING

        if (targets.size() == 1) {
            SentimentTarget target = targets.get(0);

            try {
                target.setContext(tree);
                logger.info(target + " had its sentiment score set to the sentence sentiment: " + target.getSentiment());
            } catch (Exception e) {
                logger.error("could not set context for target " + target + ": " + e.getClass());
            }
        } else if (targets.size() > 1) {
            logger.info("multiple entities in sentence (" + targets + "), finding context for each");

            // get all possible paths from ROOT to every leaf
            List<List<Tree>> paths = new ArrayList<>();
            attachPaths(tree, new ArrayList<>(), paths);

            // reduce to only relevant paths
            removeIrrelevantPaths(targets, paths);

            // remove shared paths (= context) for each sentiment target
            removeSharedSections(paths);

            // TODO: insert step here that simply removes everything in each path above the first S
            //       unless there is no S, in which case nothing is removed (= for top level sentences)

            // set each targets sentiment score based on its own local context
            for (SentimentTarget target : targets) {
                List<Tree> relevantPath = paths.get(target.getTokenIndex() - 1);  // note: CoreNLP token indexes start at 1
                Tree localTree = relevantPath.get(0);
                try {
                    target.setContext(tree);
                    logger.info(target + " had its sentiment score set to: " + target.getSentiment());
                } catch (Exception e) {
                    logger.error("could not set context for target " + target + ": " + e.getClass());
                }
            }

        }
    }

    /**
     * Removes any shared sections from a list of paths.
     * The paths should come from the same sentiment-annotated parse tree.
     * @param paths
     */
    private void removeSharedSections(List<List<Tree>> paths) {
        // make a deep copy of each relevant List inside paths
        List<List<Tree>> otherPaths = new ArrayList<>();
        for (List<Tree> path : paths) {
            if (!path.isEmpty()) {
                otherPaths.add(new ArrayList<>(path));  // note: this part is shallow
            }
        }

        // use the copy to locate shared sections
        for (List<Tree> path : paths) {
            if (!path.isEmpty()) {
                int n = -1;  // = nothing should be removed

                for (List<Tree> otherPath : otherPaths) {
                    if (!otherPath.isEmpty()) {
                        if (path.get(path.size() - 1) != otherPath.get(otherPath.size() - 1)) {  // should not be same path
                            for (int i = 0; i < path.size() && i < otherPath.size(); i++) {

                                // find lowest common denominator
                                if (path.get(i) == otherPath.get(i)) {
                                    n = i;
                                } else {
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Empties paths without sentiment targets.
     * @param targets
     * @param paths
     * @return
     */
    private void removeIrrelevantPaths(List<SentimentTarget> targets, List<List<Tree>> paths) {
        Set<Integer> relevantPathIndexes = new HashSet<>();

        for (SentimentTarget target : targets) {
            relevantPathIndexes.add(target.getTokenIndex() - 1);  // note: CoreNLP token indexes start at 1
        }

        for (int i = 0; i < paths.size(); i++) {
            if (!relevantPathIndexes.contains(i)) {
                paths.set(i, new ArrayList<>());
            }
        }
    }

    /**
     * Recursively attaches all possible paths in the parse tree to a list of paths.
     * @param tree
     * @param path
     * @param paths
     */
    private void attachPaths(Tree tree, List<Tree> path, List<List<Tree>> paths) {
        path.add(tree);

        if (tree.isLeaf()) {
            paths.add(path);
        } else {
            for (Tree child : tree.children()) {
                attachPaths(child, new ArrayList<>(path), paths);
            }
        }
    }

    // TODO: used for debugging, remove eventually
    public void parse(Tree tree, int n) {
        // RNNCoreAnnotations.getPredictedClass(tree) returns the sentiment analysis score from 0 to 4 (with -1 for n/a)
        logger.info("parse " + new String(new char[n]).replace("\0", "- ")
                + tree.value() + ": " + RNNCoreAnnotations.getPredictedClass(tree)
        );

        for (Tree child : tree.children()) {
            parse(child, n+1);
        }
    }
}
