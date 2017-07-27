package org.aksw.commons.collections.set_trie;

import java.util.Collection;
import java.util.Set;

import com.google.common.collect.ForwardingMap;

public abstract class FeatureMapForwarding<K, V>
    extends ForwardingMap<K, Set<V>>
    implements FeatureMap<K, V>
{
    @Override
    protected abstract FeatureMap<K, V> delegate();

    @Override
    public FeatureMap<K, V> getAllSubsetsOf(Collection<?> set) {
        FeatureMap<K, V> result = delegate().getAllSubsetsOf(set);
        return result;
    }

    @Override
    public FeatureMap<K, V> getAllSupersetsOf(Collection<?> set) {
        FeatureMap<K, V> result = delegate().getAllSupersetsOf(set);
        return result;
    }
}
