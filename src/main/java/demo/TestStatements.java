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
//        String example = "Henry, Louis the Dragon or Sally Bates don't like doing anything in particular.";
//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament...\n" +
//        "I'm sure you meant the EU as a whole and not the European Parliament specifically, just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";
//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution:" +
//        "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it. " +
//        "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too. " +
//        "We also got two Xiaomi air purifiers that work quite well. They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers. " +
//        "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison. If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). Of course, Western cities usually don't have those crazy smog days. " +
//        "Don't worry too much. Sometimes there's no smog for a whole week, sometime's it lasts for a whole week and you'll just stay mostly indoors and use masks when outside. You'll get used to it.";
//        String example = "They aren't pretty. She's having to make do. He really doesn't love singing out loud.";
//        String example = "He really doesn't love singing out loud.";
//        String example = "She hates flying and he loves it.";
//        String example = "They don't like doing anything in particular. Sally and Mads don't like doing anything in particular.";
        String example = "They don't like doing anything in particular and neither does she.";
        // TODO: find out why this is screwing up
        // TODO: preserve conjuctions like "and" and "or" for recreating the statement

        Annotation annotation = new Annotation(example);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            StatementUtils.printStatements(sentence);
            System.out.println();
        }
    }
}
