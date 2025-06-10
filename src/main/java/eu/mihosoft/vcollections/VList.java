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
package eu.mihosoft.vcollections;

import java.util.AbstractList;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Objects;
import java.util.function.Predicate;
import java.util.function.UnaryOperator;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import vjavax.observer.Subscription;
import vjavax.observer.collection.CollectionChangeEvent;
import vjavax.observer.collection.CollectionChangeListener;

/**
 * An observable list.
 *
 * @param <T> element type
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public interface VList<T> extends List<T>, VListObservable<T> {

    /**
     * Creates a new wrapper around the specified list. Modifying the wrapper
     * will modify the wrapped list.
     *
     * @param <T>  element type
     * @param list list to wrap
     * @return new {@link VList} that wraps the specified list
     */
    static <T> VList<T> newInstance(List<T> list) {

        return VListImpl.newInstance(list);
    }

    /**
     * Returns an unmodifiable view of this list (see {@link java.util.Collections#unmodifiableList(java.util.List)} ).
     * 
     * @return an unmodifiable view of this list
     */
    VList<T> asUnmodifiable();

    /**
     * Removes elements at the specified indices.
     * @param indices
     * @return {@code true} if this collection changed as a result of the call; {@code false} otherwise
     */
    boolean removeAll(int... indices);

    /**
     * Sets specified elements. All elements from {@code index} to {@code index+elements.size()-1}
     * are set, i.e., replaced.
     * @param index start index
     * @param elements elements to set
     * @return the elements previously at the specified positions
     */
    Collection<T> setAll(int index, Collection<T> elements);

    /**
     * Adds the specified elements at the given indices. It requires that {@code indices.length == c.size()}
     * @param indices indices
     * @param c collection containing the elements to add
     * @return {@code true} if the collection changed as a result of the call; {@code false} otherwise
     */
    boolean addAll(int[] indices, Collection<? extends T> c);

    /**
     * Sets the event info to be used for event generation.
     * @param evtInfo event info to set
     */
    void setEventInfo(String evtInfo);

    /**
     * Returns the event info used for event generation.
     * @return event info used for event generation
     */
    String getEventInfo();
}

