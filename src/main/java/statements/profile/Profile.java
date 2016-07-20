package statements.profile;


import edu.stanford.nlp.util.CoreMap;
import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import statements.matching.AdwComparison;
import statements.core.*;

import java.util.*;

public class Profile {
    private final ADW adw;
    private final DisambiguationMethod disMethod;
    private final SignatureComparison measure;
    private final Set<String> opinionVerbs;
    private final AdwComparison adwComparison;
    Map<CoreMap, Set<Statement>> statements;


    public Profile(Map<CoreMap, Set<Statement>> statements) {
        this.adw = new ADW();
        this.statements = statements;

        this.adwComparison = new AdwComparison();

        //if lexical items has to be disambiguated
        this.disMethod = DisambiguationMethod.ALIGNMENT_BASED;

        //measure for comparing semantic signatures
        this.measure = new WeightedOverlap();

        this.opinionVerbs = new HashSet<>();
        opinionVerbs.add("like#v");
        opinionVerbs.add("love#v");
        opinionVerbs.add("prefer#v");
        opinionVerbs.add("hate#v");
        opinionVerbs.add("dislike#v");
    }

    public Map<CoreMap, Set<Statement>> getSpecial() {
        Map<CoreMap, Set<Statement>> filteredStatements = new HashMap<>();

        String prototype = "I try to meditate every day.";

        for (CoreMap sentence : getInteresting().keySet()) {
            Set<Statement> sentenceStatements = statements.get(sentence);

            if (sentenceStatements != null) {
                for (Statement statement : sentenceStatements) {
                    if (adwComparison.resembles(prototype, statement)) {
                        System.out.println(statement + " resembled: " + prototype);
                        Set<Statement> filteredSentenceStatements = filteredStatements.getOrDefault(sentence, new HashSet<>());
                        filteredSentenceStatements.add(statement);
                        filteredStatements.put(sentence, filteredSentenceStatements);
                    }
                }
            }
        }

        return filteredStatements;
    }

    public Map<String, Integer> getStatementFrequencies() {
        Collection<Statement> statementList = new ArrayList<>();
        for (Set<Statement> sentenceStatements : statements.values()) {
            statementList.addAll(sentenceStatements);
        }

//        return getFrequencies(statementList);
        return null;
    }

    public Map<CoreMap, Set<Statement>> getLikes() {
        Map<CoreMap, Set<Statement>> filteredStatements = new HashMap<>();

        for (CoreMap sentence : statements.keySet()) {
            Set<Statement> sentenceStatements = statements.get(sentence);
            if (sentenceStatements != null) {
                for (Statement statement : sentenceStatements) {
                    if (statement.getVerb() != null) {
                        double highestScore = 0;
                        String statementWord = statement.getVerb().getPrimary().lemma();

                        if (statementWord != null) {
                            for (String likeVerb : opinionVerbs) {
                                statementWord += "#v";

                                double score = adw.getPairSimilarity(
                                        likeVerb, statementWord,
                                        disMethod,
                                        measure,
                                        ItemType.SURFACE_TAGGED, ItemType.SURFACE_TAGGED);

                                System.out.println(likeVerb + " vs " + statementWord);
                                System.out.println("score = " + score);
                                if (score > highestScore) highestScore = score;
                            }

                            if (isInteresting(statement) && highestScore > 0.7) {
                                Set<Statement> filteredSentenceStatements = filteredStatements.getOrDefault(sentence, new HashSet<>());
                                filteredSentenceStatements.add(statement);
                                filteredStatements.put(sentence, filteredSentenceStatements);
                            }
                        }
                    }
                }
            }
        }

        return filteredStatements;
    }

    public Map<CoreMap, Set<Statement>> getInteresting() {
        Map<CoreMap, Set<Statement>> filteredStatements = new HashMap<>();

        for (CoreMap sentence : statements.keySet()) {
            Set<Statement> sentenceStatements = statements.get(sentence);
            if (sentenceStatements != null) {
                for (Statement statement : sentenceStatements) {
//                    if (isInteresting(statement) && isWellFormed(statement)) {
                    if (isInteresting(statement)) {
                        Set<Statement> filteredSentenceStatements = filteredStatements.getOrDefault(sentence, new HashSet<>());
                        filteredSentenceStatements.add(statement);
                        filteredStatements.put(sentence, filteredSentenceStatements);
                    }
                }
            }
        }

        return filteredStatements;
    }

    /**
     * Subjective evaluation of whether a statement is interesting.
     * Currently this method simply discards statements with pronouns that are not 1st person.
     *
     * @param statement
     * @return
     */
    private boolean isInteresting(Statement statement) {
        boolean interesting = true;

        for (StatementComponent component : statement.getComponents()) {
            if (component instanceof AbstractComponent) {
                if (SpecialWords.uninterestingNouns.contains(((AbstractComponent) component).getBasicCompound())) {
//                if (Tags.PRONOUNS.contains(((AbstractComponent) component).getPrimary().tag())) {
                    return false;
                }
            } else if (component instanceof Statement) {
                interesting = isInteresting((Statement) component);
            }
        }

        return interesting;
    }

    /**
     * Subjective evaluation of whether a statement is welformed grammatically.
     * The purpose is to make sure they can parse back into plain English.
     * Note: these criteria are subject to change.
     *
     * @param statement
     * @return
     */
    private boolean isWellFormed(Statement statement) {
        return statement.gaps() == 0  && statement.getVerb() != null;
    }

    /**
     * Quick frequency count.
     * TODO: remove or revise, currently not working
     *
     * @param items
     * @param <T>
     * @return
     */
    public static <T> Map<T, Integer> getFrequencies(Collection<T> items) {
        Map<T, Integer> frequencies = new HashMap<>();

        for (T item : items) {
            int count = frequencies.getOrDefault(item, 0);
            count++;
            frequencies.put(item, count);
        }

        return frequencies;
    }
}
