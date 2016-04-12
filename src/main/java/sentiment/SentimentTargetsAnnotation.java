package sentiment;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.List;

public class SentimentTargetsAnnotation implements CoreAnnotation<List<SentimentTarget>> {
    public Class<List<SentimentTarget>> getType() {
        return ErasureUtils.<Class<List<SentimentTarget>>>uncheckedCast(List.class);
    }
}