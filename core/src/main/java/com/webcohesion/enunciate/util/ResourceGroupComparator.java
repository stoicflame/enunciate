package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.api.resources.ResourceGroup;

import java.util.Comparator;

/**
 * @author Ryan Heaton
 */
public class ResourceGroupComparator implements Comparator<ResourceGroup> {

  private final Comparator<String> pathComparator;

  public ResourceGroupComparator(PathSortStrategy strategy) {
    if (strategy == PathSortStrategy.breadth_first) {
      pathComparator = new BreadthFirstResourcePathComparator();
    }
    else {
      pathComparator = new DepthFirstResourcePathComparator();
    }
  }

  @Override
  public int compare(ResourceGroup g1, ResourceGroup g2) {
    return pathComparator.compare(g1.getSortKey(), g2.getSortKey());
  }
}
