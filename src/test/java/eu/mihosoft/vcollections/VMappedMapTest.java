package eu.mihosoft.vcollections;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

/**
 * Tests for {@link VMappedMap}.
 */
public class VMappedMapTest {

    private final Function<Integer, String> kFromOrig = i -> "k" + i;
    private final Function<String, Integer> kToOrig = s -> Integer.parseInt(s.substring(1));
    private final Function<Integer, String> vFromOrig = i -> "v" + i;
    private final Function<String, Integer> vToOrig = s -> Integer.parseInt(s.substring(1));

    @Test
    public void putPropagatesToSourceAndFiresEvent() {
        VMap<Integer, Integer> src = VMap.newInstance(new HashMap<>());
        VMap<String, String> map = VMappedMap.newInstance(src, kFromOrig, kToOrig, vFromOrig, vToOrig);

        List<VMapChangeEvent<String, String>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, String>) e));

        map.put("k1", "v5");

        Assert.assertEquals(Integer.valueOf(5), src.get(1));
        Assert.assertEquals(1, events.size());
        Assert.assertTrue(events.get(0).wasAdded());
        Assert.assertEquals("v5", events.get(0).added().entries().get("k1"));
    }

    @Test
    public void sourceChangesReflected() {
        VMap<Integer, Integer> src = VMap.newInstance(new HashMap<>());
        VMap<String, String> map = VMappedMap.newInstance(src, kFromOrig, kToOrig, vFromOrig, vToOrig);

        src.put(2, 8);

        Assert.assertEquals("v8", map.get("k2"));
    }

    @Test
    public void removePropagates() {
        VMap<Integer, Integer> src = VMap.newInstance(new HashMap<>());
        src.put(3, 9);
        VMap<String, String> map = VMappedMap.newInstance(src, kFromOrig, kToOrig, vFromOrig, vToOrig);

        List<VMapChangeEvent<String, String>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, String>) e));

        String val = map.remove("k3");

        Assert.assertEquals("v9", val);
        Assert.assertFalse(src.containsKey(3));
        Assert.assertEquals(1, events.size());
        Assert.assertTrue(events.get(0).wasRemoved());
        Assert.assertEquals("v9", events.get(0).removed().entries().get("k3"));
    }

    @Test
    public void eventInfoPropagated() {
        VMap<Integer, Integer> src = VMap.newInstance(new HashMap<>());
        VMap<String, String> map = VMappedMap.newInstance(src, kFromOrig, kToOrig, vFromOrig, vToOrig);
        map.setEventInfo("info");

        List<VMapChangeEvent<String, String>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, String>) e));
        map.put("k4", "v1");

        Assert.assertEquals("info", events.get(0).eventInfo());
    }
}
