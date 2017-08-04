package org.aksw.commons.collections.feature_map.benchmark;

import java.util.Collections;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.aksw.beast.benchmark.performance.BenchmarkTime;
import org.aksw.beast.enhanced.ResourceData;
import org.aksw.beast.rdfstream.RdfGroupBy;
import org.aksw.beast.rdfstream.RdfStream;
import org.aksw.beast.vocabs.CV;
import org.aksw.beast.vocabs.IV;
import org.aksw.beast.vocabs.OWLTIME;
import org.aksw.commons.collections.tagmap.TagMap;
import org.aksw.commons.collections.tagmap.TagMapSetTrie;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.sparql.expr.aggregate.AggAvg;
import org.apache.jena.sparql.expr.aggregate.AggSum;
import org.apache.jena.sparql.expr.aggregate.lib.AccStatStdDevPopulation;
import org.apache.jena.vocabulary.RDFS;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.codepoetics.protonpack.StreamUtils;

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


    public static Stream<Set<Integer>> createWorkloads(int size, int randSeed) {
        Random rand2 = new Random(randSeed);
        return IntStream.range(0, size).mapToObj(i -> createWorkload(rand2));
    }


    public static void metaExperiment() {
        IncrementalResourceSupplier rs = new IncrementalResourceSupplier()
                .setModelSupplier(() -> ModelFactory.createDefaultModel());

        int n = 1000000;
        // Measure the overhead of zipWithResource
        Resource p = ModelFactory.createDefaultModel().createResource();
        BenchmarkTime.benchmark(p, () -> createWorkloads(n, 0).count());
        RDFDataMgr.write(System.out, p.getModel(), RDFFormat.TURTLE_PRETTY);


        p = ModelFactory.createDefaultModel().createResource();
        BenchmarkTime.benchmark(p, () -> RdfStream.<Set<Integer>>zipWithResource(rs::get).apply(() -> createWorkloads(n, 0)).get().count());
        RDFDataMgr.write(System.out, p.getModel(), RDFFormat.TURTLE_PRETTY);


        System.out.println("Done for now.");





        //workloadFactory.get().forEach(System.out::println);

        RdfStream<Set<Integer>, ResourceData<Set<Integer>>> workflow = RdfStream
            .<Set<Integer>>zipWithResource(rs::get)
            .repeat(5, IV.run, 0)
            .filter(r -> r.getProperty(IV.run).getInt() > 2)
            .repeat(2, IV.phase, 0)
            .peek(r -> r.rename("{0}-{1}-{2}", r.getURI(), IV.phase, IV.run));


        workflow.apply(() -> createWorkloads(10, 0))
            .get()
            .forEach(x -> {
                RDFDataMgr.write(System.out, x.getModel(), RDFFormat.TURTLE_PRETTY);
                System.out.println(x.getData());
                System.out.println();
            });

//        BiConsumer<Resource, Set<?>> performLookup = (r, set) -> {
//            int lookupSize = BenchmarkTime.benchmark(r, () -> featureMap.getAllSubsetsOf(set, false).size());
//            r.addLiteral(LSQ.resultSize, lookupSize);
//        };
//
//
//        Model overall = ModelFactory.createDefaultModel();
//
//        // Build the workflow template
//        PerformanceBenchmark.createQueryPerformanceEvaluationWorkflow(
//                Set.class,
//                w -> w.as(ResourceEnh.class).getTag(Set.class).get(),
//                (r, s) -> performLookup.accept(r, s), 2, 5)
//        .peek(observationRes -> observationRes.addLiteral(IV.job, "job1"))
//        .peek(observationRes -> observationRes.addLiteral(IV.method, "method1"))
//        .map(observationRes -> observationRes.rename("http://example.org/observation/{0}-{1}", IV.run, IV.job))
//        // instanciate it for our  data
//        .apply(() -> (Stream<Resource>)null).get()
//        //.apply(() -> workloadFactory.get()).get()
//        // write out every observation resource
//        //.forEach(observationRes -> RDFDataMgr.write(System.out, observationRes.getModel(), RDFFormat.TURTLE_BLOCKS));
//        .forEach(r -> overall.add(r.getModel()));
//
//        Set<Resource> observations = overall.listResourcesWithProperty(RDF.type, QB.Observation).toSet();
//
//

    }

    // stream.map(zipWithWorkload)

    public static void main(String[] args) {
        //Resource Workload = ResourceFactory.createResource("http://example.org/ontology/Workload");
        IncrementalResourceSupplier rs = new IncrementalResourceSupplier()
                .setModelSupplier(() -> ModelFactory.createDefaultModel());

        logger.debug("Generating and loading data...");

        int n = 10000;
        int l = 10000;

        TagMap<String, Integer> featureMap = new TagMapSetTrie<>();
        StreamUtils.zipWithIndex(createWorkloads(n, 0))
            .forEach(e -> featureMap.put("item" + e.getIndex(), e.getValue()));


        List<Set<Integer>> lookups = createWorkloads(l, 666).collect(Collectors.toList());

        logger.debug("Starting benchmark...");

        List<Resource> observations = RdfStream
            .<String>zipWithResource(rs::get)
            .peek(r -> r.addLiteral(IV.job, r.getData()))
            .peek(r -> BenchmarkTime.benchmark(r, () -> lookups.forEach(set -> {
                int size = featureMap.getAllSubsetsOf(set, false).size();
                //System.out.println(size);
            })))
            .repeat(10, IV.run, -5)
            .filter(r -> r.getProperty(IV.run).getInt() >= 0)
            .peek(r -> RDFDataMgr.write(System.err, r.getModel(), RDFFormat.TURTLE_PRETTY))
            .map(r -> (Resource)r)
            .apply(Collections.singleton("task1"))
            .get()
            .collect(Collectors.toList())
            ;

        logger.debug("Aggregating...");

        List<Resource> avgs =
        RdfGroupBy.enh()
            .on(IV.job) // This is just the local name of the workload
            .agg(RDFS.label, OWLTIME.numericDuration, AggSum.class) // total time
            .agg(CV.value, OWLTIME.numericDuration, AggAvg.class)
            .agg(CV.stDev, OWLTIME.numericDuration, AccStatStdDevPopulation.class)
            .apply(observations.stream())
            .map(g -> g.rename("http://ex.org/avg/{0}", IV.job))
            .collect(Collectors.toList());

        for(Resource avg : avgs) {
            RDFDataMgr.write(System.out, avg.getModel(), RDFFormat.TURTLE_PRETTY);
        }


        logger.info("Done.");
    }
}
