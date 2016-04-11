package demo;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;
import java.util.Set;

/**
 * Created by simongray on 11/04/2016.
 */
public class TestWikipedia {
    public static void main(String[] args) {
        String fileName = "src/main/resources/titles-sorted.txt";
        Set<String> lexicon = new HashSet<>();

        try (BufferedReader br = new BufferedReader(new FileReader(fileName))) {
            String line;
            System.out.println("loading... ");
            while ((line = br.readLine()) != null) {
                lexicon.add(line.trim());
            }
            System.out.println("done!");

        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
