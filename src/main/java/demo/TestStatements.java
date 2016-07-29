package demo;


import edu.stanford.nlp.ling.CoreAnnotations.SentencesAnnotation;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import statements.annotations.StatementsAnnotation;
import statements.core.Statement;
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
        props.setProperty("ssplit.newlineIsSentenceBreak", "always");  // IMPORTANT!!
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

//        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament... \n" +
//        "I'm sure you meant the EU as a whole and not the European Parliament specifically, just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";
//        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution: " +
//        "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it. " +
//        "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too. " +
//        "They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers. " +
//        "If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). ";

//        String example = "The Danish equivalent of Andersson is Andersen and the first name Anders is, though not quite as much recently, fairly popular.";

        // TODO: not quite right, mostly due to parsing
//        String example = "Well, a country can be both Western and Southern in geographical terms, they're not exclusive groups.";


        // TODO: use ConjVerb to resolve anaphora (if subject in second statement is they/it/etc. then it can be replaced by "Apple")
//        String example = "Usually Apple is better at holding back with this stuff, but they are really losing it these days.";

//
//        String example = "I've been coming here since the very beginning and in my opinion it's better than ever.";
//        String example = "Oh, I had lots of Zamzam in Western/Muslim China. Very tasty stuff, but only the Muslims seem to drink it for some reason.";


//        String example = "I went to Copenhagen Business School myself and it's quite comforting to know that the place is also funding research into inequality.";

        // TODO: detect questions better
//        String example = "What is the future looking like for South African politics?";
//        String example = "Didn't they manage to get proportional representation in the Scottish parliament?";
//        String example = "Who comes up with stuff like this?";
//        String example = "Where are the guys and slightly older people?";


//        String example = "Pressing something extra hard should make sense physically, like for a pining action or pushing something to the back.";
//        String example = "It feels like Apple is shitting on affordances and good design these days and instead just joined the gimmick interface race that Samsung and others have been in for a while, but because it's Apple no one dares to say it doesn't make any sense.";
//        String example = "Introducing another interface feature with a learning curve just for the sake of using this new pressure sensitive technology is actually a very bad use of pressure sensitivity.";
//        String example = "Love the show and there's no denying that Justin Roiland has a unique sense of humour which is being accompanied perfectly by the cleverness of Dan Harmon's philosophical style.";
//        String example = "This is funny, end up watching it every time it's posted here on reddit.";
//        String example = "He had a great show before but couldn't continue to do his \"Colbert\" character, so people are interested in how his new thing is turning out.";
//        String example = "Christiania is a must, as is taking the boat tour in the canals to see all the different architecture.";
//        String example = "Central Copenhagen is for tourists and people from Jutland.";

        // TODO: transfer clauses to other statement
//        String example = "Check out Assistens Cemetery and Frederiksberg Park if you want to spend time in a park.";

        // TODO: should be DirectObject/Statement, not DirectObject
//        String example = "Come visit Jægerborggade in Nørrebro.";

        // TODO: transfer "in" to other statements
//        String example = "Go out drinking in Nørrebro, Christiania or Vesterbro (meat packing district).";

        // TODO: in this case, it seems like it's possible to transer the DirectObject to the other statement since subject is the same
//        String example = "I'm currently attending meditation-based stress treatment and I really enjoy it.";


        // TODO: the sentence "Hates and loves it." - doesn't separate into two statements, however doubtful if it is possible to do in a non-hackish way


        // STILL NEED TO BE FIXED

        // TODO: issue #42, missing first statement, caused by lack of "I" before "recently"
        // NOTE: inserting an "I" as the first word of the sentence gives correct output (two correct statements)
//        String example = "Recently moved here with my girlfriend and we have found that it is quite manageable.";
        /*Recently moved here with my girlfriend and we have found that it is quite manageable.
            |_ statement: {Statement: "we have found that it is quite manageable", components: 3}
                |_ component: {Subject: "it"}
                |_ component: {Verb: "have found"}
                |_ component: {Subject: "we"}*/


        // TODO: conjunction and missing components
        // TODO: "in particular" is only added to statement with Sally
