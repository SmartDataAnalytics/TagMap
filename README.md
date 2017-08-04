# TagMap
This repository features an extension of the `java.util.Map<K, Set<T>>` interface for supporting subset and superset queries over sets of tags associated with keys.
Most prominently, an implementation based on the Set Trie datastructure is provided, which is an Index Data Structure for Fast Subset and Superset Queries based on the paper by Iztok Savnik.


## Maven dependency
The artifact is published on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Corg.aksw.commons%3Aaksw-commons-util), and thus ready for use in your project.

```xml
<dependency>
    <groupId>org.aksw.commons</groupId>
    <artifactId>tagmap-core</artifactId>
    <version>1.0.0</version>
</dependency>
```


## Usage

### The interface
The project introduces the [TagMap]() interface with the following important properties:

* At the core it is a `Map` which associates keys with sets of "tags".
* It enhances the `Map` with methods for retrieving keys by subsets and supersets of tags. 


```java
public interface TagMap<K, V>
    extends Map<K, Set<V>>
{
    TagMap<K, V> getAllSubsetsOf(Collection<?> set);
    TagMap<K, V> getAllSupersetsOf(Collection<?> set);
}
```

***NOTE: The interface supports iterative refinement of a TagMap, however, at present, all implementations return a TagMapSimple which has linear complexity*** 

### Implementations
The following implementations exist:
* [TagMapSimple](): A basic implementation which is backed by a standard `Map` and implements the sub-/superset queries by means of sequential scans
* [TagMapInvertedIndex](): An implementation which internally tracks for every tag the set of keys it is associated with.
* [TagMapSetTrie](): An implementation of the Set Trie datastructure proposed by Iztok Savnik.
* For validation, we provide a util function in [ValidationUtils]() which creates a Java proxy which delegates each method invocation to two implementations, compares the return values for equality, and raises an exception if they differ.


```java
TagMap<String, Integer> simpleFm = new TagMapSimple<>();
TagMap<String, Integer> setTrieFm = new TagMapSetTrie<>();
TagMap<String, Integer> invertedListFm = new TagMapInvertedIndex<>();

TagMap<String, Integer> fm = ValidationUtils.createValidatingProxy(setTrieFm, simpleFm);

fm.put("a", Sets.newHashSet(1, 2, 3));
fm.put("b", Sets.newHashSet(1, 2, 4));
fm.put("c", Sets.newHashSet(2, 3, 4));



fm.getAllSubsetsOf(Arrays.asList(1, 2, 3))
    .entrySet().forEach(e -> System.out.println("" + e));


fm.getAllSupersetsOf(Arrays.asList(1, 4))
    .entrySet().forEach(e -> System.out.println("" + e));
```


