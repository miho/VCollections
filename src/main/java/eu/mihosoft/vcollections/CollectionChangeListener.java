/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package eu.mihosoft.vcollections;

import java.util.Collection;

/**
 *
 * @author miho
 */
public interface CollectionChangeListener<T,
        CCE extends CollectionChangeEvent<T, CO, CC>,
        CO  extends Collection<T>,
        CC  extends CollectionChange<T>> {
    
    /**
     * This method is called whenever the observed list changes.
     *
     * @param evt change event that contains detailed information on which
     * elements have been changed
     */

    void onChange(CCE evt);
}
