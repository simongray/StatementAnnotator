Annotator algorithm
----------------------------------------------

I'm documenting where I deviate from the approach taken in this
paper. The excerpt is from "Entity-Specific Sentiment
Classification of Yahoo News Comments".

```
    The context of an entity contains the words,
    phrases or sen- tences that refer to the entity.
    We use several heuristics to extract the contexts.
    Following are the three main modules of our
    context extraction algorithm:

    1. Preprocessing, where the number of entities in
    a com- ment is checked. For single entity
    comments, the entire com- ment is taken as the
    context for the entity. If a comment con- tains
    multiple entities, it is segmented into sentences
    and is given as input to the anaphora resolution
    module.
```

So far so good, no major deviations here.
I also treat the entire comment as a single context when only
one entity is discovered.

Segmenting into sentences is the common way to process
data in CoreNLP.

```
    2. Anaphora Resolution: We use a rule based
    approach to anaphora resolution. We check the type
    of entity: PER- SON (P) vs. NON-PERSON (NP) and
    assign sentences to the context of the entity if
    they have explicit mentions of that entity or
    compatible anaphoric references. For exam- ple,
    pronouns such as he, she, her, him can only be
    used to refer to a P entity, whereas they, their,
    them can be used to refer to both P and NP
    entities and it can only be used for NP entities.
    If a sentence does not have references to any
    entity, then it is added to the context of all the
    entities. Also, if a sentence has explicit
    mentions of multiple entities, then it is given as
    input to the local context extraction module.
```

At first I wanted to use the Anaphore/coreference resolution
that is included with CoreNLP, but it is simply too slow (dcoref).
It is possible that coref would be fast enough,
but I don't enough RAM on my machine to run that annotator,
so I prefer just implementing the algorithm from the paper.

My implementation differs in slightly by also merging entities
with shorter versions of themselves,
e.g. Clinton is merged with Bill Clinton,
in cases where there is little doubt
(i.e. no conflicting references to other entities).

I also use the gender annotator from CoreNLP to limit the mentions,
so that Bill Clinton will never match up with "she" in the
neighbouring sentence.

These are the words used to match:

PERSON/MALE: he, him, (they, their)
PERSON/FEMALE: she, her, (they, their)
ORGANIZATION, LOCATION: they, their, them
MISC: no matching

```
    3. Local Context Extraction: If entities occur in
    clauses that are connected with “but” (in the
    sentence), then the re- spective clauses are
    returned as local contexts for the enti- ties. If
    the sentence contains a comparison between
    entities, then it is split at the comparative term
    (adjective or adverb), with the comparative term
    added to the left part, and the two parts are
    returned as local contexts for the respective
    enti- ties. If none of the two conditions is
    satisfied, then a window of ±3 tokens around
    entities is taken as their local context.
    Identifying the Sentiment of Contexts

    After obtaining the contexts of entities, we
    classify their sen- timent into positive, negative
    or neutral sentiment classes. We model the task of
    identifying sentiment as two step clas-
    sification. In the first step, we classify the
    context of an en- tity into polar versus neutral
    sentiment classes. Next, we classify the polar
    entities into positive or negative sentiment
    classes. Next, we describe the features used in
    our classifi- cation models and our reasoning
    behind using them. Neutral vs. Polar
    Classification As already discussed, comments
    posted on news sites contain entities that are
    irrel- evant with respect to sentiment analysis
    (see Example 1 in Section ). These entities have
    no sentiment associated with

    them and are filtered out before conducting
    sentiment clas- sification of comments. We address
    this problem by classi- fying entities as polar
    vs. neutral. Irrelevant entities are clas- sified
    as neutral.
```

This is where my implementation differs in a major way.
The approach taken here is rule-based, using a few grammatical
heuristics to split shared contexts.
They use a lexical sentiment analysis system (SentiStrength)
to produce a sentiment of either positive, neutral, or negative.

In contrast, I use the CoreNLP sentiment analysis,
based on the Socher et al (2013) paper,
which produces 5-grade sentiment scores on each section
in the grammatical parse tree.
What this essentially means, is that each grammatical sub-context
already has a sentiment score and rather than using heuristics,
the implicit grammatical rules of the parse tree
allows for spits at the precise point of contention
between two different entities.

For single-entity sentences the point of contention doesn't exist,
which means that the score from the top of the tree is used,
i.e. the score of the entire sentence.
For multi-entity sentences, the score at the highest point
that is not shared by another entity is used.
