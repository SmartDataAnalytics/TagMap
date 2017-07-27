package org.aksw.commons.collections.set_trie;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

import com.google.common.collect.ForwardingMap;

/**
 * Basic implementation of the SetIndex interface which simply sequentially scans a backing map.
 * Main use cases is performance comparison and convenience wrapping of existing maps.
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public class FeatureMapSimple<K, V>
    extends ForwardingMap<K, Set<V>>
    implements FeatureMap<K, V>
{
    protected Map<K, Set<V>> map;

    public FeatureMapSimple() {
        this(new HashMap<>());
    }

    public FeatureMapSimple(Map<K, Set<V>> map) {
        super();
        this.map = map;
    }

    @Override
    protected Map<K, Set<V>> delegate() {
        return map;
    }

    @Override
    public FeatureMap<K, V> getAllSubsetsOf(Collection<?> set) {
        Map<K, Set<V>> resultMap = delegate().entrySet().stream()
            .filter(e -> set.containsAll(e.getValue()))
            .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        FeatureMap<K, V> result = new FeatureMapSimple<>(resultMap);
        return result;
    }

    @Override
    public FeatureMap<K, V> getAllSupersetsOf(Collection<?> set) {
        Map<K, Set<V>> resultMap = delegate().entrySet().stream()
                .filter(e -> e.getValue().containsAll(set))
                .collect(Collectors.toMap(Entry::getKey, Entry::getValue));

        FeatureMap<K, V> result = new FeatureMapSimple<>(resultMap);
        return result;
    }
}
