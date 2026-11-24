package eu.mihosoft.vcollections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.Assert;
import org.junit.Test;

public class VListRemovalTest {
    @Test public void bulkRemovalReportsTheActualOccurrencesInOriginalOrder() {
        String first = new String("x");
        String second = new String("x");
        VList<String> list = VList.newInstance(new ArrayList<>(Arrays.asList(first, "y", second)));
        List<VListChange<String>> events = new ArrayList<>();
        list.addChangeListener(change -> events.add(change.removed()));
        list.removeAll(Arrays.asList(new String("x")));
        Assert.assertArrayEquals(new int[]{0, 2}, events.get(0).indices());
        Assert.assertSame(first, events.get(0).elements().get(0));
        Assert.assertSame(second, events.get(0).elements().get(1));
        Assert.assertEquals(Arrays.asList("y"), list);
    }

    @Test public void retainAllPreservesEveryEqualMemberAndRemoveUsesTheStoredObject() {
        String first = new String("x");
        String second = new String("x");
        VList<String> list = VList.newInstance(new ArrayList<>(Arrays.asList(first, second, "drop")));
        List<VListChange<String>> events = new ArrayList<>();
        list.addChangeListener(change -> events.add(change.removed()));
        list.retainAll(Arrays.asList(new String("x")));
        Assert.assertArrayEquals(new int[]{2}, events.get(0).indices());
        Assert.assertTrue(list.remove(second));
        Assert.assertSame(first, events.get(1).elements().get(0));
        Assert.assertSame(second, list.get(0));
    }

    @Test public void removeIfRunsOncePerOccurrenceAndReportsPartialLinkedListEdits() {
        VList<String> list = VList.newInstance(new LinkedList<>(Arrays.asList("x", "x", "x")));
        List<VListChange<String>> events = new ArrayList<>();
        list.addChangeListener(change -> events.add(change.removed()));
        AtomicInteger position = new AtomicInteger();
        Assert.assertThrows(IllegalStateException.class, () -> list.removeIf(value -> {
            int i = position.getAndIncrement();
            if (i == 2) throw new IllegalStateException("predicate");
            return i == 1;
        }));
        Assert.assertEquals(2, list.size());
        Assert.assertEquals(1, events.size());
        Assert.assertArrayEquals(new int[]{1}, events.get(0).indices());
    }
}
