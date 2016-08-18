/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.rt;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.xml.namespace.NamespaceContext;
import javax.xml.namespace.QName;
import javax.xml.stream.*;
import javax.xml.stream.events.*;
import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class IDLFilter implements Filter {

  private ServletContext servletContext = null;
  private XMLInputFactory inputFactory;
  private XMLOutputFactory outputFactory;

  public void init(FilterConfig filterConfig) throws ServletException {
    this.inputFactory = XMLInputFactory.newInstance();
    this.outputFactory = XMLOutputFactory.newInstance();

    this.servletContext = filterConfig.getServletContext();
  }

  public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain) throws IOException, ServletException {
    HttpServletRequest request = (HttpServletRequest) servletRequest;
    RequestURIParts parts = parseParts(request);
    if (parts != null) {
      String realBaseAddress = parts.getBaseAddress();
      String idlPath = parts.getFilePath();
      InputStream idl = this.servletContext.getResourceAsStream(idlPath);
      if (idl != null) {
        servletResponse.setContentType("text/xml");
        String assumedBaseAddress = this.servletContext.getInitParameter("assumed-base-uri");
        try {
          XMLEventReader eventReader = this.inputFactory.createXMLEventReader(idl);
          XMLEventWriter eventWriter = this.outputFactory.createXMLEventWriter(servletResponse.getWriter());
          while (eventReader.hasNext()) {
            XMLEvent event = eventReader.nextEvent();
            if (event.isProcessingInstruction()) {
              String target = ((ProcessingInstruction) event).getTarget();
              if ("enunciate-assumed-base-uri".equals(target)) {
                assumedBaseAddress = ((ProcessingInstruction) event).getData();
                if (assumedBaseAddress.endsWith("/")) {
                  assumedBaseAddress = assumedBaseAddress.substring(0, assumedBaseAddress.length() - 1);
                }
              }
              continue;
            }
            else if (event.getEventType() == XMLStreamConstants.CDATA || event.getEventType() == XMLStreamConstants.CHARACTERS) {
              String data = ((Characters) event).getData();
              if (assumedBaseAddress != null && data.contains(assumedBaseAddress)) {
                data = data.replace(assumedBaseAddress, realBaseAddress);
                event = new DelegatingCharacters(((Characters) event), data);
              }
            }
            else if (event.getEventType() == XMLStreamConstants.START_ELEMENT) {
              List<Attribute> attributes = new ArrayList<Attribute>();
              Iterator attributesIt = ((StartElement) event).getAttributes();
              while (attributesIt.hasNext()) {
                Attribute attribute = (Attribute) attributesIt.next();
                String value = attribute.getValue();
                if (assumedBaseAddress != null && value.contains(assumedBaseAddress)) {
                  value = value.replace(assumedBaseAddress, realBaseAddress);
                  attribute = new DelegatingAttribute(attribute, value);
                  event = new DelegatingStartElement(((StartElement) event), attributes);
                }
                attributes.add(attribute);
              }
            }

            eventWriter.add(event);
          }

          eventReader.close();
          eventWriter.flush();
          eventWriter.close();
          return;
        }
        catch (XMLStreamException e) {
          throw new ServletException(e);
        }
      }
    }

    chain.doFilter(servletRequest, servletResponse);
  }

  protected RequestURIParts parseParts(HttpServletRequest request) {
    StringBuffer requestURI = request.getRequestURL();
    String contextPath = request.getContextPath();
    String baseAddress;
    String filePath;
    if ("".equals(contextPath)) {
      URI uri;
      try {
        uri = new URI(requestURI.toString());
      }
      catch (URISyntaxException e) {
        return null;
      }

      filePath = uri.getPath();
      baseAddress = requestURI.substring(0, requestURI.length() - filePath.length());
    }
    else {
      int splitIndex = requestURI.indexOf(contextPath) + contextPath.length();
      baseAddress = requestURI.substring(0, splitIndex);
      filePath = requestURI.substring(splitIndex);
    }

    return new RequestURIParts(baseAddress, filePath);
  }

  public void destroy() {
  }

  public static class RequestURIParts {

    private final String baseAddress;
    private final String idlPath;

    public RequestURIParts(String baseAddress, String idlPath) {
      this.baseAddress = baseAddress;
      this.idlPath = idlPath;
    }

    public String getBaseAddress() {
      return baseAddress;
    }

    public String getFilePath() {
      return idlPath;
    }
  }

  public static class DelegatingXMLEvent implements XMLEvent {

    private final XMLEvent delegate;

    public DelegatingXMLEvent(XMLEvent delegate) {
      this.delegate = delegate;
    }

    @Override
    public int getEventType() {
      return delegate.getEventType();
    }

    @Override
    public Location getLocation() {
      return delegate.getLocation();
    }

    @Override
    public boolean isStartElement() {
      return delegate.isStartElement();
    }

    @Override
    public boolean isAttribute() {
      return delegate.isAttribute();
    }

    @Override
    public boolean isNamespace() {
      return delegate.isNamespace();
    }

    @Override
    public boolean isEndElement() {
      return delegate.isEndElement();
    }

    @Override
    public boolean isEntityReference() {
      return delegate.isEntityReference();
    }

    @Override
    public boolean isProcessingInstruction() {
      return delegate.isProcessingInstruction();
    }

    @Override
    public boolean isCharacters() {
      return delegate.isCharacters();
    }

    @Override
    public boolean isStartDocument() {
      return delegate.isStartDocument();
    }

    @Override
    public boolean isEndDocument() {
      return delegate.isEndDocument();
    }

    @Override
    public StartElement asStartElement() {
      return delegate.asStartElement();
    }

    @Override
    public EndElement asEndElement() {
      return delegate.asEndElement();
    }

    @Override
    public Characters asCharacters() {
      return delegate.asCharacters();
    }

    @Override
    public QName getSchemaType() {
      return delegate.getSchemaType();
    }

    @Override
    public void writeAsEncodedUnicode(Writer writer) throws XMLStreamException {
      delegate.writeAsEncodedUnicode(writer);
    }
  }

  public static class DelegatingCharacters extends DelegatingXMLEvent implements Characters {

    private final Characters delegate;
    private final String data;

    public DelegatingCharacters(Characters delegate, String data) {
      super(delegate);
      this.delegate = delegate;
      this.data = data;
    }

    @Override
    public String getData() {
      return this.data;
    }

    @Override
    public boolean isWhiteSpace() {
      return delegate.isWhiteSpace();
    }

    @Override
    public boolean isCData() {
      return delegate.isCData();
    }

    @Override
    public boolean isIgnorableWhiteSpace() {
      return delegate.isIgnorableWhiteSpace();
    }

    @Override
    public boolean isCharacters() {
      return true;
    }

    @Override
    public Characters asCharacters() {
      return this;
    }
  }

  public static class DelegatingAttribute extends DelegatingXMLEvent implements Attribute {

    private final Attribute delegate;
    private final String value;

    public DelegatingAttribute(Attribute delegate, String value) {
      super(delegate);
      this.delegate = delegate;
      this.value = value;
    }

    @Override
    public QName getName() {
      return delegate.getName();
    }

    @Override
    public String getValue() {
      return this.value;
    }

    @Override
    public String getDTDType() {
      return delegate.getDTDType();
    }

    @Override
    public boolean isSpecified() {
      return delegate.isSpecified();
    }

    @Override
    public boolean isAttribute() {
      return true;
    }

  }

  public static class DelegatingStartElement extends DelegatingXMLEvent implements StartElement {

    private final StartElement delegate;
    private final List<Attribute> attributes;

    public DelegatingStartElement(StartElement delegate, List<Attribute> attributes) {
      super(delegate);
      this.delegate = delegate;
      this.attributes = attributes;
    }

    @Override
    public QName getName() {
      return delegate.getName();
    }

    @Override
    public Iterator getAttributes() {
      return this.attributes.iterator();
    }

    @Override
    public Iterator getNamespaces() {
      return delegate.getNamespaces();
    }

    @Override
    public Attribute getAttributeByName(QName name) {
      for (Attribute attribute : attributes) {
        if (attribute.getName().equals(name)) {
          return attribute;
        }
      }
      return null;
    }

    @Override
    public NamespaceContext getNamespaceContext() {
      return delegate.getNamespaceContext();
    }

    @Override
    public String getNamespaceURI(String prefix) {
      return delegate.getNamespaceURI(prefix);
    }

    @Override
    public boolean isStartElement() {
      return true;
    }

    @Override
    public StartElement asStartElement() {
      return this;
    }
  }

}
