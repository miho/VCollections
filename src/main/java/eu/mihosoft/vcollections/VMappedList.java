/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.AbstractList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

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

    private final Map<VListChangeListener<T>, VListChangeListener<V>> listenerMap
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
    public static <T, V> List<T> newInstance(List<V> srcList,
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
    public static <T, V> List<T> newUnmodifiableInstance(List<V> srcList,
            Function<V, T> fromOrigToThis) {
        return new VMappedList<>(Collections.unmodifiableList(srcList),
                fromOrigToThis, (e) -> {
                    throw new UnsupportedOperationException(
                            "Cannot modify an unmodifiable list.");
                });
    }

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
        return new VMappedList<>(Collections.unmodifiableList(srcList),
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
    public boolean addListChangeListener(VListChangeListener<T> l) {

        if (listenerMap.containsKey(l)) {
            return false;
        }

        // mapping list change events
        VListChangeListener<V> mappedListener = (VListChangeEvent<V> evt) -> {
            l.onChange(new VListChangeEventImpl<>(VMappedList.this,
                    VListChange.newInstance(evt.getAdded().indices(),
                            evt.getAdded().elements().stream().
                            map(fromOrigToThis).
                            collect(Collectors.toList())),
                    VListChange.newInstance(evt.getAdded().indices(),
                            evt.getRemoved().elements().stream().
                            map(fromOrigToThis).
                            collect(Collectors.toList()))
            ));
        };

        listenerMap.put(l, mappedListener);
        return originalList.addListChangeListener(mappedListener);
    }

    @Override
    public boolean removeListChangeListener(VListChangeListener<T> l) {
        if (listenerMap.containsKey(l)) {
            return originalList.removeListChangeListener(listenerMap.get(l));
        } else {
            return false;
        }
    }
}
