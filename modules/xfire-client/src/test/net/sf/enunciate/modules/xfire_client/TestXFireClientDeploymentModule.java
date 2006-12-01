package net.sf.enunciate.modules.xfire_client;

import com.sun.mirror.declaration.ClassDeclaration;
import com.sun.mirror.declaration.EnumDeclaration;
import freemarker.template.TemplateException;
import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.apt.EnunciateFreemarkerModel;
import net.sf.enunciate.contract.jaxb.ComplexTypeDefinition;
import net.sf.enunciate.contract.jaxb.EnumTypeDefinition;
import net.sf.enunciate.contract.jaxb.SimpleTypeDefinition;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.xfire_client.config.ClientPackageConversion;
import net.sf.enunciate.modules.xfire_client.annotations.*;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Arrays;

import org.codehaus.xfire.annotations.soap.SOAPBindingAnnotation;
import org.codehaus.xfire.annotations.WebParamAnnotation;

/**
 * @author Ryan Heaton
 */
public class TestXFireClientDeploymentModule extends InAPTTestCase {

  /**
   * tests the doGenerate logic.
   */
  public void testDoGenerate() throws Exception {
    EndpointInterface ei1 = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.xfire_client.BasicEIOne"));
    EndpointInterface ei2 = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.xfire_client.BasicEITwo"));
    ComplexTypeDefinition complexType = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass"));
    EnumTypeDefinition enumType = new EnumTypeDefinition((EnumDeclaration) getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageEnum"));
    SimpleTypeDefinition simpleType = new SimpleTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedSimpleType"));
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(ei1);
    model.add(ei2);
    model.add(complexType);
    model.add(simpleType);
    model.add(enumType);
    FreemarkerModel.set(model);

    final HashMap<URL, Integer> templateLedger = new HashMap<URL, Integer>();
    XFireClientDeploymentModule module = new XFireClientDeploymentModule() {
      @Override
      public void processTemplate(URL templateURL, Object model) throws IOException, TemplateException {
        int count = 0;
        if (templateLedger.containsKey(templateURL)) {
          count = templateLedger.get(templateURL);
        }

        templateLedger.put(templateURL, ++count);
      }
    };

    Enunciate enunciate = new Enunciate((String[]) null);
    module.init(enunciate);
    ClientPackageConversion conversion = new ClientPackageConversion();
    conversion.setFrom("net.nothing");
    conversion.setTo("net.something");
    module.addClientPackageConversion(conversion);
    module.doFreemarkerGenerate();

    //check that all templates were processed.
    assertEquals("all templates should have been used.", 12, templateLedger.size());
    URL xfireEnumTemplate = module.getTemplateURL("xfire-enum-type.fmt");
    URL xfireSimpleTemplate = module.getTemplateURL("xfire-simple-type.fmt");
    URL xfireComplexTemplate = module.getTemplateURL("xfire-complex-type.fmt");
    URL eiTemplate = module.getTemplateURL("jdk14/client-endpoint-interface.fmt");
    URL soapImplTemplate = module.getTemplateURL("jdk14/client-soap-endpoint-impl.fmt");
    URL faultTemplate = module.getTemplateURL("jdk14/client-web-fault.fmt");
    URL enumTypeTemplate = module.getTemplateURL("jdk14/client-enum-type.fmt");
    URL simpleTypeTemplate = module.getTemplateURL("jdk14/client-simple-type.fmt");
    URL complexTypeTemplate = module.getTemplateURL("jdk14/client-complex-type.fmt");
    URL faultBeanTemplate = module.getTemplateURL("jdk14/client-fault-bean.fmt");
    URL requestBeanTemplate = module.getTemplateURL("jdk14/client-request-bean.fmt");
    URL responseBeanTemplate = module.getTemplateURL("jdk14/client-response-bean.fmt");
    assertEquals(1, templateLedger.get(xfireEnumTemplate).intValue());
    assertEquals(1, templateLedger.get(xfireSimpleTemplate).intValue());
    assertEquals(1, templateLedger.get(xfireComplexTemplate).intValue());
    assertEquals(2, templateLedger.get(eiTemplate).intValue());
    assertEquals(2, templateLedger.get(soapImplTemplate).intValue());
    assertEquals(1, templateLedger.get(enumTypeTemplate).intValue());
    assertEquals(1, templateLedger.get(simpleTypeTemplate).intValue());
    assertEquals(1, templateLedger.get(complexTypeTemplate).intValue());
    assertEquals(2, templateLedger.get(faultTemplate).intValue());
    assertEquals(2, templateLedger.get(faultBeanTemplate).intValue());
    assertEquals(4, templateLedger.get(requestBeanTemplate).intValue());
    assertEquals(4, templateLedger.get(responseBeanTemplate).intValue());

    //check that all types were accounted for.
    ArrayList<String> typeList = new ArrayList<String>((List<String>) enunciate.getProperty("client.type.list"));
    assertEquals(3 + 4 + 4 + 2, typeList.size()); //3 jaxb types, 4 request wrappers, 4 response wrappers, 2 fault beans.
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithADate"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithADateResponse"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithAString"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithAStringResponse"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.jaxws.FloatOp"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.jaxws.FloatOpResponse"));
    assertTrue(typeList.remove("net.something.BoolOpRequest"));
    assertTrue(typeList.remove("net.something.BoolOpResponse"));
    assertTrue(typeList.remove("net.something.BasicFault2"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.jaxws.BasicFaultOneBean"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageClass"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedPackageEnum"));
    assertTrue(typeList.remove("net.sf.enunciate.samples.xfire_client.with.a.nested.pckg.NestedSimpleType"));

    //check that all annotations were correctly noted on the client-side.
    ExplicitWebAnnotations annotations = (ExplicitWebAnnotations) enunciate.getProperty("client.annotations");
    assertEquals(0, annotations.class2HandlerChain.size());
    assertEquals("There should be a property order set for each request and response bean.", 8, annotations.class2PropertyOrder.size());
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithADate"), new String[]{"date"}));
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithADateResponse"), new String[]{"return"}));
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithAString"), new String[]{"string"}));
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.sf.enunciate.samples.xfire_client.jaxws.DoSomethingWithAStringResponse"), new String[]{"return"}));
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.something.BoolOpRequest"), new String[]{"param1"}));
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.something.BoolOpResponse"), new String[]{"boolResult"}));
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.sf.enunciate.samples.xfire_client.jaxws.FloatOp"), new String[]{"d", "l"}));
    assertTrue(Arrays.equals((String[]) annotations.class2PropertyOrder.get("net.sf.enunciate.samples.xfire_client.jaxws.FloatOpResponse"), new String[]{"return"}));
    assertEquals(2, annotations.class2SOAPBinding.size());
    SerializableSOAPBindingAnnotation bindingInfo = (SerializableSOAPBindingAnnotation) annotations.class2SOAPBinding.get("net.sf.enunciate.samples.xfire_client.BasicEIOne");
    assertNotNull(bindingInfo);
    assertEquals(SOAPBindingAnnotation.STYLE_DOCUMENT, bindingInfo.getStyle());
    assertEquals(SOAPBindingAnnotation.USE_LITERAL, bindingInfo.getUse());
    assertEquals(SOAPBindingAnnotation.PARAMETER_STYLE_WRAPPED, bindingInfo.getParameterStyle());
    bindingInfo = (SerializableSOAPBindingAnnotation) annotations.class2SOAPBinding.get("net.sf.enunciate.samples.xfire_client.BasicEITwo");
    assertNotNull(bindingInfo);
    assertEquals(SOAPBindingAnnotation.STYLE_RPC, bindingInfo.getStyle());
    assertEquals(SOAPBindingAnnotation.USE_LITERAL, bindingInfo.getUse());
    assertEquals(SOAPBindingAnnotation.PARAMETER_STYLE_BARE, bindingInfo.getParameterStyle());
    assertEquals(2, annotations.class2WebService.size());
    SerializableWebServiceAnnotation wsInfo = (SerializableWebServiceAnnotation) annotations.class2WebService.get("net.sf.enunciate.samples.xfire_client.BasicEIOne");
    assertNotNull(wsInfo);
    assertEquals("BasicEIOne", wsInfo.getName());
    assertEquals("urn:xfire_client", wsInfo.getTargetNamespace());
    assertEquals("BasicEIOneSOAPPort", wsInfo.getPortName());
    assertEquals("BasicEIOneService", wsInfo.getServiceName());
    wsInfo = (SerializableWebServiceAnnotation) annotations.class2WebService.get("net.sf.enunciate.samples.xfire_client.BasicEITwo");
    assertNotNull(wsInfo);
    assertEquals("ei2", wsInfo.getName());
    assertEquals("urn:xfire_client", wsInfo.getTargetNamespace());
    assertEquals("BasicEITwoSOAPPort", wsInfo.getPortName());
    assertEquals("ei2-service", wsInfo.getServiceName());

    assertEquals(4, annotations.method2RequestWrapper.size());
    RequestWrapperAnnotation reqWrapperInfo = (RequestWrapperAnnotation) annotations.method2RequestWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithADate");
    assertNotNull(reqWrapperInfo);
    assertEquals("doSomethingWithADate", reqWrapperInfo.localName());
    assertEquals("urn:xfire_client", reqWrapperInfo.targetNamespace());
    reqWrapperInfo = (RequestWrapperAnnotation) annotations.method2RequestWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithAString");
    assertNotNull(reqWrapperInfo);
    assertEquals("doSomethingWithAString", reqWrapperInfo.localName());
    assertEquals("urn:xfire_client", reqWrapperInfo.targetNamespace());
    reqWrapperInfo = (RequestWrapperAnnotation) annotations.method2RequestWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.boolOp");
    assertNotNull(reqWrapperInfo);
    assertEquals("doBoolReq", reqWrapperInfo.localName());
    assertEquals("urn:doBoolReq", reqWrapperInfo.targetNamespace());
    reqWrapperInfo = (RequestWrapperAnnotation) annotations.method2RequestWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.floatOp");
    assertNotNull(reqWrapperInfo);
    assertEquals("floatOp", reqWrapperInfo.localName());
    assertEquals("urn:xfire_client", reqWrapperInfo.targetNamespace());

    assertEquals(4, annotations.method2ResponseWrapper.size());
    ResponseWrapperAnnotation resWrapperInfo = (ResponseWrapperAnnotation) annotations.method2ResponseWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithADate");
    assertNotNull(resWrapperInfo);
    assertEquals("doSomethingWithADateResponse", resWrapperInfo.localName());
    assertEquals("urn:xfire_client", resWrapperInfo.targetNamespace());
    resWrapperInfo = (ResponseWrapperAnnotation) annotations.method2ResponseWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithAString");
    assertNotNull(resWrapperInfo);
    assertEquals("doSomethingWithAStringResponse", resWrapperInfo.localName());
    assertEquals("urn:xfire_client", resWrapperInfo.targetNamespace());
    resWrapperInfo = (ResponseWrapperAnnotation) annotations.method2ResponseWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.boolOp");
    assertNotNull(resWrapperInfo);
    assertEquals("doBoolRes", resWrapperInfo.localName());
    assertEquals("urn:doBoolRes", resWrapperInfo.targetNamespace());
    resWrapperInfo = (ResponseWrapperAnnotation) annotations.method2ResponseWrapper.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.floatOp");
    assertNotNull(resWrapperInfo);
    assertEquals("floatOpResponse", resWrapperInfo.localName());
    assertEquals("urn:xfire_client", resWrapperInfo.targetNamespace());

    assertEquals(4, annotations.method2WebMethod.size());
    SerializableWebMethodAnnotation webMethodInfo = (SerializableWebMethodAnnotation) annotations.method2WebMethod.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithADate");
    assertNotNull(webMethodInfo);
    assertEquals("", webMethodInfo.getAction());
    assertEquals("doSomethingWithADate", webMethodInfo.getOperationName());
    webMethodInfo = (SerializableWebMethodAnnotation) annotations.method2WebMethod.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithAString");
    assertNotNull(webMethodInfo);
    assertEquals("", webMethodInfo.getAction());
    assertEquals("doSomethingWithAString", webMethodInfo.getOperationName());
    webMethodInfo = (SerializableWebMethodAnnotation) annotations.method2WebMethod.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.boolOp");
    assertNotNull(webMethodInfo);
    assertEquals("urn:doBool", webMethodInfo.getAction());
    assertEquals("doBool", webMethodInfo.getOperationName());
    webMethodInfo = (SerializableWebMethodAnnotation) annotations.method2WebMethod.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.floatOp");
    assertNotNull(webMethodInfo);
    assertEquals("", webMethodInfo.getAction());
    assertEquals("floatOp", webMethodInfo.getOperationName());

    assertEquals(4, annotations.method2WebResult.size());
    SerializableWebResultAnnotation webResultInfo = (SerializableWebResultAnnotation) annotations.method2WebResult.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithADate");
    assertNotNull(webResultInfo);
    assertEquals("return", webResultInfo.getName());
    assertEquals("return", webResultInfo.getPartName());
    assertEquals("urn:xfire_client", webResultInfo.getTargetNamespace());
    webResultInfo = (SerializableWebResultAnnotation) annotations.method2WebResult.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithAString");
    assertNotNull(webResultInfo);
    assertEquals("return", webResultInfo.getName());
    assertEquals("return", webResultInfo.getPartName());
    assertEquals("urn:xfire_client", webResultInfo.getTargetNamespace());
    webResultInfo = (SerializableWebResultAnnotation) annotations.method2WebResult.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.boolOp");
    assertNotNull(webResultInfo);
    assertEquals("boolResult", webResultInfo.getName());
    assertEquals("boolOpResultPartName", webResultInfo.getPartName());
    assertEquals("urn:boolOpResult", webResultInfo.getTargetNamespace());
    webResultInfo = (SerializableWebResultAnnotation) annotations.method2WebResult.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.floatOp");
    assertNotNull(webResultInfo);
    assertEquals("return", webResultInfo.getName());
    assertEquals("return", webResultInfo.getPartName());
    assertEquals("urn:xfire_client", webResultInfo.getTargetNamespace());

    assertEquals(5, annotations.method2WebParam.size());
    SerializableWebParamAnnotation webParamInfo = (SerializableWebParamAnnotation) annotations.method2WebParam.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithADate.0");
    assertEquals(WebParamAnnotation.MODE_IN, webParamInfo.getMode());
    assertEquals("date", webParamInfo.getName());
    assertEquals("date", webParamInfo.getPartName());
    webParamInfo = (SerializableWebParamAnnotation) annotations.method2WebParam.get("net.sf.enunciate.samples.xfire_client.BasicEIOne.doSomethingWithAString.0");
    assertEquals(WebParamAnnotation.MODE_IN, webParamInfo.getMode());
    assertEquals("string", webParamInfo.getName());
    assertEquals("string", webParamInfo.getPartName());
    webParamInfo = (SerializableWebParamAnnotation) annotations.method2WebParam.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.boolOp.0");
    assertEquals(WebParamAnnotation.MODE_IN, webParamInfo.getMode());
    assertEquals("param1", webParamInfo.getName());
    assertEquals("param1Part", webParamInfo.getPartName());
    webParamInfo = (SerializableWebParamAnnotation) annotations.method2WebParam.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.floatOp.0");
    assertEquals(WebParamAnnotation.MODE_IN, webParamInfo.getMode());
    assertEquals("d", webParamInfo.getName());
    assertEquals("d", webParamInfo.getPartName());
    webParamInfo = (SerializableWebParamAnnotation) annotations.method2WebParam.get("net.sf.enunciate.samples.xfire_client.BasicEITwo.floatOp.1");
    assertEquals(WebParamAnnotation.MODE_IN, webParamInfo.getMode());
    assertEquals("l", webParamInfo.getName());
    assertEquals("l", webParamInfo.getPartName());

    assertEquals(2, annotations.fault2WebFault.size());
    WebFaultAnnotation webFaultInfo = (WebFaultAnnotation) annotations.fault2WebFault.get("net.sf.enunciate.samples.xfire_client.BasicFaultOne");
    assertEquals(true, webFaultInfo.implicitFaultBean());
    assertEquals("net.sf.enunciate.samples.xfire_client.jaxws.BasicFaultOneBean", webFaultInfo.faultBean());
    assertEquals("BasicFaultOne", webFaultInfo.name());
    assertEquals("urn:xfire_client", webFaultInfo.targetNamespace());
    webFaultInfo = (WebFaultAnnotation) annotations.fault2WebFault.get("net.sf.enunciate.samples.xfire_client.BasicFaultTwo");
    assertEquals(true, webFaultInfo.implicitFaultBean());
    assertEquals("net.something.BasicFault2", webFaultInfo.faultBean());
    assertEquals("bf2", webFaultInfo.name());
    assertEquals("urn:bf2", webFaultInfo.targetNamespace());

    assertEquals(0, annotations.oneWayMethods.size());
  }
}
