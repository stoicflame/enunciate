package com.webcohesion.enunciate.modules.jaxrs;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.module.EnunciateModuleContext;
import com.webcohesion.enunciate.module.MediaTypeDefinitionModule;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceEntityParameter;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceMethod;
import com.webcohesion.enunciate.modules.jaxrs.model.ResourceRepresentationMetadata;
import com.webcohesion.enunciate.modules.jaxrs.model.RootResource;
import com.webcohesion.enunciate.modules.jaxrs.model.util.JaxrsUtil;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.ws.rs.Consumes;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJaxrsContext extends EnunciateModuleContext {

  private final Map<String, String> mediaTypeIds;
  private final List<RootResource> rootResources;
  private final List<TypeElement> providers;
  private final List<MediaTypeDefinitionModule> mediaTypeModules;

  public EnunciateJaxrsContext(EnunciateContext context, List<MediaTypeDefinitionModule> mediaTypeModules) {
    super(context);
    this.mediaTypeModules = mediaTypeModules;
    this.mediaTypeIds = loadKnownMediaTypes();
    this.rootResources = new ArrayList<RootResource>();
    this.providers = new ArrayList<TypeElement>();
  }

  protected Map<String, String> loadKnownMediaTypes() {
    HashMap<String, String> mediaTypes = new HashMap<String, String>();
    mediaTypes.put(MediaType.APPLICATION_ATOM_XML, "atom");
    mediaTypes.put(MediaType.APPLICATION_FORM_URLENCODED, "form");
    mediaTypes.put(MediaType.APPLICATION_JSON, "json");
    mediaTypes.put(MediaType.APPLICATION_OCTET_STREAM, "bin");
    mediaTypes.put(MediaType.APPLICATION_SVG_XML, "svg");
    mediaTypes.put(MediaType.APPLICATION_XHTML_XML, "xhtml");
    mediaTypes.put(MediaType.APPLICATION_XML, "xml");
    mediaTypes.put(MediaType.MULTIPART_FORM_DATA, "multipart");
    mediaTypes.put(MediaType.TEXT_HTML, "html");
    mediaTypes.put(MediaType.TEXT_PLAIN, "text");
    return mediaTypes;
  }

  public EnunciateContext getContext() {
    return context;
  }

  public Map<String, String> getMediaTypeIds() {
    //todo: configure media type ids?
    return mediaTypeIds;
  }

  /**
   * Add a content type.
   *
   * @param mediaType The content type to add.
   */
  public void addMediaType(String mediaType) {
    if (!mediaTypeIds.containsKey(mediaType)) {
      String id = getDefaultContentTypeId(mediaType);
      if (id != null) {
        mediaTypeIds.put(mediaType, id);
      }
    }
  }

  /**
   * Get the default content type id for the specified content type.
   *
   * @param contentType The content type.
   * @return The default content type id, or null if the content type is a wildcard type.
   */
  protected String getDefaultContentTypeId(String contentType) {
    String id = contentType;
    if (id.endsWith("/")) {
      throw new IllegalArgumentException("Illegal content type: " + id);
    }

    int semiColon = id.indexOf(';');
    if (semiColon > -1) {
      id = id.substring(0, semiColon);
    }

    int lastSlash = id.lastIndexOf('/');
    if (lastSlash > -1) {
      id = id.substring(lastSlash + 1);
    }

    int plus = id.indexOf('+');
    if (plus > -1) {
      id = id.substring(0, plus);
    }

    if (id.contains("*")) {
      //wildcard types have no ids.
      return null;
    }
    else {
      return id;
    }
  }

  public List<RootResource> getRootResources() {
    return rootResources;
  }

  public List<TypeElement> getProviders() {
    return providers;
  }

  public Set<String> getCustomResourceParameterAnnotations() {
    //todo:
    throw new UnsupportedOperationException();
  }

  public Set<String> getSystemResourceParameterAnnotations() {
    //todo:
    throw new UnsupportedOperationException();
  }


  /**
   * Add a root resource to the model.
   *
   * @param rootResource The root resource to add to the model.
   */
  public void add(RootResource rootResource) {
    LinkedList<Element> stack = new LinkedList<Element>();
    stack.push(rootResource);
    try {
      for (ResourceMethod resourceMethod : rootResource.getResourceMethods(true)) {
        addReferencedDataTypeDefinitions(resourceMethod, stack);
      }

      this.rootResources.add(rootResource);
    }
    finally {
      stack.pop();
    }
  }

  /**
   * Add the referenced type definitions for the specified resource method.
   *
   * @param resourceMethod The resource method.
   * @param stack The context stack.
   */
  protected void addReferencedDataTypeDefinitions(ResourceMethod resourceMethod, LinkedList<Element> stack) {
    stack.push(resourceMethod);

    try {
      ResourceEntityParameter ep = resourceMethod.getEntityParameter();
      if (ep != null) {
        TypeMirror type = ep.getType();
        if (type.getKind() == TypeKind.DECLARED) {
          Element element = ((DeclaredType) type).asElement();
          if (element != null) {
            stack.push(element);
            try {
              for (MediaTypeDefinitionModule module : this.mediaTypeModules) {
                module.addDataTypeDefinition(element, resourceMethod.getProducesMediaTypes(), stack);
              }
            }
            finally {
              stack.pop();
            }
          }
        }
      }

      ResourceRepresentationMetadata outputPayload = resourceMethod.getRepresentationMetadata();
      if (outputPayload != null) {
        TypeMirror returnType = outputPayload.getDelegate();
        if (returnType.getKind() == TypeKind.DECLARED) {
          Element element = ((DeclaredType) returnType).asElement();
          if (element != null) {
            stack.push(element);
            try {
              for (MediaTypeDefinitionModule module : this.mediaTypeModules) {
                module.addDataTypeDefinition(element, resourceMethod.getConsumesMediaTypes(), stack);
              }
            }
            finally {
              stack.pop();
            }
          }
        }
      }

      //todo: include referenced type definitions from the errors?
    }
    finally {
      stack.pop();
    }
  }

  /**
   * Add a JAX-RS provider to the model.
   *
   * @param declaration The declaration of the provider.
   */
  public void addJAXRSProvider(TypeElement declaration) {
    this.providers.add(declaration);

    Produces produces = declaration.getAnnotation(Produces.class);
    if (produces != null) {
      for (String contentType : JaxrsUtil.value(produces)) {
        try {
          MediaType mt = MediaType.valueOf(contentType);
          addMediaType(mt.getType() + "/" + mt.getSubtype());
        }
        catch (Exception e) {
          addMediaType(contentType);
        }
      }
    }

    Consumes consumes = declaration.getAnnotation(Consumes.class);
    if (consumes != null) {
      for (String contentType : JaxrsUtil.value(consumes)) {
        try {
          MediaType mt = MediaType.valueOf(contentType);
          addMediaType(mt.getType() + "/" + mt.getSubtype());
        }
        catch (Exception e) {
          addMediaType(contentType);
        }
      }
    }
  }

}
