package javax.observer.collection;

import java.util.Set;

/**
 * TODO
 * @author Hendrik Ebbers
 */
public interface ObservableSet<E> extends Set<E>, ObservableCollectionOriginal<E, ObservableSet<E>, SetChange<E>> {

}
