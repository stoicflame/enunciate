package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlTransient;

/**
 * @author Ryan Heaton
 */
@XmlTransient
public interface EventAttribute {

  String getName();

  String getValue();
}
