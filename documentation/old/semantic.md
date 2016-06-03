Rather than focusing simply on sentiment and entities, I want to get into more detail on the opinions. The base of this new system is an annotator called SemanticAnnotator that can find subjects and objects in a sentence. The objects can be both concepts (verb or verb+object) or simply noun phrases. The goal is to use these semantic annotations for more exact opinions and who is expressing them. By default, the assumed opinion holder is "the I" (the writer) of the comment, but s/he might also be expressing something in the third person.

These opinions can then in turn also contain a sentiment which can be attached using the chosen sentiment analysis system (Stanford's or perhaps SentiStrength). However, the sentiment becomes less relevant if the relationships can be matched, e.g. when comparing opinions if both Person X and Person Y  "play football" then sentiment is not even needed.

Sentiment would be necessary in case of comparing opinions on third parties, e.g if Person X thinks that "Clinton sucks donkeyballs" it might be hard to find an identical statement by Person Y. However, if both have an overall negative opinion about Clinton, then this can be used.

Opinions vs sentiment
=================

I want to be inspired by Bing Liu's definitions of opinion and sentiment and eventually produce Java classes that closely resemble them. Most likely, I will skip the aspect... aspect!

>Definition 2.1 (Opinion):
-------------------------------
An **opinion** is a quadruple,

 >```
(g,s,h,t),
```

>where g is the sentiment target, s is the sentiment of the opinion about the target g, h is the opinion holder (the person or organization who holds the opinion), and t is the time when the opinion is expressed.

and

>Definition 2.4 (Sentiment):
------------------------------------
Sentiment is the underlying feeling, attitude, evaluation, or emotion associated with an opinion. It is represented as a triple,

>```
(y,o,i),

>where y is the type of the sentiment, o is the orientation of the sentiment, and i is
the intensity of the sentiment.

and

>Definition 2.7 (Opinion):
---------------------------------
An opinion is a quintuple,

>```
(e,a,s,h,t),
```

>where e is the target entity, a is the target aspect of entity e on which the opinion has been given, s is the sentiment of the opinion on aspect a of entity e, h is the opinion holder, and t is the opinion posting time; s can be positive, negative, or neutral, or a rating (e.g., 1–5 stars). When an opinion is only on the entity as a whole, the special aspect GENERAL is used to denote it. Here e and a together represent the opinion target.

Implementation details
=================
TBD

General architecture
---------------------------
TBD

Integration with personal details
------------------------------------------
TBD
