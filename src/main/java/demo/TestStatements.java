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
        props.setProperty("annotators", "tokenize, ssplit, pos, depparse, statements");  // short pipeline
        props.setProperty("customAnnotatorClass.statements", "statements.StatementAnnotator");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament... \n" +
//        "I'm sure you meant the EU as a whole and not the European Parliament specifically, just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";
//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution: " +
//        "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it. " +
//        "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too. " +
//        "We also got two Xiaomi air purifiers that work quite well. They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers. " +
//        "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison. If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). Of course, Western cities usually don't have those crazy smog days. " +
//        "Don't worry too much. Sometimes there's no smog for a whole week, sometime's it lasts for a whole week and you'll just stay mostly indoors and use masks when outside. You'll get used to it.";
//        String example = "They aren't pretty. She's having to make do. He really doesn't love singing out loud.";


        // TODO: the sentence "Hates and loves it." - doesn't separate into two statements, however doubtful if it is possible to do in a non-hackish way
//        String example = "Bought some 3M 95N-rated face masks for smoggy days.\n";
//        String example = "Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes.";
//        String example = "We also got two Xiaomi air purifiers that work quite well.";
//        String example = "She's having to make do. She speaks and shouts. The amazing and furious Henry Larsson of Sweden doesn't like doing anything in particular.";
//        String example =    "I think she's mad. I don't care whether she likes me. She says that they should go. I don't care whether or not they come.";


        // STILL NEED TO BE FIXED
//        String example = "Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+.";
//        String example = "I just have a widget on my Android phone that says the current AQI from the nearest measuring station.";



        // CANNOT BE FIXED, DUE TO BUGGY PARSING
//        String example = "Anyway, just make your own rule and stick to it.";
//        String example = "We also got two Xiaomi air purifiers that work quite well.";  // TODO: should be limiting components to a single subject, verb, etc.
//        String example = "Chronically stressed people often have trouble sleeping and establishing a practice goes a long way to reduce stress.";


        // FIXED (SOMEWHAT)
//        String example = "Here's our solution: Use an air quality app.";  // TODO: allow to decide specifity of noun compounds, i.e. "the" or "an"
//        String example =    "We have found that it is quite manageable.";  // TODO: when recomposing a full statement, "that" is now missing
//        String example = "She hates and loves to fly. She hates flying and he loves it.";
//        String example = "Establishing a practice goes a long way to reduce stress.";
//        String example = "Chronically stressed people often have trouble sleeping.";  // it's kinda weird though, i.e. component: [{Subject: trouble}, {Verb: sleeping}]
//        String example = "He and she speaks and yells the words and sentences to her or him.";
//        String example = "They don't like doing anything in particular and neither does she.";  // TODO: consider whether dep(neither-10, does-11) should result in a negation of verb
        // (for example below: identical result - technically incorrect since they're slightly semantically different, but definitely a useful simplication)
//        String example = "Sally and Mads in particular don't like doing anything. Sally and Mads don't like doing anything in particular.";
//        String example =    "Here's our solution:";
//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable.";  // TODO: first part not recognised as statement, since it lacks subject
        String example =    "I don't care whether she likes me.";
//        String example = "He doesn't like doing anything.";
//        String example = "The amazing and furious Henry Larsson of Sweden doesn't like doing anything in particular.";
//        String example = "He really doesn't love singing out loud.";
//        String example = "Henry, Louis the Dragon or Sally Bates don't like doing anything in particular.";  // TODO: missing comma in string version
//        String example = "Hey, cool! Very cool, in fact.";  // no statements, as expected
//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament.";
//        String example = "We also got two Xiaomi air purifiers that work quite well";
//        String example = "Sally and Mads in particular don't like doing anything for anyone.";





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
