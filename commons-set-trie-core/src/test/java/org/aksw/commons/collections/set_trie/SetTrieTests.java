package org.aksw.commons.collections.set_trie;

import java.util.Arrays;
import java.util.List;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.junit.Test;

public class SetTrieTests {

    // TODO Add the InvertedIndex implementation for comparison
    @Test
    public void testSetTrieCorrectnessByComparison() {
        SetTrie<String, Integer> setTrie = new SetTrie<>();

        Random rand = new Random(0);
        for(int i = 0; i < 10000; ++i) {
            int size = rand.nextInt(50);
            List<Integer> set = IntStream.range(0, size).map(x -> rand.nextInt(100)).boxed().collect(Collectors.toList());

            String key = "item" + i;
            System.out.println(key + " -> " + set);

            setTrie.put(key, set);
        }


        setTrie.put("a", Arrays.asList(1, 2, 3));
        setTrie.put("b", Arrays.asList(1, 2, 4));
        setTrie.put("c", Arrays.asList(2, 3, 4));

        System.out.println("Subsets 1");
        setTrie.getAllSubsetsOf(Arrays.asList(1, 2, 3))
            .entrySet().forEach(e -> System.out.println("" + e));

        System.out.println("Supersets 1");
        setTrie.getAllSupersetsOf(Arrays.asList(1, 4))
            .entrySet().forEach(e -> System.out.println("" + e));
    }
}
