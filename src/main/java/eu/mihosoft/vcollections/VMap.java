/*
 * Copyright 2017-2019 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are
 * permitted provided that the following conditions are met:
 *
 *    1. Redistributions of source code must retain the above copyright notice, this list of
 *       conditions and the following disclaimer.
 *
 *    2. Redistributions in binary form must reproduce the above copyright notice, this list
 *       of conditions and the following disclaimer in the documentation and/or other materials
 *       provided with the distribution.
 *
 * If you use this software for scientific research then please cite the following publication(s):
 *
 * M. Hoffer, C. Poliwoda, & G. Wittum. (2013). Visual reflection library:
 * a framework for declarative GUI programming on the Java platform.
 * Computing and Visualization in Science, 2013, 16(4),
 * 181–192. http://doi.org/10.1007/s00791-014-0230-y
 *
 * THIS SOFTWARE IS PROVIDED BY Michael Hoffer <info@michaelhoffer.de> "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND
 * FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL Michael Hoffer <info@michaelhoffer.de> OR
 * CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR
 * CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
 * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON
 * ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF
 * ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 *
 * The views and conclusions contained in the software and documentation are those of the
 * authors and should not be interpreted as representing official policies, either expressed
 * or implied, of Michael Hoffer <info@michaelhoffer.de>.
 */
package eu.mihosoft.vcollections;

import java.util.AbstractSet;
import java.util.Iterator;
import java.util.AbstractMap.SimpleImmutableEntry;
import java.util.Map;
import java.util.Set;
import java.util.function.BooleanSupplier;
import vjavax.observer.Subscription;


/**
 * Observable map.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface VMap<K, V> extends Map<K, V>, VMapObservable<K, V> {

    /**
     * Creates a new wrapper around the specified map. Modifying the wrapper
     * will modify the wrapped map.
     *
     * @param <K> key type
     * @param <V> value type
     * @param map map to wrap
     * @return new {@link VMap} that wraps the specified map
     */
    static <K, V> VMap<K, V> newInstance(Map<K, V> map) {
        return VMapImpl.newInstance(map);
    }

    /**
     * Returns an unmodifiable view of this map (see
     * {@link java.util.Collections#unmodifiableMap(java.util.Map)} ).
     *
     * @return an unmodifiable view of this map
     */
    VMap<K, V> asUnmodifiable();

    /**
     * Removes all elements with the specified keys.
     *
     * @param keys keys to remove
     * @return {@code true} if this map changed as a result of the call; {@code false} otherwise
     */
    boolean removeAll(@SuppressWarnings("unchecked") K... keys);

    /**
     * Sets the event info to be used for event generation.
     *
     * @param evtInfo event info to set
     */
    void setEventInfo(String evtInfo);

    /**
     * Returns the event info used for event generation.
     *
     * @return event info used for event generation
     */
    String getEventInfo();
}

/**
 * Observable map implementation.
 */
final class VMapImpl<K, V> extends java.util.AbstractMap<K, V> implements VMap<K, V> {

    private final Map<K, V> originalMap;
    private VMapChangeSupport<K, V> mapChangeSupport;
    private VMapImpl<K, V> unmodifiableInstance;
    private String evtInfo = "";

    private VMapChangeSupport<K, V> getMapChangeSupport() {
        if (mapChangeSupport == null) {
            mapChangeSupport = new VMapChangeSupport<>();
        }
        return mapChangeSupport;
    }

    private boolean hasListeners() {
        return mapChangeSupport != null && mapChangeSupport.hasListeners();
    }

    private VMapImpl(Map<K, V> originalMap) {
        this.originalMap = originalMap;
    }

    static <K, V> VMapImpl<K, V> newInstance(Map<K, V> map) {
        return new VMapImpl<>(map);
    }

    @Override
    public void setEventInfo(String evtInfo) {
        if (evtInfo == null) {
            this.evtInfo = "";
        } else {
            this.evtInfo = evtInfo;
        }
    }

    @Override
    public String getEventInfo() {
        return this.evtInfo;
    }

