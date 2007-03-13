package org.codehaus.enunciate.util;

import com.sun.mirror.declaration.ClassDeclaration;

import java.util.Comparator;

/**
 * A comparator for instances of class declaration, comparing by fqn.
 *
 * @author Ryan Heaton
 */
public class ClassDeclarationComparator implements Comparator<ClassDeclaration> {

  public int compare(ClassDeclaration class1, ClassDeclaration class2) {
    return class1.getQualifiedName().compareTo(class2.getQualifiedName());
  }
}
