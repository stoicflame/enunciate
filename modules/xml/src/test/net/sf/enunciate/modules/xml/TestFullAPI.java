package net.sf.enunciate.modules.xml;

import com.sun.xml.xsom.*;
import static com.sun.xml.xsom.XSType.EXTENSION;
import static com.sun.xml.xsom.XSType.RESTRICTION;
import com.sun.xml.xsom.parser.XSOMParser;
import junit.framework.TestCase;
import static net.sf.enunciate.EnunciateTestUtil.getAllJavaFiles;
import static net.sf.enunciate.InAPTTestCase.getInAPTClasspath;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.main.Enunciate;
import net.sf.enunciate.modules.DeploymentModule;
import net.sf.enunciate.modules.xml.config.SchemaConfig;
import net.sf.enunciate.modules.xml.config.WsdlConfig;
import org.xml.sax.*;
import org.w3c.dom.Element;

import javax.wsdl.*;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPOperation;
import javax.wsdl.extensions.soap.SOAPBody;
import javax.wsdl.extensions.soap.SOAPFault;
import javax.wsdl.extensions.soap.SOAPAddress;
import javax.wsdl.extensions.schema.Schema;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import javax.xml.namespace.QName;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.*;
import java.util.*;

/**
 * The full-API test case.
 *
 * @author Ryan Heaton
 */
public class TestFullAPI extends TestCase {

  public static final String FULL_NAMESPACE = "http://enunciate.sf.net/samples/full";
  public static final String DATA_NAMESPACE = "http://enunciate.sf.net/samples/genealogy/data";
  public static final String CITE_NAMESPACE = "http://enunciate.sf.net/samples/genealogy/cite";

  /**
   * Tests the xml artifact generation against the "full" API.
   */
  public void testAgainstFullAPI() throws Exception {
    XMLDeploymentModule xmlModule = new XMLDeploymentModule();
    SchemaConfig schemaConfig = new SchemaConfig();
    schemaConfig.setNamespace(CITE_NAMESPACE);
    schemaConfig.setFile("cite.xsd");
    xmlModule.addSchemaConfig(schemaConfig);
    schemaConfig = new SchemaConfig();
    schemaConfig.setNamespace(DATA_NAMESPACE);
    schemaConfig.setFile("data.xsd");
    xmlModule.addSchemaConfig(schemaConfig);
    WsdlConfig wsdlConfig = new WsdlConfig();
    wsdlConfig.setNamespace(FULL_NAMESPACE);
    wsdlConfig.setFile("full.wsdl");
    xmlModule.addWsdlConfig(wsdlConfig);

    EnunciateConfiguration config = new EnunciateConfiguration(Arrays.asList((DeploymentModule) xmlModule));
    config.putNamespace(CITE_NAMESPACE, "cite");
    config.putNamespace(DATA_NAMESPACE, "data");
    config.putNamespace(FULL_NAMESPACE, "full");
    Enunciate enunciate = new Enunciate(getAllJavaFiles("full"));
    enunciate.setConfig(config);
    enunciate.setTarget(Enunciate.Target.GENERATE);
    enunciate.setClasspath(getInAPTClasspath());
    enunciate.execute();

    final File dataSchemaFile = new File(enunciate.getGenerateDir(), "xml/data.xsd");
    final File citationSchemaFile = new File(enunciate.getGenerateDir(), "xml/cite.xsd");
    File wsdlFile = new File(enunciate.getGenerateDir(), "xml/full.wsdl");

    assertTrue(dataSchemaFile.exists());
    assertTrue(citationSchemaFile.exists());
    assertTrue(wsdlFile.exists());

    //make sure the wsdl is built correctly
    WSDLReader wsdlReader = WSDLFactory.newInstance().newWSDLReader();
    Definition definition = wsdlReader.readWSDL(null, wsdlFile.toURL().toString());
    assertEquals(FULL_NAMESPACE, definition.getTargetNamespace());
    Types types = definition.getTypes();
    List extensibilityElements = types.getExtensibilityElements();
    assertEquals(1, extensibilityElements.size());
    ExtensibilityElement ee = (ExtensibilityElement) extensibilityElements.get(0);
    assertEquals(new QName(W3C_XML_SCHEMA_NS_URI, "schema"), ee.getElementType());
    Schema schema = (Schema) ee;
    Map imports = schema.getImports();
    assertEquals(2, imports.size());
    assertNotNull(imports.get(DATA_NAMESPACE));
    assertNotNull(imports.get(CITE_NAMESPACE));

    ByteArrayOutputStream tempSchema = new ByteArrayOutputStream();
    TransformerFactory tFactory = TransformerFactory.newInstance();
    Transformer transformer = tFactory.newTransformer();
    Element schemaElement = schema.getElement();
    //these namespaces need to be explicitly added because the transformer won't see any references to them...
    schemaElement.setAttribute("xmlns:data", DATA_NAMESPACE);
    schemaElement.setAttribute("xmlns:cite", CITE_NAMESPACE);
    schemaElement.setAttribute("xmlns:full", FULL_NAMESPACE);

    //write out the wsdl schema to its xml form so we can parse it with XSOM...
    DOMSource source = new DOMSource(schemaElement);
    StreamResult result = new StreamResult(tempSchema);
    transformer.transform(source, result);
    tempSchema.close();

    //set up the XSOM Parser.
    XSOMParser parser = new XSOMParser();
    parser.setErrorHandler(new ThrowEverythingHandler()); //throw all errors and warnings.
    parser.setEntityResolver(new EntityResolver() { //make sure we can resolve the schema imports.
      public InputSource resolveEntity(String publicId, String systemId) throws SAXException, IOException {
        if ("cite.xsd".equals(systemId)) {
          return new InputSource(new FileReader(citationSchemaFile));
        }
        else if ("data.xsd".equals(systemId)) {
          return new InputSource(new FileReader(dataSchemaFile));
        }
        else {
          throw new SAXException("Unknown entity: " + systemId);
        }
      }
    });

    //make sure the schema included in the wsdl is correct.
    parser.parse(new ByteArrayInputStream(tempSchema.toByteArray()));
    XSSchemaSet schemaSet = parser.getResult();
    XSSchema wsdlSchema = schemaSet.getSchema(FULL_NAMESPACE);
    assertNotNull(wsdlSchema);

    //make sure the data schema is imported and correct.
    XSSchema dataSchema = schemaSet.getSchema(DATA_NAMESPACE);
    assertNotNull(dataSchema);
    assertDataSchemaStructure(dataSchema);

    //make sure the cite schema is imported and correct.
    XSSchema citeSchema = schemaSet.getSchema(CITE_NAMESPACE);
    assertNotNull(citeSchema);
    assertCiteSchemaStructure(citeSchema);

    //now verify the rest of the WSDL...
    assertWebServiceDefinition(definition);
  }

