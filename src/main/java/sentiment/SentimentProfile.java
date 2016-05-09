package sentiment;

import edu.stanford.nlp.pipeline.Annotation;

import java.util.List;
import java.util.Map;

public class SentimentProfile {
    private final String name;
    private List<Annotation> annotations;

    public SentimentProfile(String name, List<Annotation> annotations) {
        this.name = name;
        this.annotations = annotations;

        for (Annotation annotation : annotations) {
            System.out.println("sentence -----> mentions:");
            Map<Integer, List<SentimentTarget>> mentionsPerSentence = annotation.get(SentenceSentimentTargetsAnnotation.class);
            for (int key : mentionsPerSentence.keySet()) {
                System.out.println(key + " -----> " + mentionsPerSentence.get(key));
            }
            System.out.println("\nentity -----> mentions:");
            Map<String, List<SentimentTarget>> mentionsPerEntity = annotation.get(MergedSentimentTargetsAnnotation.class);
            for (String key : mentionsPerEntity.keySet()) {
                System.out.println(key + " -----> " + mentionsPerEntity.get(key));
            }
            System.out.println("\nentity -----> score:");
            Map<String, Integer> scorePerEntity = annotation.get(MergedSentimentTargetsScoreAnnotation.class);
            for (String key : scorePerEntity.keySet()) {
                System.out.println(key + " -----> " + scorePerEntity.get(key));
            }
        }
    }

    public String getName() {
        return name;
    }

    // TODO: implement rest
}
