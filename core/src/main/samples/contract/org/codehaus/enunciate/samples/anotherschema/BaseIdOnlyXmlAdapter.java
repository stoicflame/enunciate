package org.codehaus.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.adapters.XmlAdapter;

/**
 * @author Ryan Heaton
 */
public abstract class BaseIdOnlyXmlAdapter<T> extends XmlAdapter<Long, T> {

}
