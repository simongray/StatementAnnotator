package statements.profile;

import edu.mit.jwi.item.POS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import statements.core.*;
import statements.matching.Pattern;
import statements.matching.Proxy;
import statements.matching.WordnetDictionary;

import java.io.IOException;
import java.util.*;

public class Profile {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    Set<Statement> statements;
    WordnetDictionary dict;
    Set<String> subjectWords = new HashSet<>();
    Set<String> verbWords = new HashSet<>();
    Set<String> directObjectWords = new HashSet<>();
    Set<String> indirectObjectWords = new HashSet<>();
    Set<String> topics = new HashSet<>();

    public Profile(Set<Statement> statements) throws IOException {
        this.dict = new WordnetDictionary();
        this.statements = statements;

        // unpack embedded statements according to a pattern
        // the original statements are replaced with the embedded statements based on the pattern
        unpackStatements();

        // store the topic keywords of all interesting statements
        // used to rank statements in relation to other users
        registerTopics();
    }

    /**
     * Unpack statements according to certain patterns to replace them with their embedded statements.
     */
    private void unpackStatements() {
        Set<Statement> packedStatements = new HashSet<>();
        Set<Statement> unpackedStatements = new HashSet<>();

        Pattern thinkPattern = new Pattern(
                Proxy.Subject("I", "we"),
                Proxy.Verb(dict.getSynonyms(POS.VERB, "think", "reckon", "believe")),
                Proxy.Statement()
        );

        for (Statement statement : statements) {
            if (thinkPattern.matches(statement)) {
                unpackedStatements.add(statement.getEmbeddedStatement());
                packedStatements.add(statement);
            }
        }

        statements.addAll(unpackedStatements);
        statements.removeAll(packedStatements);
        logger.info("unpacked " + packedStatements.size() + " statements");
    }

    /**
     * Store the topic keywords of all of the interesting statements.
     */
    private void registerTopics() {
        // register all of the words of the statements
        for (Statement statement : statements) {
            for (StatementComponent component : statement.getComponents()) {
                if (component instanceof AbstractComponent) {
                    AbstractComponent abstractComponent = (AbstractComponent) component;
                    if (component instanceof Subject) subjectWords.add(abstractComponent.getBasicCompound());
                    // TODO: use verbs (excluding most common ones)?
//                    if (component instanceof Verb) verbWords.add(abstractComponent.getBasicCompound());
                    if (component instanceof DirectObject) directObjectWords.add(abstractComponent.getBasicCompound());
                    if (component instanceof IndirectObject) indirectObjectWords.add(abstractComponent.getBasicCompound());
                }
            }
        }

        topics.addAll(subjectWords);
        topics.addAll(verbWords);
        topics.addAll(directObjectWords);
        topics.addAll(indirectObjectWords);
    }

    /**
     * Get the statements that evaluate as interesting according to their own internal measure.
     *
     * @return interesting statements
     */
    public Set<Statement> getInterestingStatements() {
        Set<Statement> interestingStatements = new HashSet<>();

        for (Statement statement : getStatements()) {
            if (statement.isInteresting()) interestingStatements.add(statement);
        }

        return interestingStatements;
    }

    /**
     * Returns the topics that are shared between two profiles.
     * Useful for adjusting the ranking of statements.
     *
     * @param otherProfile the other profile to compare topics with
     * @return the topics found in both profiles
     */
    public Set<String> findSharedTopics(Profile otherProfile) {
        Set<String> sharedTopics = new HashSet<>(getTopics());
        sharedTopics.retainAll(otherProfile.getTopics());
        return sharedTopics;
    }

    public List<Statement> getStatementsByLexicalDensity() {
        List<Statement> rankedStatements = new ArrayList<>(getInterestingStatements());
        rankedStatements.sort(new LexicalDensityComparator());
        return rankedStatements;
    }

    public Set<Statement> getStatements() {
        return statements;
    }

    public Set<String> getTopics() {
        return topics;
    }

    /**
     * Used to sort Statements by lexical density.
     */
    public static class LexicalDensityComparator implements Comparator<Statement> {
        @Override
        public int compare(Statement x, Statement y) {
            double xn = x.getLexicalDensity();
            double yn = y.getLexicalDensity();

            if (xn == yn) {
                return 0;
            } else {
                if (xn > yn) {
                    return -1;
                } else {
                    return 1;
                }
            }
        }
    }
}
