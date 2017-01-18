/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections.playground;

import java.util.List;

/**
 * 
 * @author Michael Hoffer <info@michaelhoffer.de>
 * @param <T> 
 */
interface CollectionChange<T> {
    List<T> elements();
}
