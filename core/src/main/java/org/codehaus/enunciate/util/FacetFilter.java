package org.codehaus.enunciate.util;

import org.codehaus.enunciate.contract.Facet;
import org.codehaus.enunciate.contract.HasFacets;

import java.util.*;

/**
 * @author Ryan Heaton
 */
public class FacetFilter {

  private final Set<String> includes;
  private final Set<String> excludes;
  private static final ThreadLocal<FacetFilter> FACET_FILTER = new ThreadLocal<FacetFilter>();

  private FacetFilter(Set<String> includes, Set<String> excludes) {
    this.includes = includes;
    this.excludes = excludes;
  }

  public static void set(Set<String> includes, Set<String> excludes) {
    if ((includes != null && !includes.isEmpty()) || (excludes != null && !excludes.isEmpty())) {
      FACET_FILTER.set(new FacetFilter(includes, excludes));
    }
  }

  public static FacetFilter get() {
    return FACET_FILTER.get();
  }

  public static void clear() {
    FACET_FILTER.remove();
  }

  public static boolean accept(HasFacets item) {
    return FACET_FILTER.get() == null || FACET_FILTER.get().acceptInternal(item);
  }

  protected boolean acceptInternal(HasFacets item) {
    if (item == null) {
      return false;
    }

    if ((includes == null || includes.isEmpty()) && (excludes == null || excludes.isEmpty())) {
      return true;
    }

    boolean accept = true;
    if (includes != null && !includes.isEmpty()) {
      boolean included = false;
      for (Facet facet : item.getFacets()) {
        if (includes.contains(facet.getName())) {
          included = true;
          break;
        }
      }
      accept = included;
    }

    //then remove the items that are explicitly excluded.
    if (excludes != null && !excludes.isEmpty()) {
      for (Facet facet : item.getFacets()) {
        if (excludes.contains(facet.getName())) {
          accept = false;
          break;
        }
      }
    }

    return accept;
  }

  public static <T extends HasFacets> Collection<T> filteredCollection(Collection<T> list) {
    FacetFilter filter = FACET_FILTER.get();
    if (filter == null) {
      return list;
    }

    ArrayList<T> filtered = new ArrayList<T>(list);
    Iterator<T> iterator = filtered.iterator();
    while (iterator.hasNext()) {
      T item = iterator.next();
      if (item != null && !filter.accept(item)) {
        iterator.remove();
      }
    }

    return filtered;
  }

  public static <K,V> Map<K,V> filteredMap(Map<K, V> map) {
    FacetFilter filter = FACET_FILTER.get();
    if (filter == null) {
      return map;
    }

    LinkedHashMap<K, V> filtered = new LinkedHashMap<K, V>(map);
    Iterator<Map.Entry<K, V>> iterator = filtered.entrySet().iterator();
    while (iterator.hasNext()) {
      Map.Entry entry = iterator.next();
      HasFacets item = entry.getKey() instanceof HasFacets ? (HasFacets) entry.getKey() : entry.getValue() instanceof HasFacets ? (HasFacets) entry.getValue() : null;
      if (item != null && !filter.accept(item)) {
        iterator.remove();
      }
    }

    return filtered;
  }

}
