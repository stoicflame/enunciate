package org.codehaus.enunciate.util;

import net.sf.jelly.apt.decorations.JavaDoc;

/**
 * @author Ryan Heaton
 */
public class Group implements Comparable<Group> {

  private final String name;
  private final JavaDoc javaDoc;

  public Group(String name) {
    this(name, null);
  }

  public Group(String name, JavaDoc javaDoc) {
    this.name = name;
    this.javaDoc = javaDoc;
  }

  public String getName() {
    return name;
  }

  public String getDocumentation() {
    return this.javaDoc == null ? null : this.javaDoc.toString();
  }

  public JavaDoc getJavaDoc() {
    return javaDoc;
  }

  public int compareTo(Group o) {
    return this.name.compareTo(o.name);
  }

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (o == null || getClass() != o.getClass()) {
      return false;
    }

    Group group = (Group) o;
    return name.equals(group.name);

  }

  @Override
  public int hashCode() {
    return name.hashCode();
  }
}
