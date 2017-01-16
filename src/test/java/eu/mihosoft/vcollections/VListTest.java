package eu.mihosoft.vcollections;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
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

        VList vList = VList.newInstance(aList);

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
        for(int i = 0; i < 100;i++) {

            int size = Math.max(1,new Random().nextInt(100));

            createAddElementsToTheEndTest(size);
            createAddElementsAtRandomPositionTest(size);

            int numberOfElementsToAdd = Math.max(1,new Random().nextInt(100));
            createAddCollectionToListTest(size,numberOfElementsToAdd);
        }
    }


    private void createAddCollectionToListTest(int listSize, int numberOfElementsToAdd) {

        // creates a list with n1 random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(listSize, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addListChangeListener(evt -> {
            if (evt.wasAdded()) {
                changes.add(evt.getAdded());
            }
        });


        List<Integer> elementsToAdd = new ArrayList<>();
        addRandomInts(numberOfElementsToAdd, elementsToAdd);

        vList.addAll(elementsToAdd);

        // changes must contain n2 added elements in one change object that equals
        // the elementsToAdd list
        Assert.assertTrue("change list must contain one element",
                changes.size() == 1);
        Assert.assertTrue("change object must contain n2 elements",
                changes.get(0).elements().size() == numberOfElementsToAdd);
        Assert.assertEquals(elementsToAdd, changes.get(0).elements());

        // index array must match indices of added elements
        int[] indices = IntStream.range(listSize, listSize + numberOfElementsToAdd).toArray();
        Assert.assertArrayEquals(indices, changes.get(0).indices());
    }

    private void createAddElementsAtRandomPositionTest(int size) {
        // creates a list with n1 random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addListChangeListener(evt -> {
            if (evt.wasAdded()) {
                changes.add(evt.getAdded());
            }
        });


        // add one element at a random position
        Integer intElemToAddAtPos1 = new Random().nextInt();
        int pos1 = new Random().nextInt(vList.size());
        vList.add(pos1, intElemToAddAtPos1);
        Assert.assertTrue("We expected one change.", changes.size() == 1);
        Assert.assertEquals(changes.get(0).indices()[0], pos1);
    }

    private void createAddElementsToTheEndTest(int size) {
        // creates a list with n1 random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addListChangeListener(evt -> {
            if (evt.wasAdded()) {
                changes.add(evt.getAdded());
            }
        });

        // add two random elements to the end of the list
        Integer intElemToAdd1 = new Random().nextInt();
        Integer intElemToAdd2 = new Random().nextInt();
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
        for(int i = 0; i < 100;i++) {

            int size = Math.max(1,new Random().nextInt(100));

            createRemoveElementFromListTest(size);
            createRemoveElementFromSpecificLocationTest(size);

            int numberOfElementsToRemove = Math.max(1,new Random().nextInt(100));
            createRemoveMultipleElementsFromListTest(size, numberOfElementsToRemove);
        }
    }

    @Test
    public void changeOnRetainNotificationTest() {
        for(int i = 0; i < 100;i++) {
            int size = Math.max(2,new Random().nextInt(100));
            int numberOfElementsToRetain = Math.max(1,size);
            createRetainInListTest(size,numberOfElementsToRetain);
        }
    }

    private void createRemoveMultipleElementsFromListTest(int size, int n2) {

        // creates a list with n1 random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addListChangeListener(evt -> {
            if (evt.wasRemoved()) {
                changes.add(evt.getRemoved());
            }
        });


        //remove multiple elements

        List<Integer> elementsToRemove = new ArrayList<>();
        addRandomInts(n2, elementsToRemove);
        // add the elements that will later be removed to the original list
        // pro prevent unwanted event generation
        aList.addAll(elementsToRemove);

        vList.removeAll(elementsToRemove);

        // changes must contain n2 added elements in one change object that equals
        // the elementsToAdd list
        Assert.assertTrue("change list must contain one element",
                changes.size() == 1);
        Assert.assertTrue("change object must contain n2 elements, only contains: " +
                        changes.get(0).elements().size(),
                changes.get(0).elements().size() == n2);
        Assert.assertEquals(elementsToRemove, changes.get(0).elements());

        // index array must match indices of added elements
        int[] indices = IntStream.range(size, size + n2).toArray();
        Assert.assertArrayEquals(indices, changes.get(0).indices());
    }

    private void createRemoveElementFromSpecificLocationTest(int n1) {
        // creates a list with n1 random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(n1, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addListChangeListener(evt -> {
            if (evt.wasRemoved()) {
                changes.add(evt.getRemoved());
            }
        });

        // remove from specific position
        int posToRemove2 = new Random().nextInt(vList.size());
        Integer intElemToRemove2 = vList.get(posToRemove2);
        vList.remove(posToRemove2);
        Assert.assertTrue("We expected one change, got: " + changes.size(), changes.size() == 1);
        Assert.assertEquals(changes.get(0).indices()[0], posToRemove2);
        Assert.assertEquals(intElemToRemove2, changes.get(0).elements().get(0));
    }

    private void createRemoveElementFromListTest(int size) {
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        VList<Integer> vList = VList.newInstance(aList);

        List<VListChange<Integer>> changes = new ArrayList<>();

        vList.addListChangeListener(evt -> {
            changes.add(evt.getRemoved());
        });

        int posToRemove1 = new Random().nextInt(vList.size());

        Integer intElemToRemove1 = vList.get(posToRemove1);
        vList.remove(intElemToRemove1);
        Assert.assertTrue("We expected one changes, got: " + changes.size(), changes.size() == 1);
        Assert.assertEquals(intElemToRemove1, changes.get(0).elements().get(0));
        Assert.assertEquals(posToRemove1, changes.get(0).indices()[0]);
    }


    private void createRetainInListTest(int size, int numberOfElementsToRetain) {

        // creates a list with n1 random integers
        List<Integer> aList = new ArrayList<>();
        addRandomInts(size, aList);

        // wrap this list in an observable vlist
        VList<Integer> vList = VList.newInstance(aList);

        // record all 'add' events
        List<VListChange<Integer>> changes = new ArrayList<>();
        vList.addListChangeListener(evt -> {
            if (evt.wasRemoved()) {
                changes.add(evt.getRemoved());
            }
        });

        int firstIndex = new Random().nextInt(size-1);
        int secondIndex= 0;

        while(secondIndex <= firstIndex) {
            secondIndex = new Random().nextInt(size);
        }
        int secondIndexFinal = secondIndex;

        List<Integer> elementsToRetain = new ArrayList(vList.subList(firstIndex,secondIndex));
        vList.retainAll(elementsToRetain);

        Assert.assertEquals(vList.size(),elementsToRetain.size());
        Assert.assertEquals(size-elementsToRetain.size(), changes.get(0).elements().size());
        Assert.assertEquals(elementsToRetain,vList);
        int[] removedIndices = IntStream.range(0,size).filter(i->i<firstIndex || i >= secondIndexFinal).toArray();

        Assert.assertArrayEquals(removedIndices, changes.get(0).indices());
    }


    private void addRandomInts(int length, List<Integer> list) {
        list.addAll(new Random().ints(length).boxed().
                collect(Collectors.toList()));
    }

    private void addRandomDoubles(int length, List<Double> list) {
        list.addAll(new Random().doubles(length).boxed().
                collect(Collectors.toList()));
    }
}
