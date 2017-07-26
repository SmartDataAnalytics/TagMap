package org.aksw.commons.collections.set_trie;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.NavigableMap;
import java.util.NavigableSet;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

/**
 * Set-based trie implementation for fast answering of super and subset queries.
 * Implementation is based on the publication
 * "Index Data Structure for Fast Subset and Superset Queries" by "Iztok Savnik"
 *
 *
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class SetTrie<K, V> {

    protected class Node
    {
        public Node(long id, Node parent, V currentValue) {
            super();
            this.id = id;
            this.parent = parent;
            this.currentValue = currentValue;
            this.nextValueToChild = null; //new TreeMap<>();//HashMap<>();
        }

        protected long id;
        Node parent;
        V currentValue;
        NavigableMap<V, Node> nextValueToChild;
        Map<K, SortedSet<V>> keyToSet;


        @Override
        public String toString() {
            Node c = this;
            List<V> vs = new ArrayList<>();
            while(c != rootNode) {
                vs.add(c.currentValue);
                c = c.parent;
            }
            Collections.reverse(vs);

            return "SetTrie node for " + vs + " with associated keys " + keyToSet.keySet();
        }
    }

    protected Comparator<? super V> comparator;

    // Mapping from key to node - used for fast removal
    protected Map<K, Node> keyToNode = new HashMap<>();

    // An id counter for human readable node ids - may be useful for debugging / logging
    protected long nextId = 0;

    // Root node of the trie datastructure
    protected Node rootNode = new Node(nextId++, null, null);

    public void put(K key, Collection<V> set) {
        // Remove any possibly existing prior association with the key
        remove(key);

        NavigableSet<V> navSet = new TreeSet<V>(comparator);
        navSet.addAll(set);
        Iterator<V> it = set.iterator();

        Node currentNode = rootNode;
        while(it.hasNext()) {
            V v = it.next();

            Node nextNode = currentNode.nextValueToChild == null ? null : currentNode.nextValueToChild.get(v);
            if(nextNode == null) {
                nextNode = new Node(nextId++, currentNode, v);
                if(currentNode.nextValueToChild == null) {
                    currentNode.nextValueToChild = new TreeMap<>();
                }
                currentNode.nextValueToChild.put(v, nextNode);
            }
            currentNode = nextNode;
        }

        if(currentNode.keyToSet == null) {
            currentNode.keyToSet = new HashMap<>();
        }

        currentNode.keyToSet.put(key, navSet);

        keyToNode.put(key, currentNode);
    }

    public void remove(Object key) {
        Node currentNode = keyToNode.get(key);

        if(currentNode != null) {
            currentNode.keyToSet.remove(key);
            if(currentNode.keyToSet.isEmpty()) {
                currentNode.keyToSet = null;
            }
        }

        while(currentNode != null && currentNode.parent != null) {
            if(currentNode.nextValueToChild == null && currentNode.keyToSet == null) {
                currentNode.parent.nextValueToChild.remove(currentNode.currentValue);
                if(currentNode.parent.nextValueToChild.isEmpty()) {
                    currentNode.parent.nextValueToChild = null;
                }

                currentNode = currentNode.parent;
            } else {
                currentNode = null;
            }
        }
    }

    public Map<K, SortedSet<V>> getAllSubsetsOf(Collection<V> set) {
        SortedSet<V> navSet = new TreeSet<V>(comparator);
        navSet.addAll(set);
        Iterator<V> it = set.iterator();

        List<Node> frontier = new ArrayList<>();
        frontier.add(rootNode);

        List<Node> nextNodes = new ArrayList<>();

        // For every value, extend the frontier with the successor nodes for that value.
        while(it.hasNext()) {
            V v = it.next();

            nextNodes.clear();
            for(Node currentNode : frontier) {
                Node nextNode = currentNode.nextValueToChild == null ? null : currentNode.nextValueToChild.get(v);
                if(nextNode != null) {
                    nextNodes.add(nextNode);
                }
            }
            frontier.addAll(nextNodes);
        }

        Map<K, SortedSet<V>> result = new HashMap<>();

        // Copy all data entries associated with the frontier to the result
        for(Node currentNode : frontier) {
            if(currentNode.keyToSet != null) {
                for(Entry<K, SortedSet<V>> e : currentNode.keyToSet.entrySet()) {
                    result.put(e.getKey(), e.getValue());
                }
            }
        }

        return result;
    }




    public Map<K, SortedSet<V>> getAllSupersetsOf(Collection<V> set) {
        Set<V> navSet = new TreeSet<V>(comparator);
        navSet.addAll(set);
        Iterator<V> it = set.iterator();


        List<Node> frontier = new ArrayList<>();
        frontier.add(rootNode);

        // For every value, extend the frontier with the successor nodes for that value.
        V from = null;
        V upto = null;

        // Use a flag for null safety so we do not rely on the comparator to treat null as the least element
        boolean isLeastFrom = true;
        while(it.hasNext()) {
            from = upto;
            upto = it.next();

            List<Node> nextNodes = new ArrayList<>();

            // Based on the frontier, we need to keep scanning nodes whose values is in the range [from, upto]
            // until we find the nodes whose values equals upto
            // Only these nodes then constitute the next frontier
            Collection<Node> currentScanNodes = frontier;
            do {
                Collection<Node> nextScanNodes = new ArrayList<>();
                for(Node currentNode : currentScanNodes) {
                    if(currentNode.nextValueToChild != null) {
                        NavigableMap<V, Node> candidateNodes = isLeastFrom
                                ? currentNode.nextValueToChild.headMap(upto, true)
                                : currentNode.nextValueToChild.subMap(from, true, upto, true);

                        for(Node candidateNode : candidateNodes.values()) {
                            if(Objects.equals(candidateNode.currentValue, upto)) {
                                nextNodes.add(candidateNode);
                            } else {
                                nextScanNodes.add(candidateNode);
                            }
                        }
                    }
                }
                currentScanNodes = nextScanNodes;
            } while(!currentScanNodes.isEmpty());

            frontier = nextNodes;

            isLeastFrom = false;
        }

        Map<K, SortedSet<V>> result = new HashMap<>();

        // Copy all data entries associated with the frontier to the result
        frontier.stream()
            .flatMap(node -> reachableNodesAcyclic(
                        node,
                        x -> (x.nextValueToChild != null ? x.nextValueToChild.values() : Collections.<Node>emptySet()).stream()))
            .forEach(currentNode -> {
                if(currentNode.keyToSet != null) {
                    for(Entry<K, SortedSet<V>> e : currentNode.keyToSet.entrySet()) {
                        result.put(e.getKey(), e.getValue());
                    }
                }
            });


        return result;
    }

    public static <T> Stream<T> reachableNodesAcyclic(T start, Function<T, Stream<T>> nav) {
        Stream<T> result = Stream.concat(
                Stream.of(start),
                nav.apply(start).flatMap(v -> reachableNodesAcyclic(v, nav)));
        return result;
    }


    public static void main(String[] args) {
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

