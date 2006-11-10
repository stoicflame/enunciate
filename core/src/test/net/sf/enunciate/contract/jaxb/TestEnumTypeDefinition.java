package net.sf.enunciate.contract.jaxb;

import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import com.sun.mirror.declaration.EnumDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;

import javax.xml.namespace.QName;
import java.util.Map;
import java.util.HashMap;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestEnumTypeDefinition extends InAPTTestCase {

  /**
   * tests getting the base type and enum values.
   */
  public void testBasic() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    FreemarkerModel.set(model);
    ClassDeclaration decl = (ClassDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.SimpleTypeSimpleContentBean");
    ComplexTypeDefinition type = new ComplexTypeDefinition(decl);
    model.add(type);

    EnumTypeDefinition bean1 = new EnumTypeDefinition((EnumDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.EnumBeanOne"));
    EnumTypeDefinition bean2 = new EnumTypeDefinition((EnumDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.EnumBeanTwo"));
    EnumTypeDefinition bean3 = new EnumTypeDefinition((EnumDeclaration) getDeclaration("net.sf.enunciate.samples.anotherschema.EnumBeanThree"));

    assertEquals(KnownXmlType.STRING.getQname(), bean1.getBaseType().getQname());
    assertEquals(new QName("http://net.sf.enunciate/core/samples/another", "SimpleTypeSimpleContentBean"), bean2.getBaseType().getQname());
    assertEquals(KnownXmlType.INT.getQname(), bean3.getBaseType().getQname());

    Map<String, String> bean1Values = new HashMap<String, String>();
    bean1Values.put("VALUE1", "value1");
    bean1Values.put("VALUE2", "blobby1");
    bean1Values.put("VALUE3", "justice");
    bean1Values.put("VALUE4", "peace");
    Map<String, String> values = bean1.getEnumValues();
    for (String value : values.keySet()) {
      assertEquals(bean1Values.remove(value), values.get(value));
    }
    assertTrue(bean1Values.isEmpty());

    Map<String, String> bean2Values = new HashMap<String, String>();
    bean2Values.put("CONST1", "CONST1");
    bean2Values.put("CONST2", "CONST2");
    bean2Values.put("CONST3", "CONST3");
    bean2Values.put("CONST4", "CONST4");
    values = bean2.getEnumValues();
    for (String value : values.keySet()) {
      assertEquals(bean2Values.remove(value), values.get(value));
    }
    assertTrue(bean2Values.isEmpty());

    Map<String, String> bean3Values = new HashMap<String, String>();
    bean3Values.put("INT1", "INT1");
    bean3Values.put("INT2", "INT2");
    bean3Values.put("INT3", "INT3");
    bean3Values.put("INT4", "INT4");
    values = bean3.getEnumValues();
    for (String value : values.keySet()) {
      assertEquals(bean3Values.remove(value), values.get(value));
    }
    assertTrue(bean3Values.isEmpty());

  }

  public static Test suite() {
    return createSuite(TestEnumTypeDefinition.class);
  }
}
