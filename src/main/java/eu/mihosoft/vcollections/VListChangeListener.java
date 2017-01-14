/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

/**
 * List change listener. This listener is called whenever a list change occures.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
@FunctionalInterface
public interface VListChangeListener<T> {

    /**
     * This method is called whenever the observed list changes.
     *
     * @param evt change event that contains detailed information on which
     * elements have been changed
     */
    void onChange(VListChangeEvent<T> evt);
}
