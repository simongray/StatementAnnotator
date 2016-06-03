Statement finding
=================

Definition
----------
Statements can consist of the following pure components:

	* subjects
	* verbs
	* direct objects
	* indirect objects

as well as

	* nested statements (= statements that act as dependent clauses)

A statement may _only_ contain 1 or 0 of each component type.

Finding algorithm
-----------------

A CoreNLP dependency graph of a sentence is used as input.

1. all pure components are extracted separately from the dependency graph
	- using simple knowledge of basic dependencies to find primary component words
	- following descendant relations of the primary words to find the complete components
	- ignoring interdependencies to other pure components
2. pure components are linked based on their interdependencies
	- conjunction relations between components cause splits of the component sets into multiple sets
4. the sets of linked pure components are used as input for constructing statements
5. nested statements are discovered based on composition dependencies in the graph and composed into other statements
6. the output statements each have 1-5 components of a unique type (S, V, DO, IO, nested statement)
