package sentiment;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;
import java.util.Map;

public class MergedSentimentTargetsAnnotation implements CoreAnnotation<Map<String, List<SentimentTarget>>> {
    public Class<Map<String, List<SentimentTarget>>> getType() {
        return ErasureUtils.<Class<Map<String, List<SentimentTarget>>>>uncheckedCast(Map.class);
    }
}
