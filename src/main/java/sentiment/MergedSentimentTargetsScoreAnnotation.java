package sentiment;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;
import java.util.Map;

public class MergedSentimentTargetsScoreAnnotation implements CoreAnnotation<Map<String, Integer>> {
    public Class<Map<String, Integer>> getType() {
        return ErasureUtils.<Class<Map<String, Integer>>>uncheckedCast(Map.class);
    }
}
