package com.webcohesion.enunciate.modules.jackson1;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.EnunciateException;
import com.webcohesion.enunciate.api.InterfaceDescriptionFile;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.datatype.Namespace;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.MediaTypeDescriptor;
import com.webcohesion.enunciate.facets.FacetFilter;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.qname.XmlQNameEnum;
import com.webcohesion.enunciate.module.EnunciateModuleContext;
import com.webcohesion.enunciate.modules.jackson1.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jackson1.api.impl.EnumDataTypeImpl;
import com.webcohesion.enunciate.modules.jackson1.api.impl.MediaTypeDescriptorImpl;
import com.webcohesion.enunciate.modules.jackson1.api.impl.ObjectDataTypeImpl;
import com.webcohesion.enunciate.modules.jackson1.model.*;
import com.webcohesion.enunciate.modules.jackson1.model.adapters.AdapterType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonType;
import com.webcohesion.enunciate.modules.jackson1.model.types.JsonTypeFactory;
import com.webcohesion.enunciate.modules.jackson1.model.types.KnownJsonType;
import com.webcohesion.enunciate.modules.jackson1.model.util.JacksonUtil;
import com.webcohesion.enunciate.modules.jackson1.model.util.MapType;

import javax.activation.DataHandler;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.SimpleTypeVisitor6;
import javax.xml.bind.JAXBElement;
import javax.xml.datatype.XMLGregorianCalendar;
import java.sql.Timestamp;
import java.util.*;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings ( "unchecked" )
public class EnunciateJackson1Context extends EnunciateModuleContext implements Syntax {

  public static final String SYNTAX_LABEL = "JSON";

  private final Map<String, JsonType> knownTypes;
  private final Map<String, TypeDefinition> typeDefinitions;
  private final boolean honorJaxb;

  public EnunciateJackson1Context(EnunciateContext context, boolean honorJaxb) {
    super(context);
    this.knownTypes = loadKnownTypes();
    this.typeDefinitions = new HashMap<String, TypeDefinition>();
    this.honorJaxb = honorJaxb;
  }

  public EnunciateContext getContext() {
    return context;
  }

  public boolean isHonorJaxb() {
    return honorJaxb;
  }

