package demo;

import edu.stanford.nlp.parser.lexparser.LexicalizedParser;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.AnnotatorImplementations;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import sentiment.*;

import java.util.List;
import java.util.Map;
import java.util.Properties;


public class TestSentimentTargets {
    public static void main(String[] args) {
        // setting up the pipeline
        Properties props = new Properties();
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, parse, sentiment, sentimenttargets");
        props.setProperty("customAnnotatorClass.sentimenttargets", "sentiment.SentimentTargetsAnnotator");
        props.put("ner.model", "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
//        props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");  // fast, more memory usage
        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");  // slow, less memory usage
        props.setProperty("sentimenttargets.composestyle", "yolo");  // TODO: change into actual style

        DemoTimer.start("pipeline launch");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        DemoTimer.stop();

        // annotate a piece of text
        DemoTimer.start("creating annotation");

//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament...\n" +
//                "I'm sure you meant the EU as a whole and not the European Parliament specifically, just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";
//////
//        String example = "Chai Jing started making the documentary when her as yet unborn daughter developed a tumour in the womb, which had to be removed very soon after her birth. Chai blames air pollution for the tumour. The film, which combines footage of a lecture with interviews and factory visits, has been compared with Al Gore's An Inconvenient Truth in both its style and likely impact. The film openly criticises state-owned energy companies, steel producers and coal factories, as well as showing the inability of the Ministry of Environmental Protection to act against the big polluters.\n" +
//                "\n" +
//                "Despite demonstrating the failure of China's regulations on pollution, the Chinese government at first did not censor the film. Instead, the People's Daily reposted the film alongside an interview with Chai, while Chen Jining, the recently appointed minister for environmental protection, praised the film, comparing its significance with Silent Spring, the 1962 book by US environmentalist Rachel Carson. However, within a week, the Communist Party’s publicity department confidentially ordered the film to be removed. An employee of China Business News was suspended for leaking the order.";

//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution:" +
//                "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it." +
//                "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too." +
//                "We also got two Xiaomi air purifiers that work quite well. They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers." +
//                "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison. If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). Of course, Western cities usually don't have those crazy smog days." +
//                "Don't worry too much. Sometimes there's no smog for a whole week, sometime's it lasts for a whole week and you'll just stay mostly indoors and use masks when outside. You'll get used to it.";


        String example = "Clinton is a cold person and a terrible human being, unlike Bernie Sanders who seems quite competent and like a true statesman. I really like Sanders, especially his ideas. In fact, Sanders deserves to win, Hillary Clinton is just there to earn more money. She doesn't want to help the people, she is just doing it for herself.";

        Annotation annotation = new Annotation(example);
//        Annotation annotation = new Annotation(
//                "George Bush was quite sentimental in his old days. " +
//                "George often thought highly of Clinton. " +
//                "He also really liked Hillary. Hillary Rodham, that is. " +
//                "She had a stranglehold on his mind and he couldn't stop thinking of her. " +
//                "In fact, Bush hated her, that piece of shit \"I hate Hillary Rodham Clinton so much\", he said to himself. " +
//                "He did like Bill Clinton, though. " +
//                "He was such as nice guy. "
//        );
        // NOTE TO SELF: common source of errors -> forgetting to put a space after every sentence

//        String singleTypeAntencedentsExample =
//                "Bill Clinton is an idiot. He is so stupid, I can't believe it and neither can she - or they, for that matter. " +
//                "Unfortunately, Hillary Clinton loves him. She loves him like no one else does. " +
//                "George Bush doesn't love Hillary or Bill Clinton. He just loves himself. And perhaps her too. "
//                "George and Hillary had a fling once. She is not over it yet. " +  // female antecedent
//                "Google is an organization, unlike Hillary Clinton or George Bush. They don't particularly like anyone. " +  // plural antecedent
//                "Bill Clinton and Google haven't had much interaction. He just goes over there sometimes. "  // male antecedent
        ;
//        Annotation annotation = new Annotation(singleTypeAntencedentsExample);


        DemoTimer.stop();

        DemoTimer.start("annotating");
        pipeline.annotate(annotation);
        DemoTimer.stop();

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
