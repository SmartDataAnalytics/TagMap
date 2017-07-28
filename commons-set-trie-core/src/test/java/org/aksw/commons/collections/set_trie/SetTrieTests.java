package org.aksw.commons.collections.set_trie;

import java.util.Arrays;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

import com.google.common.collect.Sets;

public class SetTrieTests {

    // TODO Add the InvertedIndex implementation for comparison
    @Test
    public void testSetTrieCorrectnessByComparison() {
        TagMap<String, Integer> simpleFm = new TagMapSimple<>();
        TagMap<String, Integer> setTrieFm = new TagMapSetTrie<>();
        TagMap<String, Integer> invertedListFm = new TagMapInvertedIndex<>();


        TagMap<String, Integer> fm = ValidationUtils.createValidatingProxy(setTrieFm, simpleFm);
        //new FeatureMapValidating<>(setTrieFm, simpleFm);


        Random rand = new Random(0);
        for(int i = 0; i < 100; ++i) {
            int size = rand.nextInt(50);
            Set<Integer> set = IntStream.range(0, size).map(x -> rand.nextInt(100)).boxed().collect(Collectors.toSet());

            String key = "item" + i;
            System.out.println(key + " -> " + set);

            fm.put(key, set);
        }


        fm.put("a", Sets.newHashSet(1, 2, 3));
        fm.put("b", Sets.newHashSet(1, 2, 4));
        fm.put("c", Sets.newHashSet(2, 3, 4));

        System.out.println("Subsets 1");
        fm.getAllSubsetsOf(Arrays.asList(1, 2, 3))
            .entrySet().forEach(e -> System.out.println("" + e));

        System.out.println("Supersets 1");
        fm.getAllSupersetsOf(Arrays.asList(1, 4))
            .entrySet().forEach(e -> System.out.println("" + e));
    }
}
