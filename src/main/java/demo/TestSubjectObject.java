package demo;


import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import java.util.Properties;


public class TestSubjectObject {
    public static void main(String[] args) {
        // setting up the pipeline
        Properties props = new Properties();
//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, parse, depparse, subjectobject, sentiment, sentimenttargets");  // long pipeline
        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, subjectobject");  // short
        props.setProperty("customAnnotatorClass.subjectobject", "nlp.SubjectObjectAnnotator");
        props.setProperty("customAnnotatorClass.sentimenttargets", "sentiment.SentimentTargetsAnnotator");
        props.put("ner.model", "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
//        props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");  // fast, more memory usage
        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");  // slow, less memory usage

        DemoTimer.start("pipeline launch");
        StanfordCoreNLP pipeline = new StanfordCoreNLP(props);
        DemoTimer.stop();

        String example = "The amazing Henry doesn't like doing anything in particular";

        // annotate a piece of text
        Annotation annotation = new Annotation(example);

        DemoTimer.start("annotating");
        pipeline.annotate(annotation);
        DemoTimer.stop();
    }
}
