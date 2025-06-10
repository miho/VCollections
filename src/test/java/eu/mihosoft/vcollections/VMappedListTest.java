package eu.mihosoft.vcollections;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Function;

/**
 * Tests for {@link VMappedList}.
 */
public class VMappedListTest {

    private final Function<Integer, String> fromOrig = i -> "n" + i;
    private final Function<String, Integer> toOrig = s -> Integer.parseInt(s.substring(1));

    @Test
    public void addPropagatesToSourceAndFiresEvent() {
        VList<Integer> src = VList.newInstance(new ArrayList<>());
        VList<String> list = VMappedList.newInstance(src, fromOrig, toOrig);

        List<VListChangeEvent<String>> events = new ArrayList<>();
        list.addChangeListener(e -> events.add((VListChangeEvent<String>) e));

        list.add("n5");

        Assert.assertEquals(Integer.valueOf(5), src.get(0));
        Assert.assertEquals(1, events.size());
        Assert.assertTrue(events.get(0).wasAdded());
        Assert.assertEquals("n5", events.get(0).added().elements().get(0));
    }

    @Test
    public void sourceChangesReflected() {
        VList<Integer> src = VList.newInstance(new ArrayList<>());
        VList<String> list = VMappedList.newInstance(src, fromOrig, toOrig);

        src.add(3);

        Assert.assertEquals("n3", list.get(0));
    }

    @Test
    public void removePropagates() {
        VList<Integer> src = VList.newInstance(new ArrayList<>());
        src.add(7);
        VList<String> list = VMappedList.newInstance(src, fromOrig, toOrig);

        List<VListChangeEvent<String>> events = new ArrayList<>();
        list.addChangeListener(e -> events.add((VListChangeEvent<String>) e));

        String removed = list.remove(0);

        Assert.assertEquals("n7", removed);
        Assert.assertTrue(src.isEmpty());
        Assert.assertEquals(1, events.size());
        Assert.assertTrue(events.get(0).wasRemoved());
        Assert.assertEquals("n7", events.get(0).removed().elements().get(0));
    }

    @Test
    public void eventInfoPropagated() {
        VList<Integer> src = VList.newInstance(new ArrayList<>());
        VList<String> list = VMappedList.newInstance(src, fromOrig, toOrig);
        list.setEventInfo("info");

        List<VListChangeEvent<String>> events = new ArrayList<>();
        list.addChangeListener(e -> events.add((VListChangeEvent<String>) e));
        list.add("n2");

        Assert.assertEquals("info", events.get(0).eventInfo());
    }
}
