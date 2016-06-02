package demo;

import edu.stanford.nlp.ie.util.RelationTriple;
import edu.stanford.nlp.io.IOUtils;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.naturalli.NaturalLogicAnnotations;
import edu.stanford.nlp.naturalli.OpenIE;
import edu.stanford.nlp.naturalli.SentenceFragment;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphCoreAnnotations;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.PropertiesUtils;

import java.util.Collection;
import java.util.List;
import java.util.Properties;

/**
 * A demo illustrating how to call the OpenIE system programmatically.
 */
public class OpenIEDemo {

    private OpenIEDemo() {} // static main

    public static void main(String[] args) throws Exception {
        // Create the Stanford CoreNLP pipeline
        Properties props = PropertiesUtils.asProperties(
                "annotators", "tokenize,ssplit,pos,lemma,depparse,natlog,openie"
                // , "depparse.model", "edu/stanford/nlp/models/parser/nndep/english_SD.gz"
                // "annotators", "tokenize,ssplit,pos,lemma,parse,natlog,openie"
                // , "parse.originalDependencies", "true"
        );
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

        // Annotate an example document.
        String text;
        if (args.length > 0) {
            text = IOUtils.slurpFile(args[0]);
        } else {
//            text = "Obama was born in Hawaii. He is our president.";
            text =    "Recently moved here with my girlfriend and we have found that it is quite manageable. Here's our solution: " +
                    "Use an air quality app. We use the one for http://aqicn.org/city/beijing/. I just have a widget on my Android phone that says the current AQI from the nearest measuring station. Our house rule is to use masks when it's 200+, although my girlfriend often does it from 150+. Anyway, just make your own rule and stick to it. " +
                    "Bought some 3M 95N-rated face masks for smoggy days. Just keep one in my bag at all times as the wind can direction and smoggify the nicest days in a couple of hours sometimes. You can get some nice re-usable masks where you can change the filter too. " +
                    "We also got two Xiaomi air purifiers that work quite well. They are smartphone-connected and always on, except when we're out during weekdays. The app allows you to check the latest PM2.5 index inside your flat and automate the purifiers. " +
                    "Before you go, familiarise yourself with the air quality in your own area so you have a means of comparison. If you live in a city (e.g. Copenhagen) you might be surprised that some days it can actually be quite polluted in Western cities too. I sure was (I come from Copenhagen, Denmark). Of course, Western cities usually don't have those crazy smog days. " +
                    "Don't worry too much. Sometimes there's no smog for a whole week, sometime's it lasts for a whole week and you'll just stay mostly indoors and use masks when outside. You'll get used to it.";

        }
        Annotation doc = new Annotation(text);
        pipeline.annotate(doc);

        // Loop over sentences in the document
        int sentNo = 0;
        for (CoreMap sentence : doc.get(CoreAnnotations.SentencesAnnotation.class)) {
            System.out.println("Sentence #" + ++sentNo + ": " + sentence.get(CoreAnnotations.TextAnnotation.class));
            System.out.println();

            // Print SemanticGraph
//            System.out.println(sentence.get(SemanticGraphCoreAnnotations.CollapsedDependenciesAnnotation.class).toString(SemanticGraph.OutputFormat.LIST));

            // Get the OpenIE triples for the sentence
            Collection<RelationTriple> triples = sentence.get(NaturalLogicAnnotations.RelationTriplesAnnotation.class);

            // Print the triples
            for (RelationTriple triple : triples) {
//                System.out.println(triple.confidence + "\t" +
//                        triple.subjectLemmaGloss() + "\t" +
//                        triple.relationLemmaGloss() + "\t" +
//                        triple.objectLemmaGloss());
                System.out.println(triple.confidence + "\t" +
                        triple.subjectGloss()+ "\t" +
                        triple.relationGloss() + "\t" +
                        triple.objectGloss());
                System.out.println( "head, span, head " +
                        triple.subjectHead()+ "\t" +
                        triple.relationTokenSpan() + "\t" +
                        triple.objectHead());
            }

            // Alternately, to only run e.g., the clause splitter:
//            List<SentenceFragment> clauses = new OpenIE(props).clausesInSentence(sentence);
//            for (SentenceFragment clause : clauses) {
//                System.out.println(clause.parseTree.toString(SemanticGraph.OutputFormat.LIST));
//            }
//            System.out.println();
        }
    }

}