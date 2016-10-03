Statement annotator
===================
This is a custom annotator for CoreNLP that can be used for information extraction.
The annotator builds a layer on top of the dependency parses created by nndep.
This layer consists of Statement objects: a lexico-syntactic representation of the underlying statements in a sentence.
A Statement is a container for Statement components: Subjects, Verbs, DirectObjects, and IndirectObjects -
as well as embedded Statement objects representing dependent clauses.

This annotator has not been developed to populate large knowledge databases,
but rather as way to structure natural language to allow for lexico-syntactic pattern matching.
The Statement objects of a sentence can be matched using the StatementPattern class
and its constituent components can be extracted for further processing.

Basic pipeline setup
--------------------
StatementFinder can be used independently to output statements found in a SemanticGraph,
however, it is recommended to use the CoreNLP pipeline.

```java
props.setProperty("annotators", "tokenize, ssplit, pos, lemma, depparse, statements");
props.setProperty("customAnnotatorClass.statements", "statements.StatementAnnotator");
```

The only requirement is the standard CoreNLP neural network dependency parser and its requirements,
as well as the lemma annotator.

Thesis
------
This annotator was originally conceived of as a part of a Master's thesis at the IT University of Copenhagen.
The thesis is titled ["Adding context to online discussions through the generation of user profiles"](https://github.com/simongray/StatementAnnotator/raw/master/Thesis_final_no_appendix_fixed.pdf).

