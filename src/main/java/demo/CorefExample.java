package demo;

import edu.stanford.nlp.hcoref.CorefCoreAnnotations;
import edu.stanford.nlp.hcoref.data.CorefChain;
import edu.stanford.nlp.hcoref.data.Mention;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;

import java.util.Properties;

// when using "coref": throws Exception in thread "main" java.lang.OutOfMemoryError: Java heap space
// http://stanfordnlp.github.io/CoreNLP/coref.html: "Run deterministic coref (note this version requires significantly less RAM)"
// seems like I should use the deterministic one, at least on this development machine with 4 GB of RAM
// when using "dcoref": Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
// possible solution here at bottom: http://stackoverflow.com/questions/11304599/memory-error-in-stanford-corenlp-eclipse
// suggested solution from stanford: http://nlp.stanford.edu/software/corenlp-faq.shtml#memory
// (they want 2GB or more memory for corenlp)
// SOLUTION: use -Xmx2g as VM argument when running to increase to a suitable amount of memory
// Do note that 2GB is still not enough to run neural network-based coref, must use dcoref instead
// (this example seems to use about 1.3GB memory)
public class CorefExample {
    public static void main(String[] args) throws Exception {

        String example =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution:" +
                "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it." +
                "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too." +
                "We also got two Xiami Air purifiers that work quite well. They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers." +
                "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison. If you live in a city you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). Of course, Western cities usually don't have those crazy smog days." +
                "Don't worry too much. Sometimes there's no smog for a whole week, sometime's it lasts for a whole week and you'll just stay mostly indoors and use masks when outside. You'll get used to it.";


//        Annotation document = new Annotation("Barack Obama was born in Hawaii.  He is the president.  Obama was elected in 2008.");
        Annotation document = new Annotation(example); // using longer example from reddit instead
        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,dcoref");  // using dcoref instead of coref

        // limit coref distance for better runtime performance
        props.setProperty("dcoref.maxdist", "2");

        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        System.out.println("before annotating");  // to see if it is annotation taking up memory
        pipeline.annotate(document);
        System.out.println("---");
        System.out.println("coref chains");
        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t"+cc);
        }
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t"+m);
            }
        }

        System.out.println("before annotating second time");  // to see if it is annotation taking up memory
        Annotation document2 = new Annotation(example); // using longer example from reddit instead
        pipeline.annotate(document);
        System.out.println("---");
        System.out.println("coref chains");
        for (CorefChain cc : document.get(CorefCoreAnnotations.CorefChainAnnotation.class).values()) {
            System.out.println("\t"+cc);
        }
        for (CoreMap sentence : document.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("---");
            System.out.println("mentions");
            for (Mention m : sentence.get(CorefCoreAnnotations.CorefMentionsAnnotation.class)) {
                System.out.println("\t"+m);
            }
        }
    }
}
