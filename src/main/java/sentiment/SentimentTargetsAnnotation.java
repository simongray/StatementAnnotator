package sentiment;

import edu.stanford.nlp.ling.CoreAnnotation;
import edu.stanford.nlp.util.ErasureUtils;

import java.util.Set;

public class SentimentTargetsAnnotation implements CoreAnnotation<Set<SentimentTarget>> {
    public Class<Set<SentimentTarget>> getType() {
        return ErasureUtils.<Class<Set<SentimentTarget>>>uncheckedCast(Set.class);
    }
}
