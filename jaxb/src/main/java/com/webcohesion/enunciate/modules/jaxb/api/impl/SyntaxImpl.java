package com.webcohesion.enunciate.modules.jaxb.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.model.ComplexTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.SchemaInfo;
import com.webcohesion.enunciate.modules.jaxb.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlTypeFactory;
import org.w3c.dom.Document;
import org.xml.sax.InputSource;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import jakarta.xml.bind.JAXBElement;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.Reader;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class SyntaxImpl implements Syntax {

  public static final String SYNTAX_LABEL = "XML";

  private final EnunciateJaxbContext context;
  private ApiRegistrationContext registrationContext;

  public SyntaxImpl(EnunciateJaxbContext context, ApiRegistrationContext registrationContext) {
    this.context = context;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getId() {
    return "jaxb";
  }

  @Override
  public int compareTo(Syntax syntax) {
    return getId().compareTo(syntax.getId());
  }

  @Override
  public String getSlug() {
    return "syntax_xml";
  }

  @Override
  public String getLabel() {
    return SYNTAX_LABEL;
  }

  @Override
  public boolean isEmpty() {
    return this.context.getSchemas().isEmpty();
  }

  @Override
  public List<Namespace> getNamespaces() {
    ArrayList<Namespace> namespaces = new ArrayList<Namespace>();
    for (SchemaInfo schemaInfo : this.context.getSchemas().values()) {
      namespaces.add(new NamespaceImpl(schemaInfo, registrationContext));
    }
    return namespaces;
  }

  @Override
  public boolean isAssignableToMediaType(String mediaType) {
    return mediaType != null && (mediaType.equals("*/*") || mediaType.equals("application/*") || mediaType.equals("text/*") || mediaType.endsWith("/xml") || mediaType.endsWith("+xml"));
  }

  @Override
  public MediaTypeDescriptor findMediaTypeDescriptor(String mediaType, DecoratedTypeMirror typeMirror) {
    if (mediaType == null) {
      return null;
    }

    //if it's a wildcard, we'll return an implicit descriptor.
    if (mediaType.equals("*/*") || mediaType.equals("application/*")) {
      mediaType = "application/xml";
    }
    else if (mediaType.equals("text/*")) {
      mediaType = "text/xml";
    }

    if (mediaType.endsWith("/xml") || mediaType.endsWith("+xml")) {
      DataTypeReference typeReference = findDataTypeReference(typeMirror);
      return typeReference == null ? null : new MediaTypeDescriptorImpl(mediaType, typeReference);
    }
    else {
      return null;
    }
  }

  private DataTypeReference findDataTypeReference(DecoratedTypeMirror typeMirror) {
    if (typeMirror == null) {
      return null;
    }

    if (typeMirror.isInstanceOf(JAXBElement.class)) {
      List<? extends TypeMirror> typeArguments = ((DecoratedDeclaredType) typeMirror).getTypeArguments();
      typeMirror = TypeMirrorUtils.objectType(this.context.getContext().getProcessingEnvironment());
      if (typeArguments != null && !typeArguments.isEmpty()) {
        typeMirror = (DecoratedTypeMirror) typeArguments.get(0);
      }
    }

    XmlType xmlType;

    try {
      xmlType = XmlTypeFactory.getXmlType(typeMirror, this.context);
    }
    catch (Exception e) {
      xmlType = null;
    }

    return xmlType == null ? null : new DataTypeReferenceImpl(xmlType, typeMirror.isCollection() || typeMirror.isArray(), registrationContext);
  }

  @Override
  public List<DataType> findDataTypes(String name) {
    if (name != null && !name.isEmpty()) {
      TypeElement typeElement = this.context.getContext().getProcessingEnvironment().getElementUtils().getTypeElement(name);
      if (typeElement != null) {
        TypeDefinition typeDefinition = this.context.findTypeDefinition(typeElement);
        if (typeDefinition instanceof ComplexTypeDefinition) {
          return Collections.singletonList((DataType) new ComplexDataTypeImpl((ComplexTypeDefinition) typeDefinition, registrationContext));
        }
        else if (typeDefinition instanceof EnumTypeDefinition) {
          return Collections.singletonList((DataType) new EnumDataTypeImpl((EnumTypeDefinition) typeDefinition, registrationContext));
        }
      }
    }

    return Collections.emptyList();
  }

  @Override
  public Example parseExample(Reader example) throws Exception {
    DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
    builderFactory.setNamespaceAware(true);
    DocumentBuilder domBuilder = builderFactory.newDocumentBuilder();
    Document document = domBuilder.parse(new InputSource(example));
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
    return new CustomExampleImpl(value.toString());
  }

}