  protected void assertWebServiceDefinition(Definition definition) {
    assertDefinitionMessages(definition);
    assertPortTypes(definition);
    assertBindings(definition);
    assertServices(definition);
  }

  protected void assertServices(Definition definition) {
    Map services = definition.getServices();
    assertEquals(2, services.size());
    for (Object s : services.values()) {
      Service service = (Service) s;
      assertEquals(FULL_NAMESPACE, service.getQName().getNamespaceURI());
      String serviceName = service.getQName().getLocalPart();
      if ("PersonServiceService".equals(serviceName)) {
        Map ports = service.getPorts();
        assertEquals(1, ports.size());
        Port port = (Port) ports.values().iterator().next();
        assertEquals("PersonServiceSOAPPort", port.getName());
        SOAPAddress address = (SOAPAddress) port.getExtensibilityElements().get(0);
        assertEquals("TBD", address.getLocationURI());

        assertEquals(definition.getBindings().get(new QName(FULL_NAMESPACE, "PersonServiceSOAPBinding")), port.getBinding());
      }
      else if ("source-service".equals(serviceName)) {
        Map ports = service.getPorts();
        assertEquals(1, ports.size());
        Port port = (Port) ports.values().iterator().next();
        assertEquals("SourceServiceSOAPPort", port.getName());
        SOAPAddress address = (SOAPAddress) port.getExtensibilityElements().get(0);
        assertEquals("TBD", address.getLocationURI());

        assertEquals(definition.getBindings().get(new QName(FULL_NAMESPACE, "SourceServiceSOAPBinding")), port.getBinding());
      }
      else {
        fail("Unknown service: " + serviceName);
      }
    }
  }

