package org.aksw.commons.collections.feature_map.benchmark;

import java.util.function.Function;
import java.util.function.Supplier;

import org.aksw.beast.enhanced.ResourceData;
import org.apache.jena.enhanced.EnhGraph;
import org.apache.jena.graph.Node;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;

public class IncrementalResourceSupplier
{
    protected Supplier<Model> modelSupplier;
    protected String baseIri = "http://beast.aksw.org/resource/workload";

    public Supplier<Model> getModelSupplier() {
        return modelSupplier;
    }

    public IncrementalResourceSupplier setModelSupplier(Supplier<Model> modelSupplier) {
        this.modelSupplier = modelSupplier;
        return this;
    }

    public String getBaseIri() {
        return baseIri;
    }

    public IncrementalResourceSupplier setBaseIri(String baseIri) {
        this.baseIri = baseIri;
        return this;
    }

    //@Override
    public <X> Function<X, ResourceData<X>> get() {

        int nextId[] = {0};

        return (item) -> {

            String iri = baseIri + (nextId[0]++);

            Model m = modelSupplier == null ? null : modelSupplier.get();
            Resource r = m == null ? ResourceFactory.createResource(iri) : m.createResource(iri);

            EnhGraph x = (EnhGraph)m;
            Node n = r.asNode();

            ResourceData<X> result = new ResourceData<>(n, x, item);

            return result;
        };
    }

}
