package org.aksw.commons.collections.set_trie;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class FeatureMapSetTrie<K, V>
    extends AbstractMap<K, Set<V>>
    implements FeatureMap<K, V>
{
    protected SetTrie<K, V> setTrie = new SetTrie<K, V>();

    @Override
    public Set<V> put(K key, Set<V> set) {
        Set<V> result = setTrie.put(key, set);
        return result;
    }


    @Override
    public Set<Entry<K, Set<V>>> entrySet() {
        // TODO Auto-generated method stub
        return null;
    }


    @Override
    public FeatureMap<K, V> getAllSubsetsOf(Collection<?> set) {
        Map<K, Set<V>> resultMap = setTrie.getAllSubsetsOf(set);
        FeatureMap<K, V> result = new FeatureMapSimple<>(resultMap);
        return result;
    }


    @Override
    public FeatureMap<K, V> getAllSupersetsOf(Collection<?> set) {
        Map<K, Set<V>> resultMap = setTrie.getAllSupersetsOf(set);
        FeatureMap<K, V> result = new FeatureMapSimple<>(resultMap);
        return result;
    }

}
