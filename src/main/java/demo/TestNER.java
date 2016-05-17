package demo;

import edu.stanford.nlp.ie.AbstractSequenceClassifier;
import edu.stanford.nlp.ie.crf.CRFClassifier;
import edu.stanford.nlp.ling.CoreAnnotations;
import edu.stanford.nlp.ling.CoreLabel;
import edu.stanford.nlp.pipeline.Annotation;
import edu.stanford.nlp.pipeline.StanfordCoreNLP;
import edu.stanford.nlp.util.CoreMap;
import edu.stanford.nlp.util.Triple;

import java.io.IOException;
import java.util.List;
import java.util.Properties;


public class TestNER {
    public static void main(String[] args) {
        try {
            String example =   "There is prism with a schism that contains socialism, but is void of Existentialism and Absentee-ism.";
            System.out.println("pipelined approach");

            // initiate pipeline with properties (i.e. what stages)
            Properties props = new Properties();
            props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, tokensregexner");
//        props.setProperty("annotators", "tokenize, ssplit, pos, lemma, gender, ner, regexner, parse, sentiment, sentimenttargets");
            props.setProperty("customAnnotatorClass.sentimenttargets", "sentiment.SentimentTargetsAnnotator");
            props.setProperty("customAnnotatorClass.tokensregexner", "edu.stanford.nlp.pipeline.TokensRegexNERAnnotator");
            props.put("tokensregexner.verbose", "true");
//        props.put("tokensregexner.mapping", "edu/stanford/nlp/models/regexner/type_map_clean");
            props.put("tokensregexner.mapping", "tokensregexner.txt");
            props.put("tokensregexner.noDefaultOverwriteLabels", "");
//        props.put("tokensregexner.ignorecase", "true");
            props.put("ner.model", "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz");
//            props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");  // fast, more memory usage
        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");  // slow, less memory usage

//            props.setProperty("parse.model", "edu/stanford/nlp/models/srparser/englishSR.ser.gz");  // fast, more memory usage
//        props.setProperty("parse.model", "edu/stanford/nlp/models/lexparser/englishPCFG.ser.gz");  // slow, less memory usage


            StanfordCoreNLP pipeline = new StanfordCoreNLP(props);

            // annotate a piece of text
            Annotation annotation = new Annotation(example);
            pipeline.annotate(annotation);

            // using sentence annotation, perhaps another annotation type is better suited (there are A LOT)
            // (it should be one the annotations implementing CoreAnnotation<List<CoreMap>>)
            List<CoreMap> tokenMaps = annotation.get(CoreAnnotations.SentencesAnnotation.class);

            for (CoreMap tokenMap : tokenMaps) {
                // TokensAnnotation are available for example
                List<CoreLabel> tokens = tokenMap.get(CoreAnnotations.TokensAnnotation.class);
                for (CoreLabel token : tokens) {
                    System.out.println("annotations for " + token);
//                    System.out.println("1"+token.get(CoreAnnotations.CharacterOffsetBeginAnnotation.class)); // char pos in text
//                    System.out.println("2"+token.get(CoreAnnotations.CharacterOffsetEndAnnotation.class));  // char pos in text
//                    System.out.println(token.get(CoreAnnotations.AnswerAnnotation.class));
//                    System.out.println(token.get(CoreAnnotations.EntityTypeAnnotation.class));
//                    System.out.println(token.get(CoreAnnotations.EntityClassAnnotation.class));
//                    System.out.println(token.get(CoreAnnotations.EntityRuleAnnotation.class));
                    System.out.println("3"+token.get(CoreAnnotations.NamedEntityTagAnnotation.class));  // get the NER tag
//                    System.out.println("4"+token.get(CoreAnnotations.SemanticTagAnnotation.class));
//                    System.out.println("5"+token.get(CoreAnnotations.CategoryFunctionalTagAnnotation.class));
//                    System.out.println("6"+token.get(CoreAnnotations.CoNLLDepAnnotation.class));
//                    System.out.println("7"+token.get(CoreAnnotations.BeforeAnnotation.class));
//                    System.out.println("8"+token.get(CoreAnnotations.PartOfSpeechAnnotation.class));  // get the POS
//                    System.out.println("9"+token.get(CoreAnnotations.TextAnnotation.class));  // get the word
//                    System.out.println();

//                    System.out.println(
//                            token + ", " +
//                            token.ner() + ":" + // actually includes NER in this case!! (days = DURATION)
//                            token.beginPosition() + "," +
//                            token.endPosition()
//                    );  // actually includes NER in this case!! (days = DURATION)
                }
            }

//            System.out.println();
//            System.out.println("non-pipelined approach");
//
//            // load the 4-class classifier
//            String serializedClassifier = "edu/stanford/nlp/models/ner/english.conll.4class.distsim.crf.ser.gz";
//            AbstractSequenceClassifier<CoreLabel> classifier = CRFClassifier.getClassifier(serializedClassifier);
//
//            System.out.println("Single word entities:");
//            List<List<CoreLabel>> out = classifier.classify(example);
//            for (List<CoreLabel> sentence : out) {
//                for (CoreLabel word : sentence) {
//                    // this gets you single word entities by default, not multi-word ones
//                    String nerTag = word.get(CoreAnnotations.AnswerAnnotation.class);
//
//                    // it seems that "O" (oh, not zero) is the default null value, although it doesn't work with != "O"
//                    // using length() instead to get relevant tags
//                    if (nerTag.length() > 1) {
//                        System.out.println(word + " (" + nerTag + ")");
//                    }
//                }
//            }
//
//            System.out.println("Multi-word entities:");
//
//            // for multi-word entities: http://stackoverflow.com/questions/13765349/multi-term-named-entities-in-stanford-named-entity-recognizer
//            List<Triple<String,Integer,Integer>> entities = classifier.classifyToCharacterOffsets(example);
//
//            for (Triple<String,Integer,Integer> entity : entities) {
//                char[] entityChars = new char[entity.third() - entity.second()];
//                example.getChars(entity.second(), entity.third(), entityChars, 0);
//                System.out.println(new String(entityChars) + " (" + entity.first() + ")");
//            }
//        } catch (IOException e) {
//            e.printStackTrace();
//        } catch (ClassNotFoundException e) {
//            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
