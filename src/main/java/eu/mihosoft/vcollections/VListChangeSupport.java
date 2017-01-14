/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.ArrayList;
import java.util.List;

/**
 * List change support for managing and notifying listeners.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type
 */
public final class VListChangeSupport<T> implements VListObservable<T> {

    // the registered listeners
    private final List<VListChangeListener<T>> listeners = new ArrayList<>();

    @Override
    public boolean addListChangeListener(VListChangeListener<T> l) {
        return listeners.add(l);
    }

    @Override
    public boolean removeListChangeListener(VListChangeListener<T> l) {
        return listeners.remove(l);
    }

    /**
     * Fires the specified event and notifies all registered listeners.
     *
     * @param evt event to fire
     */
    public void fireEvent(VListChangeEvent<T> evt) {
        listeners.stream().forEach((l) -> {
            l.onChange(evt);
        });
    }

    /**
     * Indicates whether listeners are currently registered. This method can be
     * used to increase efficiency and prevent unnecessary event generation.
     *
     * @return {@code true} if listeners are currently registered with this list
     * change support; {@code false} otherwise
     */
    public boolean hasListeners() {
        return !listeners.isEmpty();
    }
}
