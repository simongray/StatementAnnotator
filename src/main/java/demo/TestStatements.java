package demo;


import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.Properties;


public class TestStatements {
    public static void main(String[] args) {
        // setting up the pipeline
        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, parse, depparse, subjectobject, sentiment, sentimenttargets");  // long pipeline
        props.setProperty("annotators", "tokenize, ssplit, pos, parse, depparse, subjectobject");  // short pipeline
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

        String example = "The European Parliament with its proportional representation is a much more democratic institution than the UK parliament...\n" +
        "I'm sure you meant the EU as a whole and not the European Parliament specifically, just thought it was funny that a Brit would complain about this when the UK voting system is one of the absolute worst in the world at representing the will of the people + your other house consists of a bunch of noble people.";

        Annotation annotation = new Annotation(example);

        pipeline.annotate(annotation);
    }
}