  protected void assertBindings(Definition definition) {
    Map bindings = definition.getBindings();
    assertEquals(2, bindings.size());
    for (Object b : bindings.values()) {
      Binding binding = (Binding) b;
      assertEquals(FULL_NAMESPACE, binding.getQName().getNamespaceURI());
      String bindingName = binding.getQName().getLocalPart();
      if ("PersonServiceSOAPBinding".equals(bindingName)) {
        for (Object bo : binding.getBindingOperations()) {
          BindingOperation operation = (BindingOperation) bo;
          String operationName = operation.getName();
          if ("storePerson".equals(operationName)) {
            SOAPOperation soapOp = (SOAPOperation) operation.getExtensibilityElements().get(0);
            assertEquals("", soapOp.getSoapActionURI());
            assertEquals("document", soapOp.getStyle());

            SOAPBody soapBody = (SOAPBody) operation.getBindingInput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());
            soapBody = (SOAPBody) operation.getBindingOutput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());

            assertEquals(0, operation.getBindingFaults().size());
          }
          else if ("readPersons".equals(operationName)) {
            SOAPOperation soapOp = (SOAPOperation) operation.getExtensibilityElements().get(0);
            assertEquals("", soapOp.getSoapActionURI());
            assertEquals("document", soapOp.getStyle());

            SOAPBody soapBody = (SOAPBody) operation.getBindingInput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());
            soapBody = (SOAPBody) operation.getBindingOutput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());

            BindingFault bindingFault = (BindingFault) operation.getBindingFaults().values().iterator().next();
            SOAPFault soapFault = (SOAPFault) bindingFault.getExtensibilityElements().get(0);
            assertEquals("literal", soapFault.getUse());
          }
          else if ("deletePerson".equals(operationName)) {
            SOAPOperation soapOp = (SOAPOperation) operation.getExtensibilityElements().get(0);
            assertEquals("", soapOp.getSoapActionURI());
            assertEquals("document", soapOp.getStyle());

            SOAPBody soapBody = (SOAPBody) operation.getBindingInput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());
            soapBody = (SOAPBody) operation.getBindingOutput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());

            BindingFault bindingFault = (BindingFault) operation.getBindingFaults().values().iterator().next();
            SOAPFault soapFault = (SOAPFault) bindingFault.getExtensibilityElements().get(0);
            assertEquals("literal", soapFault.getUse());
          }
          else {
            fail("Unknown binding operation on PersonServiceSOAPBinding: " + operationName);
          }

        }
      }
      else if ("SourceServiceSOAPBinding".equals(bindingName)) {
        for (Object bo : binding.getBindingOperations()) {
          BindingOperation operation = (BindingOperation) bo;
          String operationName = operation.getName();
          if ("addSource".equals(operationName)) {
            SOAPOperation soapOp = (SOAPOperation) operation.getExtensibilityElements().get(0);
            assertEquals("", soapOp.getSoapActionURI());
            assertEquals("document", soapOp.getStyle());

            SOAPBody soapBody = (SOAPBody) operation.getBindingInput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());
            soapBody = (SOAPBody) operation.getBindingOutput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());

            assertEquals(0, operation.getBindingFaults().size());
          }
          else if ("getSource".equals(operationName)) {
            SOAPOperation soapOp = (SOAPOperation) operation.getExtensibilityElements().get(0);
            assertEquals("", soapOp.getSoapActionURI());
            assertEquals("document", soapOp.getStyle());

            SOAPBody soapBody = (SOAPBody) operation.getBindingInput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());
            soapBody = (SOAPBody) operation.getBindingOutput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());

            BindingFault bindingFault = (BindingFault) operation.getBindingFaults().values().iterator().next();
            SOAPFault soapFault = (SOAPFault) bindingFault.getExtensibilityElements().get(0);
            assertEquals("literal", soapFault.getUse());
          }
          else if ("addInfoSet".equals(operationName)) {
            SOAPOperation soapOp = (SOAPOperation) operation.getExtensibilityElements().get(0);
            assertEquals("", soapOp.getSoapActionURI());
            assertEquals("rpc", soapOp.getStyle());

            SOAPBody soapBody = (SOAPBody) operation.getBindingInput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());
            soapBody = (SOAPBody) operation.getBindingOutput().getExtensibilityElements().get(0);
            assertEquals("literal", soapBody.getUse());

            BindingFault bindingFault = (BindingFault) operation.getBindingFaults().values().iterator().next();
            SOAPFault soapFault = (SOAPFault) bindingFault.getExtensibilityElements().get(0);
            assertEquals("literal", soapFault.getUse());
          }
          else {
            fail("Unknown binding operation on SourceServiceSOAPBinding: " + operationName);
          }

        }
      }
      else {
        fail("Unknown binding: " + bindingName);
      }
    }
  }

  protected void assertPortTypes(Definition definition) {
    Map portTypes = definition.getPortTypes();
    assertEquals(2, portTypes.size());
    for (Object p : portTypes.values()) {
      PortType portType = (PortType) p;
      assertEquals(FULL_NAMESPACE, portType.getQName().getNamespaceURI());
      String portTypeName = portType.getQName().getLocalPart();
      if ("PersonService".equals(portTypeName)) {
        for (Object o : portType.getOperations()) {
          Operation operation = (Operation) o;
          String operationName = operation.getName();
          if ("storePerson".equals(operationName)) {
            Input input = operation.getInput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "PersonService.storePerson")), input.getMessage());
            Output output = operation.getOutput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "PersonService.storePersonResponse")), output.getMessage());
            Fault fault = operation.getFault("ServiceException");
            assertNull(fault);
          }
          else if ("readPersons".equals(operationName)) {
            Input input = operation.getInput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "PersonService.readPersons")), input.getMessage());
            Output output = operation.getOutput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "PersonService.readPersonsResponse")), output.getMessage());
            Fault fault = operation.getFault("ServiceException");
            assertNotNull(fault);
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "ServiceException")), fault.getMessage());
          }
          else if ("deletePerson".equals(operationName)) {
            Input input = operation.getInput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "PersonService.deletePerson")), input.getMessage());
            Output output = operation.getOutput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "PersonService.deletePersonResponse")), output.getMessage());
            Fault fault = operation.getFault("ServiceException");
            assertNotNull(fault);
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "ServiceException")), fault.getMessage());
          }
          else {
            fail("Unknown operation on PersonService: " + operationName);
          }
        }
      }
      else if ("source-service".equals(portTypeName)) {
        for (Object o : portType.getOperations()) {
          Operation operation = (Operation) o;
          String operationName = operation.getName();
          if ("addSource".equals(operationName)) {
            Input input = operation.getInput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "SourceService.addSource")), input.getMessage());
            Output output = operation.getOutput();
            assertNull(output);
            Fault fault = operation.getFault("ServiceException");
            assertNull(fault);
          }
          else if ("getSource".equals(operationName)) {
            Input input = operation.getInput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "SourceService.getSource")), input.getMessage());
            Output output = operation.getOutput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "SourceService.getSourceResponse")), output.getMessage());
            Fault fault = operation.getFault("ServiceException");
            assertNotNull(fault);
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "ServiceException")), fault.getMessage());
          }
          else if ("addInfoSet".equals(operationName)) {
            Input input = operation.getInput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "SourceService.addInfoSet")), input.getMessage());
            Output output = operation.getOutput();
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "SourceService.addInfoSetResponse")), output.getMessage());
            Fault fault = operation.getFault("ServiceException");
            assertNotNull(fault);
            assertEquals(definition.getMessage(new QName(FULL_NAMESPACE, "ServiceException")), fault.getMessage());
          }
          else {
            fail("Unknown operation on SourceService: " + operationName);
          }
        }
      }
      else {
        fail("Unknown port type: " + portTypeName);
      }
    }
  }

  protected void assertDefinitionMessages(Definition definition) {
    Map messages = definition.getMessages();
    assertEquals(12, messages.size()); //6 request messages, 5 response messages (because there is a one-way method), and 1 exception
    for (Object m : messages.values()) {
      Message message = (Message) m;
      assertEquals(FULL_NAMESPACE, message.getQName().getNamespaceURI());
      String messageName = message.getQName().getLocalPart();
      if ("ServiceException".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("ServiceException");
        assertEquals(new QName(FULL_NAMESPACE, "ServiceException"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("PersonService.storePerson".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("person");
        assertEquals(new QName(DATA_NAMESPACE, "Person"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("PersonService.readPersons".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("readPersons");
        assertEquals(new QName(FULL_NAMESPACE, "readPersons"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("PersonService.deletePerson".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("deletePerson");
        assertEquals(new QName(FULL_NAMESPACE, "deletePerson"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("PersonService.storePersonResponse".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("return");
        assertEquals(new QName(DATA_NAMESPACE, "Person"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("PersonService.readPersonsResponse".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("readPersonsResponse");
        assertEquals(new QName(FULL_NAMESPACE, "readPersonsResponse"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("PersonService.deletePersonResponse".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("deletePersonResponse");
        assertEquals(new QName(FULL_NAMESPACE, "deletePersonResponse"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("SourceService.addSource".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("addSource");
        assertEquals(new QName(FULL_NAMESPACE, "addSource"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("SourceService.getSource".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("getSource");
        assertEquals(new QName(FULL_NAMESPACE, "getSource"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("SourceService.addInfoSet".equals(messageName)) {
        assertEquals(2, message.getParts().size());
        Part part = message.getPart("sourceId");
        assertNull(part.getElementName());
        assertEquals(new QName(W3C_XML_SCHEMA_NS_URI, "string"), part.getTypeName());
        part = message.getPart("infoSet");
        assertNull(part.getElementName());
        assertEquals(new QName(CITE_NAMESPACE, "InfoSet"), part.getTypeName());
      }
      else if ("SourceService.getSourceResponse".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("getSourceResponse");
        assertEquals(new QName(FULL_NAMESPACE, "getSourceResponse"), part.getElementName());
        assertNull(part.getTypeName());
      }
      else if ("SourceService.addInfoSetResponse".equals(messageName)) {
        assertEquals(1, message.getParts().size());
        Part part = message.getPart("return");
        assertNull(part.getElementName());
        assertEquals(new QName(W3C_XML_SCHEMA_NS_URI, "string"), part.getTypeName());
      }
      else {
        fail("Unknown web message: " + messageName);
      }
    }
  }

  protected void assertCiteSchemaStructure(XSSchema citeSchema) {
    Map<String, XSElementDecl> rootElements = citeSchema.getElementDecls();
    assertEquals(3, rootElements.size());
    XSElementDecl contributorElement = rootElements.get("Contributor");
    assertContributorElement(contributorElement);
    XSElementDecl sourceElement = rootElements.get("Source");
    assertSourceElement(sourceElement);
    XSElementDecl repositoryElement = rootElements.get("Repository");
    assertRepositoryElement(repositoryElement);

    Map<String, XSSimpleType> simpleTypes = citeSchema.getSimpleTypes();
    assertEquals(1, simpleTypes.size());
    XSSimpleType emailType = simpleTypes.get("EMail");
    assertNotNull(emailType);
    assertEmailType(emailType);

    Map<String, XSComplexType> complexTypes = citeSchema.getComplexTypes();
    assertEquals(4, complexTypes.size());
    XSComplexType contributorType = complexTypes.get("Contributor");
    assertNotNull(contributorType);
    assertContributorType(contributorType);
    XSComplexType infosetType = complexTypes.get("InfoSet");
    assertNotNull(infosetType);
    assertInfosetType(infosetType);
    XSComplexType repositoryType = complexTypes.get("Repository");
    assertNotNull(repositoryType);
    assertRepositoryType(repositoryType);
    XSComplexType sourceType = complexTypes.get("Source");
    assertNotNull(sourceType);
    assertSourceType(sourceType);
  }

  protected void assertEmailType(XSSimpleType emailType) {
    assertTrue(emailType.isRestriction());
    XSRestrictionSimpleType restriction = emailType.asRestriction();
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", restriction.getSimpleBaseType());
    assertEquals(0, restriction.getDeclaredFacets().size());
  }

  protected void assertContributorElement(XSElementDecl contributorElement) {
    assertEquals("Contributor", contributorElement.getName());
    XSType personType = contributorElement.getType();
    assertEquals("Contributor", personType.getName());
    assertEquals(CITE_NAMESPACE, personType.getTargetNamespace());
  }

  protected void assertSourceElement(XSElementDecl sourceElement) {
    assertEquals("Source", sourceElement.getName());
    XSType personType = sourceElement.getType();
    assertEquals("Source", personType.getName());
    assertEquals(CITE_NAMESPACE, personType.getTargetNamespace());
  }

  protected void assertRepositoryElement(XSElementDecl repositoryElement) {
    assertEquals("Repository", repositoryElement.getName());
    XSType personType = repositoryElement.getType();
    assertEquals("Repository", personType.getName());
    assertEquals(CITE_NAMESPACE, personType.getTargetNamespace());
  }

  protected void assertDataSchemaStructure(XSSchema dataSchema) {
    Map<String, XSElementDecl> rootElements = dataSchema.getElementDecls();
    assertEquals(1, rootElements.size());
    XSElementDecl personElement = rootElements.values().iterator().next();
    assertPersonRootElement(personElement);

    Map<String, XSSimpleType> simpleTypes = dataSchema.getSimpleTypes();
    assertEquals(3, simpleTypes.size());
    XSSimpleType eventTypeType = simpleTypes.get("EventType");
    assertNotNull(eventTypeType);
    assertEventTypeType(eventTypeType);
    XSSimpleType factTypeType = simpleTypes.get("FactType");
    assertNotNull(factTypeType);
    assertFactTypeType(factTypeType);
    XSSimpleType genderTypeType = simpleTypes.get("GenderType");
    assertNotNull(genderTypeType);
    assertGenderTypeType(genderTypeType);

    Map<String, XSComplexType> complexTypes = dataSchema.getComplexTypes();
    assertEquals(8, complexTypes.size());
    XSComplexType assertionType = complexTypes.get("Assertion");
    assertNotNull(assertionType);
    assertAssertionType(assertionType);
    XSComplexType occurringAssertionType = complexTypes.get("OccurringAssertion");
    assertNotNull(occurringAssertionType);
    assertOccurringAssertionType(occurringAssertionType);
    XSComplexType eventType = complexTypes.get("Event");
    assertNotNull(eventType);
    assertEventType(eventType);
    XSComplexType factType = complexTypes.get("Fact");
    assertNotNull(factType);
    assertFactType(factType);
    XSComplexType genderType = complexTypes.get("Gender");
    assertNotNull(genderType);
    assertGenderType(genderType);
    XSComplexType nameType = complexTypes.get("Name");
    assertNotNull(nameType);
    assertNameType(nameType);
    XSComplexType relationshipType = complexTypes.get("Relationship");
    assertNotNull(relationshipType);
    assertRelationshipType(relationshipType);
    XSComplexType personType = complexTypes.get("Person");
    assertNotNull(personType);
    assertPersonType(personType);
  }

  protected void assertEventType(XSComplexType eventType) {
    assertEquals("Event", eventType.getName());
    assertFalse(eventType.isAbstract());
    assertQNameEquals(DATA_NAMESPACE, "OccurringAssertion", eventType.getBaseType());
    assertEquals(EXTENSION, eventType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = eventType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("type".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(DATA_NAMESPACE, "EventType", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = eventType.getExplicitContent();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(1, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("description".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertContributorType(XSComplexType contributorType) {
    assertEquals("Contributor", contributorType.getName());
    assertFalse(contributorType.isAbstract());
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "anyType", contributorType.getBaseType());
    assertEquals(RESTRICTION, contributorType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = contributorType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("id".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "ID", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = contributorType.getContentType();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(2, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("contactName".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else if ("emails".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        XSType emailListType = elementDecl.getType();
        assertEmailListType(emailListType);
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertRepositoryType(XSComplexType repositoryType) {
    assertEquals("Repository", repositoryType.getName());
    assertFalse(repositoryType.isAbstract());
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "anyType", repositoryType.getBaseType());
    assertEquals(RESTRICTION, repositoryType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = repositoryType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("id".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "ID", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = repositoryType.getContentType();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(2, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("location".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else if ("email".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(CITE_NAMESPACE, "EMail", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertSourceType(XSComplexType sourceType) {
    assertEquals("Source", sourceType.getName());
    assertFalse(sourceType.isAbstract());
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "anyType", sourceType.getBaseType());
    assertEquals(RESTRICTION, sourceType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = sourceType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("id".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "ID", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = sourceType.getContentType();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(4, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("title".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else if ("link".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else if ("infoSets".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(XSParticle.UNBOUNDED, childElement.getMaxOccurs());
        assertQNameEquals(CITE_NAMESPACE, "InfoSet", elementDecl.getType());
      }
      else if ("Repository".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertSame(sourceType.getOwnerSchema().getElementDecls().get("Repository"), elementDecl);
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertInfosetType(XSComplexType infosetType) {
    assertEquals("InfoSet", infosetType.getName());
    assertFalse(infosetType.isAbstract());
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "anyType", infosetType.getBaseType());
    assertEquals(RESTRICTION, infosetType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = infosetType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("id".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "ID", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = infosetType.getContentType();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(4, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("inferences".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(XSParticle.UNBOUNDED, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "IDREF", elementDecl.getType());
      }
      else if ("Contributor".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertSame(infosetType.getOwnerSchema().getElementDecls().get("Contributor"), elementDecl);
      }
      else if ("source".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "IDREF", elementDecl.getType());
      }
      else if ("sourceReference".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertPersonType(XSComplexType personType) {
    assertEquals("Person", personType.getName());
    assertFalse(personType.isAbstract());
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "anyType", personType.getBaseType());
    assertEquals(RESTRICTION, personType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = personType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("id".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "ID", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = personType.getContentType();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(5, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("gender".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(DATA_NAMESPACE, "Gender", elementDecl.getType());
      }
      else if ("names".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(XSParticle.UNBOUNDED, childElement.getMaxOccurs());
        assertQNameEquals(DATA_NAMESPACE, "Name", elementDecl.getType());
      }
      else if ("events".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(XSParticle.UNBOUNDED, childElement.getMaxOccurs());
        assertQNameEquals(DATA_NAMESPACE, "Event", elementDecl.getType());
      }
      else if ("facts".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(XSParticle.UNBOUNDED, childElement.getMaxOccurs());
        assertQNameEquals(DATA_NAMESPACE, "Fact", elementDecl.getType());
      }
      else if ("relationships".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(XSParticle.UNBOUNDED, childElement.getMaxOccurs());
        assertQNameEquals(DATA_NAMESPACE, "Relationship", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertRelationshipType(XSComplexType relationshipType) {
    assertEquals("Relationship", relationshipType.getName());
    assertFalse(relationshipType.isAbstract());
    assertQNameEquals(DATA_NAMESPACE, "Assertion", relationshipType.getBaseType());
    assertEquals(EXTENSION, relationshipType.getDerivationMethod());
    assertEquals(0, relationshipType.getAttributeUses().size());

    XSContentType contentType = relationshipType.getExplicitContent();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(3, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("type".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        XSType relationshipTypeType = elementDecl.getType();
        assertRelationshipTypeType(relationshipTypeType);
      }
      else if ("sourcePersonName".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(DATA_NAMESPACE, "Name", elementDecl.getType());
      }
      else if ("targetPersonName".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(DATA_NAMESPACE, "Name", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertNameType(XSComplexType nameType) {
    assertEquals("Name", nameType.getName());
    assertFalse(nameType.isAbstract());
    assertQNameEquals(DATA_NAMESPACE, "Assertion", nameType.getBaseType());
    assertEquals(EXTENSION, nameType.getDerivationMethod());
    assertEquals(0, nameType.getAttributeUses().size());

    XSContentType contentType = nameType.getExplicitContent();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(1, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("value".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertGenderType(XSComplexType genderType) {
    assertEquals("Gender", genderType.getName());
    assertFalse(genderType.isAbstract());
    assertQNameEquals(DATA_NAMESPACE, "Assertion", genderType.getBaseType());
    assertEquals(EXTENSION, genderType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = genderType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("type".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(DATA_NAMESPACE, "GenderType", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = genderType.getExplicitContent();
    assertNotNull(contentType.asEmpty());
  }

  protected void assertFactType(XSComplexType factType) {
    assertEquals("Fact", factType.getName());
    assertFalse(factType.isAbstract());
    assertQNameEquals(DATA_NAMESPACE, "OccurringAssertion", factType.getBaseType());
    assertEquals(EXTENSION, factType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = factType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("type".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(DATA_NAMESPACE, "FactType", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = factType.getExplicitContent();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(2, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("description".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else if ("value".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertAssertionType(XSComplexType assertionType) {
    assertEquals("Assertion", assertionType.getName());
    assertTrue(assertionType.isAbstract());
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "anyType", assertionType.getBaseType());
    assertEquals(RESTRICTION, assertionType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = assertionType.getAttributeUses();
    assertEquals(1, attributes.size());
    for (XSAttributeUse attribute : attributes) {
      XSAttributeDecl attributeDecl = attribute.getDecl();
      String attributeName = attributeDecl.getName();
      if ("id".equals(attributeName)) {
        assertFalse(attribute.isRequired());
        assertNull(attributeDecl.getDefaultValue());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "ID", attributeDecl.getType());
      }
      else {
        fail("Unknown attribute: " + attributeName);
      }
    }

    XSContentType contentType = assertionType.getContentType();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(2, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("note".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else if ("infoSet".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "IDREF", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertOccurringAssertionType(XSComplexType assertionType) {
    assertEquals("OccurringAssertion", assertionType.getName());
    assertTrue(assertionType.isAbstract());
    assertQNameEquals(DATA_NAMESPACE, "Assertion", assertionType.getBaseType());
    assertEquals(EXTENSION, assertionType.getDerivationMethod());

    Collection<? extends XSAttributeUse> attributes = assertionType.getAttributeUses();
    assertEquals(0, attributes.size());

    XSContentType contentType = assertionType.getExplicitContent();
    XSParticle particle = contentType.asParticle();
    assertNotNull(particle);
    assertTrue(particle.getTerm().isModelGroup());
    XSModelGroup modelGroup = particle.getTerm().asModelGroup();
    assertEquals(XSModelGroup.Compositor.SEQUENCE, modelGroup.getCompositor());
    XSParticle[] childElements = modelGroup.getChildren();
    assertEquals(2, childElements.length);
    for (XSParticle childElement : childElements) {
      assertTrue(childElement.getTerm().isElementDecl());
      XSElementDecl elementDecl = childElement.getTerm().asElementDecl();
      String childElementName = elementDecl.getName();
      if ("date".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "dateTime", elementDecl.getType());
      }
      else if ("place".equals(childElementName)) {
        assertEquals(0, childElement.getMinOccurs());
        assertEquals(1, childElement.getMaxOccurs());
        assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", elementDecl.getType());
      }
      else {
        fail("Unknown child element: " + childElementName);
      }
    }
  }

  protected void assertGenderTypeType(XSSimpleType genderTypeType) {
    assertEquals("GenderType", genderTypeType.getName());
    assertTrue(genderTypeType.isRestriction());
    XSRestrictionSimpleType restriction = genderTypeType.asRestriction();
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", restriction.getSimpleBaseType());

    //test all the facets of the enumeration...
    HashSet<String> enums = new HashSet<String>(Arrays.asList("m", "f"));
    List<XSFacet> facets = restriction.getDeclaredFacets(XSFacet.FACET_ENUMERATION);
    assertEquals(enums.size(), facets.size());
    for (XSFacet facet : facets) {
      assertTrue(enums.remove(facet.getValue().toString()));
    }
    assertTrue(enums.isEmpty());
  }

  protected void assertFactTypeType(XSSimpleType factTypeType) {
    assertEquals("FactType", factTypeType.getName());
    assertTrue(factTypeType.isRestriction());
    XSRestrictionSimpleType restriction = factTypeType.asRestriction();
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", restriction.getSimpleBaseType());

    //test all the facets of the enumeration...
    HashSet<String> enums = new HashSet<String>(Arrays.asList("occupation", "possessions", "race", "nation_of_origin", "physical_description"));
    List<XSFacet> facets = restriction.getDeclaredFacets(XSFacet.FACET_ENUMERATION);
    assertEquals(enums.size(), facets.size());
    for (XSFacet facet : facets) {
      assertTrue(enums.remove(facet.getValue().toString()));
    }
    assertTrue(enums.isEmpty());
  }

  protected void assertEventTypeType(XSSimpleType eventTypeType) {
    assertEquals("EventType", eventTypeType.getName());
    assertTrue(eventTypeType.isRestriction());
    XSRestrictionSimpleType restriction = eventTypeType.asRestriction();
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", restriction.getSimpleBaseType());

    //test all the facets of the enumeration...
    HashSet<String> enums = new HashSet<String>(Arrays.asList("birth", "christening", "marriage", "death", "burial"));
    List<XSFacet> facets = restriction.getDeclaredFacets(XSFacet.FACET_ENUMERATION);
    assertEquals(enums.size(), facets.size());
    for (XSFacet facet : facets) {
      assertTrue(enums.remove(facet.getValue().toString()));
    }
    assertTrue(enums.isEmpty());
  }

  protected void assertRelationshipTypeType(XSType relationshipTypeType) {
    assertTrue(relationshipTypeType.isLocal());
    assertTrue(relationshipTypeType.isSimpleType());
    XSSimpleType asSimpleType = relationshipTypeType.asSimpleType();
    assertTrue(asSimpleType.isRestriction());
    XSRestrictionSimpleType restriction = asSimpleType.asRestriction();
    assertQNameEquals(W3C_XML_SCHEMA_NS_URI, "string", restriction.getSimpleBaseType());

    //test all the facets of the enumeration...
    HashSet<String> enums = new HashSet<String>(Arrays.asList("spouse", "parent", "child"));
    List<XSFacet> facets = restriction.getDeclaredFacets(XSFacet.FACET_ENUMERATION);
    assertEquals(enums.size(), facets.size());
    for (XSFacet facet : facets) {
      assertTrue(enums.remove(facet.getValue().toString()));
    }
    assertTrue(enums.isEmpty());
  }

  protected void assertEmailListType(XSType emailListType) {
    assertFalse(emailListType.isGlobal());
    assertTrue(emailListType.isSimpleType());
    XSSimpleType asSimpleType = emailListType.asSimpleType();
    assertTrue(asSimpleType.isList());
    XSListSimpleType listType = asSimpleType.asList();
    assertQNameEquals(CITE_NAMESPACE, "EMail", listType.getItemType());
  }

  protected void assertPersonRootElement(XSElementDecl personElement) {
    assertEquals("Person", personElement.getName());
    XSType personType = personElement.getType();
    assertEquals("Person", personType.getName());
    assertEquals(DATA_NAMESPACE, personType.getTargetNamespace());
  }

  public static void assertQNameEquals(String targetNamespace, String name, XSDeclaration declaration) {
    assertEquals(new QName(targetNamespace, name), getQName(declaration));
  }

  public static QName getQName(XSDeclaration declaration) {
    return new QName(declaration.getTargetNamespace(), declaration.getName());
  }

  private static class ThrowEverythingHandler implements ErrorHandler {

    public void warning(SAXParseException exception) throws SAXException {
      throw new SAXException(exception);
    }

    public void error(SAXParseException exception) throws SAXException {
      throw new SAXException(exception);
    }

    public void fatalError(SAXParseException exception) throws SAXException {
      throw new SAXException(exception);
    }
  }

}
