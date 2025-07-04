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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import vjavax.observer.collection.CollectionChange;

/**
 * Represents a map change.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VMapChange<K, V> extends CollectionChange<Entry<K, V>> {

    /**
     * @return changed entries
     */
    Map<K, V> entries();

    /**
     * @return keys of changed entries
     */
    default Collection<K> keys() {
        return entries().keySet();
    }

    @Override
    default List<Entry<K, V>> elements() {
        return new ArrayList<>(entries().entrySet());
    }

    /**
     * Creates a new map change.
     *
     * @param <K> key type
     * @param <V> value type
     * @param entries changed entries
     * @return new map change object
     */
    static <K, V> VMapChange<K, V> newInstance(Map<K, V> entries) {
        Objects.requireNonNull(entries);
        return new VMapChangeImpl<>(entries);
    }

    /**
     * Creates an empty map change object.
     *
     * @param <K> key type
     * @param <V> value type
     * @return an empty map change object
     */
    @SuppressWarnings("unchecked")
    static <K, V> VMapChange<K, V> empty() {
        return (VMapChange<K, V>) VMapChangeImpl.EMPTY;
    }

    /**
     * Indicates whether this object contains map changes.
     *
     * @return {@code true} if this object contains changes; {@code false}
     * otherwise
     */
    default boolean hasChanges() {
        return !entries().isEmpty();
    }
}

class VMapChangeImpl<K, V> implements VMapChange<K, V> {

    private final Map<K, V> entries;

    @SuppressWarnings("unchecked")
    static final VMapChange<?, ?> EMPTY = new VMapChangeImpl<>(Collections.emptyMap());

    VMapChangeImpl(Map<K, V> entries) {
        this.entries = entries;
    }

    @Override
    public Map<K, V> entries() {
        return entries;
    }
}
