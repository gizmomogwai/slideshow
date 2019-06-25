package com.flopcode.slideshow;

import java.util.Iterator;

public class With {
    public static <T> Iterable<Index<T>> index(Iterable<T> of) {
        return () -> {
            Iterator<T> i = of.iterator();
            return new Iterator<Index<T>>() {
                int index = 0;

                public boolean hasNext() {
                    return i.hasNext();
                }

                public Index<T> next() {
                    return new Index<>(i.next(), index++);
                }
            };
        };
    }

    static class Index<T> {
        T value;
        int index;

        Index(T value, int index) {
            this.value = value;
            this.index = index;
        }
    }
}
