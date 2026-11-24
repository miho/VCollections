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
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Test;
import static org.junit.Assert.*;

public class VSetTest {
    @Test public void eventsUseStoredMembersAndBothViewSources() {
        VSet<String> set = VSet.newInstance();
        String stored = new String("a");
        VSet<String> ro = set.asUnmodifiable();
        List<VSetChangeEvent<String>> changes = new ArrayList<>();
        ro.addChangeListener(changes::add);
        set.add(stored);
        assertFalse(set.add(new String("a")));
        set.remove(new String("a"));
        assertEquals(2, changes.size());
        assertSame(stored, changes.get(1).removed().iterator().next());
        assertSame(ro, changes.get(1).source());
        set.add(null);
        assertTrue(set.remove(null));
        set.addAll(Arrays.asList("a","b","c"));
        ro.iterator().next();
        assertThrows(UnsupportedOperationException.class, () -> ro.iterator().remove());
        set.retainAll(Collections.singleton("b"));
        assertEquals(new HashSet<>(Arrays.asList("a","c")), changes.get(5).removed());
        assertSame(ro, ro.asUnmodifiable());
    }

    @Test public void boundedBatchesKeepTheirPreviousMembershipOnFailure() {
        VSet<String> set = VSet.checked(String.class, 2);
        set.add("old");
        List<VSetChangeEvent<String>> events = new ArrayList<>();
        set.addChangeListener(events::add);
        assertThrows(IllegalArgumentException.class, () -> set.addAll(Arrays.asList("new","extra")));
        assertThrows(NullPointerException.class, () -> set.replaceContents(Arrays.asList("new",null)));
        assertEquals(Collections.singleton("old"), set);
        assertTrue(events.isEmpty());
        set.replaceContents(Arrays.asList("x","y","x"));
        assertEquals(1, events.size());
        assertEquals(Collections.singleton("old"), events.get(0).removed());
        assertThrows(UnsupportedOperationException.class, () -> events.get(0).added().clear());
        assertFalse(set.replaceContents(Arrays.asList("y","x")));
        assertEquals(1, events.size());
    }

    @Test public void failedListenerStillDeliversToViewAndIndependentListeners() {
        VSet<String> set = VSet.newInstance();
        AtomicInteger deliveries = new AtomicInteger();
        RuntimeException failure = new RuntimeException();
        set.addChangeListener(e -> { throw failure; });
        set.addChangeListener(e -> deliveries.incrementAndGet());
        set.asUnmodifiable().addChangeListener(e -> deliveries.incrementAndGet());
        assertSame(failure, assertThrows(RuntimeException.class, () -> set.add("a")));
        assertEquals(2, deliveries.get());
    }

}
