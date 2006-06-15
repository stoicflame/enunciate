package net.sf.enunciate.contract.jaxb.types;

import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;

/**
 * A decorator that decorates the relevant type mirrors as xml type mirrors.
 *
 * @author Ryan Heaton
 */
public class XmlTypeDecorator implements TypeVisitor {

  private XmlTypeMirror decoratedTypeMirror;

  public static XmlTypeMirror decorate(TypeMirror typeMirror) {
    XmlTypeDecorator instance = new XmlTypeDecorator();
    typeMirror.accept(instance);
    return instance.decoratedTypeMirror;
  }

  public void visitTypeMirror(TypeMirror typeMirror) {
    throw new IllegalArgumentException(typeMirror + " isn't a valid xml type.");
  }

  public void visitPrimitiveType(PrimitiveType primitiveType) {
    this.decoratedTypeMirror = new XmlPrimitiveType(primitiveType);
  }

  public void visitVoidType(VoidType voidType) {
    throw new IllegalArgumentException(voidType + " isn't a valid xml type.");
  }

  public void visitReferenceType(ReferenceType referenceType) {
    throw new IllegalArgumentException(referenceType + " isn't a valid xml type.");
  }

  public void visitDeclaredType(DeclaredType declaredType) {
    throw new IllegalArgumentException(declaredType + " isn't a valid xml type.");
  }

  public void visitClassType(ClassType classType) {
    this.decoratedTypeMirror = new XmlClassType(classType);
  }

  public void visitEnumType(EnumType enumType) {
    this.decoratedTypeMirror = new XmlEnumType(enumType);
  }

  public void visitInterfaceType(InterfaceType interfaceType) {
    throw new IllegalArgumentException(interfaceType + " isn't a valid xml type.");
  }

  public void visitAnnotationType(AnnotationType annotationType) {
    throw new IllegalArgumentException(annotationType + " isn't a valid xml type.");
  }

  public void visitArrayType(ArrayType arrayType) {
    this.decoratedTypeMirror = new XmlArrayType(arrayType);
  }

  public void visitTypeVariable(TypeVariable typeVariable) {
    throw new IllegalArgumentException(typeVariable + " isn't a valid xml type.");
  }

  public void visitWildcardType(WildcardType wildcardType) {
    throw new IllegalArgumentException(wildcardType + " isn't a valid xml type.");
  }
}
