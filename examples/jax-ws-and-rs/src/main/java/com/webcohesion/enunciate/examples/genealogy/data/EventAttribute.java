package com.webcohesion.enunciate.examples.genealogy.data;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ryan Heaton
 */
@XmlTransient
public interface EventAttribute {

  String getName();

  String getValue();
}
