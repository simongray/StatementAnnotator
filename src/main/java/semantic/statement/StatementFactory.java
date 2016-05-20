package semantic.statement;

import edu.stanford.nlp.util.CoreMap;

import java.util.List;

/**
 * This class handles the construction of Statements.
 */
public class StatementFactory {
    public static List<Statement> makeStatements(CoreMap sentence) {
        // TODO: basic algorithm
        /*
            Algorithm sketch:
                1. One-word sentences are discarded
                2. The graph is analysed for subjects and these are used to create statements.
                3. If no subjects can be found, the statement is assumed to be self-referential
                   and the verb phrase is analysed instead (e.g. "hate cycling!").

                   TODO: capitalisation confusion in verb phrases
                   it seems like parser results turn verb phrases like "hate cycling"
                   into compound noun phrases if the first word is capitalised ("Hate") and can also be a noun.
                   "Hated cycling" resolves correctly, though, since there is no way to confuse it.
                   Will probably need to hack around that somehow to get the best results.
         */

        return null;
    }
}
