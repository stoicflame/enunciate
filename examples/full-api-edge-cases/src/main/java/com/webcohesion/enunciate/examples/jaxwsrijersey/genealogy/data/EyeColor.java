package com.webcohesion.enunciate.examples.jaxwsrijersey.genealogy.data;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Ryan Heaton
 */
@XmlJavaTypeAdapter(EyeColorAdapter.class)
public enum EyeColor {

  brown,

  hazel,

  blue
}
