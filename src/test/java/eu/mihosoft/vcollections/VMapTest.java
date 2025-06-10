/*
 * Copyright 2017-2019 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 */
package eu.mihosoft.vcollections;

import org.junit.Assert;
import org.junit.Test;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.stream.Collectors;

/**
 * Tests for {@link VMap}.
 */
public class VMapTest {

    @Test
    public void eventInfoTest() {
        VMap<String, Integer> map = VMap.newInstance(new HashMap<String, Integer>());
        String info = "info";
        map.setEventInfo(info);
        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));
        map.put("a", 1);
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(info, events.get(0).eventInfo());
    }

    @Test
    public void wrapMapEqualsTest() {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(10, base);
        VMap<String, Integer> map = VMap.newInstance(base);

        Assert.assertEquals(base, map);

        // modify map
        addRandomEntries(3, map);
        Assert.assertEquals(base, map);

        String keyToRemove = map.keySet().iterator().next();
        map.remove(keyToRemove);
        Assert.assertEquals(base, map);
    }

    @Test
    public void vMapNotEqualsTest() {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(5, base);
        VMap<String, Integer> m1 = VMap.newInstance(base);
        VMap<String, Integer> m2 = VMap.newInstance(new HashMap<>(base));

        String key = base.keySet().iterator().next();
        m2.put(key, m2.get(key) + 1);
        Assert.assertNotEquals(m1, m2);
    }

    @Test
    public void changeOnPutNotificationTest() {
        for (int i = 0; i < 50; i++) {
            int size = Math.max(1, getRandom().nextInt(20));
            createPutEntryTest(size);
            createPutAllTest(size, Math.max(1, getRandom().nextInt(5)));
            createUpdateEntryTest(size);
        }
    }

    private void createPutEntryTest(int size) {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(size, base);
        VMap<String, Integer> map = VMap.newInstance(base);

        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));

        String k = randomKey();
        int v = getRandom().nextInt();
        map.put(k, v);

        Assert.assertEquals(1, events.size());
        VMapChangeEvent<String, Integer> evt = events.get(0);
        Assert.assertTrue(evt.wasAdded());
        Assert.assertFalse(evt.wasRemoved());
        Assert.assertEquals(v, evt.added().entries().get(k).intValue());
    }

    private void createUpdateEntryTest(int size) {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(size, base);
        VMap<String, Integer> map = VMap.newInstance(base);

        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));

        String k = base.keySet().iterator().next();
        int newVal = getRandom().nextInt();
        int oldVal = base.get(k);
        map.put(k, newVal);

        Assert.assertEquals(1, events.size());
        VMapChangeEvent<String, Integer> evt = events.get(0);
        Assert.assertTrue(evt.wasSet());
        Assert.assertEquals(oldVal, evt.removed().entries().get(k).intValue());
        Assert.assertEquals(newVal, evt.added().entries().get(k).intValue());
    }

    private void createPutAllTest(int baseSize, int addSize) {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(baseSize, base);
        VMap<String, Integer> map = VMap.newInstance(base);

        Map<String, Integer> toAdd = new HashMap<>();
        addRandomEntries(addSize, toAdd);

        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));

        map.putAll(toAdd);

        Assert.assertEquals(1, events.size());
        VMapChangeEvent<String, Integer> evt = events.get(0);
        Assert.assertTrue(evt.wasAdded());
        Assert.assertEquals(toAdd, evt.added().entries());
    }

    @Test
    public void changeOnRemoveNotificationTest() {
        for (int i = 0; i < 50; i++) {
            int size = Math.max(2, getRandom().nextInt(20));
            createRemoveEntryTest(size);
            createRemoveAllEntriesTest(size, Math.max(1, size - 1));
        }
    }

    private void createRemoveEntryTest(int size) {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(size, base);
        VMap<String, Integer> map = VMap.newInstance(base);

        String key = base.keySet().iterator().next();
        int val = base.get(key);

        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));

        map.remove(key);

        Assert.assertEquals(1, events.size());
        VMapChangeEvent<String, Integer> evt = events.get(0);
        Assert.assertTrue(evt.wasRemoved());
        Assert.assertEquals(val, evt.removed().entries().get(key).intValue());
    }

    private void createRemoveAllEntriesTest(int size, int removeCount) {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(size, base);
        VMap<String, Integer> map = VMap.newInstance(base);

        List<String> keys = new ArrayList<>(base.keySet()).subList(0, removeCount);
        Map<String, Integer> expected = keys.stream().collect(Collectors.toMap(k -> k, base::get));

        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));

        map.removeAll(keys.toArray(new String[0]));

        Assert.assertEquals(1, events.size());
        VMapChangeEvent<String, Integer> evt = events.get(0);
        Assert.assertTrue(evt.wasRemoved());
        Assert.assertEquals(expected, evt.removed().entries());
    }

    @Test
    public void clearEventTest() {
        Map<String, Integer> base = new HashMap<>();
        addRandomEntries(5, base);
        VMap<String, Integer> map = VMap.newInstance(base);

        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));

        int sizeBefore = base.size();
        map.clear();
        Assert.assertEquals(1, events.size());
        Assert.assertEquals(sizeBefore, events.get(0).removed().entries().size());
        Assert.assertTrue(map.isEmpty());
    }

    @Test
    public void removeChangeListenerResetsSupport() throws Exception {
        VMap<String, Integer> map = VMap.newInstance(new HashMap<String, Integer>());
        VMapChangeListener<String, Integer> l = evt -> {};
        map.addChangeListener(l);
        map.removeChangeListener(l);

        Field f = map.getClass().getDeclaredField("mapChangeSupport");
        f.setAccessible(true);
        Assert.assertNull(f.get(map));
    }

    @Test
    public void observableOfAggregatesEvents() {
        VMap<String, Integer> m1 = VMap.newInstance(new HashMap<String, Integer>());
        VMap<String, Integer> m2 = VMap.newInstance(new HashMap<String, Integer>());
        VMapObservable<String, Integer> obs = VMapObservable.of(m1, m2);

        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        obs.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));

        m1.put("a", 1);
        m2.put("b", 2);

        Assert.assertEquals(2, events.size());
    }

    private static String randomKey() {
        return "k" + getRandom().nextInt();
    }

    private static void addRandomEntries(int length, Map<String, Integer> map) {
        Random r = getRandom();
        for (int i = 0; i < length; i++) {
            map.put(randomKey(), r.nextInt());
        }
    }

    public static Random getRandom() {
        return VListTest.getRandom();
    }
}
