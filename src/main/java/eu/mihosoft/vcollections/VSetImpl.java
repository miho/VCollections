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

import java.util.*;
import java.util.function.Consumer;
import java.util.function.Predicate;
import vjavax.observer.Subscription;

/** The mutable handle and its read-only view share one membership and notification sequence. */
final class VSetImpl<E> extends AbstractSet<E> implements VSet<E> {
    private final VSetImpl<E> owner;
    private final Class<E> elementType;
    private final int upper;
    private final LinkedHashMap<E, E> values;
    private final List<Consumer<? super VSetChangeEvent<E>>> listeners = new ArrayList<>();
    private VSetImpl<E> view;
    private int version;

    VSetImpl(Class<E> elementType, int upper) {
        if (upper < -1) throw new IllegalArgumentException("Invalid upper bound " + upper);
        this.owner = this;
        this.values = new LinkedHashMap<>();
        this.elementType = elementType;
        this.upper = upper;
    }

    private VSetImpl(VSetImpl<E> owner) {
        this.owner = owner;
        this.values = owner.values;
        this.elementType = owner.elementType;
        this.upper = owner.upper;
    }

    @Override public boolean isReadOnly() { return owner != this; }

    @Override public VSet<E> asUnmodifiable() {
        if (isReadOnly()) return this;
        if (view == null) view = new VSetImpl<>(this);
        return view;
    }

    @Override public Subscription addChangeListener(Consumer<? super VSetChangeEvent<E>> listener) {
        Objects.requireNonNull(listener);
        // Each subscription has an independent identity, even for repeated registrations.
        Consumer<VSetChangeEvent<E>> subscription = listener::accept;
        listeners.add(subscription);
        return () -> listeners.remove(subscription);
    }

    @Override public boolean contains(Object value) { return values.containsKey(value); }
    @Override public int size() { return values.size(); }

    private void writable() {
        if (isReadOnly()) throw new UnsupportedOperationException("Read-only set");
    }

    private E check(E value) {
        return elementType == null ? value : elementType.cast(Objects.requireNonNull(value, "set member"));
    }

    private void checkSize(int count) {
        if (upper != -1 && count > upper) throw new IllegalArgumentException("Set exceeds upper bound " + upper);
    }

    @Override public boolean add(E value) {
        writable();
        check(value);
        if (values.containsKey(value)) return false;
        checkSize(size() + 1);
        values.put(value, value);
        version++;
        if (observed()) changed(Collections.singleton(value), Collections.emptySet());
        return true;
    }

    @Override public boolean remove(Object value) {
        writable();
        if (!values.containsKey(value)) return false;
        E stored = values.remove(value);
        version++;
        if (observed()) changed(Collections.emptySet(), Collections.singleton(stored));
        return true;
    }

    @Override public boolean addAll(Collection<? extends E> incoming) {
        writable();
        LinkedHashSet<E> next = new LinkedHashSet<>(values.keySet());
        for (E value : incoming) next.add(check(value));
        return commit(next);
    }
    @Override public boolean replaceContents(Collection<? extends E> incoming) {
        writable();
        LinkedHashSet<E> next = new LinkedHashSet<>();
        for (E value : incoming) next.add(check(value));
        return commit(next);
    }

    @Override public boolean removeAll(Collection<?> others) {
        writable();
        Objects.requireNonNull(others);
        return removeIf(others::contains);
    }
    @Override public boolean retainAll(Collection<?> others) {
        writable();
        Objects.requireNonNull(others);
        return removeIf(value -> !others.contains(value));
    }
    @Override public boolean removeIf(Predicate<? super E> predicate) {
        writable();
        Objects.requireNonNull(predicate);
        LinkedHashSet<E> next = new LinkedHashSet<>(values.keySet());
        int before = version;
        next.removeIf(predicate);
        if (before != version) throw new ConcurrentModificationException();
        return commit(next);
    }
    @Override public void clear() {
        writable();
        commit(new LinkedHashSet<>());
    }

    private boolean commit(LinkedHashSet<E> next) {
        checkSize(next.size());
        LinkedHashSet<E> removed = new LinkedHashSet<>(values.keySet());
        removed.removeAll(next);
        LinkedHashSet<E> added = new LinkedHashSet<>(next);
        added.removeAll(values.keySet());
        if (removed.isEmpty() && added.isEmpty()) return false;
        values.keySet().removeAll(removed);
        for (E value : added) values.put(value, value);
        version++;
        if (observed()) changed(Collections.unmodifiableSet(added), Collections.unmodifiableSet(removed));
        return true;
    }

    private boolean observed() { return !listeners.isEmpty() || view != null && !view.listeners.isEmpty(); }

    private void changed(Set<E> added, Set<E> removed) {
        List<Consumer<? super VSetChangeEvent<E>>> mutableListeners = listeners.isEmpty() ? Collections.emptyList() : new ArrayList<>(listeners);
        List<Consumer<? super VSetChangeEvent<E>>> viewListeners =
                view == null || view.listeners.isEmpty() ? Collections.emptyList() : new ArrayList<>(view.listeners);
        Throwable failure = mutableListeners.isEmpty() ? null : notify(mutableListeners, new VSetChangeEvent<>(this, added, removed), null);
        if (!viewListeners.isEmpty()) failure = notify(viewListeners, new VSetChangeEvent<>(view, added, removed), failure);
        if (failure instanceof Error) throw (Error) failure;
        if (failure != null) throw (RuntimeException) failure;
    }

    private Throwable notify(List<Consumer<? super VSetChangeEvent<E>>> callbacks, VSetChangeEvent<E> event, Throwable failure) {
        for (Consumer<? super VSetChangeEvent<E>> callback : callbacks) {
            try {
                callback.accept(event);
            } catch (RuntimeException | Error ex) {
                if (failure == null) failure = ex;
                else if (failure != ex) failure.addSuppressed(ex);
            }
        }
        return failure;
    }

    @Override public Iterator<E> iterator() {
        Iterator<E> entries = values.keySet().iterator();
        return new Iterator<E>() {
            private E current;
            private boolean removable;
            @Override public boolean hasNext() { return entries.hasNext(); }
            @Override public E next() {
                current = entries.next();
                removable = true;
                return current;
            }
            @Override public void remove() {
                writable();
                if (!removable) throw new IllegalStateException();
                entries.remove();
                removable = false;
                version++;
                if (observed()) changed(Collections.emptySet(), Collections.singleton(current));
            }
        };
    }
}