/**
 * OBservable list implementation.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
final class VListImpl<T> extends AbstractList<T> implements VList<T> {

    private final List<T> originalList;

    private VListChangeSupport<T> listChangeSupport;
    private VListImpl<T> unmodifiableInstance;

    private String evtInfo = "";

    private VListChangeSupport<T> getListChangeSupport() {

        if (listChangeSupport == null) {
            listChangeSupport = new VListChangeSupport<>();
        }

        return listChangeSupport;
    }

    @Override
    public String getEventInfo() {
        return this.evtInfo;
    }

    @Override
    public void setEventInfo(String evtInfo) {
        if(evtInfo==null) {
            this.evtInfo = "";
        } else {
            this.evtInfo = evtInfo;
        }
    }

    private boolean hasListeners() {
        return listChangeSupport != null;
    }

    private void _vmf_fireChangeEvent(CollectionChangeEvent<T, ? super VList<T>, ? super VListChange<T>> evt) {
        if (hasListeners()) {
            listChangeSupport.fireEvent(evt);
        }
    }

    private VListImpl(List<T> originalList) {
        this.originalList = originalList;
    }

    public static <T> VListImpl<T> newInstance(List<T> list) {
        return new VListImpl<>(list);
    }

    @Override
    public int size() {
        return originalList.size();
    }

    @Override
    public boolean isEmpty() {
        return originalList.isEmpty();
    }

    @Override
    public boolean contains(Object o) {
        return originalList.contains(o);
    }

    @Override
    public Iterator<T> iterator() {
        return listIterator();
    }

    @Override
    public Object[] toArray() {
        return originalList.toArray();
    }

    @Override
    public <T> T[] toArray(T[] a) {
        return originalList.toArray(a);
    }

    @Override
    public boolean add(T e) {

        int index = 0;

        if (hasListeners()) {
            index = size();
        }

        boolean result = originalList.add(e);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getAddedEvent(this,
                            new int[]{index},
                            Arrays.asList(e),getEventInfo()));
        }

        return result;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean remove(Object o) {
        int index = 0;

        if (hasListeners()) {
            index = indexOf(o);
        }

        boolean result = originalList.remove(o);

        if (hasListeners() && result) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getRemovedEvent(this,
                            new int[]{index},
                            (List<T>) Arrays.asList(o),getEventInfo()));
        }

        return result;
    }

    @Override
    public boolean containsAll(Collection<?> c) {
        return originalList.containsAll(c);
    }

    @Override
    public boolean addAll(Collection<? extends T> c) {

        int sizeBefore = size();

        boolean result = originalList.addAll(c);

        if (hasListeners()) {
            int[] indices = new int[c.size()];

            for (int i = 0; i < indices.length; i++) {
                indices[i] = sizeBefore + i;
            }

            _vmf_fireChangeEvent(VListChangeEvent.
                    getAddedEvent(this, indices, new ArrayList<>(c),getEventInfo()));
        }

        return result;
    }

    @Override
    public boolean addAll(int index, Collection<? extends T> c) {

        boolean result = originalList.addAll(index, c);

        if (hasListeners()) {
            int[] indices = new int[c.size()];

            for (int i = 0; i < indices.length; i++) {
                indices[i] = index + i;
            }

            _vmf_fireChangeEvent(VListChangeEvent.
                    getAddedEvent(this, indices, new ArrayList<>(c),getEventInfo()));
        }

        return result;
    }

    @Override
    public boolean addAll(int[] indices, Collection<? extends T> c) {

        Objects.requireNonNull(indices, "Indices must not be null");
        Objects.requireNonNull(c, "collection must not be null");

        if(indices.length!=c.size()) {
            throw new RuntimeException("The number of indices must match the number of elements to add");
        }

        // only sort indices if we have listeners
        int[] indicesSorted;
        if(hasListeners()) {
            indicesSorted = indices.clone();
            Arrays.sort(indicesSorted);
        } else {
            indicesSorted = indices;
        }

        Iterator<? extends T> it = c.iterator();

        boolean changed = indicesSorted.length>0;

        for (int i = indicesSorted.length - 1; i > -1; i--) {
            originalList.add(indicesSorted[i],it.next());
        }

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getAddedEvent(this, indices, new ArrayList<>(c),getEventInfo()));
        }

        return changed;
    }

    @Override
    @SuppressWarnings("unchecked")
    public boolean removeAll(Collection<?> c) {

        int[] indices = null;

        if (hasListeners()) {
            c = c.stream().filter(e -> contains(e)).
                    collect(Collectors.toList());

            indices = c.stream().mapToInt(e -> indexOf(e)).toArray();
        }

        boolean result = originalList.removeAll(c);

        if (hasListeners()) {

            _vmf_fireChangeEvent(VListChangeEvent.getRemovedEvent(this, indices,
                    new ArrayList(c),getEventInfo()
            ));

        }

        return result;

    }

    @Override
    public Collection<T> setAll(int index, Collection<T> elements) {

        if(elements.isEmpty()) return Collections.emptyList();

        int toIndex = index+elements.size();

        int[] indices = IntStream.range(index,toIndex).toArray();

        Iterator<T> it = elements.iterator();

        List<T> previousElements = new ArrayList<>(elements.size());

        int i = 0;
        while(it.hasNext()) {
            previousElements.add(originalList.set(indices[i],it.next()));
            i++;
        }

        List<T> oldElements = new ArrayList<>(previousElements);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.getSetEvent(
                    this, indices, oldElements, new ArrayList<T>(elements), getEventInfo()
            ));
        }

        return previousElements;
    }

    @Override
    public boolean removeAll(int... indices) {

        if (indices.length == 0) return true;

        List<T> removedElements = new ArrayList<>(indices.length);

        int[] indicesSorted = indices.clone();

        Arrays.sort(indicesSorted);

        for (int i = indicesSorted.length - 1; i > -1; i--) {
            T e = originalList.remove(indicesSorted[i]);
            removedElements.add(e);
        }

        Collections.reverse(removedElements);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.getRemovedEvent(
                    this, indicesSorted,
                    removedElements,getEventInfo()
            ));
        }

        return !removedElements.isEmpty();
    }

    @Override
    public boolean retainAll(Collection<?> c) {

        int[] indices = null;
        List<T> elementsToRemove = null;

        if (hasListeners()) {
            c = c.stream().filter(e -> contains(e)).collect(Collectors.toList());

            List<Integer> indexList = c.stream().map(e -> indexOf(e)).
                    collect(Collectors.toList());

            indices = IntStream.range(0, size()).
                    filter(i -> !indexList.contains(i)).toArray();

            elementsToRemove = IntStream.of(indices).boxed().
                    map(i -> get(i)).collect(Collectors.toList());
        }

        boolean result = originalList.retainAll(c);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.getRemovedEvent(this, indices,
                    elementsToRemove,getEventInfo()
            ));
        }

        return result;

    }

    @Override
    public void clear() {

        // don't do anything if this list is already empty
        if(isEmpty()) {
            return;
        }

        List<T> elementsBefore = null;
        int[] indices = null;

        if (hasListeners()) {
            indices = IntStream.range(0, size()).toArray();
            elementsBefore = new ArrayList<>(originalList);
        }

        originalList.clear();

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getRemovedEvent(this,
                            indices,
                            new ArrayList<T>(elementsBefore),getEventInfo()));
        }
    }

    @Override
    public T get(int index) {
        return originalList.get(index);
    }

    @Override
    public T set(int index, T element) {
        T result = originalList.set(index, element);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getSetEvent(this,
                            new int[]{index},
                            Arrays.asList(result), Arrays.asList(element),getEventInfo()));
        }

        return result;
    }

    @Override
    public void add(int index, T element) {
        originalList.add(index, element);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getAddedEvent(this,
                            new int[]{index},
                            Arrays.asList(element),getEventInfo()));
        }
    }

    @Override
    public T remove(int index) {

        T element = null;

        if (hasListeners()) {
            element = get(index);
        }

        T result = originalList.remove(index);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getRemovedEvent(this,
                            new int[]{index},
                            Arrays.asList(element),getEventInfo()));
        }

        return result;
    }

    @Override
    public int indexOf(Object o) {
        return originalList.indexOf(o);
    }

    @Override
    public int lastIndexOf(Object o) {
        return originalList.lastIndexOf(o);
    }

    @Override
    public ListIterator<T> listIterator(final int index) {
        return new VListIterator<>(this, index);
    }

    @Override
    public ListIterator<T> listIterator() {
        return listIterator(0);
    }

    @Override
    public List<T> subList(int fromIndex, int toIndex) {
        return new VSubList<>(super.subList(fromIndex, toIndex));
    }

    @Override
    public boolean removeIf(Predicate<? super T> filter) {

        Objects.requireNonNull(filter);

        List<T> removed = null;
        int[] indices = null;

        if (hasListeners()) {
            removed = originalList.stream().filter(filter).
                    collect(Collectors.toList());

            indices = removed.stream().mapToInt(e -> indexOf(e)).toArray();
        }

        boolean result = originalList.removeIf(filter);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getRemovedEvent(this,
                            indices,
                            removed,getEventInfo()));
        }

        return result;
    }

    @Override
    public void replaceAll(UnaryOperator<T> operator) {

        List<T> elementsBefore = null;

        if (hasListeners()) {
            elementsBefore = new ArrayList<>(originalList);
        }
        originalList.replaceAll(operator);

        if (hasListeners()) {
            int[] indices = IntStream.range(0, size()).toArray();
            _vmf_fireChangeEvent(VListChangeEvent.
                    getSetEvent(this,
                            indices,
                            new ArrayList<T>(elementsBefore),
                            new ArrayList<T>(originalList),getEventInfo()));
        }
    }

    @Override
    public void sort(Comparator<? super T> comparator) {

        if (hasListeners()) {
            List<Integer> indicesList = new ArrayList<>();
            List<T> changesRemoved = new ArrayList<>();
            List<T> changesAdded = new ArrayList<>();
            List<T> beforeSort = new ArrayList<>(originalList);

            originalList.sort(comparator);

            // generate change set
            for (int i = 0; i < size(); i++) {
                if (!Objects.equals(beforeSort.get(i), originalList.get(i))) {
                    indicesList.add(i);
                    changesRemoved.add(beforeSort.get(i));
                    changesAdded.add(originalList.get(i));
                }
            }

            // fire event
            if (!changesAdded.isEmpty()) {
                int[] indices = indicesList.stream().mapToInt(i -> i).toArray();
                _vmf_fireChangeEvent(VListChangeEvent.getSetEvent(
                        this, indices,
                        changesRemoved,
                        changesAdded,getEventInfo()));
            }
        } else {
            originalList.sort(comparator);
        }
    }

    @Override
    public Subscription addChangeListener(CollectionChangeListener<T, ? super VList<T>, ? super VListChange<T>> l) {
        return getListChangeSupport().addChangeListener(l);
    }

    @Override
    public boolean removeChangeListener(CollectionChangeListener<T, ? super VList<T>, ? super VListChange<T>> l) {
        boolean result = getListChangeSupport().removeChangeListener(l);

        if (!getListChangeSupport().hasListeners()) {
            listChangeSupport = null;
        }

        return result;
    }

    @Override
    public VList<T> asUnmodifiable() {
        if(unmodifiableInstance==null) {
            unmodifiableInstance = new VListImpl<>(Collections.unmodifiableList(originalList));
            
            syncLists(this, unmodifiableInstance);
        }
        
        return unmodifiableInstance;
    }

    private void syncLists(VListImpl<T> aThis, VListImpl<T> unmodifiableInstance) {

        aThis.addChangeListener((VListChangeListener<T>) unmodifiableInstance::_vmf_fireChangeEvent);
        
    }

    //    @Override
//    public boolean addChangeListener(CollectionChangeListener<T, ? super VListChangeEvent<T>, ? super VList<T>, ? super VListChange<T>> l) {
//        return getListChangeSupport().addChangeListener(l);
//    }
//
//    @Override
//    public boolean removeChangeListener(CollectionChangeListener<T, ? super VListChangeEvent<T>, ? super VList<T>, ? super VListChange<T>> l) {
//        boolean result = getListChangeSupport().removeChangeListener(l);
//
//        if (!getListChangeSupport().hasListeners()) {
//            listChangeSupport = null;
//        }
//
//        return result;
//    }
    private static class VListIterator<V> implements ListIterator<V> {

        private final VListImpl<V> parent;
        private int lastIndex = -1;
        private V lastElement;

        public VListIterator(VListImpl<V> parent, int index) {
            this.parent = parent;
            originalIterator
                    = parent.originalList.listIterator(index);
        }

        private final ListIterator<V> originalIterator;

        @Override
        public void add(V e) {

            int addIndex = nextIndex();
            originalIterator.add(e);

            if (parent.hasListeners()) {
                parent._vmf_fireChangeEvent(
                        VListChangeEvent.getAddedEvent(
                                parent,
                                new int[]{addIndex},
                                Arrays.asList(e)));
            }
        }

        @Override
        public void remove() {

            if (parent.hasListeners()) {
                int removeIndex = previousIndex();
                parent._vmf_fireChangeEvent(
                        VListChangeEvent.getRemovedEvent(
                                parent,
                                new int[]{removeIndex},
                                Arrays.asList(lastElement), parent.getEventInfo()));
            }

            originalIterator.remove();
        }

        @Override
        @SuppressWarnings("unchecked")
        public void set(V e) {

            int setIndex = Math.max(0, previousIndex());

            List<V> elementBefore = null;

            if (parent.hasListeners()) {
                if (parent.isEmpty()) {
                    elementBefore = Collections.EMPTY_LIST;
                } else {
                    elementBefore = Arrays.asList(lastElement);
                }
            }

            originalIterator.set(e);

            if (parent.hasListeners()) {
                parent._vmf_fireChangeEvent(
                        VListChangeEvent.getSetEvent(
                                parent,
                                new int[]{setIndex},
                                elementBefore, Arrays.asList(e),parent.getEventInfo()));
            }
        }

        @Override
        public boolean hasNext() {
            return originalIterator.hasNext();
        }

        @Override
        public V next() {
            return lastElement = originalIterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return originalIterator.hasPrevious();
        }

        @Override
        public V previous() {
            return lastElement = originalIterator.previous();
        }

        @Override
        public int nextIndex() {
            return lastIndex = originalIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return lastIndex = originalIterator.previousIndex();
        }
    }

    ;

    private static class VSubList<T> implements List<T> {

        private final List<T> parent;

        public VSubList(List<T> parent) {
            this.parent = parent;
        }

        @Override
        public int size() {
            return parent.size();
        }

        @Override
        public boolean isEmpty() {
            return parent.isEmpty();
        }

        @Override
        public boolean contains(Object o) {
            return parent.contains(o);
        }

        @Override
        public Iterator<T> iterator() {
            return parent.iterator();
        }

        @Override
        public Object[] toArray() {
            return parent.toArray();
        }

        @Override
        public <T> T[] toArray(T[] a) {
            return parent.toArray(a);
        }

        @Override
        public boolean add(T e) {
            return parent.add(e);
        }

        @Override
        public boolean remove(Object o) {
            return parent.remove(o);
        }

        @Override
        public boolean containsAll(Collection<?> c) {
            return parent.containsAll(c);
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            return parent.addAll(c);
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            return parent.addAll(index, c);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            return parent.removeAll(c);
        }

        @Override
        public boolean retainAll(Collection<?> c) {
            return parent.retainAll(c);
        }

        @Override
        public void clear() {
            parent.clear();
        }

        @Override
        public T get(int index) {
            return parent.get(index);
        }

        @Override
        public T set(int index, T element) {
            return parent.set(index, element);
        }

        @Override
        public void add(int index, T element) {
            parent.add(index, element);
        }

        @Override
        public T remove(int index) {
            return parent.remove(index);
        }

        @Override
        public int indexOf(Object o) {
            return parent.indexOf(o);
        }

        @Override
        public int lastIndexOf(Object o) {
            return parent.lastIndexOf(o);
        }

        @Override
        public ListIterator<T> listIterator() {
            return parent.listIterator();
        }

        @Override
        public ListIterator<T> listIterator(int index) {
            return parent.listIterator(index);
        }

        @Override
        public List<T> subList(int fromIndex, int toIndex) {
            return new VSubList<>(parent.subList(fromIndex, toIndex));
        }

    }

    @Override
    public boolean equals(Object o) {
        return originalList.equals(o);
    }

    @Override
    public int hashCode() {
        return originalList.hashCode();
    }
}
