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
// TODO: when using "dcoref": Exception in thread "main" java.lang.OutOfMemoryError: GC overhead limit exceeded
// possible solution here at bottom: http://stackoverflow.com/questions/11304599/memory-error-in-stanford-corenlp-eclipse
public class CorefExample {
    public static void main(String[] args) throws Exception {

        Annotation document = new Annotation("Barack Obama was born in Hawaii.  He is the president.  Obama was elected in 2008.");
        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,coref");
        props.setProperty("annotators", "tokenize,ssplit,pos,lemma,ner,parse,mention,dcoref");  // using dcoref instead of coref
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
    }
}
