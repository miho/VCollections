/*
 * Copyright 2017-2026 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
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
 * 181â€“192. http://doi.org/10.1007/s00791-014-0230-y
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

import org.junit.Test;
import vjavax.observer.collection.CollectionChangeEvent;
import java.util.*;
import static org.junit.Assert.*;

public class VListIteratorReplayTest {
    @Test public void forwardAndBackwardSetAndRemoveUseReturnedElementIndex() {
        for (boolean backwards : new boolean[]{false, true}) {
            VList<String> source = VList.newInstance(new ArrayList<>(Arrays.asList("a", "b", "c")));
            List<CollectionChangeEvent<String, VList<String>, VListChange<String>>> events = new ArrayList<>();
            source.addChangeListener((VListChangeListener<String>) events::add);
            ListIterator<String> iterator = source.listIterator(backwards ? 2 : 1);
            assertEquals("b", backwards ? iterator.previous() : iterator.next());
            iterator.nextIndex(); iterator.previousIndex(); // Queries must not change last-returned state.
            iterator.set("x"); iterator.set("y"); iterator.remove();
            assertEquals(Arrays.asList("a", "c"), source);
            assertEquals(3, events.size());
            VList<String> replay = VList.newInstance(new ArrayList<>(Arrays.asList("a", "b", "c")));
            for (CollectionChangeEvent<String, VList<String>, VListChange<String>> event : events) {
                assertArrayEquals(new int[]{1}, event.wasSet() ? event.added().indices() : event.removed().indices());
                if (event.wasSet()) replay.setAll(event.added().indices()[0], event.added().elements());
                else replay.removeAll(event.removed().indices());
            }
            assertEquals(source, replay);
            assertEquals(Arrays.asList("b"), events.get(0).removed().elements());
            assertEquals(Arrays.asList("x"), events.get(1).removed().elements());
            assertEquals(Arrays.asList("y"), events.get(2).removed().elements());
            Collections.reverse(events);
            for (CollectionChangeEvent<String, VList<String>, VListChange<String>> event : events) {
                if (event.wasSet()) replay.setAll(event.removed().indices()[0], event.removed().elements());
                else replay.addAll(event.removed().indices(), event.removed().elements());
            }
            assertEquals(Arrays.asList("a", "b", "c"), replay);
        }
    }

    @Test public void invalidIteratorMutationsEmitNoEvents() {
        VList<String> source = VList.newInstance(new ArrayList<>(Arrays.asList("a", "b")));
        List<CollectionChangeEvent<String, VList<String>, VListChange<String>>> events = new ArrayList<>();
        source.addChangeListener((VListChangeListener<String>) events::add);
        ListIterator<String> iterator = source.listIterator();
        assertThrows(IllegalStateException.class, iterator::remove);
        assertThrows(IllegalStateException.class, () -> iterator.set("invalid"));
        assertTrue(events.isEmpty());
        iterator.next(); iterator.remove();
        assertThrows(IllegalStateException.class, iterator::remove);
        assertEquals(1, events.size());
        iterator.add("new");
        assertThrows(IllegalStateException.class, () -> iterator.set("invalid"));
        assertThrows(IllegalStateException.class, iterator::remove);
        assertEquals(2, events.size());
    }
}
