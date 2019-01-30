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

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;
import vjavax.observer.Subscription;
import vjavax.observer.collection.CollectionChangeListener;

/**
 * Creates a mapped list that keeps up to date with the original list.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type of the target list
 * @param <V> element type of the source list
 */
public final class VMappedList<T, V> extends AbstractList<T> implements VList<T> {

    private final VList<V> originalList;
    private final Function<V, T> fromOrigToThis;
    private final Function<T, V> fromThisToOrig;

    private final Map<CollectionChangeListener<T, ? super VList<T>, ? super VListChange<T>>, VListChangeListener<V>> listenerMap
            = new HashMap<>();

    /**
     * Creates a new mapped list that maps each element in the original list to
     * the specified target type.
     *
     * Each change made in the source list is reflected in the target list and
     * vice versa.
     *
     * @param <T> element type of the target list
     * @param <V> element type of the source list
     *
     * @param srcList list to map
     * @param fromOrigToThis mapping from the original element type to the
     * target element type
     * @param fromThisToOrig mapping from the target element type to the
     * original element type
     * @return a new mapped list
     */
    public static <T, V> VList<T> newInstance(VList<V> srcList,
            Function<V, T> fromOrigToThis,
            Function<T, V> fromThisToOrig) {
        return new VMappedList<>(srcList, fromOrigToThis, fromThisToOrig);
    }

    /**
     * Creates a new unmodifiable mapped list that maps each element in the
     * original list to the specified target type.
     *
     * That is, each change made in the source list is reflected in the target
     * list. Modifications to the target list, however are impossible.
     *
     * @param <T> element type of the target list
     * @param <V> element type of the source list
     *
     * @param srcList list to map
     * @param fromOrigToThis mapping from the original element type to the
     * target element type
     * @return a new unmodifiable mapped list
     */
    public static <T, V> VList<T> newUnmodifiableInstance(VList<V> srcList,
            Function<V, T> fromOrigToThis) {
        return new VMappedList<>(srcList.asUnmodifiable(),
                fromOrigToThis, (e) -> {
                    throw new UnsupportedOperationException(
                            "Cannot modify an unmodifiable list.");
                });
    }

    /**
     * Cretes a mapped list.
     *
     * @param originalList
     * @param fromOrigToThis
     * @param fromThisToOrig
     */
    private VMappedList(List<V> originalList,
            Function<V, T> fromOrigToThis,
            Function<T, V> fromThisToOrig) {

        if (originalList instanceof VList) {
            this.originalList = (VList<V>) originalList;
        } else {
            this.originalList = VList.newInstance(originalList);
        }

        this.fromOrigToThis = fromOrigToThis;
        this.fromThisToOrig = fromThisToOrig;
    }

    @Override
    public T get(int index) {
        return fromOrigToThis.apply(originalList.get(index));
    }

    @Override
    public int size() {
        return originalList.size();
    }

    @Override
    public T set(int index, T e) {
        return fromOrigToThis.apply(
                originalList.set(index, fromThisToOrig.apply(e)));
    }

    @Override
    public void add(int index, T e) {
        originalList.add(index, fromThisToOrig.apply(e));
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {
        return originalList.addAll(c.stream().
                map(fromThisToOrig).
                collect(Collectors.toList()));
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {
        return originalList.addAll(index, c.stream().
                map(fromThisToOrig).
                collect(Collectors.toList()));
    }

    @Override
    public T remove(int index) {
        return fromOrigToThis.apply(originalList.remove(index));
    }

    @Override
    public boolean removeAll(int... indices) {
        return originalList.removeAll(indices);
    }

    @Override
    public Collection<T> setAll(int index, Collection<T> elements) {
        Collection<V> prevElements = originalList.setAll(index, elements.stream().
                map(fromThisToOrig).collect(Collectors.toList()));

        // return mapped elements
        return prevElements.stream().
                map(fromOrigToThis).collect(Collectors.toList());
    }

    @Override
    public boolean addAll(int[] indices, Collection<? extends T> c) {
        return originalList.addAll(indices, c.stream().
                map(fromThisToOrig).collect(Collectors.toList()));
    }

    @Override
    @SuppressWarnings("unchecked")
    public Subscription addChangeListener(CollectionChangeListener<T, ? super VList<T>, ? super VListChange<T>> l) {
//        if (listenerMap.containsKey(l)) {
//            return false;
//        }

        VListChangeListener<V> mappedListener = (evt) -> {

            VListChangeEvent e = new VListChangeEventImpl<>(VMappedList.this,
                    VListChange.newInstance(evt.added().indices(),
                            evt.added().elements().stream().
                            map(fromOrigToThis).
                            collect(Collectors.toList())),
                    VListChange.newInstance(evt.removed().indices(),
                            evt.removed().elements().stream().
                            map(fromOrigToThis).
                            collect(Collectors.toList()))
            );

            l.onChange(e);

        };

        listenerMap.put(l, mappedListener);
        originalList.addChangeListener(mappedListener);
        return () -> {
            listenerMap.remove(l);
            originalList.removeChangeListener(mappedListener);
        };
    }

    @Override
    public boolean removeChangeListener(CollectionChangeListener<T, ? super VList<T>, ? super VListChange<T>> l) {
        if (listenerMap.containsKey(l)) {
            VListChangeListener<V> mappedListener = listenerMap.remove(l);
            return originalList.removeChangeListener(mappedListener);
        } else {
            return false;
        }
    }
    
    
    @Override
    public VList<T> asUnmodifiable() {
        throw new UnsupportedOperationException("Unsupported operation: use 'newUnModifiableInstance(...)' instead.");
    }
}
