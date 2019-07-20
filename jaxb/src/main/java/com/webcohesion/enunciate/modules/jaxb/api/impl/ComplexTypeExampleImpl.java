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
package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.DocumentationExample;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.Attribute;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.ElementDeclaration;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;
import com.webcohesion.enunciate.util.TypeHintUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Element;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
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
import java.util.Collections;
import java.util.LinkedList;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class ComplexTypeExampleImpl extends ExampleImpl {

  private final ComplexTypeDefinition typeDefinition;
  private final List<DataTypeReference.ContainerType> containers;
  private final ApiRegistrationContext registrationContext;

  public ComplexTypeExampleImpl(ComplexTypeDefinition type, ApiRegistrationContext registrationContext) {
    this(type, null, registrationContext);
  }

  public ComplexTypeExampleImpl(ComplexTypeDefinition typeDefinition, List<DataTypeReference.ContainerType> containers, ApiRegistrationContext registrationContext) {
    this.typeDefinition = typeDefinition;
    this.containers = containers == null ? Collections.<DataTypeReference.ContainerType>emptyList() : containers;
    this.registrationContext = registrationContext;
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

      Element outer = rootElement;
      for (DataTypeReference.ContainerType container : this.containers) {
        Element containerEl = document.createElementNS("", container.name());
        containerEl.appendChild(outer);
        outer = containerEl;
      }

      document.appendChild(outer);

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
      FacetFilter facetFilter = registrationContext.getFacetFilter();
      for (Attribute attribute : type.getAttributes()) {
        if (ElementUtils.findDeprecationMessage(attribute, null) != null) {
          continue;
        }

        if (!facetFilter.accept(attribute)) {
          continue;
        }

        String example = "...";

        JavaDoc.JavaDocTagList tags = getDocumentationExampleTags(attribute);
        if (tags != null && tags.size() > 0) {
          String tag = tags.get(0).trim();
          example = tag.isEmpty() ? null : tag;
        }

        DocumentationExample documentationExample = getDocumentationExample(attribute);
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

        String configuredExample = getConfiguredExample(attribute);
        if (configuredExample != null) {
          example = configuredExample;
        }

        rootElement.setAttributeNS(attribute.getNamespace(), attribute.getName(), example);
        if (attribute.getNamespace() == null) {
          defaultNamespace = null;
        }
      }

      if (type.getValue() != null) {
        String example = "...";

        JavaDoc.JavaDocTagList tags = getDocumentationExampleTags(type.getValue());
        if (tags != null && tags.size() > 0) {
          String tag = tags.get(0).trim();
          example = tag.isEmpty() ? null : tag;
        }

        DocumentationExample documentationExample = getDocumentationExample(type.getValue());
        if (documentationExample != null) {
          if (!"##default".equals(documentationExample.value())) {
            example = documentationExample.value();
          }
        }

        String configuredExample = getConfiguredExample(type.getValue());
        if (configuredExample != null) {
          example = configuredExample;
        }

        rootElement.setTextContent(example);
      }
      else {
        for (com.webcohesion.enunciate.modules.jaxb.model.Element element : type.getElements()) {
          if (ElementUtils.findDeprecationMessage(element, null) != null) {
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

            JavaDoc.JavaDocTagList tags = choice.getJavaDoc().get("documentationType");
            if (tags != null && tags.size() > 0) {
              String tag = tags.get(0).trim();
              if (!tag.isEmpty()) {
                TypeElement typeElement = type.getContext().getContext().getProcessingEnvironment().getElementUtils().getTypeElement(tag);
                if (typeElement != null) {
                  baseType = XmlTypeFactory.getXmlType(typeElement.asType(), type.getContext());
                }
                else {
                  type.getContext().getContext().getLogger().warn("Invalid documentation type %s.", tag);
                }
              }
            }

            DocumentationExample documentationExample = getDocumentationExample(choice);
            if (documentationExample != null) {
              TypeMirror typeHint = TypeHintUtils.getTypeHint(documentationExample.type(), type.getContext().getContext().getProcessingEnvironment(), null);
              if (typeHint != null) {
                baseType = XmlTypeFactory.getXmlType(typeHint, type.getContext());
              }
            }

            if (baseType instanceof XmlClassType && ((XmlClassType) baseType).getTypeDefinition() instanceof ComplexTypeDefinition) {
              String defaultChildNs = build(childElement, (ComplexTypeDefinition) ((XmlClassType) baseType).getTypeDefinition(), document, context);
              if (defaultChildNs == null) {
                defaultNamespace = null;
              }
            }
            else {
              String example = "...";

              tags = getDocumentationExampleTags(choice);
              if (tags != null && tags.size() > 0) {
                String tag = tags.get(0).trim();
                example = tag.isEmpty() ? null : tag;
              }

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

              String configuredExample = getConfiguredExample(choice);
              if (configuredExample != null) {
                example = configuredExample;
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

      if (type.getAnyElement() != null && ElementUtils.findDeprecationMessage(type.getAnyElement(), null) == null) {
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

  private DocumentationExample getDocumentationExample(Accessor member) {
    DocumentationExample annotation = member.getAnnotation(DocumentationExample.class);
    if (annotation == null) {
      DecoratedTypeMirror accessorType = member.getBareAccessorType();
      if (accessorType instanceof DecoratedDeclaredType) {
        annotation = ((DecoratedDeclaredType) accessorType).asElement().getAnnotation(DocumentationExample.class);
      }
    }
    return annotation;
  }

  private JavaDoc.JavaDocTagList getDocumentationExampleTags(Accessor member) {
    JavaDoc.JavaDocTagList tags = member.getJavaDoc().get("documentationExample");
    if (tags == null || tags.isEmpty()) {
      DecoratedTypeMirror accessorType = member.getBareAccessorType();
      if (accessorType instanceof DecoratedDeclaredType) {
        javax.lang.model.element.Element element = ((DecoratedDeclaredType) accessorType).asElement();
        tags = element instanceof DecoratedElement ? ((DecoratedElement) element).getJavaDoc().get("documentationExample") : null;
      }
    }
    return tags;
  }

  private String getConfiguredExample(Accessor member) {
    String configuredExample = null;
    DecoratedTypeMirror accessorType = member.getBareAccessorType();
    if (accessorType instanceof DecoratedDeclaredType) {
      javax.lang.model.element.Element element = ((DecoratedDeclaredType) accessorType).asElement();
      if (element instanceof TypeElement) {
        configuredExample = member.getContext().lookupExternalExample((TypeElement) element);
      }
    }
    return configuredExample;
  }

  private static class Context {
    LinkedList<String> stack;
    int currentIndex = 0;
  }
}
