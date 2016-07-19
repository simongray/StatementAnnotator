package demo;

import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;

import java.io.File;

public class TestADW {
    static private String CONFIG_DIR = "config/";
    static private String CONFIG_FILE = "adw.properties";

    public static void main(String[] args) {
        ADW pipeLine = new ADW();

        String testText1 = "like#v";
        String testText2 = "love#v";





        String text1 = "a# mill that is powered by the wind";
        ItemType text1Type = ItemType.SURFACE;

        String text2 = "c#n rotate#v wind#n";
        ItemType text2Type = ItemType.SURFACE_TAGGED;

        String text3 = "windmill.n.1 wind.n.1 rotate.v.1";	//or windmill#n#1
        ItemType text3Type = ItemType.WORD_SENSE;

        String text4 = "windmill%1:06:01::  windmill%1:06:01::";
        ItemType text4Type = ItemType.SENSE_KEYS;

        String text5 = "terminate";
        ItemType text5Type = ItemType.SURFACE;

        String text6 = "fire#v";
        ItemType text6Type = ItemType.SURFACE_TAGGED;

        //if lexical items has to be disambiguated
        DisambiguationMethod disMethod = DisambiguationMethod.ALIGNMENT_BASED;

        //measure for comparing semantic signatures
        SignatureComparison measure = new WeightedOverlap();






        double score = pipeLine.getPairSimilarity(
                testText1, testText2,
                disMethod,
                measure,
                ItemType.SURFACE_TAGGED, ItemType.SURFACE_TAGGED);
        System.out.println(score+"\t"+testText1+"\t"+testText2);




//
//
//
//        double score1 = pipeLine.getPairSimilarity(
//                text1, text2,
//                disMethod,
//                measure,
//                text1Type, text2Type);
//        System.out.println(score1+"\t"+text1+"\t"+text2);
//
//        double score2 = pipeLine.getPairSimilarity(
//                text1, text3,
//                disMethod,
//                measure,
//                text1Type, text3Type);
//        System.out.println(score2+"\t"+text1+"\t"+text3);
//
//        double score3 = pipeLine.getPairSimilarity(
//                text1, text4,
//                disMethod,
//                measure,
//                text1Type, text4Type);
//        System.out.println(score3+"\t"+text1+"\t"+text4);
//
//        double score4 = pipeLine.getPairSimilarity(
//                text2, text3,
//                disMethod,
//                measure,
//                text2Type, text3Type);
//        System.out.println(score4+"\t"+text2+"\t"+text3);
//
//        double score5 = pipeLine.getPairSimilarity(
//                text3, text4,
//                disMethod,
//                measure,
//                text3Type, text4Type);
//        System.out.println(score5+"\t"+text3+"\t"+text4);
//
//        double score6 = pipeLine.getPairSimilarity(
//                text5, text6,
//                disMethod,
//                measure,
//                text5Type, text6Type);
//        System.out.println(score6+"\t"+text5+"\t"+text6);



        File configFile = new File(CONFIG_DIR, CONFIG_FILE);
        System.out.println(configFile.getAbsolutePath());
        System.out.println(configFile.exists());
    }
}