//        String example = "Sally and Mads in particular don't like doing anything for anyone.";
        // TODO: the always on is not included for some reason
//        String example = "They are smartphone-connected and always on, except when we're out during weekdays.";






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
//        String example = "I sure was (I come from Copenhagen, Denmark) :)";
        /*
        I sure was (I come from Copenhagen, Denmark).
          |_ statement: {Statement: "I sure was -LRB- I come from Copenhagen, Denmark -RRB-", components: 4}
             |_ component: {Subject: "I"}
             |_ component: {IndirectObject: "from Copenhagen, Denmark"}
             |_ component: {Verb: "sure"}
             |_ component: {Subject: "I"}
         */




        // CANNOT BE FIXED, DUE TO BUGGY PARSING
//        String example = "We both use Android phones.";  // works in GrammarScope
//        String example = "Is the jump in the early nineties due to a reclassification of countries in the Soviet sphere of influence?";
//        String example = "Anyway, just make your own rule and stick to it.";
        // TODO: this one is really bad
//        String example = "Chronically stressed people often have trouble sleeping and establishing a practice goes a long way to reduce stress.";
//        String example = "I'm sure you meant the EU as a whole and not the European Parliament specifically";  // specifically should modify to "meant", not "sure"
//        String example = "Bought some 3M 95N-rated face masks for smoggy days.\n";
        // TODO: in this case, the indirect object does not even respect a normal conjunction
//        String example = "I keep one in my bag and on my head at all times.";
        // TODO: second indirect object is not found, problem is that nmod:on is replaced by conj(keep,head) in this example
//        String example = "Her and I keep them in my bag and on my head";
        // TODO: doesn't recognise "her" as dobj
//        String example = "She hated him and her.";
//        String example = "She spoke to and hugged him.";  // REALLY WEIRD
        /*
        She spoke to and hugged him.
          |_ statement: {Statement: "She spoke to and hugged him", components: 3}
          |  |_ component: {Subject: "She"}
          |  |_ component: {IndirectObject: "to and hugged him"}
          |  |_ component: {Verb: "spoke", labels: "conjchild"}
          |_ statement: {Statement: "She spoke to and hugged him", components: 3}
             |_ component: {Subject: "She"}
             |_ component: {IndirectObject: "to and hugged him"}
             |_ component: {Verb: "spoke", labels: "conjparent"}
         */
//        String example = "She both hugged and spoke to him.";
        // TODO: since there is no subject in the final statement, the parser thinks the "I" of the previous statement should be the subject
//        String example = "Malmø can be interesting, I guess, but definitely spend a day in Helsingør.";
//        String example = "This is not really a machine learning question, it's more like a basic programming question.";

        // TODO: three statements instead of two, "what" is treated as dobj, works in GrammarScope though
//        String example = "I think the technology is nice, but I don't like what they use it for.";

        // TODO: missing second statement in conjunction, works in GrammarScope
//        String example = "Suddenly the stinky tofu is no longer stinky and actually tastes great (served with a garlic dip).";

        // TODO: this one is just one weird parsing mess
//        String example = "Yeah, I'm from Northern Zealand and I pronounce them differently when stressing stuff, although I'll sometimes be lazy and just pronounce them the same.";

//        String example = "Chicago is very rare here, mostly places like Pizza Hut serve deep pan pizzas and I'm sure it is a far cry from the original.";

        // TODO: a good example of how much the parser fucks up when encountering the word "and" - is there a way to detect this situation?
//        String example = "Even today the most viewed shows are often found on the national broadcasters channels and many people don't have more than a couple of alternatives to those (of course with streaming etc this is changing very fast).";
        String example = "Even today the most viewed shows are often found on the national broadcasters channels. many people don't have more than a couple of alternatives to those (of course with streaming etc this is changing very fast).";


        // FIXED (SOMEWHAT)
