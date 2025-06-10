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
import java.util.Map.Entry;

/**
 * Map change event. An event contains information about all changes made by the
 * action that fired the event.
 *
 * @param <K> key type
 * @param <V> value type
 */
public interface VMapChangeEvent<K, V> {

    default boolean wasAdded() {
        return added().hasChanges();
    }

    default boolean wasRemoved() {
        return removed().hasChanges();
    }

    default boolean wasSet() {
        return added().hasChanges() && removed().hasChanges();
    }

    VMapChange<K, V> added();

    VMapChange<K, V> removed();

    VMap<K, V> source();

    default String eventInfo() { return ""; }

    static <K, V> VMapChangeEvent<K, V> getAddedEvent(VMap<K, V> source, Map<K, V> added) {
        return new VMapChangeEventImpl<>(source, VMapChange.newInstance(added), VMapChange.empty());
    }

    static <K, V> VMapChangeEvent<K, V> getAddedEvent(VMap<K, V> source, Map<K, V> added, String evtInfo) {
        return new VMapChangeEventImpl<>(source, VMapChange.newInstance(added), VMapChange.empty(), evtInfo);
    }

    static <K, V> VMapChangeEvent<K, V> getRemovedEvent(VMap<K, V> source, Map<K, V> removed) {
        return new VMapChangeEventImpl<>(source, VMapChange.empty(), VMapChange.newInstance(removed));
    }

    static <K, V> VMapChangeEvent<K, V> getRemovedEvent(VMap<K, V> source, Map<K, V> removed, String evtInfo) {
        return new VMapChangeEventImpl<>(source, VMapChange.empty(), VMapChange.newInstance(removed), evtInfo);
    }

    static <K, V> VMapChangeEvent<K, V> getSetEvent(VMap<K, V> source, Map<K, V> removed, Map<K, V> added) {
        return new VMapChangeEventImpl<>(source, VMapChange.newInstance(added), VMapChange.newInstance(removed));
    }

    static <K, V> VMapChangeEvent<K, V> getSetEvent(VMap<K, V> source, Map<K, V> removed, Map<K, V> added, String evtInfo) {
        return new VMapChangeEventImpl<>(source, VMapChange.newInstance(added), VMapChange.newInstance(removed), evtInfo);
    }

    String toStringWithDetails();
}

class VMapChangeEventImpl<K, V> implements VMapChangeEvent<K, V> {

    private final VMap<K, V> source;
    private final VMapChange<K, V> added;
    private final VMapChange<K, V> removed;
    private final String evtInfo;

    VMapChangeEventImpl(VMap<K, V> source, VMapChange<K, V> added, VMapChange<K, V> removed) {
        this.source = source;
        this.added = added;
        this.removed = removed;
        this.evtInfo = "";
    }

    VMapChangeEventImpl(VMap<K, V> source, VMapChange<K, V> added, VMapChange<K, V> removed, String evtInfo) {
        this.source = source;
        this.added = added;
        this.removed = removed;
        this.evtInfo = evtInfo;
    }

    @Override
    public boolean wasAdded() {
        return added.hasChanges();
    }

    @Override
    public boolean wasRemoved() {
        return removed.hasChanges();
    }

    @Override
    public boolean wasSet() {
        return added.hasChanges() && removed.hasChanges();
    }

    @Override
    public VMapChange<K, V> added() {
        return added;
    }

    @Override
    public VMapChange<K, V> removed() {
        return removed;
    }

    @Override
    public VMap<K, V> source() {
        return source;
    }

    @Override
    public String eventInfo() {
        return evtInfo;
    }

    @Override
    public String toString() {
        return "event: #added=" + added().entries().size() + ", #removed=" + removed().entries().size();
    }

    @Override
    public String toStringWithDetails() {
        StringBuilder sb = new StringBuilder();
        sb.append("event: [#removed: ").append(removed.entries().size()).append(", #added: ").append(added.entries().size()).append("]\n");
        sb.append("removed-keys   = ").append(removed.keys()).append("\n");
        sb.append("added-keys     = ").append(added.keys()).append("\n");
        return sb.toString();
    }
}
