/*
 * Copyright 2017-2019 Michael Hoffer <info@michaelhoffer.de>. All rights reserved.
 */
package eu.mihosoft.vcollections;

/**
 * Map change listener. This listener is called whenever a map change occurs.
 */
@FunctionalInterface
public interface VMapChangeListener<K, V> {
    void onChange(VMapChangeEvent<K, V> evt);
}
