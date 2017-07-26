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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import vjavax.observer.Subscription;
import vjavax.observer.collection.CollectionChangeListener;
import vjavax.observer.collection.CollectionObservable;

/**
 * List observable.
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type
 */
public interface VListObservable<T> extends CollectionObservable<T, VList<T>, VListChange<T>> {

    @SafeVarargs
    static <V> VListObservable<V> of(VList<V>... lists) {
        return new VListObservableImpl<>(Arrays.asList(lists));
    }

    static <V> VListObservable<V> of(Collection<VList<V>> lists) {
        return new VListObservableImpl<>(lists);
    }
}

class VListObservableImpl<T> implements VListObservable<T> {

    private final Collection<VList<T>> lists;
    private final Map<Object, Collection<Subscription>> subscriptions = new HashMap<>();

    VListObservableImpl(Collection<VList<T>> lists) {
        this.lists = lists;
    }

    @Override
    public Subscription addChangeListener(
            CollectionChangeListener<T, ? super VList<T>, ? super VListChange<T>> l) {

        Collection<Subscription> subscriptionsOfL = subscriptions.get(l);

        if (subscriptionsOfL == null) {
            subscriptionsOfL = new ArrayList<>();
            subscriptions.put(l, subscriptionsOfL);
        }

        for (VList<T> list : lists) {
            Subscription s = list.addChangeListener(l);

            subscriptionsOfL.add(s);
        }

        return () -> {
            Collection<Subscription> subscriptionsOfLtmp
                    = subscriptions.get(l);

            if (subscriptionsOfLtmp != null) {
                subscriptionsOfLtmp.forEach(s -> {
                    s.unsubscribe();
                });
            }
        };
    }

    @Override
    public boolean removeChangeListener(
            CollectionChangeListener<T, ? super VList<T>, ? super VListChange<T>> l) {
        Collection<Subscription> subscriptionsOfL = subscriptions.get(l);

        if (subscriptions.containsKey(l) && subscriptionsOfL != null) {
            subscriptionsOfL.forEach(s -> {
                s.unsubscribe();
            });
            subscriptions.remove(l);
        }

        return subscriptionsOfL != null;
    }

}
