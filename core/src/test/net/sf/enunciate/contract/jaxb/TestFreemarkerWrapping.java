package net.sf.enunciate.contract.jaxb;

import freemarker.template.TemplateBooleanModel;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModel;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.jelly.apt.freemarker.APTJellyObjectWrapper;
import junit.framework.TestCase;

/**
 * @author Ryan Heaton
 */
public class TestFreemarkerWrapping extends TestCase {

  public void testWrapEnums() throws Exception {
    APTJellyObjectWrapper wrapper = new APTJellyObjectWrapper();
    TemplateModel wrapped = wrapper.wrap(KnownXmlType.STRING);
    assertTrue(wrapped instanceof TemplateHashModel);
    TemplateHashModel hash = ((TemplateHashModel) wrapped);
    assertNotNull(hash.get("anonymous"));

    wrapped = wrapper.wrap(ContentType.COMPLEX);
    assertTrue(wrapped instanceof TemplateHashModel);
    hash = ((TemplateHashModel) wrapped);
    TemplateModel complex = hash.get("complex");
    assertNotNull(complex);
    assertTrue(complex instanceof TemplateBooleanModel);
    assertTrue(((TemplateBooleanModel) complex).getAsBoolean());
  }
}
