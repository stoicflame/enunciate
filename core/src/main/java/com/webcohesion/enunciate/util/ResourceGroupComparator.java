package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.api.PathSummary;
import com.webcohesion.enunciate.api.resources.ResourceGroup;

import java.util.Comparator;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ResourceGroupComparator implements Comparator<ResourceGroup> {

  private final ResourcePathComparator pathComparator = new ResourcePathComparator();

  @Override
  public int compare(ResourceGroup g1, ResourceGroup g2) {
    List<PathSummary> paths1 = g1.getPaths();
    List<PathSummary> paths2 = g2.getPaths();
    String path1 = paths1.isEmpty() ? "" : paths1.get(0).getPath();
    String path2 = paths2.isEmpty() ? "" : paths2.get(0).getPath();
    return pathComparator.compare(path1, path2);
  }
}
