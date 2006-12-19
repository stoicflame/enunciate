package net.sf.enunciate.contract.jaxws;

import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.types.KnownXmlType;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.jelly.apt.freemarker.FreemarkerModel;
import com.sun.mirror.declaration.ClassDeclaration;

import javax.xml.namespace.QName;
import java.util.Collection;
import java.util.Iterator;

import junit.framework.Test;

/**
 * @author Ryan Heaton
 */
public class TestWebParam extends InAPTTestCase {

  /**
   * Tests the names and properties of a web param.
   */
  public void testNamesAndProperties() throws Exception {
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanOne")));
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanTwo")));
    model.add(new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanThree")));
    FreemarkerModel.set(model);
    EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.WebMethodExamples"));

    WebMethod docLitBareMethod = null;
    WebMethod docLitWrappedMethod = null;
    WebMethod rpcLitWrappedMethod = null;
    Collection<WebMethod> webMethods = ei.getWebMethods();
    for (WebMethod webMethod : webMethods) {
      String simpleName = webMethod.getSimpleName();
      if ("docLitBareMethod".equals(simpleName)) {
        docLitBareMethod = webMethod;
      }
      else if ("docLitWrappedMethod".equals(simpleName)) {
        docLitWrappedMethod = webMethod;
      }
      else if ("rpcLitWrappedMethod".equals(simpleName)) {
        rpcLitWrappedMethod = webMethod;
      }
    }

    Collection<WebParam> params = docLitBareMethod.getWebParameters();
    WebParam bareParam = params.iterator().next();
    assertEquals("beanTwo", bareParam.getElementName());
    assertEquals("beanTwo", bareParam.getPartName());
    assertEquals("WebMethodExamples.docLitBareMethod", bareParam.getMessageName());
    assertSame(WebMessagePart.ParticleType.ELEMENT, bareParam.getParticleType());
    assertEquals(new QName("urn:web-method-examples", "beanTwo"), bareParam.getParticleQName());
    assertTrue(bareParam.isImplicitSchemaElement());
    assertEquals(new QName(null, "beanTwo"), bareParam.getTypeQName());
    assertEquals(1, bareParam.getMinOccurs());
    assertEquals("1", bareParam.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.IN, bareParam.getMode());
    assertFalse(bareParam.isHeader());
    assertTrue(bareParam.isInput());
    assertFalse(bareParam.isOutput());
    assertFalse(bareParam.isHolder());
    assertFalse(bareParam.isFault());
    assertEquals(1, bareParam.getParts().size());

    params = docLitWrappedMethod.getWebParameters();
    Iterator<WebParam> paramIt = params.iterator();
    WebParam param1 = paramIt.next();
    assertEquals("hah", param1.getElementName());
    assertEquals("hoo", param1.getPartName());
    assertNull(param1.getMessageName());
    assertSame(WebMessagePart.ParticleType.ELEMENT, param1.getParticleType());
    assertEquals(new QName("urn:web-method-examples", "hah"), param1.getParticleQName());
    assertTrue(param1.isImplicitSchemaElement());
    assertEquals(KnownXmlType.BOOLEAN.getQname(), param1.getTypeQName());
    assertEquals(1, param1.getMinOccurs());
    assertEquals("1", param1.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.IN, param1.getMode());
    assertFalse(param1.isHeader());
    assertTrue(param1.isInput());
    assertFalse(param1.isOutput());
    assertFalse(param1.isHolder());
    assertFalse(param1.isFault());
    try {
      param1.getParts().size();
      fail("A doc/lit wrapped parameter shouldn't support having parts.");
    }
    catch (UnsupportedOperationException e) {

    }

    WebParam param2 = paramIt.next();
    assertEquals("i", param2.getElementName());
    assertEquals("i", param2.getPartName());
    assertNull(param2.getMessageName());
    assertSame(WebMessagePart.ParticleType.ELEMENT, param2.getParticleType());
    assertEquals(new QName("urn:web-method-examples", "i"), param2.getParticleQName());
    assertTrue(param2.isImplicitSchemaElement());
    assertEquals(KnownXmlType.INT.getQname(), param2.getTypeQName());
    assertEquals(1, param2.getMinOccurs());
    assertEquals("1", param2.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.IN, param2.getMode());
    assertFalse(param2.isHeader());
    assertTrue(param2.isInput());
    assertFalse(param2.isOutput());
    assertFalse(param2.isHolder());
    assertFalse(param2.isFault());
    try {
      param2.getParts().size();
      fail("A doc/lit wrapped parameter shouldn't support having parts.");
    }
    catch (UnsupportedOperationException e) {

    }

    WebParam param3 = paramIt.next();
    assertEquals("s", param3.getElementName());
    assertEquals("s", param3.getPartName());
    assertEquals("WebMethodExamples.docLitWrappedMethod.s", param3.getMessageName());
    assertSame(WebMessagePart.ParticleType.ELEMENT, param3.getParticleType());
    assertEquals(new QName("urn:web-method-examples", "s"), param3.getParticleQName());
    assertTrue(param3.isImplicitSchemaElement());
    assertEquals(KnownXmlType.SHORT.getQname(), param3.getTypeQName());
    assertEquals(1, param3.getMinOccurs());
    assertEquals("1", param3.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.IN, param3.getMode());
    assertTrue(param3.isHeader());
    assertTrue(param3.isInput());
    assertFalse(param3.isOutput());
    assertFalse(param3.isHolder());
    assertFalse(param3.isFault());
    assertEquals(1, param3.getParts().size());

    WebParam param4 = paramIt.next();
    assertEquals("c", param4.getElementName());
    assertEquals("c", param4.getPartName());
    assertNull(param4.getMessageName());
    assertSame(WebMessagePart.ParticleType.ELEMENT, param4.getParticleType());
    assertEquals(new QName("urn:web-method-examples", "c"), param4.getParticleQName());
    assertTrue(param4.isImplicitSchemaElement());
    assertEquals(KnownXmlType.FLOAT.getQname(), param4.getTypeQName());
    assertEquals(1, param4.getMinOccurs());
    assertEquals("1", param4.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.INOUT, param4.getMode());
    assertFalse(param4.isHeader());
    assertTrue(param4.isInput());
    assertTrue(param4.isOutput());
    assertTrue(param4.isHolder());
    assertFalse(param4.isFault());
    try {
      param4.getParts().size();
      fail("A doc/lit wrapped parameter shouldn't support having parts.");
    }
    catch (UnsupportedOperationException e) {

    }

    params = rpcLitWrappedMethod.getWebParameters();
    paramIt = params.iterator();
    param1 = paramIt.next();
    assertEquals("hah", param1.getElementName());
    assertEquals("hoo", param1.getPartName());
    assertNull(param1.getMessageName());
    assertSame(WebMessagePart.ParticleType.TYPE, param1.getParticleType());
    assertEquals(KnownXmlType.BOOLEAN.getQname(), param1.getParticleQName());
    assertFalse(param1.isImplicitSchemaElement());
    assertEquals(KnownXmlType.BOOLEAN.getQname(), param1.getTypeQName());
    assertSame(javax.jws.WebParam.Mode.IN, param1.getMode());
    assertFalse(param1.isHeader());
    assertTrue(param1.isInput());
    assertFalse(param1.isOutput());
    assertFalse(param1.isHolder());
    assertFalse(param1.isFault());
    try {
      param1.getParts().size();
      fail("A doc/lit wrapped parameter shouldn't support having parts.");
    }
    catch (UnsupportedOperationException e) {

    }

    param2 = paramIt.next();
    assertEquals("i", param2.getElementName());
    assertEquals("i", param2.getPartName());
    assertNull(param2.getMessageName());
    assertSame(WebMessagePart.ParticleType.TYPE, param2.getParticleType());
    assertEquals(KnownXmlType.INT.getQname(), param2.getParticleQName());
    assertFalse(param2.isImplicitSchemaElement());
    assertEquals(KnownXmlType.INT.getQname(), param2.getTypeQName());
    assertEquals(1, param2.getMinOccurs());
    assertEquals("1", param2.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.IN, param2.getMode());
    assertFalse(param2.isHeader());
    assertTrue(param2.isInput());
    assertFalse(param2.isOutput());
    assertFalse(param2.isHolder());
    assertFalse(param2.isFault());
    try {
      param2.getParts().size();
      fail("A doc/lit wrapped parameter shouldn't support having parts.");
    }
    catch (UnsupportedOperationException e) {

    }

    param3 = paramIt.next();
    assertEquals("s", param3.getElementName());
    assertEquals("s", param3.getPartName());
    assertEquals("WebMethodExamples.rpcLitWrappedMethod.s", param3.getMessageName());
    assertSame(WebMessagePart.ParticleType.TYPE, param3.getParticleType());
    assertEquals(KnownXmlType.SHORT.getQname(), param3.getParticleQName());
    assertFalse(param3.isImplicitSchemaElement());
    assertEquals(KnownXmlType.SHORT.getQname(), param3.getTypeQName());
    assertEquals(1, param3.getMinOccurs());
    assertEquals("1", param3.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.IN, param3.getMode());
    assertTrue(param3.isHeader());
    assertTrue(param3.isInput());
    assertFalse(param3.isOutput());
    assertFalse(param3.isHolder());
    assertFalse(param3.isFault());
    assertEquals(1, param3.getParts().size());

    param4 = paramIt.next();
    assertEquals("c", param4.getElementName());
    assertEquals("c", param4.getPartName());
    assertNull(param4.getMessageName());
    assertSame(WebMessagePart.ParticleType.TYPE, param4.getParticleType());
    assertEquals(KnownXmlType.FLOAT.getQname(), param4.getParticleQName());
    assertFalse(param4.isImplicitSchemaElement());
    assertEquals(KnownXmlType.FLOAT.getQname(), param4.getTypeQName());
    assertEquals(1, param4.getMinOccurs());
    assertEquals("1", param4.getMaxOccurs());
    assertSame(javax.jws.WebParam.Mode.INOUT, param4.getMode());
    assertFalse(param4.isHeader());
    assertTrue(param4.isInput());
    assertTrue(param4.isOutput());
    assertTrue(param4.isHolder());
    assertFalse(param4.isFault());
    try {
      param4.getParts().size();
      fail("A doc/lit wrapped parameter shouldn't support having parts.");
    }
    catch (UnsupportedOperationException e) {

    }

  }

  public static Test suite() {
    return createSuite(TestWebParam.class);
  }
}
