# VCollections
[![Build Status](https://travis-ci.org/miho/VCollections.svg?branch=master)](https://travis-ci.org/miho/VCollections) [![Javadocs](https://www.javadoc.io/badge/eu.mihosoft.vcollections/vcollections.svg?color=blue)](https://www.javadoc.io/doc/eu.mihosoft.vcollections/vcollections)
<a href="https://foojay.io/today/works-with-openjdk">
   <img align="right" 
        src="https://github.com/foojayio/badges/raw/main/works_with_openjdk/Works-with-OpenJDK.png"   
        width="100">
</a>

<br>


Lightweight observable collections (used by VMF and [VRL](http://vrl-studio.mihosoft.eu)).

## Features
- Observable List
- Mapped List (keeps two lists with different element types in sync)
- coming sooner or later: ObservableMap and ObservableSet

## Code Sample

### Observing Changes

```java
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
        
        // unsubscribe the listener from vList
        subscription.unsubscribe();
        
        // add elements without generating events
        System.out.println(">> add one collection of elements without notification");
        vList.addAll(Arrays.asList(4, 5, 6));
    }
}
```

### Observing Unmodifiable Lists

```java
public class Main {
    public static void main(String[] args) {
        // creates an ordinary list
        List<Integer> list = new ArrayList<>();

        // to make the list observable, we wrap it in a VList
        VList<Integer> vList = VList.newInstance(list);
        
        // How to make the list unmodifiable and still listen to changes?
        // THIS DOES NOT WORK: VList.newInstance(Collections.unmodifiableList(list));
        VList<Integer> vListUnmodifiable = vList.asUnmodifiable();

        // to get notified, we add a change listener to the unmodifiable VList
        Subscription subscription = vListUnmodifiable.addChangeListener((evt) -> {
            // for now, we just print the changes
            System.out.println(EventUtil.toStringWithDetails(evt));
        });

        // add element and generate event
        vList.add(1);

        // modifying the unmodifiable list results in RuntimeException...
        vListUnmodifiable.add(37);
        
    }
}
```

## How to Build VCollections

### Requirements

- Java >= 1.8
- Internet connection (dependencies are downloaded automatically)
- IDE: [Gradle](http://www.gradle.org/) Plugin (not necessary for command line usage)

### IDE

Open the `VCollections` [Gradle](http://www.gradle.org/) project in your favourite IDE (tested with NetBeans 8.2) and build it
by calling the `assemble` task.

### Command Line

Navigate to the [Gradle](http://www.gradle.org/) project (e.g., `path/to/VCollections`) and enter the following command

#### Bash (Linux/OS X/Cygwin/other Unix-like shell)

    bash gradlew assemble
    
#### Windows (CMD)

    gradlew assemble
