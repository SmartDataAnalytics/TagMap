package org.aksw.commons.collections.feature_map.benchmark;

import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.function.BiConsumer;
import java.util.function.Supplier;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.beast.benchmark.performance.BenchmarkTime;
import org.aksw.beast.benchmark.performance.PerformanceBenchmark;
import org.aksw.beast.enhanced.ModelFactoryEnh;
import org.aksw.beast.enhanced.ResourceEnh;
import org.aksw.beast.rdfstream.RdfGroupBy;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.IV;
import org.aksw.beast.vocabs.OWLTIME;
import org.aksw.beast.vocabs.QB;
import org.aksw.commons.collections.set_trie.TagMap;
import org.aksw.commons.collections.set_trie.TagMapSetTrie;
import org.aksw.commons.collections.set_trie.TagMapSimple;
import org.aksw.iguana.vocab.IguanaVocab;
import org.aksw.simba.lsq.vocab.LSQ;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.rdf.model.ResourceFactory;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.expr.aggregate.lib.AccStatStdDevPopulation;
import org.apache.jena.vocabulary.RDF;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class FeatureMapBenchmark {

    private static final Logger logger = LoggerFactory.getLogger(FeatureMapBenchmark.class);


//    public Set<Integer> createIntSet(int size, Random rand) {
//        Set<Integer> result = IntStream.range(0, size).map(x -> rand.nextInt(100)).boxed().collect(Collectors.toSet());
//        return result;
//    }

    public static Set<Integer> createWorkload(Random rand) {
        int size = rand.nextInt(50);
        Set<Integer> result = IntStream.range(0, size).map(x -> rand.nextInt(100)).boxed().collect(Collectors.toSet());

        return result;
    }

    public static void main(String[] args) {

        Resource Workload = ResourceFactory.createResource("http://example.org/ontology/Workload");


        TagMap<String, Integer> featureMap = new TagMapSetTrie<>();
        // Create the dataset
        Random rand = new Random(0);
        IntStream.range(0, 1000).forEach(i -> featureMap.put("item" + i, createWorkload(rand)));


        Supplier<Stream<Resource>> workloadFactory = () -> {
            Random rand2 = new Random(0);
            return IntStream.range(0, 10000)
                .mapToObj(i -> {
                    Set<Integer> set = createWorkload(rand2);

                    Resource r = ModelFactoryEnh.createModel()
                        .createResource("http://aksw.org/resource/benchmark/featuremap/workload" + i).as(ResourceEnh.class)
                        .addTag(Set.class, set)
                        .addProperty(RDF.type, Workload);

                    return r;
                });
        };


        BiConsumer<Resource, Set<?>> performLookup = (r, set) -> {
            int lookupSize = BenchmarkTime.benchmark(r, () -> featureMap.getAllSubsetsOf(set, false).size());
            r.addLiteral(LSQ.resultSize, lookupSize);
        };

        Model overall = ModelFactory.createDefaultModel();

        // Build the workflow template
        PerformanceBenchmark.createQueryPerformanceEvaluationWorkflow(
                Set.class,
                w -> w.as(ResourceEnh.class).getTag(Set.class).get(),
                (r, s) -> performLookup.accept(r, s), 2, 5)
        .peek(observationRes -> observationRes.addLiteral(IV.job, "job1"))
        .peek(observationRes -> observationRes.addLiteral(IV.method, "method1"))
        .map(observationRes -> observationRes.rename("http://example.org/observation/{0}-{1}", IV.run, IV.job))
        // instanciate it for our  data
        .apply(() -> workloadFactory.get()).get()
        // write out every observation resource
        //.forEach(observationRes -> RDFDataMgr.write(System.out, observationRes.getModel(), RDFFormat.TURTLE_BLOCKS));
        .forEach(r -> overall.add(r.getModel()));

        Set<Resource> observations = overall.listResourcesWithProperty(RDF.type, QB.Observation).toSet();



        List<Resource> avgs =
        RdfGroupBy.enh()
            //.on(IguanaVocab.workload)
            .on(IV.job) // This is just the local name of the workload
            .on(IV.method)
            .agg(RDFS.label, OWLTIME.numericDuration, AggSum.class) // total time
            .agg(CV.value, OWLTIME.numericDuration, AggAvg.class)
            .agg(CV.stDev, OWLTIME.numericDuration, AccStatStdDevPopulation.class)
            .apply(observations.stream())
            //.map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", IV.job, IV.thread, IV.thread))
            .map(g -> g.rename("http://ex.org/avg/query{0}-user{1}", IV.job, IV.method))
            .collect(Collectors.toList());

        for(Resource avg : avgs) {
            RDFDataMgr.write(System.out, avg.getModel(), RDFFormat.TURTLE_PRETTY);
        }


        logger.info("Done.");
    }
}
