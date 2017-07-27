package org.aksw.commons.collections.set_trie;

import java.util.Collection;
import java.util.Map;
import java.util.Set;

/**
 * A map where each key is associated with a set of features
 *
 * @author raven
 *
 * @param <K>
 * @param <V>
 */
public interface FeatureMap<K, V>
    extends Map<K, Set<V>>
{
    FeatureMap<K, V> getAllSubsetsOf(Collection<?> set);
    FeatureMap<K, V> getAllSupersetsOf(Collection<?> set);
}
