package statements.matching;

import it.uniroma1.lcl.adw.ADW;
import it.uniroma1.lcl.adw.DisambiguationMethod;
import it.uniroma1.lcl.adw.ItemType;
import it.uniroma1.lcl.adw.comparison.SignatureComparison;
import it.uniroma1.lcl.adw.comparison.WeightedOverlap;
import statements.core.Statement;

import java.util.HashSet;
import java.util.Set;

public class AdwComparison {
    private final ADW adw;
    private final DisambiguationMethod disMethod;
    private final SignatureComparison measure;
    private final Set<String> opinionVerbs;

    public AdwComparison() {
        this.adw = new ADW();

        // if lexical items has to be disambiguated
        this.disMethod = DisambiguationMethod.ALIGNMENT_BASED;

        // measure for comparing semantic signatures
        this.measure = new WeightedOverlap();

        this.opinionVerbs = new HashSet<>();
        opinionVerbs.add("like#v");
        opinionVerbs.add("love#v");
        opinionVerbs.add("prefer#v");
        opinionVerbs.add("hate#v");
        opinionVerbs.add("dislike#v");
    }

    public boolean resembles(String prototype, Statement otherStatement) {
        double score = adw.getPairSimilarity(
                prototype,
                otherStatement.getSentence(),
                disMethod,
                measure,
                ItemType.SURFACE,
                ItemType.SURFACE);

        return score > 0.5;
    }
}
