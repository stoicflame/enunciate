package com.webcohesion.enunciate.util;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class SortedList<T> extends ArrayList<T> {
    private Comparator<T> comparator;

    public SortedList(Comparator<T> comparator) {
        this.comparator = comparator;
    }

    @Override
    public boolean add(T t) {
        int index = Collections.binarySearch(this, t, comparator);
        if (index<0) {
            index = -index - 1;
        }
        if (index>=size()) {
            super.add(t);
        } else {
            super.add(index, t);
        }
        return true;
    }
}

