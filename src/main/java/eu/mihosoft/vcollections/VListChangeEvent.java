/*
 * Copyright 2017 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
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

    public String toStringWithDetails() {
        
        StringBuilder sb = new StringBuilder();

        int numAdded = this.getAdded().elements().size();
        int numRemoved = this.getRemoved().elements().size();

        sb.append("#removed: ").append(numRemoved).append(", added: ").
                append(numAdded).append("\n");

        sb.append("removed-indices = [");
        for (int i = 0; i < numRemoved; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.getRemoved().indices()[i]);
        }
        sb.append("]\n");

        sb.append("removed-elements = [");
        for (int i = 0; i < numRemoved; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.getRemoved().elements().get(i));
        }
        sb.append("]\n");

        sb.append("added-indices    = [");
        for (int i = 0; i < numAdded; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.getAdded().indices()[i]);
        }
        sb.append("]\n");

        sb.append("added-elements   = [");
        for (int i = 0; i < numAdded; i++) {
            if (i > 0) {
                sb.append(", ");
            }
            sb.append(this.getAdded().elements().get(i));
        }
        sb.append("]\n");
        
        return sb.toString();
    }

}
