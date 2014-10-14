package org.codehaus.enunciate.samples.schema;

import javax.xml.bind.annotation.XmlElementWrapper;
import javax.xml.bind.annotation.XmlElementRef;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class WrappedElementRefBean {

  private List<BeanThree> list1;
  private List<BeanThree> list2;

  @XmlElementWrapper(name = "list1")
  @XmlElementRef(type=BeanThree.class)
  public List<BeanThree> getList1() {
    return list1;
  }

  public void setList1(List<BeanThree> list1) {
    this.list1 = list1;
  }

  @XmlElementWrapper(name = "list2")
  @XmlElementRef(type=BeanThree.class)
  public List<BeanThree> getList2() {
    return list2;
  }

  public void setList2(List<BeanThree> list2) {
    this.list2 = list2;
  }
}
