package org.aksw.commons.collections.set_trie;

import java.util.AbstractMap;
import java.util.Collection;
import java.util.Map;
import java.util.Set;

public class TagMapSetTrie<K, V>
    extends AbstractMap<K, Set<V>>
    implements TagMap<K, V>
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
    public TagMap<K, V> getAllSubsetsOf(Collection<?> set, boolean strict) {
        Map<K, Set<V>> resultMap = setTrie.getAllSubsetsOf(set);
        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }


    @Override
    public TagMap<K, V> getAllSupersetsOf(Collection<?> set, boolean strict) {
        Map<K, Set<V>> resultMap = setTrie.getAllSupersetsOf(set);
        TagMap<K, V> result = new TagMapSimple<>(resultMap);
        return result;
    }

}
