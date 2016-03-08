package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.EnunciateConfiguration;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.Resource;

import java.util.Comparator;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ResourceComparator implements Comparator<Resource> {

  private final Comparator<String> pathComparator;

  public ResourceComparator(EnunciateConfiguration.PathSortStrategy strategy) {
    if (strategy == EnunciateConfiguration.PathSortStrategy.breadth_first) {
      pathComparator = new BreadthFirstResourcePathComparator();
    }
    else {
      pathComparator = new DepthFirstResourcePathComparator();
    }
  }

  @Override
  public int compare(Resource g1, Resource g2) {
    int compare = pathComparator.compare(g1.getPath(), g2.getPath());
    if (compare == 0) {
      List<? extends Method> m1 = g1.getMethods();
      List<? extends Method> m2 = g2.getMethods();
      if (m1.size() == 1 && m2.size() == 1) {
        compare = (m1.get(0).getHttpMethod().compareTo(m2.get(0).getHttpMethod()));
      }
    }
    return compare;
  }
}
