package com.webcohesion.enunciate.util;

import java.util.Comparator;

/**
 * @author Ryan Heaton
 */
public class ResourcePathComparator implements Comparator<String> {

  public int compare(String resource1Path, String resource2Path) {
    String[] path1Segments = resource1Path.split("/");
    String[] path2Segments = resource2Path.split("/");
    int comparison = path1Segments.length - path2Segments.length;
    if (comparison == 0) {
      int index = 0;
      while (path1Segments.length > index && path2Segments.length > index && comparison == 0) {
        String subpath1 = path1Segments[index];
        String subpath2 = path2Segments[index];
        comparison = subpath1.compareTo(subpath2);
        index++;
      }
    }

    return comparison;
  }
}
