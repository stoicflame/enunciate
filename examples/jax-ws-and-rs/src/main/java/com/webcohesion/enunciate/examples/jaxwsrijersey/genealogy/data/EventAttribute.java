package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ryan Heaton
 */
@XmlTransient
public interface EventAttribute {

  String getName();

  String getValue();
}
