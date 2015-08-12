package com.webcohesion.enunciate.modules.jackson;

import com.fasterxml.jackson.annotation.JacksonAnnotation;
import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.*;
import org.reflections.adapters.MetadataAdapter;

import javax.lang.model.element.*;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJacksonModule extends BasicEnunicateModule implements TypeFilteringModule, MediaTypeDefinitionModule, ApiRegistryAwareModule, ApiProviderModule {

  private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
  private boolean jacksonDetected = false;
  private boolean jaxbSupportDetected = false;
  private EnunciateJacksonContext jacksonContext;
  private ApiRegistry apiRegistry;

  @Override
  public String getName() {
    return "jackson";
  }

  public boolean isHonorJaxbAnnotations() {
    return this.config.getBoolean("[@honorJaxb]", this.jaxbSupportDetected);
  }

  @Override
  public void setApiRegistry(ApiRegistry registry) {
    this.apiRegistry = registry;
  }

  @Override
  public boolean isEnabled() {
    return !this.config.getBoolean("[@disabled]", !jacksonDetected);
  }

  @Override
  public void call(EnunciateContext context) {
    this.jacksonContext = new EnunciateJacksonContext(context, isHonorJaxbAnnotations());
    DataTypeDetectionStrategy detectionStrategy = getDataTypeDetectionStrategy();
    if (detectionStrategy != DataTypeDetectionStrategy.passive) {
      Set<? extends Element> elements = detectionStrategy == DataTypeDetectionStrategy.local ? context.getRoundEnvironment().getRootElements() : context.getApiElements();
      for (Element declaration : elements) {
        addPotentialJacksonElement(declaration, new LinkedList<Element>());
      }
    }
  }

  public DataTypeDetectionStrategy getDataTypeDetectionStrategy() {
    String dataTypeDetection = this.config.getString("[@datatype-detection]", null);

    if (dataTypeDetection != null) {
      try {
        return DataTypeDetectionStrategy.valueOf(dataTypeDetection);
      }
      catch (IllegalArgumentException e) {
        //fall through...
      }
    }

    return this.defaultDataTypeDetectionStrategy == null ? DataTypeDetectionStrategy.local : this.defaultDataTypeDetectionStrategy;
  }

  @Override
  public void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy) {
    this.defaultDataTypeDetectionStrategy = strategy;
  }

  @Override
  public void addDataTypeDefinition(Element element, Set<String> declaredMediaTypes, LinkedList<Element> contextStack) {
    boolean jsonApplies = false;
    for (String mediaType : declaredMediaTypes) {
      if ("*/*".equals(mediaType) || "text/*".equals(mediaType) || "application/*".equals(mediaType) || "application/json".equals(mediaType) || mediaType.endsWith("+json")) {
        jsonApplies = true;
        break;
      }
    }

    if (jsonApplies) {
      addPotentialJacksonElement(element, contextStack);
    }
    else {
      debug("Element %s is NOT to be added as a Jackson data type because %s doesn't seem to include JSON.", element, declaredMediaTypes);
    }
  }

  protected void addPotentialJacksonElement(Element declaration, LinkedList<Element> contextStack) {
    if (declaration instanceof TypeElement) {
      if (!this.jacksonContext.isKnownTypeDefinition((TypeElement) declaration) && isExplicitTypeDefinition(declaration, this.jacksonContext.isHonorJaxb())) {
        if (this.jacksonContext.getTypeDefinitions().isEmpty()) {
          //if this is the first type definition, make sure we register the JSON syntax.
          apiRegistry.getSyntaxes().add(this.jacksonContext);
        }
        this.jacksonContext.add(this.jacksonContext.createTypeDefinition((TypeElement) declaration), contextStack);
      }
    }
  }

  protected boolean isExplicitTypeDefinition(Element declaration, boolean honorJaxb) {
    if (declaration.getKind() != ElementKind.CLASS) {
      debug("%s isn't a potential Jackson type because it's not a class.", declaration);
      return false;
    }

    PackageElement pckg = this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration);
    if ((pckg != null) && (pckg.getAnnotation(Ignore.class) != null)) {
      debug("%s isn't a potential Jackson type because its package is annotated as to be ignored.", declaration);
      return false;
    }

    if (isThrowable(declaration)) {
      debug("%s isn't a potential Jackson type because it's an instance of java.lang.Throwable.", declaration);
      return false;
    }

    List<? extends AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
    boolean explicitXMLTypeOrElement = false;
    for (AnnotationMirror mirror : annotationMirrors) {
      Element annotationDeclaration = mirror.getAnnotationType().asElement();
      if (annotationDeclaration != null) {
        String fqn = annotationDeclaration instanceof TypeElement ? ((TypeElement)annotationDeclaration).getQualifiedName().toString() : "";
        //exclude all XmlTransient types and all jaxws types.
        if (JsonIgnore.class.getName().equals(fqn)
          || fqn.startsWith("javax.xml.ws")
          || fqn.startsWith("javax.ws.rs")
          || fqn.startsWith("javax.jws")) {
          debug("%s isn't a potential Jackson type because of annotation %s.", declaration, fqn);
          return false;
        }
        else {
          if (honorJaxb) {
            if (XmlTransient.class.getName().equals(fqn)) {
              debug("%s isn't a potential Jackson type because of annotation %s.", declaration, fqn);
              return false;
            }

            if ((XmlType.class.getName().equals(fqn)) || (XmlRootElement.class.getName().equals(fqn))) {
              debug("%s will be considered a Jackson type because we're honoring the %s annotation.", declaration, fqn);
              explicitXMLTypeOrElement = true;
            }
          }

          explicitXMLTypeOrElement = explicitXMLTypeOrElement || isJacksonSerializationAnnotation(fqn);
        }
      }

      if (explicitXMLTypeOrElement) {
        break;
      }
    }

    return explicitXMLTypeOrElement;
  }

  /**
   * Whether the specified declaration is throwable.
   *
   * @param declaration The declaration to determine whether it is throwable.
   * @return Whether the specified declaration is throwable.
   */
  protected boolean isThrowable(Element declaration) {
    return declaration.getKind() == ElementKind.CLASS && ((DecoratedTypeMirror) declaration.asType()).isInstanceOf(Throwable.class);
  }

  @Override
  public boolean acceptType(Object type, MetadataAdapter metadata) {
    String classname = metadata.getClassName(type);
    this.jacksonDetected |= ObjectMapper.class.getName().equals(classname);
    this.jaxbSupportDetected |= "com.fasterxml.jackson.module.jaxb.JaxbAnnotationIntrospector".equals(classname);

    List<String> classAnnotations = metadata.getClassAnnotationNames(type);
    if (classAnnotations != null) {
      for (String fqn : classAnnotations) {
        if (isJacksonSerializationAnnotation(fqn)) {
          return true;
        }
      }
    }
    return false;
  }

  boolean isJacksonSerializationAnnotation(String fqn) {
    return !JacksonAnnotation.class.getName().equals(fqn) && (JsonSerialize.class.getName().equals(fqn) || fqn.startsWith(JsonFormat.class.getPackage().getName()));
  }
}
