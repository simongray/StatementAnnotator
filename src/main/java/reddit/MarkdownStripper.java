package reddit;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

/**
 * Convert Markdown-formatted text to very boring, plain text.
 * Will remove anything beyond the text itself + basic grammatical symbols.
 * Useful for preparing markdown text for NLP tasks.
 *
 * Reference for Java's regex: http://docs.oracle.com/javase/6/docs/api/java/util/regex/Pattern.html
 */
public class MarkdownStripper {
    /**
     * Patterns matching different kinds of Markdown syntax.
     */
    private Map<String, String> patterns = new HashMap<>();
    {
        patterns.put("prefix underscore", "_+([^_])");
        patterns.put("postfix underscore", "([^_])_+");
        patterns.put("prefix asterisk", "\\*+([^\\*])");
        patterns.put("postfix asterisk", "([^\\*])\\*+");
        patterns.put("prefix strike-through", "~~([^~])");
        patterns.put("postfix strike-through", "([^~])~~");
        patterns.put("link", "\\[([^\\[]+)\\]\\([^)]+\\)" );
        patterns.put("quote", ">+\\s*([^>\\n]+)");
        // TODO: add more patterns
    }

    final String DEFAULT_REPLACEMENT = "$1";

    /**
     * Special replacement patterns for certain cases.
     */
    private Map<String, String> replacements = new HashMap<>();
    {
        replacements.put("quote", "\"$1\"");
    }

    public MarkdownStripper() {}

    /**
     * Initialize this MarkdownStripper with a set of ignored patterns.
     * @param ignoredPatterns
     */
    public MarkdownStripper(Set<String> ignoredPatterns) {
        for (String key : ignoredPatterns) {
            patterns.remove(key);
        }
    }

    /**
     * Strip markdown formatting from a piece of text.
     * @param text
     * @return
     */
    public String strip(String text) {
        for (String key : patterns.keySet()) {
            String pattern = patterns.get(key);
            String replacement = replacements.containsKey(key)? replacements.get(key) : DEFAULT_REPLACEMENT;
            text = text.replaceAll(pattern, replacement);
        }

        return text;
    }
}
