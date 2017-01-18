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
 * @param <T> element type os the collection
 */
public interface CollectionChange<T> {
    List<T> elements();
}
