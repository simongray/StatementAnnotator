package sentiment;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;
import java.util.Map;

public class SentenceSentimentTargetsAnnotation implements CoreAnnotation<Map<Integer, List<SentimentTarget>>> {
    public Class<Map<Integer, List<SentimentTarget>>> getType() {
        return ErasureUtils.<Class<Map<Integer, List<SentimentTarget>>>>uncheckedCast(Map.class);
    }
}
