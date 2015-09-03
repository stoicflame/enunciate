package com.webcohesion.enunciate.modules.java_json_client;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnumRef;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class ClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final MergedJsonContext jsonContext;

  public ClientClassnameForMethod(Map<String, String> conversions, MergedJsonContext context) {
    super(conversions, context.getContext());
    this.jsonContext = context;
  }

  @Override
  public String convertUnwrappedObject(Object unwrapped) throws TemplateModelException {
    if (unwrapped instanceof Entity) {
      List<? extends MediaTypeDescriptor> mediaTypes = ((Entity) unwrapped).getMediaTypes();
      for (MediaTypeDescriptor mediaType : mediaTypes) {
        if (this.jsonContext.getLabel().equals(mediaType.getSyntax())) {
          DataTypeReference dataType = mediaType.getDataType();
          return super.convertUnwrappedObject(this.jsonContext.findType(dataType));
        }
      }

      return "byte[]";
    }

    return super.convertUnwrappedObject(unwrapped);
  }

  @Override
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    TypeMirror adaptingType = this.jsonContext.findAdaptingType(element);
    if (adaptingType != null) {
      return convert(adaptingType);
    }
    else if (element instanceof Element && ((Element) element).getAnnotation(XmlQNameEnumRef.class) != null) {
      return "String";
    }
    else {
      return super.convert(element);
    }
  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    TypeMirror adaptingType = jsonContext.findAdaptingType(declaration);
    if (adaptingType != null) {
      return convert(adaptingType);
    }
    if (declaration.getKind() == ElementKind.CLASS) {
      DecoratedTypeMirror superType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(declaration.getSuperclass(), this.context.getProcessingEnvironment());
      if (superType != null && superType.isInstanceOf(JAXBElement.class.getName())) {
        //for client conversions, we're going to generalize subclasses of JAXBElement to JAXBElement
        return convert(superType);
      }
    }
    String convertedPackage = convertPackage(this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration));
    ClientName specifiedName = declaration.getAnnotation(ClientName.class);
    String simpleName = specifiedName == null ? declaration.getSimpleName().toString() : specifiedName.value();
    return convertedPackage + getPackageSeparator() + simpleName;
  }
}
