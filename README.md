# VCollections
[![Build Status](https://travis-ci.org/miho/VCollections.svg?branch=master)](https://travis-ci.org/miho/VCollections)[ ![Download](https://api.bintray.com/packages/miho/VCollections/VCollections/images/download.svg) ](https://bintray.com/miho/VCollections/VCollections/_latestVersion)

Lightweight observable collections (used by VMF and VRL)

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

        // remove collection of elements (generates only one events)
        System.out.println(">> remove 3 individual elements");
        vList.removeAll(Arrays.asList(4,5,6));
    }
}
```
