Context Resolvers
=================

Context resolvers are modules that determine which part of a sentence is the appropriate context for an entity.
They are implementations of the ContextResolver interface. The default implementation is the LongestPathResolver.
Other implementations can be used by setting the appropriate property when building the CoreNLP pipeline.

LongestPathResolver ("longestpath")
-----------------------------------

Paths are found from each entity up through the tree to the ROOT tag.
This context resolver uses as context the _highest uncrossed point_
in the path from the entity to the highest placed S tag on its path (i.e. the S closest to the ROOT tag).
If other entity paths intersect, then they use the point starting immediately before the crossing point as context.
If a path contains no S tag, the ROOT is treated as the S tag instead.
