# VCollections
[![Build Status](https://travis-ci.org/miho/VCollections.svg?branch=master)](https://travis-ci.org/miho/VCollections)[ ![Download](https://api.bintray.com/packages/miho/VCollections/VCollections/images/download.svg) ](https://bintray.com/miho/VCollections/VCollections/_latestVersion)

Lightweight observable collections (used by VMF and [VRL](http://vrl-studio.mihosoft.eu)).

**UPDATE: we might switch to another event API. Expect changes...**

## Features
- Observable List
- Mapped List (keeps two lists with different element types in sync)
- coming soon: ObservableMap and ObservableSet

## Code Sample

```java
public class Main {
    public static void main(String[] args) {
        // creates an ordinary list
        List<Integer> list = new ArrayList<>();

        // to make the list observable, we wrap it in a VList
        VList<Integer> vList = VList.newInstance(list);

        // to get notified, we add a change listener to the VList
        vList.addListChangeListener((evt) -> {
            // for now, we just print the changes
            System.out.println(evt.toStringWithDetails());
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