  public Collection<TypeDefinition> getTypeDefinitions() {
    return this.typeDefinitions.values();
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
    return this.typeDefinitions.isEmpty();
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
      DataTypeReference typeReference = findDataTypeReference(typeMirror);
      return new MediaTypeDescriptorImpl(mediaType, typeReference);
    }
    else {
      return null;
    }
  }

  private  DataTypeReference findDataTypeReference(DecoratedTypeMirror typeMirror) {
    if (typeMirror == null) {
      return null;
    }

    JsonType jsonType;
    try {
      jsonType = JsonTypeFactory.getJsonType(typeMirror, this);
    }
    catch (Exception e) {
      jsonType = null;
    }

    return jsonType == null ? null : new DataTypeReferenceImpl(jsonType);
  }

  @Override
  public List<Namespace> getNamespaces() {
    return Arrays.asList(getNamespace());
  }

  public Namespace getNamespace() {
    return new JacksonNamespace();
  }

  public JsonType getKnownType(Element declaration) {
    if (declaration instanceof TypeElement) {
      return this.knownTypes.get(((TypeElement) declaration).getQualifiedName().toString());
    }
    return null;
  }

  public TypeDefinition findTypeDefinition(Element declaration) {
    if (declaration instanceof TypeElement) {
      return this.typeDefinitions.get(((TypeElement) declaration).getQualifiedName().toString());
    }
    return null;
  }

  protected Map<String, JsonType> loadKnownTypes() {
    HashMap<String, JsonType> knownTypes = new HashMap<String, JsonType>();

    knownTypes.put(Boolean.class.getName(), KnownJsonType.BOOLEAN);
    knownTypes.put(Byte.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Character.class.getName(), KnownJsonType.STRING);
    knownTypes.put(Double.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Float.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Integer.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Long.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Short.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Boolean.TYPE.getName(), KnownJsonType.BOOLEAN);
    knownTypes.put(Byte.TYPE.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Double.TYPE.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Float.TYPE.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Integer.TYPE.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Long.TYPE.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Short.TYPE.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Character.TYPE.getName(), KnownJsonType.STRING);
    knownTypes.put(String.class.getName(), KnownJsonType.STRING);
    knownTypes.put(java.math.BigInteger.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(java.math.BigDecimal.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(java.util.Calendar.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(java.util.Date.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(Timestamp.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(java.net.URI.class.getName(), KnownJsonType.STRING);
    knownTypes.put(java.lang.Object.class.getName(), KnownJsonType.OBJECT);
    knownTypes.put(byte[].class.getName(), KnownJsonType.STRING);
    knownTypes.put(DataHandler.class.getName(), KnownJsonType.STRING);
    knownTypes.put(java.util.UUID.class.getName(), KnownJsonType.STRING);
    knownTypes.put(XMLGregorianCalendar.class.getName(), KnownJsonType.NUMBER);
    knownTypes.put(GregorianCalendar.class.getName(), KnownJsonType.NUMBER);

    return knownTypes;
  }

  /**
   * Find the type definition for a class given the class's declaration.
   *
   * @param declaration The declaration.
   * @return The type definition.
   */
  protected TypeDefinition createTypeDefinition(TypeElement declaration) {
    if (declaration.getKind() == ElementKind.INTERFACE) {
      if (declaration.getAnnotation(javax.xml.bind.annotation.XmlType.class) != null) {
        throw new EnunciateException(declaration.getQualifiedName() + ": an interface must not be annotated with @XmlType.");
      }
    }

    declaration = narrowToAdaptingType(declaration);

    if (isEnumType(declaration)) {
      if (declaration.getAnnotation(XmlQNameEnum.class) != null) {
        return new QNameEnumTypeDefinition(declaration, this);
      }
      else {
        return new EnumTypeDefinition(declaration, this);
      }
    }
    else {
      ObjectTypeDefinition typeDef = new ObjectTypeDefinition(declaration, this);
      if ((typeDef.getValue() != null) && (hasNoMembers(typeDef))) {
        return new SimpleTypeDefinition(typeDef);
      }
      else {
        return typeDef;
      }
    }
  }

  /**
   * Narrows the existing declaration down to its adapting declaration, if it's being adapted. Otherwise, the original declaration will be returned.
   *
   * @param declaration The declaration to narrow.
   * @return The narrowed declaration.
   */
  protected TypeElement narrowToAdaptingType(TypeElement declaration) {
    AdapterType adapterType = JacksonUtil.findAdapterType(declaration, this);
    if (adapterType != null) {
      TypeMirror adaptingType = adapterType.getAdaptingType();
      if (adaptingType.getKind() != TypeKind.DECLARED) {
        return declaration;
      }
      else {
        TypeElement adaptingDeclaration = (TypeElement) ((DeclaredType) adaptingType).asElement();
        if (adaptingDeclaration == null) {
          throw new EnunciateException(String.format("Class %s is being adapted by a type (%s) that doesn't seem to be on the classpath.", declaration.getQualifiedName(), adaptingType));
        }
        return adaptingDeclaration;
      }
    }
    return declaration;
  }

  /**
   * A quick check to see if a declaration defines a enum schema type.
   *
   * @param declaration The declaration to check.
   * @return the value of the check.
   */
  protected boolean isEnumType(TypeElement declaration) {
    return declaration.getKind() == ElementKind.ENUM;
  }

  /**
   * Whether the specified type definition has neither attributes nor elements.
   *
   * @param typeDef The type def.
   * @return Whether the specified type definition has neither attributes nor elements.
   */
  protected boolean hasNoMembers(TypeDefinition typeDef) {
    boolean none = typeDef.getMembers().isEmpty();
    TypeElement superDeclaration = (TypeElement) ((DeclaredType)typeDef.getSuperclass()).asElement();
    if (!Object.class.getName().equals(superDeclaration.getQualifiedName().toString())) {
      none &= hasNoMembers(new ObjectTypeDefinition(superDeclaration, this));
    }
    return none;
  }

  /**
   * Add a type definition to the model.
   *
   * @param typeDef The type definition to add to the model.
   */
  public void add(TypeDefinition typeDef) {
    add(typeDef, new LinkedList<Element>());
  }

  public boolean isKnownTypeDefinition(TypeElement el) {
    return findTypeDefinition(el) != null || isKnownType(el);
  }

  public void add(TypeDefinition typeDef, LinkedList<Element> stack) {
    if (findTypeDefinition(typeDef) == null && !isKnownType(typeDef)) {
      this.typeDefinitions.put(typeDef.getQualifiedName().toString(), typeDef);
      debug("Added %s as a Jackson type definition.", typeDef.getQualifiedName());

      typeDef.getReferencedFrom().addAll(stack);
      try {
        stack.push(typeDef);

        addSeeAlsoTypeDefinitions(typeDef, stack);

        for (Member member : typeDef.getMembers()) {
          addReferencedTypeDefinitions(member, stack);
        }

        Value value = typeDef.getValue();
        if (value != null) {
          addReferencedTypeDefinitions(value, stack);
        }

        TypeMirror superclass = typeDef.getSuperclass();
        if (!typeDef.isEnum() && superclass != null) {
          addReferencedTypeDefinitions(superclass, stack);
        }
      }
      finally {
        stack.pop();
      }
    }
  }

  protected void addReferencedTypeDefinitions(Accessor accessor, LinkedList<Element> stack) {
    addSeeAlsoTypeDefinitions(accessor, stack);
    TypeMirror enumRef = accessor.getQNameEnumRef();
    if (enumRef != null) {
      addReferencedTypeDefinitions(enumRef, stack);
    }
  }

  /**
   * Add the type definition(s) referenced by the given value.
   *
   * @param value The value.
   * @param stack The context stack.
   */
  protected void addReferencedTypeDefinitions(Value value, LinkedList<Element> stack) {
    addReferencedTypeDefinitions((Accessor) value, stack);
    if (value.isAdapted()) {
      addReferencedTypeDefinitions(value.getAdapterType(), stack);
    }
    else if (value.getQNameEnumRef() == null) {
      addReferencedTypeDefinitions(value.getAccessorType(), stack);
    }
  }

  /**
   * Add the referenced type definitions for the specified element.
   *
   * @param member The element.
   * @param stack The context stack.
   */
  protected void addReferencedTypeDefinitions(Member member, LinkedList<Element> stack) {
    addReferencedTypeDefinitions((Accessor) member, stack);
    for (Member choice : member.getChoices()) {
      if (choice.isAdapted()) {
        addReferencedTypeDefinitions(choice.getAdapterType(), stack);
      }
      else if (choice.getQNameEnumRef() == null) {
        addReferencedTypeDefinitions(choice.getAccessorType(), stack);
      }
    }
  }

  /**
   * Adds any referenced type definitions for the specified type mirror.
   *
   * @param type The type mirror.
   */
  protected void addReferencedTypeDefinitions(TypeMirror type, LinkedList<Element> stack) {
    type.accept(new ReferencedJsonDefinitionVisitor(), stack);
  }

  /**
   * Add any type definitions that are specified as "see also".
   *
   * @param declaration The declaration.
   */
  protected void addSeeAlsoTypeDefinitions(Element declaration, LinkedList<Element> stack) {
    //todo: figure out how to do "see also" stuff in Jackson 1
  }

  /**
   * Whether the specified type is a known type.
   *
   * @param typeDef The type def.
   * @return Whether the specified type is a known type.
   */
  protected boolean isKnownType(TypeElement typeDef) {
    return knownTypes.containsKey(typeDef.getQualifiedName().toString()) || ((DecoratedTypeMirror)typeDef.asType()).isInstanceOf(JAXBElement.class);
  }

  /**
   * Visitor for XML-referenced type definitions.
   */
  private class ReferencedJsonDefinitionVisitor extends SimpleTypeVisitor6<Void, LinkedList<Element>> {

    @Override
    public Void visitArray(ArrayType t, LinkedList<Element> stack) {
      return t.getComponentType().accept(this, stack);
    }

    @Override
    public Void visitDeclared(DeclaredType declaredType, LinkedList<Element> stack) {
      TypeElement declaration = (TypeElement) declaredType.asElement();
      if (declaration.getKind() == ElementKind.ENUM) {
        if (!isKnownTypeDefinition(declaration)) {
          add(createTypeDefinition(declaration), stack);
        }
      }
      else if (declaredType instanceof AdapterType) {
        ((AdapterType) declaredType).getAdaptingType().accept(this, stack);
      }
      else if (MapType.findMapType(declaredType, EnunciateJackson1Context.this) == null) {
        String qualifiedName = declaration.getQualifiedName().toString();
        if (Object.class.getName().equals(qualifiedName)) {
          //skip base object; not a type definition.
          return null;
        }

        if (stack.contains(declaration)) {
          //we're already visiting this class...
          return null;
        }

        stack.push(declaration);
        try {
          if (!isKnownTypeDefinition(declaration) && declaration.getKind() == ElementKind.CLASS && !((DecoratedDeclaredType) declaredType).isCollection() && !((DecoratedDeclaredType) declaredType).isInstanceOf(JAXBElement.class)) {
            add(createTypeDefinition(declaration), stack);
          }

          List<? extends TypeMirror> typeArgs = declaredType.getTypeArguments();
          if (typeArgs != null) {
            for (TypeMirror typeArg : typeArgs) {
              typeArg.accept(this, stack);
            }
          }
        }
        finally {
          stack.pop();
        }
      }

      return null;
    }

    @Override
    public Void visitTypeVariable(TypeVariable t, LinkedList<Element> stack) {
      return t.getUpperBound().accept(this, stack);
    }

    @Override
    public Void visitWildcard(WildcardType t, LinkedList<Element> stack) {
      TypeMirror extendsBound = t.getExtendsBound();
      if (extendsBound != null) {
        extendsBound.accept(this, stack);
      }

      TypeMirror superBound = t.getSuperBound();
      if (superBound != null) {
        superBound.accept(this, stack);
      }

      return null;
    }

    @Override
    public Void visitUnknown(TypeMirror t, LinkedList<Element> stack) {
      return defaultAction(t, stack);
    }

  }

  private class JacksonNamespace implements Namespace {
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
      Collection<TypeDefinition> typeDefinitions = getTypeDefinitions();
      ArrayList<DataType> dataTypes = new ArrayList<DataType>();
      FacetFilter facetFilter = getContext().getConfiguration().getFacetFilter();
      for (TypeDefinition typeDefinition : typeDefinitions) {
        if (!facetFilter.accept(typeDefinition)) {
          continue;
        }

        if (typeDefinition instanceof ObjectTypeDefinition) {
          dataTypes.add(new ObjectDataTypeImpl((ObjectTypeDefinition) typeDefinition));
        }
        else if (typeDefinition instanceof EnumTypeDefinition) {
          dataTypes.add(new EnumDataTypeImpl((EnumTypeDefinition) typeDefinition));
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
}
