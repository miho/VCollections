/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.Collection;

/**
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type of the collection
 * @param <OC> observed collection type
 * @param <CC> collection change type
 */
public interface CollectionObservable<T, OC extends Collection<T>, CC extends CollectionChange<T>> {

    /**
     * Adds the specified listener to this list. The listener will be notified
     * about every modification made to this list.
     *
     * @param l listener to add
     * @return the listener that has been added to this observable
     */
    boolean addListChangeListener(CollectionChangeListener<T, ? super OC, ? super CC> l);

    /**
     * Removes the specified listener from this list.
     *
     * @param l listener to remove
     * @return {@code true} if this listener is removed from this observable;
     * {@code false} otherwise (if the listener has already been removed from
     * this list)
     */
    boolean removeListChangeListener(CollectionChangeListener<T, ? super OC, ? super CC> l);
}
