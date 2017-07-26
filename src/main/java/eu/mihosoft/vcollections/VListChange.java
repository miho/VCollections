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

import java.util.Collections;
import java.util.List;
import java.util.Objects;
import vjavax.observer.collection.ListChange;

/**
 * Represents a list change. Every listz change consists of an element list
 * containing the changed elements and an index array which contains the indices
 * of the changed elements.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VListChange<T> extends ListChange<T>{

    /**
     * @return the indices of the changed elements
     */
    @Override
    int[] indices();

    /**
     * @return changed elements
     */
    @Override
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
    @SuppressWarnings("unchecked")
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
 * @param <T> element type
 * 
 * @author Michael Hoffer (info@michaelhoffer.de)
 */
class VListChangeImpl<T> implements VListChange<T> {

    private final int[] indices;
    private final List<T> elements;

    @SuppressWarnings("unchecked")
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
