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
import org.junit.Test;
import static org.junit.Assert.*;

public class VListOrderTest {
    @Test public void movesAreOneReplacementWithTheActualBeforeAndAfter() {
        VList<String> list = VList.newInstance(new ArrayList<>(Arrays.asList("a","b","c")));
        List<VListChange<String>> before = new ArrayList<>(), after = new ArrayList<>();
        list.addChangeListener(change -> {
            assertTrue(change.wasSet());
            before.add(change.removed());
            after.add(change.added());
        });
        list.move(0,2);
        assertEquals(Arrays.asList("b","c","a"), list);
        assertEquals(Arrays.asList("a","b","c"), before.get(0).elements());
        assertEquals(list, after.get(0).elements());
        list.move(2,2);
        assertEquals(1, before.size());
        assertThrows(IndexOutOfBoundsException.class, () -> list.move(3,0));
        assertThrows(UnsupportedOperationException.class, () -> list.asUnmodifiable().move(0,2));
    }

    @Test public void indexedInsertionsKeepIndexValuePairsAndValidateTheWholeBatch() {
        for (boolean observed : new boolean[]{false,true}) {
            VList<String> list = VList.newInstance(new LinkedList<>(Arrays.asList("base","end")));
            List<VListChange<String>> changes = new ArrayList<>();
            if (observed) list.addChangeListener(e -> changes.add(e.added()));
            list.addAll(new int[]{4,0,2}, Arrays.asList("z","a","m"));
            assertEquals(Arrays.asList("a","base","m","end","z"), list);
            if (observed) {
                assertEquals(1, changes.size());
                assertArrayEquals(new int[]{0,2,4}, changes.get(0).indices());
                assertEquals(Arrays.asList("a","m","z"), changes.get(0).elements());
            }
            assertThrows(IndexOutOfBoundsException.class, () -> list.addAll(new int[]{1,99}, Arrays.asList("q","w")));
            assertThrows(IllegalArgumentException.class, () -> list.addAll(new int[]{0,0}, Arrays.asList("q","w")));
            assertThrows(UnsupportedOperationException.class, () -> list.asUnmodifiable().addAll(new int[]{0}, Arrays.asList("x")));
            assertEquals(Arrays.asList("a","base","m","end","z"), list);
        }
        VList<String> empty = VList.newInstance(new ArrayList<>());
        empty.addAll(new int[]{1,0}, Arrays.asList("b","a"));
        assertEquals(Arrays.asList("a","b"), empty);
    }

    @SuppressWarnings({"rawtypes","unchecked"})
    @Test public void backingTypeFailureRollsBackEarlierInsertionsWithoutNotifications() {
        VList raw = VList.newInstance(Collections.checkedList(new ArrayList<String>(), String.class));
        raw.addChangeListener(evt -> fail("Rolled back batch must not emit changes"));
        assertThrows(ClassCastException.class, () -> raw.addAll(new int[]{0,1}, Arrays.asList("ok",7)));
        assertTrue(raw.isEmpty());
    }
}