    private void fireChangeEvent(VMapChangeEvent<K, V> evt) {
        if (hasListeners()) {
            mapChangeSupport.fireEvent(evt);
        }
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
        return originalMap.containsKey(key);
    }

    @Override
    public boolean containsValue(Object value) {
        return originalMap.containsValue(value);
    }

    @Override
    public V get(Object key) {
        return originalMap.get(key);
    }

    @Override
    public V put(K key, V value) {
        boolean hadKey = originalMap.containsKey(key);
        V previous = originalMap.put(key, value);

        if (hasListeners()) {
            if (hadKey) {
                fireChangeEvent(VMapChangeEvent.getSetEvent(this,
                        mapping(key, previous),
                        mapping(key, value), getEventInfo()));
            } else {
                fireChangeEvent(VMapChangeEvent.getAddedEvent(this,
                        mapping(key, value), getEventInfo()));
            }
        }
        return previous;
    }

    @Override
    public V remove(Object key) {
        if (!originalMap.containsKey(key)) {
            return null;
        }
        V removed = originalMap.remove(key);
        if (hasListeners()) {
            fireChangeEvent(VMapChangeEvent.getRemovedEvent(this,
                    mapping((K) key, removed), getEventInfo()));
        }
        return removed;
    }

    @Override
    public void clear() {
        if (!originalMap.isEmpty()) {
            bulkEdit(() -> {
                originalMap.clear();
                return true;
            }, null);
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> entries) {
        if (!entries.isEmpty()) {
            bulkEdit(() -> {
                originalMap.putAll(entries);
                return true;
            }, entries);
        }
    }

    // A caller-supplied map can use a comparator or identity keys, evict entries,
    // normalize writes, or accept only part of a batch before throwing. Delegate
    // the operation unchanged and observe the actual result, including on failure.
    private boolean bulkEdit(BooleanSupplier edit, Map<? extends K, ?> written) {
        if (!hasListeners()) return edit.getAsBoolean();
        Map<K, V> before = VMapChangeImpl.copyOf(originalMap);
        boolean changed;
        try {
            changed = edit.getAsBoolean();
        } catch (RuntimeException | Error failure) {
            try {
                fireDifference(before, null);
            } catch (RuntimeException | Error notificationFailure) {
                if (notificationFailure != failure) failure.addSuppressed(notificationFailure);
            }
            throw failure;
        }
        fireDifference(before, written);
        return changed;
    }

    private void fireDifference(Map<K, V> before, Map<? extends K, ?> written) {
        Map<K, V> after = VMapChangeImpl.copyOf(originalMap);
        Map<K, Boolean> assigned = VMapChangeImpl.emptyLike(after);
        // A successful putAll also reports assignments of existing equal values.
        if (written != null) written.keySet().forEach(key -> assigned.put(key, true));
        Map<K, V> removed = VMapChangeImpl.emptyLike(before);
        for (Entry<K, V> entry : before.entrySet()) {
            if (assigned.containsKey(entry.getKey()) || !after.entrySet().contains(entry)) {
                removed.put(entry.getKey(), entry.getValue());
            }
        }
        Map<K, V> added = VMapChangeImpl.emptyLike(after);
        for (Entry<K, V> entry : after.entrySet()) {
            if (assigned.containsKey(entry.getKey()) || !before.entrySet().contains(entry)) {
                added.put(entry.getKey(), entry.getValue());
            }
        }
        if (!removed.isEmpty() || !added.isEmpty()) {
            fireChangeEvent(VMapChangeEvent.getSetEvent(this, removed, added, getEventInfo()));
        }
    }

    private Map<K, V> mapping(K key, V value) {
        Map<K, V> entries = VMapChangeImpl.emptyLike(originalMap);
        entries.put(key, value);
        return entries;
    }

    @Override
    public Set<Entry<K, V>> entrySet() {
        return new AbstractSet<Entry<K, V>>() {
            @Override public int size() { return originalMap.size(); }
            @Override public void clear() { VMapImpl.this.clear(); }
            @Override public boolean contains(Object entry) { return originalMap.entrySet().contains(entry); }

            @Override
            public boolean remove(Object entry) {
                if (!(entry instanceof Entry)) return false;
                Entry<?, ?> candidate = (Entry<?, ?>) entry;
                Object key = candidate.getKey();
                if (!originalMap.entrySet().contains(entry)) return false;
                V previous = originalMap.get(key);
                if (!originalMap.entrySet().remove(entry)) return false;
                if (hasListeners()) {
                    @SuppressWarnings("unchecked") K removedKey = (K) key;
                    fireChangeEvent(VMapChangeEvent.getRemovedEvent(VMapImpl.this,
                            mapping(removedKey, previous), getEventInfo()));
                }
                return true;
            }

            @Override
            public Iterator<Entry<K, V>> iterator() {
                Iterator<Entry<K, V>> delegate = originalMap.entrySet().iterator();
                return new Iterator<Entry<K, V>>() {
                    private Entry<K, V> current;

                    @Override public boolean hasNext() { return delegate.hasNext(); }

                    @Override
                    public Entry<K, V> next() {
                        current = delegate.next();
                        return new ObservableEntry(current);
                    }

                    @Override
                    public void remove() {
                        if (current == null) throw new IllegalStateException();
                        K key = current.getKey();
                        V previous = current.getValue();
                        delegate.remove();
                        current = null;
                        if (hasListeners()) {
                            fireChangeEvent(VMapChangeEvent.getRemovedEvent(VMapImpl.this,
                                    mapping(key, previous), getEventInfo()));
                        }
                    }
                };
            }
        };
    }

    private final class ObservableEntry implements Entry<K, V> {
        private final Entry<K, V> delegate;

        ObservableEntry(Entry<K, V> delegate) { this.delegate = delegate; }
        @Override public K getKey() { return delegate.getKey(); }
        @Override public V getValue() { return delegate.getValue(); }
        @Override public boolean equals(Object other) { return delegate.equals(other); }
        @Override public int hashCode() { return delegate.hashCode(); }
        @Override public String toString() { return delegate.toString(); }

        @Override
        public V setValue(V value) {
            // Held entries can be detached by a preceding removal or reinsertion.
            // Checking the backing entry set avoids both phantom events and access-order
            // mutations caused by get() while an iterator is active.
            Entry<K, V> replacement = new SimpleImmutableEntry<>(getKey(), value);
            boolean changing = hasListeners() && originalMap.entrySet().contains(delegate)
                    && !originalMap.entrySet().contains(replacement);
            V previous = delegate.setValue(value);
            if (changing && originalMap.entrySet().contains(replacement)) {
                fireChangeEvent(VMapChangeEvent.getSetEvent(VMapImpl.this,
                        mapping(getKey(), previous),
                        mapping(getKey(), value), getEventInfo()));
            }
            return previous;
        }
    }

    @Override
    public VMap<K, V> asUnmodifiable() {
        if (unmodifiableInstance == null) {
            unmodifiableInstance = new VMapImpl<>(java.util.Collections.unmodifiableMap(originalMap));
            syncMaps(this, unmodifiableInstance);
        }
        return unmodifiableInstance;
    }

    private void syncMaps(VMapImpl<K, V> src, VMapImpl<K, V> target) {
        src.addChangeListener(event -> {
            if (target.hasListeners()) {
                target.fireChangeEvent(new VMapChangeEventImpl<>(target,
                        event.added(), event.removed(), event.eventInfo()));
            }
        });
    }

    @Override
    public boolean removeAll(K... keys) {
        if (keys.length == 0) return false;
        return bulkEdit(() -> {
            boolean changed = false;
            for (K key : keys) {
                if (originalMap.containsKey(key)) {
                    originalMap.remove(key);
                    changed = true;
                }
            }
            return changed;
        }, null);
    }

    @Override
    public Subscription addChangeListener(VMapChangeListener<K, V> l) {
        return getMapChangeSupport().addChangeListener(l);
    }

    @Override
    public boolean removeChangeListener(VMapChangeListener<K, V> l) {
        boolean result = getMapChangeSupport().removeChangeListener(l);
        if (!getMapChangeSupport().hasListeners()) {
            mapChangeSupport = null;
        }
        return result;
    }
}
