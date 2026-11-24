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

    @Test
    public void writableViewsPublishStableChanges() {
        VMap<String, Integer> map = VMap.newInstance(new java.util.LinkedHashMap<>());
        VMap<String, Integer> readOnly = map.asUnmodifiable();
        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        readOnly.addChangeListener(e -> events.add((VMapChangeEvent<String, Integer>) e));
        map.put("a", null);
        map.put("b", 2);
        Assert.assertNull(map.entrySet().iterator().next().setValue(1));
        map.replaceAll((k, v) -> v + 10);
        map.values().remove(11);
        map.keySet().remove("b");
        Assert.assertTrue(map.isEmpty());
        Assert.assertEquals(7, events.size());
        Assert.assertSame(readOnly, events.get(2).source());
        Assert.assertTrue(events.get(2).removed().entries().containsKey("a"));
        Assert.assertNull(events.get(2).removed().entries().get("a"));
        Assert.assertEquals(Integer.valueOf(11), events.get(5).removed().entries().get("a"));
        Assert.assertThrows(UnsupportedOperationException.class, () -> events.get(2).added().entries().clear());
        map.put("x", 1);
        Assert.assertThrows(UnsupportedOperationException.class,
                () -> events.get(2).source().entrySet().iterator().next().setValue(42));
    }

    @Test
    public void reportsCommittedPrefixAndContinuesAfterListenerFailure() {
        Map<String, Integer> backing = new HashMap<String, Integer>() {
            @Override
            public void putAll(Map<? extends String, ? extends Integer> values) {
                put("a", values.get("a"));
                throw new IllegalArgumentException("second key rejected");
            }
        };
        VMap<String, Integer> map = VMap.newInstance(backing);
        map.addChangeListener(e -> { throw new IllegalStateException("listener"); });
        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(events::add);
        Map<String, Integer> changes = new java.util.LinkedHashMap<>();
        changes.put("a", 1);
        changes.put("b", 2);
        Throwable failure = Assert.assertThrows(IllegalArgumentException.class, () -> map.putAll(changes));
        Assert.assertEquals(1, failure.getSuppressed().length);
        Assert.assertEquals(java.util.Collections.singletonMap("a", 1), events.get(0).added().entries());
    }

    @Test
    public void detachedEntriesCannotPublishChangesToTheLiveMap() {
        VMap<String, Integer> map = VMap.newInstance(new java.util.LinkedHashMap<>());
        map.put("key", 1);
        Map.Entry<String, Integer> detached = map.entrySet().iterator().next();
        map.remove("key");
        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        map.addChangeListener(events::add);
        detached.setValue(2);
        Assert.assertTrue(map.isEmpty());
        map.put("key", 2);
        events.clear();
        detached.setValue(3);
        Assert.assertEquals(Integer.valueOf(2), map.get("key"));
        Assert.assertTrue(events.isEmpty());
    }

    @Test
    public void bulkUpdatesPreserveIdentityAndComparatorSemantics() {
        String first = new String("key");
        String second = new String("key");
        Map<String, Integer> input = new java.util.IdentityHashMap<>();
        input.put(first, 1);
        input.put(second, 2);
        VMap<String, Integer> identity = VMap.newInstance(new java.util.IdentityHashMap<>());
        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        identity.addChangeListener(events::add);
        identity.putAll(input);
        Assert.assertEquals(2, identity.size());
        Map<String, Integer> added = events.get(0).added().entries();
        Assert.assertEquals(2, added.size());
        Assert.assertEquals(Integer.valueOf(1), added.get(first));
        Assert.assertEquals(Integer.valueOf(2), added.get(second));
        Assert.assertFalse(added.containsKey(new String("key")));
        identity.removeAll(first, second);
        Assert.assertEquals(2, events.get(1).removed().entries().size());

        events.clear();
        VMap<String, Integer> insensitive = VMap.newInstance(new java.util.TreeMap<>(String.CASE_INSENSITIVE_ORDER));
        insensitive.put("EXISTING", 1);
        insensitive.addChangeListener(events::add);
        Map<String, Integer> aliases = new java.util.LinkedHashMap<>();
        aliases.put("existing", 2);
        aliases.put("KEY", 3);
        aliases.put("key", 4);
        insensitive.putAll(aliases);
        Assert.assertEquals(2, insensitive.size());
        Assert.assertEquals(2, events.get(0).added().entries().size());
        Assert.assertEquals(Integer.valueOf(4), events.get(0).added().entries().get("Key"));
        Assert.assertEquals(Integer.valueOf(1), events.get(0).removed().entries().get("Existing"));
    }

    @Test
    public void partiallyFailedRemovalsPublishOnlyCommittedMappings() {
        List<VMapChangeEvent<String, Integer>> events = new ArrayList<>();
        VMap<String, Integer> sorted = VMap.newInstance(new java.util.TreeMap<>());
        sorted.put("a", 1);
        sorted.put("b", 2);
        sorted.addChangeListener(events::add);
        Assert.assertThrows(NullPointerException.class, () -> sorted.removeAll("a", null));
        Assert.assertEquals(java.util.Collections.singletonMap("a", 1), events.get(0).removed().entries());
        Assert.assertFalse(sorted.removeAll());
        Assert.assertEquals(1, events.size());

        events.clear();
        VMap<String, Integer> partialClear = VMap.newInstance(new HashMap<String, Integer>() {
            @Override public void clear() {
                remove("a");
                throw new UnsupportedOperationException("b is protected");
            }
        });
        partialClear.put("a", 1);
        partialClear.put("b", 2);
        partialClear.addChangeListener(events::add);
        Assert.assertThrows(UnsupportedOperationException.class, partialClear::clear);
        Assert.assertEquals(java.util.Collections.singletonMap("b", 2), partialClear);
        Assert.assertEquals(java.util.Collections.singletonMap("a", 1), events.get(0).removed().entries());
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
