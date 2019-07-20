package com.webcohesion.enunciate.modules.jackson1.api.impl;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.*;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.modules.jackson1.EnunciateJackson1Context;
import com.webcohesion.enunciate.modules.jackson1.model.EnumTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.ObjectTypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.TypeDefinition;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonTypeFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.SerializationConfig;

import javax.lang.model.element.TypeElement;
import java.io.Reader;
import java.util.*;

/**
 * @author Ryan Heaton
 */
public class SyntaxImpl implements Syntax, Namespace {

  public static final String SYNTAX_LABEL = "JSON";

  private final EnunciateJackson1Context context;
  private ApiRegistrationContext registrationContext;

  public SyntaxImpl(EnunciateJackson1Context context, ApiRegistrationContext registrationContext) {
    this.context = context;
    this.registrationContext = registrationContext;
  }

  @Override
  public String getId() {
    return "jackson1";
  }

  @Override
  public int compareTo(Syntax syntax) {
    return getId().compareTo(syntax.getId());
  }

  @Override
  public String getSlug() {
    return "syntax_json";
  }

  @Override
  public String getLabel() {
    return SYNTAX_LABEL;
  }

  @Override
  public boolean isEmpty() {
    return this.context.getTypeDefinitions().isEmpty();
  }

  @Override
  public List<Namespace> getNamespaces() {
    return Collections.singletonList((Namespace) this);
  }

  @Override
  public boolean isAssignableToMediaType(String mediaType) {
    return mediaType != null && (mediaType.equals("*/*") || mediaType.equals("application/*") || mediaType.endsWith("/json") || mediaType.endsWith("+json"));
  }

  @Override
  public MediaTypeDescriptor findMediaTypeDescriptor(String mediaType, DecoratedTypeMirror typeMirror) {
    if (mediaType == null) {
      return null;
    }

    //if it's a wildcard, we'll return an implicit descriptor.
    if (mediaType.equals("*/*") || mediaType.equals("application/*")) {
      mediaType = "application/json";
    }

    if (mediaType.endsWith("/json") || mediaType.endsWith("+json")) {
      typeMirror = this.context.resolveSyntheticType(typeMirror);
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

    JsonType jsonType;
    try {
      jsonType = JsonTypeFactory.getJsonType(typeMirror, this.context);
    }
    catch (Exception e) {
      jsonType = null;
    }

    return jsonType == null ? null : new DataTypeReferenceImpl(jsonType, registrationContext);
  }

  @Override
  public List<DataType> findDataTypes(String name) {
    if (name != null && !name.isEmpty()) {
      TypeElement typeElement = this.context.getContext().getProcessingEnvironment().getElementUtils().getTypeElement(name);
      if (typeElement != null) {
        TypeDefinition typeDefinition = this.context.findTypeDefinition(typeElement);
        if (typeDefinition instanceof ObjectTypeDefinition) {
          return Collections.singletonList((DataType) new ObjectDataTypeImpl((ObjectTypeDefinition) typeDefinition, registrationContext));
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
    ObjectMapper mapper = new ObjectMapper().enable(SerializationConfig.Feature.INDENT_OUTPUT);
    return new CustomExampleImpl(mapper.writeValueAsString(mapper.readTree(example)));
  }

  @Override
  public String getUri() {
    return null; //json has no namespace uri.
  }

  @Override
  public InterfaceDescriptionFile getSchemaFile() {
    return null; //todo: json schema?
  }

  @Override
  public List<? extends DataType> getTypes() {
    Collection<TypeDefinition> typeDefinitions = this.context.getTypeDefinitions();
    ArrayList<DataType> dataTypes = new ArrayList<DataType>();
    FacetFilter facetFilter = registrationContext.getFacetFilter();
    for (TypeDefinition typeDefinition : typeDefinitions) {
      if (!facetFilter.accept(typeDefinition)) {
        continue;
      }

      if (typeDefinition instanceof ObjectTypeDefinition) {
        dataTypes.add(new ObjectDataTypeImpl((ObjectTypeDefinition) typeDefinition, registrationContext));
      }
      else if (typeDefinition instanceof EnumTypeDefinition) {
        dataTypes.add(new EnumDataTypeImpl((EnumTypeDefinition) typeDefinition, registrationContext));
      }
    }

    Collections.sort(dataTypes, new Comparator<DataType>() {
      @Override
      public int compare(DataType o1, DataType o2) {
        return o1.getLabel().compareTo(o2.getLabel());
      }
    });

    return dataTypes;
  }
}
