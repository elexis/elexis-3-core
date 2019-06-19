# What is it

HAPI FHIR is a simple-but-powerful library for adding FHIR messaging to your application.
See http://hapifhir.io/

# Why are the libraries contained in this plugin not in the target?

HAPI Fhir switch to a structure, where there is only a single plugin (base) and all the
other plugins are fragments to this plugin. It is not possible at development time
to include fragments, which in this case contain necessary libraries. In order to solve
this, these bundles where simply downloaded from maven, and packed within a wrapper bundle
which is what you see here.