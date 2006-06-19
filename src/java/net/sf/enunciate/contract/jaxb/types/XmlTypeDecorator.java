package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;

import java.util.Collection;

/**
 * A decorator that decorates the relevant type mirrors as xml type mirrors.
 *
 * @author Ryan Heaton
 */
public class XmlTypeDecorator implements TypeVisitor {

  private XmlTypeMirror decoratedTypeMirror;
  private String errorMessage = null;

  /**
   * Decorate a type mirror as an xml type.
   *
   * @param typeMirror The type mirror to decorate.
   * @return The xml type for the specified type mirror.
   * @throws XmlTypeException If the type is invalid or unknown as an xml type.
   */
  public static XmlTypeMirror decorate(TypeMirror typeMirror) throws XmlTypeException {
    if (typeMirror instanceof XmlTypeMirror) {
      return ((XmlTypeMirror) typeMirror);
    }
    XmlTypeDecorator instance = new XmlTypeDecorator();
    typeMirror.accept(instance);

    if (instance.errorMessage == null) {
      throw new XmlTypeException(instance.errorMessage);
    }

    return instance.decoratedTypeMirror;
  }

  public void visitTypeMirror(TypeMirror typeMirror) {
    this.decoratedTypeMirror = null;
    this.errorMessage = "Unknown xml type: " + typeMirror;
  }

  public void visitPrimitiveType(PrimitiveType primitiveType) {
    this.decoratedTypeMirror = new XmlPrimitiveType(primitiveType);
  }

  public void visitVoidType(VoidType voidType) {
    this.decoratedTypeMirror = null;
    this.errorMessage = "Void is not a valid xml type.";
  }

  public void visitReferenceType(ReferenceType referenceType) {
    this.decoratedTypeMirror = null;
    this.errorMessage = "Unknown xml type: " + referenceType;
  }

  public void visitDeclaredType(DeclaredType declaredType) {
    this.decoratedTypeMirror = null;
    this.errorMessage = "Unknown xml type: " + declaredType;
  }

  public void visitClassType(ClassType classType) {
    try {
      XmlClassType xmlClassType = new XmlClassType(classType);
      if (xmlClassType.isCollection()) {
        //if it's a colleciton type, the xml type is its component type.
        Collection<TypeMirror> actualTypeArguments = classType.getActualTypeArguments();
        if (actualTypeArguments.isEmpty()) {
          //no type arguments, java.lang.Object type.
          this.decoratedTypeMirror = KnownXmlType.ANY_TYPE;
        }

        TypeMirror componentType = actualTypeArguments.iterator().next();
        componentType.accept(this);
      }
      else {
        this.decoratedTypeMirror = xmlClassType;
      }
    }
    catch (XmlTypeException e) {
      this.errorMessage = e.getMessage();
    }
  }

  public void visitEnumType(EnumType enumType) {
    try {
      this.decoratedTypeMirror = new XmlEnumType(enumType);
    }
    catch (XmlTypeException e) {
      this.errorMessage = e.getMessage();
    }
  }

  public void visitInterfaceType(InterfaceType interfaceType) {
    this.decoratedTypeMirror = null;
    this.errorMessage = "An interface type cannot be an xml type.";
  }

  public void visitAnnotationType(AnnotationType annotationType) {
    this.decoratedTypeMirror = null;
    this.errorMessage = "An annotation type cannot be an xml type.";
  }

  public void visitArrayType(ArrayType arrayType) {
    try {
      this.decoratedTypeMirror = new XmlArrayType(arrayType);
    }
    catch (XmlTypeException e) {
      this.errorMessage = e.getMessage();
    }
  }

  public void visitTypeVariable(TypeVariable typeVariable) {
    try {
      this.decoratedTypeMirror = new XmlTypeVariable(typeVariable);
    }
    catch (XmlTypeException e) {
      this.errorMessage = e.getMessage();
    }
  }

  public void visitWildcardType(WildcardType wildcardType) {
    try {
      this.decoratedTypeMirror = new XmlWildcardType(wildcardType);
    }
    catch (XmlTypeException e) {
      this.errorMessage = e.getMessage();
    }
  }
}