//        String example = "Here's our solution: Use an air quality app.";  // TODO: allow to decide specifity of noun compounds, i.e. "the" or "an"
//        String example =    "We have found that it is quite manageable.";
//        String example = "She hates and loves to fly. She hates flying and he loves it.";
//        String example = "Chronically stressed people often have trouble sleeping.";  // it's kinda weird though, i.e. component: [{Subject: trouble}, {Verb: sleeping}]
//        String example = "He and she speaks and yells the compound and sentences to her or him.";
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
//        String example = "They aren't pretty. She's having to make do. He really doesn't love singing out loud.";
//        String example =    "I think she's mad. I don't care whether she likes me. She says that they should go.";
//        String example =    "I don't care whether or not they come.";
        // TODO: issue #51
//        String example = "I just have a widget on my Android phone that says the current AQI from the nearest measuring station.";

//        String example = "Henry, Louis the Dragon or Sally Bates don't like doing anything in particular.";  // xcomp verb example


//        String example = "She speaks and shouts.";
//        String example = "Of course, Western cities usually don't have those crazy smog days.";  // TODO: there's a comma before the verb
//        String example = "Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+.";
//        String example = "just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";  // TODO: hilariously long verb
//        String example = "Don't worry too much.";
//        String example = "You'll get used to it.";
//        String example = "Sometimes there's no smog for a whole week, sometimes it lasts for a whole week and you'll just stay mostly indoors and use masks when outside.";  // an example of parataxis
//        String example = "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison.";
//        String example = "That's not the way he sees it.";
//        String example = "They live in a house in Copenhagen.";  // sequence indirect object
//        String example = "Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes.";
//        String example = "I recently moved here with my girlfriend and we have found that it is quite manageable.";
        // TODO: should the two statements be linked?
//        String example = "Here's our solution: Use an air quality app.";
        /*
        Here's our solution: Use an air quality app.
          |_ statement: {Statement: "Use an air quality app", components: 2}
          |  |_ component: {Verb: "Use"}
          |  |_ component: {DirectObject: "an air quality app"}
          |_ statement: {Statement: "Here's our solution :", components: 2}
             |_ component: {Verb: "Here's :"}
             |_ component: {Subject: "our solution"}
         */
//        String example = "The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers.";
//        String example = "The app allows you to smile and wave.";
//        String example = "It's a big deal.";
//        String example = "We also got two Xiaomi air purifiers that work quite well.";
//        String example = "Establishing a practice goes a long way to reduce stress.";  // csubj verb example
//        String example = "The app and the house are a big deal.";
//        String example = "I keep them in my bag and on my head";
//        String example = "I sing songs and write words in English.";
//        String example = "She cried and yelled at him.";  // not attaching "at him" to "cried, which good enough
//        String example = "She yelled at him and cried.";
//        String example = "She yelled at him and cried at her.";
//        String example = "She shot and killed him.";
//        String example = "He pointed at her and spoke.";
//        String example = "She hated him and he hated her.";
//        String example = "She hated her and Lis.";
//        String example = "Gandalf gave Frodo the ring from Mordor.";  // iobj + nmod tied to direct object
//        String example = "She lives in a house in a lake outside of Copenhagen.";  // both sequences and shared governor
//        String example = "She lives in a house outside of Copenhagen.";
//        String example = "Is Greece considered Eastern Europe in Italy??";  // question
//        String example = "The stinky tofu in other parts of China is too old.";  // has nmod related to subject
//        String example = "It's once a week,  3 hours and is run by two psychologists.";  // re-using subject
//        String example = "Maybe the angry doomsayers just haven't found this thread yet.";
//        String example = "I also do a non-directive meditation involving listening to sounds atmospheric sounds which I picked from a book on yoga philosophy.";
//        String example = "I can totally sympathize with him as it is indeed very difficult for foreigners to get into the Danish labour market for a number of reasons, but this idea that Sweden is doing any better is definitely not true.";
//        String example = "Usually Apple is better at holding back with this stuff, but they are really losing it these days.";
//        String example = "Policy outside collective bargaining is often also created in a deliberative democratic process including hearing affected parties.";

        Annotation annotation = new Annotation(example);
        pipeline.annotate(annotation);
        List<CoreMap> sentences = annotation.get(SentencesAnnotation.class);

        for (CoreMap sentence : sentences) {
            Set<Statement> statements = sentence.get(StatementsAnnotation.class);
            System.out.println(sentence);
            StatementUtils.printStatements(statements);
        }
    }
}
