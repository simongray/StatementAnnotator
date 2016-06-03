Statement annotator
===================

This is a custom annotator for CoreNLP that performs information extraction on sentences.
The extractions are added as annotations on the sentences and are essentially a more flexible kind of n-tuple
than what is typically used for information extraction, e.g. 3-tuple fact extraction.

It has not been developed to populate large knowledge databases,
but rather as way to structure natural language to allow for easier

* comparison of statements, e.g. between different individuals
* composition of related statements into natural language text

However, it also has other potential uses, including being used for fact extraction.

Basic pipeline setup
--------------------

StatementFinder can be used independently to output statements found in a SemanticGraph,
however, it is recommended to use the CoreNLP pipeline.

```java
props.setProperty("annotators", "tokenize, ssplit, pos, depparse, statements");
props.setProperty("customAnnotatorClass.statements", "statements.StatementAnnotator");
```

The only requirement is the standard CoreNLP neural network dependency parser and its requirements.
