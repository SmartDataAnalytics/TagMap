# SetTrie
An Implementation of an Index Data Structure for Fast Subset and Superset Queries (based on the paper by Iztok Savnik)


## Maven dependency
The artifact is published on [Maven Central](http://search.maven.org/#search%7Cga%7C1%7Corg.aksw.commons%3Aaksw-commons-util), and thus ready for use in your project.

```xml
<dependency>
    <groupId>org.aksw.commons</groupId>
    <artifactId>commons-set-trie-core</artifactId>
    <version>1.0.0-SNAPSHOT</version>
</dependency>
```


## Usage

### The interface
The project introduces the [FeatureMap]() interface with the following important properties:

* At the core it is a `Map` which associates keys with sets of "features".
* It enhances the `Map` with methods for retrieving keys by subsets and supersets of features. 


```java
public interface FeatureMap<K, V>
    extends Map<K, Set<V>>
{
    FeatureMap<K, V> getAllSubsetsOf(Collection<?> set);
    FeatureMap<K, V> getAllSupersetsOf(Collection<?> set);
}
```


### Implementations
The following implementations exist:
* [FeatureMapSimple](): A basic implementation which is backed by a standard `Map` and implements the sub-/superset queries by means of sequential scans
* [FeatureMapInvertedIndex](): An implementation which internally tracks for every feature the set of keys it is associated with.
* [FeatureMapSetTrie](): An implementation of the Set Trie datastructure proposed by Iztok Savnik.
* For validation, we provide a util function in [ValidationUtils]() which creates a Java proxy which delegates each method invocation to two implementations, compares the return values for equality, and raises an exception if they differ.


```java
FeatureMap<String, Integer> simpleFm = new FeatureMapSimple<>();
FeatureMap<String, Integer> setTrieFm = new FeatureMapSetTrie<>();
FeatureMap<String, Integer> invertedListFm = new FeatureMapInvertedIndex<>();

FeatureMap<String, Integer> fm = ValidationUtils.createValidatingProxy(setTrieFm, simpleFm);

fm.put("a", Sets.newHashSet(1, 2, 3));
fm.put("b", Sets.newHashSet(1, 2, 4));
fm.put("c", Sets.newHashSet(2, 3, 4));



fm.getAllSubsetsOf(Arrays.asList(1, 2, 3))
    .entrySet().forEach(e -> System.out.println("" + e));


fm.getAllSupersetsOf(Arrays.asList(1, 4))
    .entrySet().forEach(e -> System.out.println("" + e));
```


