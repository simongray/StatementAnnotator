package sentiment;

import edu.stanford.nlp.util.CoreMap;

import java.util.List;

public interface ContextResolver {
    /**
     * Find and attach the appropriate context for each target in a sentence.
     * The context is a subtree of the sentence tree.
     * Different ContextResolver implementations resolve contexts in different ways.
     *
     * @param targets
     * @param sentence
     */
    public void attachContext(List<SentimentTarget> targets, CoreMap sentence);
}
