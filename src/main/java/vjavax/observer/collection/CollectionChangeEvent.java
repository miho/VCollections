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
/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package vjavax.observer.collection;

import java.util.Collection;

/**
 * 
 * @param <T> element type of the collection
 * @param <OC> observed collection type
 * @param <CC> collection change type
 * 
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
public interface CollectionChangeEvent<T, OC extends Collection<T>, CC extends CollectionChange<T>> {
    /**
     * Indicates whether elements were added during this event.
     *
     * @return {@code true} if elements were added during this event;
     * {@code false} otherwise
     */
    default boolean wasAdded() {
        return !added().elements().isEmpty();
    }

    /**
     * Indicates whether elements were removed during this event.
     *
     * @return {@code true} if elements were removed during this event;
     * {@code false} otherwise
     */
    default boolean wasRemoved() {
        return !removed().elements().isEmpty();
    }

    /**
     * Indicates whether elements were set, e.g., replaced during this event.
     *
     * @return {@code true} if elements were set during this event;
     * {@code false} otherwise
     */
    default boolean wasSet() {
        return wasAdded() && wasRemoved();
    }

    /**
     * Returns the change that contains all elements that were added during this
     * event.
     *
     * @return the change that contains all elements that were added during this
     * event
     */
    CC added();

    /**
     * Returns the change that contains all elements that were removed during
     * this event.
     *
     * @return the change that contains all elements that were removed during
     * this event
     */
    CC removed();

    /**
     * Returns the source collection, e.g., the collection that fired the change event
     *
     * @return the source list
     */
    OC source();

    /**
     * Additional event info. This can be used to categorize events asnd provide domain specific
     * information that can be evaluated by all event listeners/subscribers.
     * @return
     */
    default String eventInfo() { return ""; }
}
