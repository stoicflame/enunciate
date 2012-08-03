package org.codehaus.enunciate.samples.genealogy.data;

import javax.xml.bind.annotation.XmlSeeAlso;

/**
 * @author Ryan Heaton
 */
@XmlSeeAlso(EventAttributeImpl.class)
public interface EventAttribute {

  String getName();

  String getValue();
}
