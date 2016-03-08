package com.webcohesion.enunciate.util;

import java.util.Comparator;

/**
 * @author Ryan Heaton
 */
public class DepthFirstResourcePathComparator implements Comparator<String> {

  public int compare(String resource1Path, String resource2Path) {
    String[] path1Segments = resource1Path.split("/");
    String[] path2Segments = resource2Path.split("/");
    int index = 0;
    int comparison = 0;
    while ((index<path1Segments.length || index<path2Segments.length) && comparison==0) {
      if (index>=path1Segments.length || index>=path2Segments.length) {
        comparison = path1Segments.length - path2Segments.length;
      } else {
        String subpath1 = path1Segments[index];
        String subpath2 = path2Segments[index];
        comparison = subpath1.compareTo(subpath2);
        index++;
      }
    }

    return comparison;
  }
}
