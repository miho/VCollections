/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections.playground;

import java.util.List;

interface Listener {
    
}

class MyContainer<T extends Listener> {
    
    private List<T> list;

    public <MT extends T> void add(MT e) {
        list.add(e);
    }
}

interface Element {
    
}

/**
 *
 * @author Michael Hoffer <info@michaelhoffer.de>
 */
public class MyTest {
    public static void main(String[] args) {
        
    }
}
