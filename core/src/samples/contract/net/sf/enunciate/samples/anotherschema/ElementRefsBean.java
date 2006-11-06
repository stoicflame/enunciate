package net.sf.enunciate.samples.anotherschema;

import javax.xml.bind.annotation.XmlElementRefs;
import javax.xml.bind.annotation.XmlElementRef;
import java.util.Collection;

/**
 * @author Ryan Heaton
 */
public class ElementRefsBean {

  private Collection<BeanOne> beanOnes;
  private Collection<BeanThree> beanThrees;
  private Collection<Object> foursAndThrees;

  @XmlElementRef
  public Collection<BeanOne> getBeanOnes() {
    return beanOnes;
  }

  public void setBeanOnes(Collection<BeanOne> beanOnes) {
    this.beanOnes = beanOnes;
  }

  @XmlElementRef
  public Collection<BeanThree> getBeanThrees() {
    return beanThrees;
  }

  public void setBeanThrees(Collection<BeanThree> beanThrees) {
    this.beanThrees = beanThrees;
  }

  @XmlElementRefs (
    {@XmlElementRef (
      type=BeanFour.class
    ),
    @XmlElementRef (
      type = BeanThree.class
    )}
  )
  public Collection<Object> getFoursAndThrees() {
    return foursAndThrees;
  }

  public void setFoursAndThrees(Collection<Object> foursAndThrees) {
    this.foursAndThrees = foursAndThrees;
  }
}
