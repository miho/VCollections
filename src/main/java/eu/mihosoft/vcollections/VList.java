/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
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

/**
 * An observable list.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type
 */
public interface VList<T> extends List<T>, VListObservable<T> {

    /**
     * Creates a new wrapper around the specified list. Modifying the wrapper
     * will modify the wrapped list.
     * 
     * @param <T> element type
     * @param list list to wrap
     * @return new {@link VList} that wraps the specified list
     */
    public static <T> VList<T> newInstance(List<T> list) {

        return VListImpl.newInstance(list);
    }
}

/**
 * OBservable list implementation.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
final class VListImpl<T> extends AbstractList<T> implements VList<T> {

    private final List<T> originalList;

    private VListChangeSupport<T> listChangeSupport;

    private VListChangeSupport<T> getListChangeSupport() {

        if (listChangeSupport == null) {
            listChangeSupport = new VListChangeSupport<>();
        }

        return listChangeSupport;
    }

    private boolean hasListeners() {
        return listChangeSupport != null;
    }

    private void _vmf_fireChangeEvent(VListChangeEvent<T> evt) {
        if (hasListeners()) {
            listChangeSupport.fireEvent(evt);
        }
    }

    @Override
    public boolean addListChangeListener(VListChangeListener<T> l) {
        return getListChangeSupport().addListChangeListener(l);
    }

    @Override
    public boolean removeListChangeListener(VListChangeListener<T> l) {
        boolean result = getListChangeSupport().removeListChangeListener(l);

        if (!getListChangeSupport().hasListeners()) {
            listChangeSupport = null;
        }

        return result;
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
            index = indexOf(e);
        }

        boolean result = originalList.add(e);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getAddedEvent(this,
                            new int[]{index},
                            Arrays.asList(e)));
        }

        return result;
    }

    @Override
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
                            (List<T>) Arrays.asList(o)));
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
                    getAddedEvent(this, indices, new ArrayList<>(c)));
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
                    getAddedEvent(this, indices, new ArrayList<>(c)));
        }

        return result;
    }

    @Override
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
                    new ArrayList(c)
            ));

        }

        return result;

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
                    elementsToRemove
            ));
        }

        return result;

    }

    @Override
    public void clear() {
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
                            new ArrayList<T>(elementsBefore)));
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
                            Arrays.asList(result), Arrays.asList(element)));
        }

        return result;
    }

    @Override
    public void add(int index, T element) {
        originalList.add(index, element);

        if (hasListeners()) {
            _vmf_fireChangeEvent(VListChangeEvent.
                    getRemovedEvent(this,
                            new int[]{index},
                            Arrays.asList(element)));
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
                            Arrays.asList(element)));
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
                            removed));
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
                            new ArrayList<T>(originalList)));
        }
    }

    @Override
    public void sort(Comparator<? super T> comparator) {

        if (hasListeners()) {
            List<Integer> indicesList = new ArrayList<>();
            List<T> changesRemoved = new ArrayList<>();
            List<T> changesAdded = new ArrayList<>();
            List<T> beforeSort = new ArrayList(originalList);

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
                        changesAdded));
            }
        } else {
            originalList.sort(comparator);
        }
    }

    private static class VListIterator<V> implements ListIterator<V> {

        private final VListImpl<V> parent;

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
                V element = parent.get(previousIndex());
                parent._vmf_fireChangeEvent(
                        VListChangeEvent.getRemovedEvent(
                                parent,
                                new int[]{removeIndex},
                                Arrays.asList(element)));
            }

            originalIterator.remove();
        }

        @Override
        public void set(V e) {

            int setIndex = Math.max(0, previousIndex());

            List<V> elementBefore = null;

            if (parent.hasListeners()) {
                if (parent.isEmpty()) {
                    elementBefore = Collections.EMPTY_LIST;
                } else {
                    elementBefore = Arrays.asList(parent.get(setIndex));
                }
            }

            originalIterator.set(e);

            if (parent.hasListeners()) {
                parent._vmf_fireChangeEvent(
                        VListChangeEvent.getSetEvent(
                                parent,
                                new int[]{setIndex},
                                elementBefore, Arrays.asList(e)));
            }
        }

        @Override
        public boolean hasNext() {
            return originalIterator.hasNext();
        }

        @Override
        public V next() {
            return originalIterator.next();
        }

        @Override
        public boolean hasPrevious() {
            return originalIterator.hasPrevious();
        }

        @Override
        public V previous() {
            return originalIterator.previous();
        }

        @Override
        public int nextIndex() {
            return originalIterator.nextIndex();
        }

        @Override
        public int previousIndex() {
            return originalIterator.previousIndex();
        }
    };

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

}
