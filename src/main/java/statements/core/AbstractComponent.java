package statements.core;


import edu.stanford.nlp.ling.IndexedWord;
import edu.stanford.nlp.semgraph.SemanticGraph;
import edu.stanford.nlp.semgraph.SemanticGraphEdge;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashSet;
import java.util.Set;

/**
 * A component of a natural language statement.
 */
public abstract class AbstractComponent implements StatementComponent {
    protected final Logger logger = LoggerFactory.getLogger(getClass());

    /**
     * The primary word of the component.
     * Used as the entry point for the rest of the compound.
     */
    protected final IndexedWord primary;

    /**
     * The main compound derived from the outgoing connections of the primary word.
     * Represents the entire component, although it does not include specific parts
     * - such as negations - that are found separately.
     */
    protected final Set<IndexedWord> compound;
    protected final Set<IndexedWord> remaining;
    protected final Set<IndexedWord> all;
    protected final int lowestIndex;
    protected final int highestIndex;

    /**
     * The incoming connections of the primary word, i.e. its governors.
     * Used to find connections between different components.
     */
    protected final Set<IndexedWord> governors;
    protected final Set<IndexedWord> embeddingGovernors;

    /**
     * Conjunctions (= sibling components).
     */
    protected final Set<IndexedWord> conjunction;

    /**
     * Labels are useful to document certain special behaviour of a component.
     */
    protected final Set<String> labels;

    protected final Set<IndexedWord> negations;
    protected final Set<IndexedWord> punctuation;
    protected final Set<IndexedWord> markers;
    protected final Set<IndexedWord> cc;
    protected final Set<IndexedWord> determiners;
    protected final Set<IndexedWord> prepositions;
    protected final Set<IndexedWord> possessives;
//    protected final Set<IndexedWord> adverbialClauses;  // TODO: trying out making this embedded instead
    protected final Set<IndexedWord> nounClauses;
    protected final Set<IndexedWord> basicCompound;
    protected final Set<IndexedWord> normalCompound;
    protected final Set<IndexedWord> otherDescriptives;  // used to store descriptive/clausal type content not fitting other categories

    public AbstractComponent(IndexedWord primary, SemanticGraph graph) {
       this(primary, graph, new HashSet<>());
    }

