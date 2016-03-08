package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.api.PathSummary;

import java.util.Comparator;

/**
 * @author Ryan Heaton
 */
public class PathSummaryComparator implements Comparator<PathSummary> {

    private final Comparator<String> pathComparator;

    public PathSummaryComparator(PathSortStrategy strategy) {
        if (strategy== PathSortStrategy.breadth_first) {
            pathComparator = new BreadthFirstResourcePathComparator();
        } else {
            pathComparator = new DepthFirstResourcePathComparator();
        }
    }

    @Override
    public int compare(PathSummary p1, PathSummary p2) {
        return pathComparator.compare(p1.getPath(), p2.getPath());
    }
}
