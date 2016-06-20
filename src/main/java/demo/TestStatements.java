package demo;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
import statements.core.StatementComponent;
import statements.core.StatementUtils;

import java.util.List;
import java.util.Properties;
import java.util.Set;


public class TestStatements {
    public static void main(String[] args) {
        // setting up the pipeline
        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, parse, depparse, subjectobject, sentiment, sentimenttargets");  // long pipeline
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, subjectobject");  // short pipeline
        props.setProperty("customAnnotatorClass.subjectobject", "statements.StatementAnnotator");
        props.setProperty("customAnnotatorClass.sentimenttargets", "sentiment.SentimentTargetsAnnotator");
        props.put("ner.model", "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
//        props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");  // fast, more memory usage
        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");  // slow, less memory usage
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

//        String example = "The amazing and furious Henry Larsson of Sweden doesn't like doing anything in particular.";
//        String example = "The amazing Henry doesn't like doing anything in particular.";
//        String example = "Hey, cool! Very cool, in fact. Henry, Louis the Dragon and Sally Bates don't like doing anything in particular.";

        // TODO: consider if subjects like this should be split into multiple statements like verbs are
//        String example = "Henry, Louis the Dragon or Sally Bates don't like doing anything in particular.";

//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament."; // TODO: this one is still tough
//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament... \n" +
//        "I'm sure you meant the EU as a whole and not the European Parliament specifically, just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";
//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution: " +
//        "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it. " +
//        "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too. " +
//        "We also got two Xiaomi air purifiers that work quite well. They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers. " +
//        "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison. If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). Of course, Western cities usually don't have those crazy smog days. " +
//        "Don't worry too much. Sometimes there's no smog for a whole week, sometime's it lasts for a whole week and you'll just stay mostly indoors and use masks when outside. You'll get used to it.";
//        String example = "They aren't pretty. She's having to make do. He really doesn't love singing out loud.";
//        String example = "He really doesn't love singing out loud.";
//        String example = "She hates flying and he loves it.";

        // TODO: the inverted first version fucks stuff up
//        String example = "Sally and Mads in particular don't like doing anything. Sally and Mads don't like doing anything in particular.";

//        String example = "They don't like doing anything in particular and neither does she.";

//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution:";
//        String example =    "We have found that it is quite manageable.";

//        String example = "Here's our solution: Use an air quality app.";
//        String example = "I just have a widget on my Android phone that says the current AQI from the nearest measuring station.";
//        String example = "Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+.";

        // TODO: this fucks up majorly
//        String example = "Anyway, just make your own rule and stick to it.";

        // TODO: this fucks up
//        String example = "She and the rest speaks and shoots at her and him from here and there.";
//        String example = "He speaks to her and him.";

        // TODO: fucks up with verb conjunction, objects depends on "yells" not on "speaks", while subject depends on "speaks"
        String example = "He and she speaks and yells the words and sentences to her or him.";



        // TODO: the sentence "Hates and loves it." - doesn't separate into two statements, however doubtful if it is possible to do in a non-hackish way
//        String example = "Bought some 3M 95N-rated face masks for smoggy days.\n";
//        String example = "Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes.";
//        String example = "We also got two Xiaomi air purifiers that work quite well.";
//        String example = "She's having to make do. She speaks and shouts. The amazing and furious Henry Larsson of Sweden doesn't like doing anything in particular.";
//        String example =    "I don't care whether she likes me.";
//        String example =    "I think she's mad. I don't care whether she likes me. She says that they should go. I don't care whether or not they come.";
//        String example = "He doesn't like doing anything.";



        Annotation annotation = new Annotation(example);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            Set<Statement> statements = sentence.get(StatementsAnnotation.class);
            System.out.println(sentence);
            StatementUtils.printStatements(statements);
            System.out.println(statements);
        }
    }
}
