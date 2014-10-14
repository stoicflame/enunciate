package org.codehaus.enunciate.samples.anotherschema;

/**
 * @author Ryan Heaton
 */
public class IdOnlyXmlAdapter extends BaseIdOnlyXmlAdapter<BeanTwo> {

  @Override
  public BeanTwo unmarshal(Long v) throws Exception {
    return new BeanTwo();
  }

  @Override
  public Long marshal(BeanTwo v) throws Exception {
    return Long.MAX_VALUE;
  }
}