    public AbstractComponent(IndexedWord primary, SemanticGraph graph, Set<String> labels) {
        this.primary = primary;
        compound = StatementUtils.findCompound(primary, graph, getIgnoredRelations(), null);

        // remove interjections based on POS tags
        // sometimes interjections are not found in the relations!
        compound.removeAll(PartsOfSpeech.reduceToAllowedTags(compound, PartsOfSpeech.INTERJECTIONS));

        // smallest compound
        basicCompound = StatementUtils.findSpecificChildren(Relations.COMPOUND, primary, graph);
        basicCompound.add(primary);

        // medium compound
        normalCompound = StatementUtils.findSpecificChildren(Relations.AMOD, primary, graph);
        normalCompound.addAll(basicCompound);

        // relevant parts of component that are not separated out from the compound
        // Note: none currently!

        // relevant parts of the component that have been separated out from the compound
        prepositions = StatementUtils.findSpecificChildren(Relations.CASE, primary, graph);
        negations = StatementUtils.findSpecificChildren(Relations.NEG, primary, graph);
        punctuation = StatementUtils.findSpecificChildren(Relations.PUNCT, primary, graph);
        markers = StatementUtils.findSpecificChildren(Relations.MARK, primary, graph);
        cc = StatementUtils.findSpecificChildren(Relations.CC, primary, graph);
        determiners = StatementUtils.findSpecificChildren(Relations.DET, primary, graph);
        possessives = StatementUtils.findSpecificDescendants(Relations.NMOD_POSS, primary, graph);
//        adverbialClauses = StatementUtils.findSpecificDescendants(Relations.ADVCL, primary, graph);  // TODO: trying out making this embedded instead
        nounClauses = StatementUtils.findSpecificDescendants(Relations.ACL, primary, graph);
        nounClauses.addAll(StatementUtils.findSpecificDescendants(Relations.ACL_RELCL, primary, graph));
        otherDescriptives = StatementUtils.findSpecificDescendants(Relations.DESCRIPTIVE_NMOD, primary, graph);

        // conjunction are used to loosely "link" separate statements
        conjunction = StatementUtils.findSpecificChildren(Relations.CONJ, primary, graph);
        conjunction.addAll(StatementUtils.findSpecificParents(Relations.CONJ, primary, graph));

        // the stuff that doesn't go directly into the compound
        // used by containing statements to reproduce the statement text
        remaining = new HashSet<>();
        remaining.addAll(prepositions);
        remaining.addAll(negations);
        remaining.addAll(markers);
        remaining.addAll(determiners);
        remaining.addAll(possessives);
//        remaining.addAll(adverbialClauses) // TODO: trying out making this embedded instead
        remaining.addAll(nounClauses);
        remaining.addAll(otherDescriptives);
        all = new HashSet<>(compound);
        all.addAll(remaining);

        // when there are prepositions in a component (e.g. "in" or "from") which are hidden elements
        // everything that goes before the prepositions should be moved to $remaining to retain order
        if (hasPreposition()) {
            int firstDeterminerIndex = -1;
            for (IndexedWord word : prepositions) {
                if (firstDeterminerIndex == -1) {
                    firstDeterminerIndex = word.index();
                } else if (word.index() < firstDeterminerIndex) {
                    firstDeterminerIndex = word.index();
                }
            }
            if (firstDeterminerIndex > 1) {
                Set<IndexedWord> predeterminerWords = new HashSet<>();
                for (IndexedWord word : compound) {
                    if (word.index() < firstDeterminerIndex) {
                        predeterminerWords.add(word);
                    }
                }
                remaining.addAll(predeterminerWords);
                compound.removeAll(predeterminerWords);
            }
        }

        // determine the size of the array used to count gaps
        // TODO: do we really need this? (and gaps() in StatementComponent)
        int highestIndex = -1;
        int lowestIndex = -1;
        for (IndexedWord word : all) {
            if (word.index() > highestIndex) {
                highestIndex = word.index();
            }

            // assigns the first index as the lowest by default
            if (lowestIndex == -1) {
                lowestIndex = word.index();
            } else if (word.index() < lowestIndex) {
                lowestIndex = word.index();
            }
        }
        this.highestIndex = highestIndex;
        this.lowestIndex = lowestIndex;

        // the governors/parents of the component
        // some relations are ignored, e.g. the conj relation which is not treated as governor since it defines siblings
        governors = new HashSet<>();
        embeddingGovernors = new HashSet<>();
        for (SemanticGraphEdge edge : graph.incomingEdgeList(primary)) {
            // it is important to leave out certain governor relations
            // (e.g. the conj relation, since multiple of the same component type should not be connecting)
            if (!Relations.IGNORED_CONNECTING_RELATIONS.contains(edge.getRelation().getShortName())) {
                governors.add(edge.getGovernor());
            }
            if (Relations.EMBEDDED_STATEMENT_SCOPES.contains(edge.getRelation().getShortName())) {
                embeddingGovernors.add(edge.getGovernor());
            }
        }

        this.labels = labels;
    }

    /**
     * Describes which relations are ignored when producing the compound.
     */
    protected Set<String> getIgnoredRelations() {
        return Relations.IGNORED_OUTGOING_RELATIONS;
    }

    public String getBasicCompound() {
        return StatementUtils.join(basicCompound, true, true);
    }

    public String getNormalCompound() {
        return StatementUtils.join(normalCompound, true, true);
    }

    /**
     * The primary word contained within the compound component.
     *
     * @return primary word
     */
    public IndexedWord getPrimary() {
        return primary;
    }

    /**
     * Every main word of the component.
     *
     * @return compound
     */
    public Set<IndexedWord> getCompound() {
        return compound;
    }

    /**
     * All words of the component.
     *
     * @return compound
     */
    public Set<IndexedWord> getAll() {  // TODO: rename
        return all;
    }

    /**
     * The remaining words of the component.
     *
     * @return compound
     */
    public Set<IndexedWord> getRemaining() {  // TODO: rename
        return remaining;
    }

    /**
     * The negations.
     *
     * @return negations
     */
    public Set<IndexedWord> getNegations() {
        return negations;
    }

