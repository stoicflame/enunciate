package net.sf.enunciate.modules.xml;

import junit.framework.TestCase;
import net.sf.enunciate.modules.xml.config.SchemaConfig;
import net.sf.enunciate.modules.xml.config.WsdlConfig;
import net.sf.enunciate.modules.DeploymentModule;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.main.Enunciate;
import static net.sf.enunciate.EnunciateTestUtil.getAllJavaFiles;
import static net.sf.enunciate.InAPTTestCase.getInAPTClasspath;

import java.util.*;
import java.io.File;

import com.sun.xml.xsom.parser.XSOMParser;
import com.sun.xml.xsom.*;

import static javax.xml.XMLConstants.W3C_XML_SCHEMA_NS_URI;
import static com.sun.xml.xsom.XSType.*;
import javax.xml.namespace.QName;

import org.xml.sax.ErrorHandler;
import org.xml.sax.SAXParseException;
import org.xml.sax.SAXException;

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
    Enunciate enunciate = new Enunciate(getAllJavaFiles("full"));
    enunciate.setConfig(config);
    enunciate.setTarget(Enunciate.Target.GENERATE);
    enunciate.setClasspath(getInAPTClasspath());
    enunciate.execute();

    File dataSchemaFile = new File(enunciate.getGenerateDir(), "xml/data.xsd");
    File citationSchemaFile = new File(enunciate.getGenerateDir(), "xml/cite.xsd");
    File wsdlFile = new File(enunciate.getGenerateDir(), "xml/full.wsdl");

    assertTrue(dataSchemaFile.exists());
    assertTrue(citationSchemaFile.exists());
    assertTrue(wsdlFile.exists());

    XSOMParser parser = new XSOMParser();
    parser.setErrorHandler(new ThrowEverythingHandler());
    parser.parse(dataSchemaFile);
    XSSchemaSet schemaSet = parser.getResult();
    XSSchema citeSchema = schemaSet.getSchema(CITE_NAMESPACE);
    assertNull("The cite schema shouldn't have been imported.", citeSchema);
    XSSchema dataSchema = schemaSet.getSchema(DATA_NAMESPACE);
    assertNotNull(dataSchema);
    assertDataSchemaStructure(dataSchema);  
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
