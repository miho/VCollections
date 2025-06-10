/*
 * Copyright 2017-2019 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 */
package eu.mihosoft.vcollections;

import java.util.AbstractMap;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;
import vjavax.observer.Subscription;

/**
 * Mapped observable map that reflects changes from and to an underlying map.
 *
 * @param <K> key type of this map
 * @param <V> value type of this map
 * @param <OK> key type of the original map
 * @param <OV> value type of the original map
 */
public final class VMappedMap<K, V, OK, OV> extends AbstractMap<K, V> implements VMap<K, V> {

    private final VMap<OK, OV> originalMap;
    private final Function<OK, K> fromOrigKey;
    private final Function<K, OK> fromThisKey;
    private final Function<OV, V> fromOrigValue;
    private final Function<V, OV> fromThisValue;

    private final Map<VMapChangeListener<K, V>, VMapChangeListener<OK, OV>> listenerMap = new HashMap<>();

    public static <K, V, OK, OV> VMap<K, V> newInstance(Map<OK, OV> srcMap,
            Function<OK, K> fromOrigKey,
            Function<K, OK> fromThisKey,
            Function<OV, V> fromOrigValue,
            Function<V, OV> fromThisValue) {
        return new VMappedMap<>(srcMap, fromOrigKey, fromThisKey, fromOrigValue, fromThisValue);
    }

    public static <K, V, OK, OV> VMap<K, V> newUnmodifiableInstance(VMap<OK, OV> srcMap,
            Function<OK, K> fromOrigKey,
            Function<OV, V> fromOrigValue) {
        return new VMappedMap<>(srcMap.asUnmodifiable(), fromOrigKey,
                k -> { throw new UnsupportedOperationException("Cannot modify an unmodifiable map."); },
                fromOrigValue,
                v -> { throw new UnsupportedOperationException("Cannot modify an unmodifiable map."); });
    }

    @SuppressWarnings("unchecked")
    private VMappedMap(Map<OK, OV> srcMap,
            Function<OK, K> fromOrigKey,
            Function<K, OK> fromThisKey,
            Function<OV, V> fromOrigValue,
            Function<V, OV> fromThisValue) {
        if (srcMap instanceof VMap) {
            this.originalMap = (VMap<OK, OV>) srcMap;
        } else {
            this.originalMap = VMap.newInstance(srcMap);
        }
        this.fromOrigKey = fromOrigKey;
        this.fromThisKey = fromThisKey;
        this.fromOrigValue = fromOrigValue;
        this.fromThisValue = fromThisValue;
    }

    @Override
    public void setEventInfo(String evtInfo) {
        originalMap.setEventInfo(evtInfo);
    }

    @Override
    public String getEventInfo() {
        return originalMap.getEventInfo();
    }

    @Override
    public int size() {
        return originalMap.size();
    }

    @Override
    public boolean isEmpty() {
        return originalMap.isEmpty();
    }

    @Override
    public boolean containsKey(Object key) {
        return originalMap.containsKey(fromThisKey.apply((K) key));
    }

    @Override
    public boolean containsValue(Object value) {
        return originalMap.containsValue(fromThisValue.apply((V) value));
    }

    @Override
    public V get(Object key) {
        OV v = originalMap.get(fromThisKey.apply((K) key));
        return v != null ? fromOrigValue.apply(v) : null;
    }

    @Override
    public V put(K key, V value) {
        OV prev = originalMap.put(fromThisKey.apply(key), fromThisValue.apply(value));
        return prev != null ? fromOrigValue.apply(prev) : null;
    }

    @Override
    public V remove(Object key) {
        OV prev = originalMap.remove(fromThisKey.apply((K) key));
        return prev != null ? fromOrigValue.apply(prev) : null;
    }

    @Override
    public void clear() {
        originalMap.clear();
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        Map<OK, OV> mapped = new HashMap<>();
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            mapped.put(fromThisKey.apply(e.getKey()), fromThisValue.apply(e.getValue()));
        }
        originalMap.putAll(mapped);
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return originalMap.entrySet().stream()
                .map(e -> new SimpleEntry<>(fromOrigKey.apply(e.getKey()), fromOrigValue.apply(e.getValue())))
                .collect(Collectors.toSet());
    }

    @Override
    public VMap<K, V> asUnmodifiable() {
        throw new UnsupportedOperationException("Unsupported operation: use 'newUnmodifiableInstance(...)' instead.");
    }

    @Override
    public boolean removeAll(K... keys) {
        OK[] mapped = java.util.Arrays.stream(keys)
                .map(fromThisKey)
                .toArray(size -> (OK[]) new Object[size]);
        return originalMap.removeAll(mapped);
    }

    @Override
    public Subscription addChangeListener(VMapChangeListener<K, V> l) {
        VMapChangeListener<OK, OV> mappedListener = evt -> {
            Map<K, V> added = evt.added().entries().entrySet().stream()
                    .collect(Collectors.toMap(e -> fromOrigKey.apply(e.getKey()), e -> fromOrigValue.apply(e.getValue())));
            Map<K, V> removed = evt.removed().entries().entrySet().stream()
                    .collect(Collectors.toMap(e -> fromOrigKey.apply(e.getKey()), e -> fromOrigValue.apply(e.getValue())));
            VMapChangeEvent<K, V> e = new VMapChangeEventImpl<>(VMappedMap.this,
                    VMapChange.newInstance(added),
                    VMapChange.newInstance(removed),
                    evt.eventInfo());
            l.onChange(e);
        };
        listenerMap.put(l, mappedListener);
        return originalMap.addChangeListener(mappedListener);
    }

    @Override
    public boolean removeChangeListener(VMapChangeListener<K, V> l) {
        VMapChangeListener<OK, OV> ml = listenerMap.remove(l);
        if (ml != null) {
            return originalMap.removeChangeListener(ml);
        }
        return false;
    }
}
