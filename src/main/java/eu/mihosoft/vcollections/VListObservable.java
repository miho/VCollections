/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

/**
 * List observable.
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T>
 */
public interface VListObservable<T> {

    /**
     * Adds the specified listener to this list. The listener will be notified
     * about every modification made to this list.
     *
     * @param l listener to add
     * @return {@code true} if this listener is added to this list;
     * {@code false} otherwise (if the listener has already been added to this
     * list)
     */
    boolean addListChangeListener(VListChangeListener<T> l);

    /**
     * Removes the specified listener from this list.
     *
     * @param l listener to remove
     * @return {@code true} if this listener is removed from this list;
     * {@code false} otherwise (if the listener has already been removed from
     * this list)
     */
    boolean removeListChangeListener(VListChangeListener<T> l);

}
