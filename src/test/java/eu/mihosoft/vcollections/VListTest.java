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

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.ListIterator;
import java.util.Random;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

/**
 * Created by miho on 16.01.2017.
 */
public class VListTest {

    @Test
    public void wrapListEqualsTest() {
        List<Integer> aList = new ArrayList<>();
        addRandomInts(10, aList);

        VList<Integer> vList = VList.newInstance(aList);

        // wrapped list must equal to plain list
        Assert.assertEquals(aList, vList);

        // after modifying one, they must still be equal
        addRandomInts(3, vList);

        // wrapped list must equal to plain list
        Assert.assertEquals(aList, vList);

        // after removing elements, they must still be equal
        vList.remove(3);

        // wrapped list must equal to plain list
        Assert.assertEquals(aList, vList);

    }

    @Test
    public void changeOnAddNotificationTest() {
        for (int i = 0; i < 100; i++) {
            int size = Math.max(1, getRandom().nextInt(100));

            createAddElementsToTheEndTest(size);
            createAddElementsAtRandomPositionTest(size);

            int numberOfElementsToAdd = Math.max(1, getRandom().nextInt(100));
            createAddCollectionToListTest(size, numberOfElementsToAdd);
        }
    }

    private void createAddCollectionToListTest(
            int listSize, int numberOfElementsToAdd) {

        // creates a list with listSize random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(listSize, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasAdded()) {
                changes.add(evt.added());
            }
        });

        // determine elements that shall be added
        List<Integer> elementsToAdd = new ArrayList<>();
        addRandomInts(numberOfElementsToAdd, elementsToAdd);

        // add the elements
        vList.addAll(elementsToAdd);

        // changes must contain n2 added elements in one change object that equals
        // the elementsToAdd list
        Assert.assertTrue("change list must contain one element",
                changes.size() == 1);
        Assert.assertTrue("change object must contain n2 elements",
                changes.get(0).elements().size() == numberOfElementsToAdd);
        Assert.assertEquals(elementsToAdd, changes.get(0).elements());

        // index array must match indices of added elements
        int[] indices = IntStream.range(listSize,
                listSize + numberOfElementsToAdd).toArray();
        Assert.assertArrayEquals(indices, changes.get(0).indices());
    }

    private void createAddElementsAtRandomPositionTest(int size) {
        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasAdded()) {
                changes.add(evt.added());
            }
        });

        // add one element at a random position
        Integer intElemToAddAtPos1 = getRandom().nextInt();
        int pos1 = getRandom().nextInt(vList.size());
        vList.add(pos1, intElemToAddAtPos1);
        Assert.assertTrue("We expected one change.", changes.size() == 1);
        Assert.assertEquals(changes.get(0).indices()[0], pos1);
    }

    private void createAddElementsToTheEndTest(int size) {
        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasAdded()) {
                changes.add(evt.added());
            }
        });

        // add two random elements to the end of the list
        Integer intElemToAdd1 = getRandom().nextInt();
        Integer intElemToAdd2 = getRandom().nextInt();
        vList.add(intElemToAdd1);
        vList.add(intElemToAdd2);
        Assert.assertTrue("We expected two changes.", changes.size() == 2);
        Assert.assertEquals(intElemToAdd1, changes.get(0).elements().get(0));
        Assert.assertEquals(intElemToAdd2, changes.get(1).elements().get(0));
        Assert.assertEquals(size + 0, changes.get(0).indices()[0]);
        Assert.assertEquals(size + 1, changes.get(1).indices()[0]);
    }

    @Test
    public void changeOnRemoveNotificationTest() {
        for (int i = 0; i < 100; i++) {

            int size = Math.max(1, getRandom().nextInt(100));

            createRemoveElementFromListTest(size);
            createRemoveElementFromSpecificLocationTest(size);

            int numberOfElementsToRemove
                    = Math.max(1, getRandom().nextInt(100));
            createRemoveMultipleElementsFromListTest(
                    size, numberOfElementsToRemove);
        }
    }

    @Test
    public void changeOnRetainNotificationTest() {
        for (int i = 0; i < 100; i++) {
            int size = Math.max(2, getRandom().nextInt(100));
            createRetainInListTest(size);
        }
    }

    private void createRemoveMultipleElementsFromListTest(int size,
            int numberOfElementsToRemove) {

        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasRemoved()) {
                changes.add(evt.removed());
            }
        });

        //remove multiple elements
        List<Integer> elementsToRemove = new ArrayList<>();
        addRandomInts(numberOfElementsToRemove, elementsToRemove);
        // add the elements that will later be removed to the original list
        // pro prevent unwanted event generation
        aList.addAll(elementsToRemove);

        // remove the elements
        vList.removeAll(elementsToRemove);

        // changes must contain elementsToRemove added elements in one change
        // the elementsToAdd list object that equals
        Assert.assertTrue("change list must contain one element",
                changes.size() == 1);

        // test whether the reported elements are equal to the removed elements
        Assert.assertEquals(elementsToRemove, changes.get(0).elements());

        // index array must match indices of added elements
        int[] indices = IntStream.range(size,
                size + numberOfElementsToRemove).toArray();
        Assert.assertArrayEquals(indices, changes.get(0).indices());
    }

    private void createRemoveElementFromSpecificLocationTest(int size) {
        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'remove' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasRemoved()) {
                changes.add(evt.removed());
            }
        });

        // determine position of element to remove
        int posToRemove2 = getRandom().nextInt(vList.size());
        // get element to remove
        Integer intElemToRemove2 = vList.get(posToRemove2);
        // remove element
        vList.remove(posToRemove2);

        // check that the remove command was reported
        Assert.assertTrue("We expected one change, got: "
                + changes.size(), changes.size() == 1);
        // check that the reported index matches the actual index
        Assert.assertEquals(changes.get(0).indices()[0], posToRemove2);
        // check that the reported element matches the actual element
        Assert.assertEquals(intElemToRemove2, changes.get(0).elements().get(0));
    }

    private void createRemoveElementFromListTest(int size) {

        // initialize vlist
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'remove' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            changes.add(evt.removed());
        });

        // determin position of element to remove
        int posToRemove1 = getRandom().nextInt(vList.size());
        // get element to remove
        Integer intElemToRemove1 = vList.get(posToRemove1);
        // remove element
        vList.remove(intElemToRemove1);

        // test whether change was reported
        Assert.assertTrue("We expected one changes, got: "
                + changes.size(), changes.size() == 1);
        // check that the reported element equals the element that has been
        // removed
        Assert.assertEquals(intElemToRemove1, changes.get(0).elements().get(0));
        // check that the reported index is equal to the actual index
        Assert.assertEquals(posToRemove1, changes.get(0).indices()[0]);
    }

    private void createRetainInListTest(int size) {

        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasRemoved()) {
                changes.add(evt.removed());
            }
        });

        // finding indices of retained elements
        int firstIndex = getRandom().nextInt(size - 1);
        int secondIndex = 0;
        while (secondIndex <= firstIndex) {
            secondIndex = getRandom().nextInt(size);
        }
        int secondIndexFinal = secondIndex;

        // copy elements to retain
        List<Integer> elementsToRetain
                = new ArrayList<>(vList.subList(firstIndex, secondIndex));
        vList.retainAll(elementsToRetain);

        // test whether list only contains retained elements
        Assert.assertEquals(elementsToRetain, vList);

        // compute expected indices
        int[] removedIndices = IntStream.range(0, size).filter(
                i -> i < firstIndex || i >= secondIndexFinal).toArray();

        // test whether reported index array equals the expected indices
        Assert.assertArrayEquals(removedIndices, changes.get(0).indices());
    }

    @Test
    public void setElementTest() {
        for (int i = 0; i < 100; i++) {
            int size = Math.max(1, getRandom().nextInt(100));
            createSetElementTest(size);
        }
    }

    private void createSetElementTest(int size) {
        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' and remove events
        List<VListChange<Integer>> added = new ArrayList<>();
        List<VListChange<Integer>> removed = new ArrayList<>();
        vList.addChangeListener(evt -> {
            added.add(evt.added());
            removed.add(evt.removed());
        });

        // index where to set the element
        int index = getRandom().nextInt(size);
        // element to set
        Integer element = getRandom().nextInt();
        Integer prevElement = vList.get(index);

        vList.set(index, element);

        // check that the remove command was reported
        Assert.assertTrue("We expected one change, got: "
                + removed.size(), removed.size() == 1);
        // check that the add command was reported
        Assert.assertTrue("We expected one change, got: "
                + added.size(), added.size() == 1);
        // check whether the removed element matches the reported element
        Assert.assertEquals(prevElement, removed.get(0).elements().get(0));
        // check whether the index of the removed element matches the reported
        // index
        Assert.assertEquals(index, removed.get(0).indices()[0]);
        // check whether the removed element matches the reported element
        Assert.assertEquals(element, added.get(0).elements().get(0));
        // check whether the index of the removed element matches the reported
        // index
        Assert.assertEquals(index, added.get(0).indices()[0]);
    }

    @Test
    public void iteratorTest() {
        for (int i = 0; i < 100; i++) {
            int size = Math.max(1, getRandom().nextInt(100));
            createIteratorAddTest(size);
            createIteratorRemoveTest(size);
            createIteratorSetTest(size);
        }
    }

    private void createIteratorAddTest(int size) {
        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasAdded()) {
                changes.add(evt.added());
            }
        });

        // element to add
        Integer element = getRandom().nextInt();
        // index where to add the element
        int index = getRandom().nextInt(size);

        ListIterator<Integer> iterator = vList.listIterator();

        int counter = 0;
        while (iterator.hasNext()) {
            if (counter == index) {
                iterator.add(element);
            }
            iterator.next();
            counter++;
        }

        // check that the add command was reported
        Assert.assertTrue("We expected one change, got: "
                + changes.size(), changes.size() == 1);
        // check whether the added element matches the reported element
        Assert.assertEquals(element, changes.get(0).elements().get(0));
        // check whether the index of the added element matches the reported
        // index
        Assert.assertEquals(index, changes.get(0).indices()[0]);
    }

    private void createIteratorRemoveTest(int size) {
        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'remove' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addChangeListener(evt -> {
            if (evt.wasRemoved()) {
                changes.add(evt.removed());
            }
        });

        // index where to remove the element
        int index = getRandom().nextInt(size);
        // element to remove
        Integer element = vList.get(index);

        ListIterator<Integer> iterator = vList.listIterator();

        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next();
            if (counter == index) {
                iterator.remove();
            }
            counter++;
        }

        // check that the remove command was reported
        Assert.assertTrue("We expected one change, got: "
                + changes.size(), changes.size() == 1);
        // check whether the removed element matches the reported element
        Assert.assertEquals(element, changes.get(0).elements().get(0));
        // check whether the index of the removed element matches the reported
        // index
        Assert.assertEquals(index, changes.get(0).indices()[0]);
    }

    private void createIteratorSetTest(int size) {
        // creates a list with size random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' and remove events
        List<VListChange<Integer>> added = new ArrayList<>();
        List<VListChange<Integer>> removed = new ArrayList<>();
        vList.addChangeListener(evt -> {
            added.add(evt.added());
            removed.add(evt.removed());
        });

        // index where to set the element
        int index = getRandom().nextInt(size);
        // element to set
        Integer element = getRandom().nextInt();
        Integer prevElement = vList.get(index);

        ListIterator<Integer> iterator = vList.listIterator();

        int counter = 0;
        while (iterator.hasNext()) {
            iterator.next();
            if (counter == index) {
                iterator.set(element);
            }
            counter++;
        }
        // check that the remove command was reported
        Assert.assertTrue("We expected one change, got: "
                + removed.size(), removed.size() == 1);
        // check that the add command was reported
        Assert.assertTrue("We expected one change, got: "
                + added.size(), added.size() == 1);
        // check whether the removed element matches the reported element
        Assert.assertEquals(prevElement, removed.get(0).elements().get(0));
        // check whether the index of the removed element matches the reported
        // index
        Assert.assertEquals(index, removed.get(0).indices()[0]);
        // check whether the removed element matches the reported element
        Assert.assertEquals(element, added.get(0).elements().get(0));
        // check whether the index of the removed element matches the reported
        // index
        Assert.assertEquals(index, added.get(0).indices()[0]);
    }

    private static long seed = 0;
    private static Random random = null;

    public static long getSeed() {
        if (seed == 0) {
            seed = System.currentTimeMillis();
            System.out.println(">> Performing tests with seed " + seed);
        }

        return seed;
    }

    public static Random getRandom() {
        if (random == null) {
            System.out.println(">> Initializing Random Generator");
            random = new Random(getSeed());
        }

        return random;
    }

    private void addRandomInts(int length, List<Integer> list) {
        list.addAll(getRandom().ints(length).boxed().
                collect(Collectors.toList()));
    }

    private void addRandomDoubles(int length, List<Double> list) {
        list.addAll(getRandom().doubles(length).boxed().
                collect(Collectors.toList()));
    }
}
