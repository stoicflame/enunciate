package com.webcohesion.enunciate.javac.decorations;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedDeclaredType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedExecutableType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedPrimitiveType;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.*;
import javax.lang.model.util.Types;
import java.util.List;

/**
 * @author Ryan Heaton
 */
public class DecoratedTypes implements Types {

  private final Types delegate;
  private final ProcessingEnvironment env;

  public DecoratedTypes(Types delegate, ProcessingEnvironment env) {
    while (delegate instanceof DecoratedTypes) {
      delegate = ((DecoratedTypes) env).delegate;
    }

    this.delegate = delegate;
    this.env = env;
  }

  public Element asElement(TypeMirror t) {
    while (t instanceof DecoratedTypeMirror) {
      t = ((DecoratedTypeMirror) t).getDelegate();
    }

    return ElementDecorator.decorate(delegate.asElement(t), this.env);
  }

  public TypeMirror capture(TypeMirror t) {
    while (t instanceof DecoratedTypeMirror) {
      t = ((DecoratedTypeMirror) t).getDelegate();
    }

    return TypeMirrorDecorator.decorate(delegate.capture(t), this.env);
  }

  public NullType getNullType() {
    return TypeMirrorDecorator.decorate(delegate.getNullType(), this.env);
  }

  public PrimitiveType getPrimitiveType(TypeKind kind) {
    return TypeMirrorDecorator.decorate(delegate.getPrimitiveType(kind), this.env);
  }

  public DeclaredType getDeclaredType(TypeElement type, TypeMirror... typeArgs) {
    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    TypeMirror[] copy = new TypeMirror[typeArgs.length];
    for (int i = 0; i < typeArgs.length; i++) {
      TypeMirror t = typeArgs[i];
      while (t instanceof DecoratedTypeMirror) {
        t = ((DecoratedTypeMirror) t).getDelegate();
      }
      copy[i] = t;
    }

    return TypeMirrorDecorator.decorate(delegate.getDeclaredType(type, copy), this.env);
  }

  public DeclaredType getDeclaredType(DeclaredType containing, TypeElement type, TypeMirror... typeArgs) {
    while (containing instanceof DecoratedDeclaredType) {
      containing = ((DecoratedDeclaredType) containing).getDelegate();
    }

    while (type instanceof DecoratedTypeElement) {
      type = ((DecoratedTypeElement) type).getDelegate();
    }

    TypeMirror[] copy = new TypeMirror[typeArgs.length];
    for (int i = 0; i < typeArgs.length; i++) {
      TypeMirror t = typeArgs[i];
      while (t instanceof DecoratedTypeMirror) {
        t = ((DecoratedTypeMirror) t).getDelegate();
      }
      copy[i] = t;
    }

    return delegate.getDeclaredType(containing, type, copy);
  }

  public NoType getNoType(TypeKind kind) {
    return TypeMirrorDecorator.decorate(delegate.getNoType(kind), this.env);
  }

  public TypeMirror erasure(TypeMirror t) {
    while (t instanceof DecoratedTypeMirror) {
      t = ((DecoratedTypeMirror) t).getDelegate();
    }

    return TypeMirrorDecorator.decorate(delegate.erasure(t), this.env);
  }

  public WildcardType getWildcardType(TypeMirror extendsBound, TypeMirror superBound) {
    while (extendsBound instanceof DecoratedTypeMirror) {
      extendsBound = ((DecoratedTypeMirror) extendsBound).getDelegate();
    }

    while (superBound instanceof DecoratedTypeMirror) {
      superBound = ((DecoratedTypeMirror) superBound).getDelegate();
    }

    return delegate.getWildcardType(extendsBound, superBound);
  }

  public boolean isSameType(TypeMirror t1, TypeMirror t2) {
    while (t1 instanceof DecoratedTypeMirror) {
      t1 = ((DecoratedTypeMirror) t1).getDelegate();
    }

    while (t2 instanceof DecoratedTypeMirror) {
      t2 = ((DecoratedTypeMirror) t2).getDelegate();
    }

    return delegate.isSameType(t1, t2);
  }

  public boolean isSubtype(TypeMirror t1, TypeMirror t2) {
    while (t1 instanceof DecoratedTypeMirror) {
      t1 = ((DecoratedTypeMirror) t1).getDelegate();
    }

    while (t2 instanceof DecoratedTypeMirror) {
      t2 = ((DecoratedTypeMirror) t2).getDelegate();
    }

    return delegate.isSubtype(t1, t2);
  }

  public TypeElement boxedClass(PrimitiveType p) {
    while (p instanceof DecoratedPrimitiveType) {
      p = ((DecoratedPrimitiveType) p).getDelegate();
    }

    return ElementDecorator.decorate(delegate.boxedClass(p), this.env);
  }

  public ArrayType getArrayType(TypeMirror componentType) {
    while (componentType instanceof DecoratedTypeMirror) {
      componentType = ((DecoratedTypeMirror) componentType).getDelegate();
    }

    return TypeMirrorDecorator.decorate(delegate.getArrayType(componentType), this.env);
  }

  public boolean contains(TypeMirror t1, TypeMirror t2) {
    while (t1 instanceof DecoratedTypeMirror) {
      t1 = ((DecoratedTypeMirror) t1).getDelegate();
    }

    while (t2 instanceof DecoratedTypeMirror) {
      t2 = ((DecoratedTypeMirror) t2).getDelegate();
    }

    return delegate.contains(t1, t2);
  }

  public boolean isSubsignature(ExecutableType m1, ExecutableType m2) {
    while (m1 instanceof DecoratedExecutableType) {
      m1 = ((DecoratedExecutableType) m1).getDelegate();
    }

    while (m2 instanceof DecoratedExecutableType) {
      m2 = ((DecoratedExecutableType) m2).getDelegate();
    }

    return delegate.isSubsignature(m1, m2);
  }

  public boolean isAssignable(TypeMirror t1, TypeMirror t2) {
    while (t1 instanceof DecoratedTypeMirror) {
      t1 = ((DecoratedTypeMirror) t1).getDelegate();
    }

    while (t2 instanceof DecoratedTypeMirror) {
      t2 = ((DecoratedTypeMirror) t2).getDelegate();
    }

    return delegate.isAssignable(t1, t2);
  }

  public List<? extends TypeMirror> directSupertypes(TypeMirror t) {
    while (t instanceof DecoratedTypeMirror) {
      t = ((DecoratedTypeMirror) t).getDelegate();
    }

    return TypeMirrorDecorator.decorate(delegate.directSupertypes(t), this.env);
  }

  public TypeMirror asMemberOf(DeclaredType containing, Element element) {
    while (containing instanceof DecoratedDeclaredType) {
      containing = ((DecoratedDeclaredType) containing).getDelegate();
    }

    while (element instanceof DecoratedElement) {
      element = ((DecoratedElement) element).getDelegate();
    }

    return delegate.asMemberOf(containing, element);
  }

  public PrimitiveType unboxedType(TypeMirror t) {
    return delegate.unboxedType(t);
  }
}
