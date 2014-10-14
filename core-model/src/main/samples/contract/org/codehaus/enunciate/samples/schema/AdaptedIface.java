package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * @author Ryan Heaton
 */
@XmlJavaTypeAdapter( IfaceAdapter.class )
public interface AdaptedIface {

  String getMember();

}
