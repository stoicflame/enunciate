package org.codehaus.enunciate.tests;

import epcglobal.epcis_masterdata.xsd._1.*;
import epcglobal.epcis_query.xsd._1.QueryResults;
import epcglobal.epcis_query.xsd._1.QueryResultsBody;
import junit.framework.TestCase;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.bind.JAXBContext;
import javax.xml.bind.JAXBElement;
import javax.xml.namespace.QName;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;

/**
 * @author Ryan Heaton
 */
public class TestAttributeTypeSerialization extends TestCase {

  /**
   * tests serialization of attributetype...
   */
  public void testSerializationOfAttributeType() throws Exception {
    QueryResults results = new QueryResults();
    QueryResultsBody body = new QueryResultsBody();
    VocabularyListType vl = new VocabularyListType();
    VocabularyType vt = new VocabularyType();
    VocabularyElementListType elementListType = new VocabularyElementListType();

    VocabularyElementType elementType = new VocabularyElementType();
    AttributeType at = new AttributeType();
    at.setId("someid");
    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
    Document document = builder.parse(new ByteArrayInputStream("<mynode>value</mynode>".getBytes("utf-8")));
    document.normalizeDocument();
    at.getAny().add(document.getDocumentElement());

    document = builder.newDocument();
    Element el = document.createElement("myelement");
    el.setTextContent("howdy");
    document.appendChild(el);
    elementType.getAttribute().add(at);
    elementListType.getVocabularyElement().add(document.getDocumentElement());
    vt.setVocabularyElementList(elementListType);
    vl.getVocabulary().add(vt);
    body.setVocabularyList(vl);
    results.setResultsBody(body);

    ByteArrayOutputStream out = new ByteArrayOutputStream();
    JAXBContext.newInstance(QueryResults.class).createMarshaller().marshal(new JAXBElement(new QName("urn:hi", "hi"), QueryResults.class, results), out);
//    System.out.println(new String(out.toByteArray()));
  }

}
