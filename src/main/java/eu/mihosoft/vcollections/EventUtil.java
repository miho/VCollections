/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import javax.observer.collection.CollectionChangeEvent;

/**
 * Utility class for events. 
 * 
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
public final class EventUtil {

    private EventUtil() {
        throw new AssertionError("Please don't instantiate me!");
    }

    /**
     * Returns a detailed string representation of the specified event or the
     * result of the {@code toString()} method if details are not available for
     * the specified event.
     *
     * @param evt event
     * @return detailed string representation of the specified event
     */
    public static String toStringWithDetails(CollectionChangeEvent evt) {
        if (evt instanceof VListChangeEvent) {
            return ((VListChangeEvent) evt).toStringWithDetails();
        } else {
            return evt.toString();
        }
    }

}