    /**
     * The punctuation.
     *
     * @return punctuation
     */
    public Set<IndexedWord> getPunctuation() {
        return punctuation;
    }

    /**
     * All conjunction words.
     *
     * @return markers
     */
    public Set<IndexedWord> getCc() {
        return cc;
    }

    /**
     * All markers.
     *
     * @return markers
     */
    public Set<IndexedWord> getMarkers() {
        return markers;
    }

    /**
     * All prepositions.
     *
     * @return prepositions
     */
    public Set<IndexedWord> getPrepositions() {
        return prepositions;
    }

    /**
     * All prepositions.
     *
     * @return prepositions
     */
    public Set<IndexedWord> getPossessives() {
        return possessives;
    }

    /**
     * All adverbial clauses for every entry.
     *
     * @return adverbial clauses
     */
    public Set<IndexedWord> getAdverbialClauses() {
//        return adverbialClauses; // TODO: trying out making this embedded instead
        return new HashSet<>();
    }

    /**
     * All noun clauses for every entry.
     *
     * @return noun clauses
     */
    public Set<IndexedWord> getNounClauses() {
        return nounClauses;
    }

    /**
     * All clauses (of all kinds) for every entry.
     *
     * @return clauses
     */
    public Set<IndexedWord> getDescriptives() {
        Set<IndexedWord> clauses = new HashSet<>();
        clauses.addAll(getAdverbialClauses());
        clauses.addAll(getNounClauses());
        clauses.addAll(getOtherDescriptives());
        return clauses;
    }

    /**
     * Outgoing (= governing) compound.
     * Useful for establishing whether this statement component is connected to another component.
     *
     * @return governors
     */
    public Set<IndexedWord> getGovernors() {
        return governors;
    }

    public Set<IndexedWord> getEmbeddingGovernors() {
        return embeddingGovernors;
    }

    /**
     * Conjunctions.
     *
     * @return conjunction
     */
    public Set<IndexedWord> getConjunction() {
        return conjunction;
    }

    /**
     * Whether the component is negated.
     *
     * @return true if negated
     */
    public boolean isNegated() {
        return negations.size() % 2 != 0;
    }

    /**
     * Whether a component is plural.
     * Only applies to nouns (obviously).
     *
     * @return true if plural
     */
    public boolean isPlural() {
        return PartsOfSpeech.PLURAL.contains(getPrimary().tag()) || Lexicon.PLURAL_NON_NOUNS.contains(getPrimary().word().toLowerCase());
    }

    /**
     * Whether a component is specific or general.
     * @return true if specific
     */
    public boolean isSpecific() {
        for (IndexedWord word : determiners) {
            if (Lexicon.SPECIFIC_DETERMINERS.contains(word.word().toLowerCase())) {
                return true;
            }
        }

       return false;
    }

    /**
     * Whether a component is very specific.
     * This only differs from isSpecific(...) by not including considering the definite article.
     *
     * @return true if specific
     */
    public boolean isLocal() {
        for (IndexedWord determiner : determiners) {
            if (Lexicon.LOCAL_DETERMINERS.contains(determiner.word().toLowerCase())) {
                return true;
            }
        }

        return false;
    }

    /**
     * Whether a component is capitalised.
     * This method checks the normal compound to see if any of the words are capitalised,
     * e.g. the words "American president" would return true.
     *
     * @return true if capitalised
     */
    public boolean isCapitalised() {
        for (IndexedWord word : normalCompound) {
            if (Character.isUpperCase(word.word().charAt(0))) return true;
        }

        return false;
    }

    public boolean isPronoun() {
        // TODO: what about possesives?
        return getPrimary().tag().equals(PartsOfSpeech.PRP);
    }

    public boolean isProperNoun() {
        return PartsOfSpeech.PROPER_NOUNS.contains(getPrimary().tag());
    }

    public boolean isNoun() {
        return PartsOfSpeech.NOUNS.contains(getPrimary().tag());
    }

    public boolean isVerb() {
        return PartsOfSpeech.VERBS.contains(getPrimary().tag());
    }

    public boolean isAdjective() {
        return PartsOfSpeech.ADJECTIVES.contains(getPrimary().tag());
    }

