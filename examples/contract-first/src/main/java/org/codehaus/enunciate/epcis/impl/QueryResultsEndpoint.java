package org.codehaus.enunciate.epcis.impl;

import epcglobal.epcis_masterdata.xsd._1.*;
import epcglobal.epcis_query.xsd._1.QueryResults;
import epcglobal.epcis_query.xsd._1.QueryResultsBody;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.xml.sax.SAXException;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.ByteArrayInputStream;
import java.io.IOException;

/**
 * @author Ryan Heaton
 */
@Path ("/qr")
public class QueryResultsEndpoint {

  @GET
  public QueryResultsWrapper getQueryResults() {
    QueryResults results = new QueryResults();
    QueryResultsBody body = new QueryResultsBody();
    VocabularyListType vl = new VocabularyListType();
    VocabularyType vt = new VocabularyType();
    VocabularyElementListType elementListType = new VocabularyElementListType();

    VocabularyElementType elementType = new VocabularyElementType();
    AttributeType at = new AttributeType();
    at.setId("someid");
    DocumentBuilder builder;
    Document document;
    try {
      builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
      document = builder.parse(new ByteArrayInputStream("<mynode>value</mynode>".getBytes("utf-8")));
    }
    catch (Exception e) {
      throw new RuntimeException(e);
    }

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

    QueryResultsWrapper wrapper = new QueryResultsWrapper();
    wrapper.setResults(results);
    return wrapper;
  }
}
