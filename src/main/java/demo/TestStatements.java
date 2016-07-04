package demo;


import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import statements.annotations.StatementsAnnotation;
import statements.core.AbstractComponent;
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
//        "They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers. " +
//        "If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). ";



        // TODO: the sentence "Hates and loves it." - doesn't separate into two statements, however doubtful if it is possible to do in a non-hackish way


//        String example = "The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers.";

        // STILL NEED TO BE FIXED
        // TODO: the cc (= "and") is not preserved in the indirect object text since it is related to the verb
//        String example = "I keep them in my bag and on my head";

        // TODO: double subjects, missing a lot of information
//        String example = "Recently moved here with my girlfriend and we have found that it is quite manageable.";
        /*Recently moved here with my girlfriend and we have found that it is quite manageable.
            |_ statement: {Statement: "we have found that it is quite manageable", components: 3}
                |_ component: {Subject: "it"}
                |_ component: {Verb: "have found"}
                |_ component: {Subject: "we"}*/

        // TODO: missing verb in DirectObject construction
//        String example = "Here's our solution: Use an air quality app.";
        /*
        Here's our solution: Use an air quality app.
          |_ statement: {Statement: "Here's our solution : Use an air quality app", components: 3}
             |_ component: {DirectObject: "an air quality app"}
             |_ component: {Subject: "our solution"}
             |_ component: {Verb: "Here's :"}
         */

        // TODO: double DirectObjects
//        String example = "The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers.";
        /*
        The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers.
          |_ statement: {Statement: "The app allows you to check the latest PM2 .5 index inside your flat and automate the purifiers", components: 5}
             |_ component: {Verb: "allows"}
             |_ component: {IndirectObject: "inside your flat"}
             |_ component: {Subject: "The app"}
             |_ component: {DirectObject: "check the latest PM2 .5 index and automate the purifiers", entries: 2}
             |_ component: {DirectObject: "you"}
         */

        // TODO: multiple issues
//        String example = "If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too.";
        /*
        If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too.
          |_ statement: {Statement: "If you live in a city -LRB- e.g. Copenhagen -RRB- you might be surprised that some days it can actually be quite polluted in Western cities too", components: 4}
             |_ component: {IndirectObject: "in a city -LRB- e.g. Copenhagen -RRB-"}
             |_ component: {DirectObject: "that"}
             |_ component: {Subject: "you"}
             |_ component: {Verb: "might be surprised", clause: "If you live"}
         */

        // TODO: double subjects, confusion caused by parentheses)
        String example = " sure was (I come from Copenhagen, Denmark).";
        /*
        I sure was (I come from Copenhagen, Denmark).
          |_ statement: {Statement: "I sure was -LRB- I come from Copenhagen, Denmark -RRB-", components: 4}
             |_ component: {Subject: "I"}
             |_ component: {IndirectObject: "from Copenhagen, Denmark"}
             |_ component: {Verb: "sure"}
             |_ component: {Subject: "I"}
         */

        // CANNOT BE FIXED, DUE TO BUGGY PARSING
//        String example = "Anyway, just make your own rule and stick to it.";
//        String example = "We also got two Xiaomi air purifiers that work quite well.";  // TODO: should be limiting components to a single subject, verb, etc.
//        String example = "Chronically stressed people often have trouble sleeping and establishing a practice goes a long way to reduce stress.";
//        String example = "I'm sure you meant the EU as a whole and not the European Parliament specifically";  // specifically should modify to "meant", not "sure"
//        String example = "Bought some 3M 95N-rated face masks for smoggy days.\n";
        // TODO: in this case, the indirect object does not even respect a normal conjunction
//        String example = "I keep one in my bag and on my head at all times.";


        // FIXED (SOMEWHAT)
//        String example = "Here's our solution: Use an air quality app.";  // TODO: allow to decide specifity of noun compounds, i.e. "the" or "an"
//        String example =    "We have found that it is quite manageable.";
//        String example = "She hates and loves to fly. She hates flying and he loves it.";
//        String example = "Establishing a practice goes a long way to reduce stress.";
//        String example = "Chronically stressed people often have trouble sleeping.";  // it's kinda weird though, i.e. component: [{Subject: trouble}, {Verb: sleeping}]
//        String example = "He and she speaks and yells the words and sentences to her or him.";
//        String example = "They don't like doing anything in particular and neither does she.";  // TODO: consider whether dep(neither-10, does-11) should result in a negation of verb
        // (for example below: identical result - technically incorrect since they're slightly semantically different, but definitely a useful simplication)
//        String example = "Sally and Mads in particular don't like doing anything. Sally and Mads don't like doing anything in particular.";
//        String example =    "Here's our solution:";
//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable.";  // TODO: first part not recognised as statement, since it lacks subject
//        String example =    "I don't care whether she likes me.";
//        String example = "He doesn't like doing anything.";
//        String example = "The amazing and furious Henry Larsson of Sweden doesn't like doing anything in particular.";
//        String example = "He really doesn't love singing out loud.";
//        String example = "Hey, cool! Very cool, in fact.";  // no statements, as expected
//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament.";
//        String example = "We also got two Xiaomi air purifiers that work quite well";
//        String example = "Sally and Mads in particular don't like doing anything for anyone.";
//        String example = "They aren't pretty. She's having to make do. He really doesn't love singing out loud.";
//        String example =    "I think she's mad. I don't care whether she likes me. She says that they should go.";
//        String example =    "I don't care whether or not they come.";
//        String example = "I just have a widget on my Android phone that says the current AQI from the nearest measuring station.";
//        String example = "Henry, Louis the Dragon or Sally Bates don't like doing anything in particular.";
//        String example = "She speaks and shouts.";
//        String example = "Of course, Western cities usually don't have those crazy smog days.";  // TODO: there's a comma before the verb
//        String example = "Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+.";
//        String example = "just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";  // TODO: hilariously long verb
//        String example = "Don't worry too much.";
//        String example = "You'll get used to it.";
//        String example = "Sometimes there's no smog for a whole week, sometimes it lasts for a whole week and you'll just stay mostly indoors and use masks when outside.";  // an example of parataxis
//        String example = "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison.";
//        String example = "They are smartphone-connected and always on, except when we're out during weekdays.";
//        String example = "That's not the way he sees it.";
//        String example = "They live in a house in Copenhagen.";  // sequence indirect object
//        String example = "Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes.";







        Annotation annotation = new Annotation(example);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            Set<Statement> statements = sentence.get(StatementsAnnotation.class);
            System.out.println(sentence);
            StatementUtils.printStatements(statements);

//            if (statements != null) {
//                for (Statement statement : statements) {
//                    for (StatementComponent statementComponent : statement.getComponents()) {
//                        if (statementComponent instanceof AbstractComponent) {
//                            AbstractComponent component = (AbstractComponent) statementComponent;
//                            System.out.println("entries: " + component.getEntries());
//                            System.out.println("small compounds: " + component.getSmallCompounds());
//                            System.out.println("compounds: " + component.getCompounds());
//                        }
//                    }
//                }
//            }
        }
    }
}