    public boolean isAdverb() {
        return PartsOfSpeech.ADVERBS.contains(getPrimary().tag());
    }

    public boolean isFirstPerson() {
        return isPronoun() && Lexicon.FIRST_PERSON.contains(getPrimary().word().toLowerCase());
    }

    public boolean isSecondPerson() {
        return isPronoun() && Lexicon.SECOND_PERSON.contains(getPrimary().word().toLowerCase());
    }

    public boolean hasPossessive() {
        return !getPossessives().isEmpty();
    }

    public boolean hasFirstPersonPossessive() {
        if (hasPossessive()) {
            for (IndexedWord word : getPossessives()) {
                if (Lexicon.FIRST_PERSON_POSSESSIVES.contains(word.word().toLowerCase())) return true;
            }
        }

        return false;
    }

    public boolean hasSecondPersonPossessive() {
        if (hasPossessive()) {
            for (IndexedWord word : getPossessives()) {
                if (Lexicon.SECOND_PERSON_POSSESSIVES.contains(word.word().toLowerCase())) return true;
            }
        }

        return false;
    }

    public boolean hasThirdPersonPossessive() {
        return hasPossessive() && !hasFirstPersonPossessive() && !hasSecondPersonPossessive();
    }

    public boolean hasPreposition() {
        return !getPrepositions().isEmpty();
    }

    private Set<String> getPosTags() {
        Set<String> partsOfSpeech = new HashSet<>();
        for (IndexedWord word : getCompound()) {
            partsOfSpeech.add(word.tag());
        }
        return partsOfSpeech;
    }

    public boolean hasPosTags(String... tags) {
        Set<String> partsOfSpeech = getPosTags();
        for (String tag : tags) if (partsOfSpeech.contains(tag)) return true;
        return false;
    }

    public boolean hasPosTags(Set<String> tags) {
        return hasPosTags(tags.toArray(new String[tags.size()]));
    }

    public boolean hasDescription() {
        return !getDescriptives().isEmpty();
    }

    /**
     * The compound as a string.
     *
     * @return the longest string possible
     */
    protected String getString() {
        Set<IndexedWord> expandedCompound = getCompound();
        expandedCompound.addAll(getNegations());
        return StatementUtils.join(expandedCompound);
    }

    /**
     * The labels of component.
     * In most cases, components don't have a label, but in some cases they can be useful.
     * Component labels can be used to label their containing statements.
     * Labeling containing statements can provide information
     * on, for example, how an embedded statement fits into its parent statement.
     *
     * @return labels
     */
    public Set<String> getLabels() {
        return labels;
    }

    @Override
    public boolean contains(StatementComponent otherComponent) {
        return false;
    }

    @Override
    public String toString() {
        Set<String> conjunctions = new HashSet<>();
        for (IndexedWord word : getConjunction()) {
            conjunctions.add(word.word());
        }

        return "{" +
            getClass().getSimpleName() + ": \"" + getString() + "\"" +
//            ", gaps: " + gaps() +  // TODO: remove after done debugging
            (!getDescriptives().isEmpty()? ", description: \"" + StatementUtils.join(getDescriptives()) + "\"" : "") +
            (!getPrepositions().isEmpty()? ", preposition: \"" + StatementUtils.join(getPrepositions()) + "\"" : "") +
            (!getPossessives().isEmpty()? ", possessive: \"" + StatementUtils.join(getPossessives()) + "\"" : "") +
//            (!getPossessives().isEmpty()? ", possessive: \"" + getPossessives() + "\"" : "") +
//            ", pos: \"" + getPrimary().tag() + "\"" +
            ", primary: \"" + getPrimary() + "\"" +
//            ", local: \"" + (isLocal()? "yes" : "no") + "\"" +
//            ", negated: \"" + (isNegated()? "yes" : "no") + "\"" +
//            ", plural: \"" + (isPlural()? "yes" : "no") + "\"" +
//            (!getLabels().isEmpty()? ", labels: \"" + String.join(", ", getLabels()) + "\"" : "") +
            (!conjunctions.isEmpty()? ", conjunction: \"" + String.join(", ", conjunctions) + "\"" : "") +
        "}";
    }

    public Set<IndexedWord> getOtherDescriptives() {
        return otherDescriptives;
    }
}
