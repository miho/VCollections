/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.List;

/**
 * List change event. An event contains information about all changes made by
 * the action that fired the event.
 *
 * Events contain information about added and removed elements. Additionally,
 * events contain information whether the changes were caused by a set
 * opperation (removed elements are replaced by added elements).
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VListChangeEvent<T> {

    /**
     * Indicates whether elements were added during this event.
     *
     * @return {@code true} if elements were added during this event;
     * {@code false} otherwise
     */
    boolean wasAdded();

    /**
     * Indicates whether elements were removed during this event.
     *
     * @return {@code true} if elements were removed during this event;
     * {@code false} otherwise
     */
    boolean wasRemoved();

    /**
     * Indicates whether elements were set, e.g., replaced during this event.
     *
     * @return {@code true} if elements were set during this event;
     * {@code false} otherwise
     */
    boolean wasSet();

    /**
     * Returns the change that contains all elements that were added during this
     * event.
     *
     * @return the change that contains all elements that were added during this
     * event
     */
    VListChange<T> getAdded();

    /**
     * Returns the change that contains all elements that were removed during
     * this event.
     *
     * @return the change that contains all elements that were removed during
     * this event
     */
    VListChange<T> getRemoved();

    /**
     * Returns the source list, e.g., the list that fired the change event
     *
     * @return the source list
     */
    List<T> getSource();

    /**
     * Returns an event that contains the changes produced by the specified
     * 'add(..)' operation.
     *
     * @param <V> element type
     * @param source source list
     * @param indices indices of the elements that were added
     * @param elements elements that were added
     * @return an event that contains the changes produced by the specified
     * 'add(..)' operation
     */
    static <V> VListChangeEvent<V> getAddedEvent(List<V> source,
            int[] indices, List<V> elements) {
        return new VListChangeEventImpl<>(
                source,
                VListChange.newInstance(indices, elements),
                VListChange.empty());
    }

    /**
     * Returns an event that contains the changes produced by the specified
     * 'remove(..)' operation.
     *
     * @param <V> element type
     * @param source source list
     * @param indices indices of the elements that were remove
     * @param elements elements that were removes
     * @return an event that contains the changes produced by the specified
     * 'remove(..)' operation
     */
    static <V> VListChangeEvent<V> getRemovedEvent(List<V> source,
            int[] indices, List<V> elements) {
        return new VListChangeEventImpl<>(
                source,
                VListChange.empty(),
                VListChange.newInstance(indices, elements));
    }

    /**
     * Returns an event that contains the changes produced by the specified
     * 'set(..)' operation.
     *
     * @param <V> element type
     * @param source source list
     * @param indices indices of the elements that were set/replaced
     * @param elementsRemoved elements that were removed
     * @param elementsAdded elements that were added
     * @return an event that contains the changes produced by the specified
     * 'set(..)' operation
     */
    static <V> VListChangeEvent<V> getSetEvent(List<V> source,
            int[] indices, List<V> elementsRemoved, List<V> elementsAdded) {
        return new VListChangeEventImpl<>(
                source,
                VListChange.newInstance(indices, elementsAdded),
                VListChange.newInstance(indices, elementsRemoved));
    }
}

/**
 * List change event implementation.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type
 */
class VListChangeEventImpl<T> implements VListChangeEvent<T> {

    private final List<T> source;

    private final VListChange<T> added;
    private final VListChange<T> removed;

    public VListChangeEventImpl(List<T> source, VListChange<T> added,
            VListChange<T> removed) {
        this.source = source;
        this.added = added;
        this.removed = removed;
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
    public VListChange<T> getAdded() {
        return added;
    }

    @Override
    public VListChange<T> getRemoved() {
        return removed;
    }

    @Override
    public List<T> getSource() {
        return source;
    }

    @Override
    public String toString() {
        return "event: #added=" + getAdded().elements().size()
                + ", #removed=" + getRemoved().elements().size();
    }

}
