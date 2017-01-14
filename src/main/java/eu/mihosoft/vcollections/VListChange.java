/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.Collections;
import java.util.List;
import java.util.Objects;

/**
 * Represents a list change. Every listz change consists of an element list
 * containing the changed elements and an index array which contains the indices
 * of the changed elements.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VListChange<T> {

    /**
     * @return the indices of the changed elements
     */
    int[] indices();

    /**
     * @return changed elements
     */
    List<T> elements();

    /**
     * Creates a new list change.
     *
     * @param <V> element type
     * @param indices indices of the changed elements
     * @param elements changed elements
     * @return new list change object
     */
    static <V> VListChange<V> newInstance(int[] indices, List<V> elements) {
        Objects.requireNonNull(indices);
        Objects.requireNonNull(elements);
        return new VListChangeImpl<>(indices, elements);
    }

    /**
     * Creates an empty list change object.
     *
     * @param <V> element type
     * @return an empty list change object
     */
    static <V> VListChange<V> empty() {
        return (VListChange<V>) VListChangeImpl.EMPTY;
    }

    /**
     * Indicates whether this object contains list changes.
     *
     * @return {@code true} if this object contains changes; {@code false}
     * otherwise
     */
    default boolean hasChanges() {
        return indices().length != 0 || !elements().isEmpty();
    }
}

/**
 * List change implementation.
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type
 */
class VListChangeImpl<T> implements VListChange<T> {

    private final int[] indices;
    private final List<T> elements;

    static final VListChange<?> EMPTY = new VListChangeImpl<>(new int[0], Collections.EMPTY_LIST);

    public VListChangeImpl(int[] indices, List<T> elements) {
        this.indices = indices;
        this.elements = elements;
    }

    @Override
    public int[] indices() {
        return indices;
    }

    @Override
    public List<T> elements() {
        return elements;
    }

}
