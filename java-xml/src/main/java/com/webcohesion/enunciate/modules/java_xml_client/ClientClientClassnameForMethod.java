package com.webcohesion.enunciate.modules.java_xml_client;

import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.resources.Entity;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.ClientName;
import com.webcohesion.enunciate.modules.jaxb.EnunciateJaxbContext;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxb.model.Accessor;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.Adaptable;
import com.webcohesion.enunciate.modules.jaxb.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlClassType;
import com.webcohesion.enunciate.modules.jaxb.model.types.XmlType;
import com.webcohesion.enunciate.modules.jaxb.model.util.JAXBUtil;
import com.webcohesion.enunciate.util.HasClientConvertibleType;
import freemarker.template.TemplateModelException;

import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.xml.bind.JAXBElement;
import java.util.List;
import java.util.Map;

/**
 * @author Ryan Heaton
 */
public class ClientClientClassnameForMethod extends com.webcohesion.enunciate.util.freemarker.ClientClassnameForMethod {

  private final EnunciateJaxbContext jaxbContext;

  public ClientClientClassnameForMethod(Map<String, String> conversions, EnunciateJaxbContext context) {
    super(conversions, context.getContext());
    this.jaxbContext = context;
  }

  @Override
  public String convertUnwrappedObject(Object unwrapped) throws TemplateModelException {
    if (unwrapped instanceof Entity) {
      List<? extends MediaTypeDescriptor> mediaTypes = ((Entity) unwrapped).getMediaTypes();
      for (MediaTypeDescriptor mediaType : mediaTypes) {
        if (mediaType.getSyntax().equals(this.jaxbContext.getLabel())) {
          DataTypeReference dataType = mediaType.getDataType();
          if (dataType instanceof DataTypeReferenceImpl) {
            XmlType xmlType = ((DataTypeReferenceImpl) dataType).getXmlType();
            if (xmlType instanceof XmlClassType) {
              return ((XmlClassType) xmlType).getTypeDefinition().getQualifiedName().toString();
            }
          }
        }
      }

      return "byte[]";
    }

    return super.convertUnwrappedObject(unwrapped);
  }

  @Override
  public String convert(HasClientConvertibleType element) throws TemplateModelException {
    if (element instanceof Accessor && ((Accessor)element).isXmlList() && !((Accessor)element).isAdapted() && ((Accessor)element).getBareAccessorType().isInterface()) {
      if (((Accessor)element).isCollectionType()) {
        return "java.util.List<Object>";
      }
      else {
        return "Object";
      }
    }
    else if (element instanceof Adaptable && ((Adaptable) element).isAdapted()) {
      return convert(((Adaptable) element).getAdapterType().getAdaptingType((DecoratedTypeMirror) element.getClientConvertibleType(), this.context));
    }
    else {
      return super.convert(element);
    }
  }

  @Override
  public String convert(TypeElement declaration) throws TemplateModelException {
    AdapterType adapterType = JAXBUtil.findAdapterType(declaration, this.jaxbContext);
    if (adapterType != null) {
      return convert(adapterType.getAdaptingType());
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
