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
}
