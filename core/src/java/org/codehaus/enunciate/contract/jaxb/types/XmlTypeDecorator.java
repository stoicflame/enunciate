package org.codehaus.enunciate.contract.jaxb.types;

import com.sun.mirror.type.*;
import com.sun.mirror.util.TypeVisitor;
import org.codehaus.enunciate.apt.EnunciateFreemarkerModel;
import org.codehaus.enunciate.contract.jaxb.TypeDefinition;
import net.sf.jelly.apt.decorations.TypeMirrorDecorator;
import net.sf.jelly.apt.decorations.type.DecoratedClassType;
import net.sf.jelly.apt.decorations.type.DecoratedTypeMirror;
import net.sf.jelly.apt.freemarker.FreemarkerModel;

import java.util.Iterator;

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

    if (instance.errorMessage != null) {
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
    DecoratedClassType type = (DecoratedClassType) TypeMirrorDecorator.decorate(classType);
    if (type.isCollection()) {
      visitCollectionType(type);
    }
    else {
      EnunciateFreemarkerModel model = (EnunciateFreemarkerModel) FreemarkerModel.get();
      XmlTypeMirror knownOrSpecifiedType = model.getKnownOrSpecifiedType(classType);
      if (knownOrSpecifiedType != null) {
        this.decoratedTypeMirror = knownOrSpecifiedType;
      }
      else {
        TypeDefinition typeDefinition = null;
        if (classType.getDeclaration() != null) {
          typeDefinition = model.findTypeDefinition(classType.getDeclaration());
        }

        if (typeDefinition != null) {
          this.decoratedTypeMirror = new XmlClassType(typeDefinition);
        }
        else {
          this.decoratedTypeMirror = null;
          this.errorMessage = "Unknown xml type for class: " + classType; 
        }
      }
    }
  }

  protected void visitCollectionType(DeclaredType classType) {
    //if it's a colleciton type, the xml type is its component type.
    Iterator<TypeMirror> actualTypeArguments = classType.getActualTypeArguments().iterator();
    if (!actualTypeArguments.hasNext()) {
      //no type arguments, java.lang.Object type.
      this.decoratedTypeMirror = KnownXmlType.ANY_TYPE;
    }
    else {
      TypeMirror componentType = actualTypeArguments.next();
      componentType.accept(this);
    }
  }

  public void visitEnumType(EnumType enumType) {
    visitClassType(enumType);
  }

  public void visitInterfaceType(InterfaceType interfaceType) {
    DecoratedTypeMirror type = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(interfaceType);
    if (type.isCollection()) {
      visitCollectionType(interfaceType);
    }
    else {
      this.decoratedTypeMirror = null;
      this.errorMessage = "An interface type cannot be an xml type.";
    }
  }

  public void visitAnnotationType(AnnotationType annotationType) {
    this.decoratedTypeMirror = null;
    this.errorMessage = "An annotation type cannot be an xml type.";
  }

  public void visitArrayType(ArrayType arrayType) {
    //special case for byte[]...
    TypeMirror componentType = arrayType.getComponentType();
    if ((componentType instanceof PrimitiveType) && (((PrimitiveType) componentType).getKind() == PrimitiveType.Kind.BYTE)) {
      this.decoratedTypeMirror = KnownXmlType.BASE64_BINARY;
    }
    else {
      componentType.accept(this);
      
      if (this.errorMessage != null) {
        this.errorMessage = "Problem with the array component type: " + this.errorMessage;
      }
    }
  }

  public void visitTypeVariable(TypeVariable typeVariable) {
    Iterator<ReferenceType> bounds = typeVariable.getDeclaration().getBounds().iterator();
    if (!bounds.hasNext()) {
      this.decoratedTypeMirror = KnownXmlType.ANY_TYPE;
    }
    else {
      bounds.next().accept(this);
      if (this.errorMessage != null) {
        this.errorMessage = "Problem with the type variable bounds: " + this.errorMessage;
      }
    }
  }

  public void visitWildcardType(WildcardType wildcardType) {
    Iterator<ReferenceType> upperBounds = wildcardType.getUpperBounds().iterator();
    if (!upperBounds.hasNext()) {
      this.decoratedTypeMirror = KnownXmlType.ANY_TYPE;
    }
    else {
      upperBounds.next().accept(this);

      if (this.errorMessage != null) {
        this.errorMessage = "Problem with wildcard bounds: " + this.errorMessage;
      }
    }
  }
}
