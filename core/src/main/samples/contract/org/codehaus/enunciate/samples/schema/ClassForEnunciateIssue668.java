package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlRootElement;
import java.util.HashMap;

/**
 * @author Ryan Heaton
 */
@XmlRootElement
public class ClassForEnunciateIssue668 {

  public String member2;

  @XmlRootElement
  public static final class StaticInner {

    public String member1;
  }

  public static final class ClassThatExtendsHashMap extends HashMap {

  }
}
