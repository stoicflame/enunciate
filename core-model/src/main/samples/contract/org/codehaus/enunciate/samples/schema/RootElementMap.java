package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
@XmlJavaTypeAdapter( RootElementMapAdapter.class )
public class RootElementMap extends HashMap<String, Object> {

}
