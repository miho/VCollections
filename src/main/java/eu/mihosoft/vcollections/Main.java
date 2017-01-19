/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import javax.observer.Subscription;

public class Main {
    public static void main(String[] args) {
        // creates an ordinary list
        List<Integer> list = new ArrayList<>();

        // to make the list observable, we wrap it in a VList
        VList<Integer> vList = VList.newInstance(list);

        // to get notified, we add a change listener to the VList
        Subscription subscription = vList.addChangeListener((evt) -> {
            // for now, we just print the changes
            System.out.println(EventUtil.toStringWithDetails(evt));
        });

        // add individual elements (generates 3 events)
        System.out.println(">> add 3 individual elements");
        vList.add(1);
        vList.add(2);
        vList.add(3);

        // add collection of elements (generates only one event)
        System.out.println(">> add one collection of elements");
        vList.addAll(Arrays.asList(4, 5, 6));

        // remove individual elements (generates 3 events)
        System.out.println(">> remove 3 individual elements");
        vList.remove((Integer) 1);
        vList.remove((Integer) 2);
        vList.remove((Integer) 3);

        // remove collection of elements (generates only one event)
        System.out.println(">> remove one collection of elements");
        vList.removeAll(Arrays.asList(4,5,6));
        
        // unsubscribe l from vList
        subscription.unsubscribe();
        
        //add elements without generating events
        System.out.println(">> add one collection of elements without notification");
        vList.addAll(Arrays.asList(4, 5, 6));
    }
}