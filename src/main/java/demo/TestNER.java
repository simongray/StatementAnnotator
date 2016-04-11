package demo;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.List;


public class TestNER {
    public static void main(String[] args) {
        try {
            // load the 4-class classifier
            String serializedClassifier = "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz";
            AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);

            String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution:" +
                                "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it." +
                                "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too." +
                                "We also got two Xiami Air purifiers that work quite well. They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers." +
                                "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison. If you live in a city you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). Of course, Western cities usually don't have those crazy smog days." +
                                "Don't worry too much. Sometimes there's no smog for a whole week, sometime's it lasts for a whole week and you'll just stay mostly indoors and use masks when outside. You'll get used to it.";


            System.out.println("Single word entities:");
            List<List<CoreLabel>> out = classifier.classify(example);
            for (List<CoreLabel> sentence : out) {
                for (CoreLabel word : sentence) {
                    // this gets you single word entities by default, not multi-word ones
                    String nerTag = word.get(CoreAnnotations.AnswerAnnotation.class);

                    // it seems that "O" (oh, not zero) is the default null value, although it doesn't work with != "O"
                    // using length() instead to get relevant tags
                    if (nerTag.length() > 1) {
                        System.out.println(word + " (" + nerTag + ")");
                    }
                }
            }

            System.out.println("Multi-word entities:");

            // for multi-word entities: http://stackoverflow.com/questions/13765349/multi-term-named-entities-in-stanford-named-entity-recognizer
            List<Triple<String,Integer,Integer>> entities = classifier.classifyToCharacterOffsets(example);

            for (Triple<String,Integer,Integer> entity : entities) {
                char[] entityChars = new char[entity.third() - entity.second()];
                example.getChars(entity.second(), entity.third(), entityChars, 0);
                System.out.println(new String(entityChars) + " (" + entity.first() + ")");
            }

        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

    }
}
