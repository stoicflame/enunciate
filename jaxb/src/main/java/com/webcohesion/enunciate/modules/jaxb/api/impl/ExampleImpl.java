package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.datatype.Example;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.jaxb.model.Attribute;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.StringWriter;
import java.util.LinkedList;

/**
 * @author Ryan Heaton
 */
public class ExampleImpl implements Example {

  private final ComplexTypeDefinition typeDefinition;

  public ExampleImpl(ComplexTypeDefinition typeDefinition) {
    this.typeDefinition = typeDefinition;
  }

  @Override
  public String getLang() {
    return "xml";
  }

  @Override
  public String getBody() {
    try {
      DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
      builderFactory.setNamespaceAware(true);
      DocumentBuilder domBuilder = builderFactory.newDocumentBuilder();
      Document document = domBuilder.newDocument();

      String rootName = Character.toLowerCase(this.typeDefinition.getSimpleName().charAt(0)) + "-----";
      String rootNamespace = this.typeDefinition.getNamespace();
      ElementDeclaration element = typeDefinition.getContext().findElementDeclaration(typeDefinition);
      if (element != null) {
        rootName = element.getName();
        rootNamespace = element.getNamespace();
      }

      Element rootElement = document.createElementNS(rootNamespace, rootName);
      document.appendChild(rootElement);

      Context context = new Context();
      context.stack = new LinkedList<String>();
      build(rootElement, this.typeDefinition, document, context);

      TransformerFactory transformerFactory = TransformerFactory.newInstance();
      Transformer transformer = transformerFactory.newTransformer();
      transformer.setOutputProperty(OutputKeys.INDENT, "yes");
      transformer.setOutputProperty(OutputKeys.METHOD, "xml");
      transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "yes");
      transformer.setOutputProperty(OutputKeys.ENCODING, "utf-8");
      transformer.setOutputProperty("{http://xml.apache.org/xslt}indent-amount", "2");
      DOMSource source = new DOMSource(document);
      StringWriter value = new StringWriter();
      transformer.transform(source, new StreamResult(value));
      return value.toString();
    }
    catch (ParserConfigurationException e) {
      throw new EnunciateException(e);
    }
    catch (TransformerException e) {
      throw new EnunciateException(e);
    }
  }

  private String build(Element rootElement, ComplexTypeDefinition type, final Document document, Context context) {
    if (context.stack.size() > 2) {
      //don't go deeper than 2 for fear of the OOM (see https://github.com/stoicflame/enunciate/issues/139).
      return rootElement.getNamespaceURI();
    }

    if (context.stack.contains(type.getQualifiedName().toString())) {
      return rootElement.getNamespaceURI();
    }

    String defaultNamespace = rootElement.getNamespaceURI();
    context.stack.push(type.getQualifiedName().toString());
    try {
      FacetFilter facetFilter = type.getContext().getContext().getConfiguration().getFacetFilter();
      for (Attribute attribute : type.getAttributes()) {
        if (ElementUtils.findDeprecationMessage(attribute) != null) {
          continue;
        }

        if (!facetFilter.accept(attribute)) {
          continue;
        }

        String example = "...";
        DocumentationExample documentationExample = attribute.getAnnotation(DocumentationExample.class);
        if (documentationExample != null) {
          if (documentationExample.exclude()) {
            continue;
          }
          else if (context.currentIndex == 1 && !"##default".equals(documentationExample.value2())) {
            example = documentationExample.value2();
          }
          else if (!"##default".equals(documentationExample.value())) {
            example = documentationExample.value();
          }
        }
        rootElement.setAttributeNS(attribute.getNamespace(), attribute.getName(), example);
        if (attribute.getNamespace() == null) {
          defaultNamespace = null;
        }
      }

      if (type.getValue() != null) {
        String example = "...";
        DocumentationExample documentationExample = type.getValue().getAnnotation(DocumentationExample.class);
        if (documentationExample != null) {
          if (!"##default".equals(documentationExample.value())) {
            example = documentationExample.value();
          }
        }

        rootElement.setTextContent(example);
      }
      else {
        for (com.webcohesion.enunciate.modules.jaxb.model.Element element : type.getElements()) {
          if (ElementUtils.findDeprecationMessage(element) != null) {
            continue;
          }

          if (!facetFilter.accept(element)) {
            continue;
          }

          Element currentElement = rootElement;
          if (element.isWrapped()) {
            Element wrapper = document.createElementNS(element.getWrapperNamespace(), element.getWrapperName());
            rootElement.appendChild(wrapper);
            currentElement = wrapper;
            if (element.getWrapperNamespace() == null) {
              defaultNamespace = null;
            }
          }

          for (com.webcohesion.enunciate.modules.jaxb.model.Element choice : element.getChoices()) {
            Element childElement = document.createElementNS(choice.getNamespace(), choice.getName());
            if (choice.getNamespace() == null) {
              defaultNamespace = null;
            }

            XmlType baseType = choice.getXmlType();
            if (baseType instanceof XmlClassType && ((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition) {
              String defaultChildNs = build(childElement, (ComplexTypeDefinition) ((XmlClassType) baseType).getTypeDefinition(), document, context);
              if (defaultChildNs == null) {
                defaultNamespace = null;
              }
            }
            else {
              String example = "...";
              DocumentationExample documentationExample = choice.getAnnotation(DocumentationExample.class);
              if (documentationExample != null) {
                if (documentationExample.exclude()) {
                  continue;
                }
                else if (context.currentIndex == 1 && !"##default".equals(documentationExample.value2())) {
                  example = documentationExample.value2();
                }
                else if (!"##default".equals(documentationExample.value())) {
                  example = documentationExample.value();
                }
              }

              childElement.setTextContent(example);
            }

            currentElement.appendChild(childElement);
          }
        }
      }


      XmlType supertype = type.getBaseType();
      if (supertype instanceof XmlClassType && ((XmlClassType)supertype).getTypeDefinition() instanceof ComplexTypeDefinition) {
        String defaultSuperNs = build(rootElement, (ComplexTypeDefinition) ((XmlClassType) supertype).getTypeDefinition(), document, context);
        if (defaultSuperNs == null) {
          defaultNamespace = null;
        }
      }

      if (type.getAnyElement() != null && ElementUtils.findDeprecationMessage(type.getAnyElement()) == null) {
        Element extension1 = document.createElementNS(defaultNamespace, "extension1");
        extension1.setTextContent("...");
        rootElement.appendChild(extension1);
        Element extension2 = document.createElementNS(defaultNamespace, "extension2");
        extension2.setTextContent("...");
        rootElement.appendChild(extension2);
      }
    }
    finally {
      context.stack.pop();
    }

    return defaultNamespace;
  }

  private static class Context {
    LinkedList<String> stack;
    int currentIndex = 0;
  }
}
