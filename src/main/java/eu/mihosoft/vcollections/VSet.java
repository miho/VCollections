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

import java.util.Collection;
import java.util.Set;
import java.util.function.Consumer;
import vjavax.observer.Subscription;

/** Observable set. Events describe actual membership changes, independent of iteration order. */
public interface VSet<E> extends Set<E> {
    /** Creates an empty, insertion-ordered set with standard Java equality. */
    static <E> VSet<E> newInstance() {
        return new VSetImpl<>(null, -1);
    }

    /** Creates a set of non-null values with a checked element type and optional upper bound. */
    static <E> VSet<E> checked(Class<E> elementType, int upper) {
        return new VSetImpl<>(java.util.Objects.requireNonNull(elementType), upper);
    }

    /** Changes membership in one observable operation, after validating all incoming values. */
    boolean replaceContents(Collection<? extends E> elements);

    /** Returns a live, unmodifiable view that publishes itself as the event source. */
    VSet<E> asUnmodifiable();
    /** Whether membership changes through this view are forbidden. */
    boolean isReadOnly();
    /** Subscribes to committed membership changes. Repeated registrations unsubscribe independently. */
    Subscription addChangeListener(Consumer<? super VSetChangeEvent<E>> listener);
}
