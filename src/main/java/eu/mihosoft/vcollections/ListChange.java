/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.List;

/**
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> element type of the collection
 */
interface ListChange<T> extends CollectionChange<T>{

    /**
     * @return the indices of the changed elements
     */
    int[] indices();

    /**
     * @return changed elements
     */
    @Override
    List<T> elements();
}
