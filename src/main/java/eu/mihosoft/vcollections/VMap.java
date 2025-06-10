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

import java.util.Map;
import vjavax.observer.Subscription;
import eu.mihosoft.vcollections.VMapChangeListener;

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
                        java.util.Collections.singletonMap(key, previous),
                        java.util.Collections.singletonMap(key, value), getEventInfo()));
            } else {
                fireChangeEvent(VMapChangeEvent.getAddedEvent(this,
                        java.util.Collections.singletonMap(key, value), getEventInfo()));
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
                    java.util.Collections.singletonMap((K) key, removed), getEventInfo()));
        }
        return removed;
    }

    @Override
    public void clear() {
        if (originalMap.isEmpty()) {
            return;
        }
        Map<K, V> removed = new java.util.LinkedHashMap<>(originalMap);
        originalMap.clear();
        if (hasListeners()) {
            fireChangeEvent(VMapChangeEvent.getRemovedEvent(this, removed, getEventInfo()));
        }
    }

    @Override
    public void putAll(Map<? extends K, ? extends V> m) {
        if (m.isEmpty()) return;
        Map<K, V> added = new java.util.LinkedHashMap<>();
        Map<K, V> removed = new java.util.LinkedHashMap<>();
        for (Entry<? extends K, ? extends V> e : m.entrySet()) {
            K k = e.getKey();
            V v = e.getValue();
            if (originalMap.containsKey(k)) {
                V prev = originalMap.put(k, v);
                removed.put(k, prev);
                added.put(k, v);
            } else {
                originalMap.put(k, v);
                added.put(k, v);
            }
        }
        if (hasListeners()) {
            fireChangeEvent(VMapChangeEvent.getSetEvent(this, removed, added, getEventInfo()));
        }
    }

    @Override
    public java.util.Set<Entry<K, V>> entrySet() {
        return originalMap.entrySet();
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
        src.addChangeListener(target::fireChangeEvent);
    }

    @Override
    public boolean removeAll(K... keys) {
        if (keys.length == 0) return true;
        Map<K, V> removed = new java.util.LinkedHashMap<>();
        for (K k : keys) {
            if (originalMap.containsKey(k)) {
                removed.put(k, originalMap.remove(k));
            }
        }
        if (hasListeners() && !removed.isEmpty()) {
            fireChangeEvent(VMapChangeEvent.getRemovedEvent(this, removed, getEventInfo()));
        }
        return !removed.isEmpty();
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
