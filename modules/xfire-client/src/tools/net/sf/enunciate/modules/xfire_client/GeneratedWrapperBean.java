package net.sf.enunciate.modules.xfire_client;

import javax.xml.namespace.QName;

/**
 * A marker interface for generated wrapper beans (i.e. request wrappers, response wrappers, implicit fault beans).
 * <p/>
 * A generated wrapper bean must also conform to the following conventions in order to be correctly (de)serialized:
 *
 * <ul>
 *   <li>The beans conform to the JAXWS specification for request/response/fault bean wrappers.</li>
 *   <li>If the method has a collection or an array as a parameter, there is a special "addTo<i>Property</i>" method that can be used to add items
 *       to the collection or the array</li>
 *   <li>The supplied metadata contains the property order for each of the request/response beans so the binding can (de)serialize the parameters in
 *       the correct order.</li>
 * </ul>
 *
 * @author Ryan Heaton
 */
public interface GeneratedWrapperBean {

  /**
   * The qname of the wrapper thread.
   *
   * @return The qname of the wrapper thread.
   */
  public QName getWrapperQName();

}
