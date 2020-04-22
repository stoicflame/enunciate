package com.webcohesion.enunciate.modules.jackson.javac;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;
import com.webcohesion.enunciate.javac.decorations.adaptors.ExecutableElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.adaptors.TypeElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.type.TypeMirrorUtils;

import javax.lang.model.element.*;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class InterfaceJacksonTypeElement implements TypeElementAdaptor {

  private final DeclaredType root;
  private final TypeElement element;
  private final DecoratedProcessingEnvironment env;

  public InterfaceJacksonTypeElement(DeclaredType root, DecoratedProcessingEnvironment env) {
    this.root = root;
    this.env = env;
    this.element = (TypeElement) root.asElement();
  }

  @Override
  public Name getBinaryName() {
    return this.env.getElementUtils().getBinaryName(this.element);
  }

  @Override
  public String getDocComment() {
    return this.env.getElementUtils().getDocComment(element);
  }

  @Override
  public boolean isDeprecated() {
    return this.env.getElementUtils().isDeprecated(element);
  }

  @Override
  public List<? extends Element> getAllMembers() {
    return decorate(this.env.getElementUtils().getAllMembers(element));
  }

  protected List<? extends Element> decorate(List<? extends Element> elements) {
    ArrayList<Element> members = new ArrayList<Element>();
    for (Element member : elements) {
      if (member instanceof ExecutableElement) {
        members.add(new InterfaceExecutable((ExecutableElement) member));
      }
    }
    return members;
  }

  @Override
  public boolean overrides(ExecutableElement overrider, ExecutableElement overridden) {
    return env.getElementUtils().overrides(overrider, overridden, element);
  }

  @Override
  public PackageElement getPackage() {
    return this.env.getElementUtils().getPackageOf(element);
  }

  @Override
  public List<? extends AnnotationMirror> getAllAnnotationMirrors() {
    return this.env.getElementUtils().getAllAnnotationMirrors(this.element);
  }

  @Override
  public boolean hides(Element hidden) {
    return false;
  }

  @Override
  public boolean isHiddenBy(Element hider) {
    return false;
  }

  @Override
  public SourcePosition getSourcePosition() {
    return new SourcePosition(null, null, -1, -1, -1);
  }

  @Override
  public List<? extends Element> getEnclosedElements() {
    return decorate(this.element.getEnclosedElements());
  }

  @Override
  public NestingKind getNestingKind() {
    return NestingKind.TOP_LEVEL;
  }

  @Override
  public Name getQualifiedName() {
    return this.element.getQualifiedName();
  }

  @Override
  public Name getSimpleName() {
    return this.element.getSimpleName();
  }

  @Override
  public TypeMirror getSuperclass() {
    List<? extends TypeMirror> ifaces = this.element.getInterfaces();
    //for now, we'll just say there's a "superclass" if we only extend one interface.
    //there's a potential for supporting multiple "superclasses". Take a look at the usages of com.webcohesion.enunciate.modules.jackson.model.ObjectTypeDefinition.getSupertype
    //and see what it would take to make it return a list.
    return ifaces == null || ifaces.size() != 1 ? TypeMirrorUtils.objectType(this.env) : ifaces.get(0);
  }

  @Override
  public List<? extends TypeMirror> getInterfaces() {
    return this.element.getInterfaces();
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    return this.element.getTypeParameters();
  }

  @Override
  public Element getEnclosingElement() {
    return this.element.getEnclosingElement();
  }

  @Override
  public TypeMirror asType() {
    return new InterfaceJacksonDeclaredType(this.root, this.env);
  }

  @Override
  public ElementKind getKind() {
    return ElementKind.CLASS;
  }

  @Override
  public List<? extends AnnotationMirror> getAnnotationMirrors() {
    return this.element.getAnnotationMirrors();
  }

  @Override
  public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
    return this.element.getAnnotation(annotationType);
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return this.element.getAnnotationsByType(annotationType);
  }

  @Override
  public Set<Modifier> getModifiers() {
    EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
    modifiers.addAll(this.element.getModifiers());
    modifiers.add(Modifier.ABSTRACT);
    return modifiers;
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }

  private class InterfaceExecutable implements ExecutableElementAdaptor {

    private final ExecutableElement executableElement;

    private InterfaceExecutable(ExecutableElement executableElement) {
      this.executableElement = executableElement;
    }

    @Override
    public boolean overrides(ExecutableElement overridden, TypeElement scope) {
      while (overridden instanceof InterfaceExecutable) {
        overridden = ((InterfaceExecutable) overridden).executableElement;
      }
      return env.getElementUtils().overrides(this.executableElement, overridden, scope);
    }

    @Override
    public boolean isOverriddenBy(ExecutableElement overrider, TypeElement type) {
      while (overrider instanceof InterfaceExecutable) {
        overrider = ((InterfaceExecutable) overrider).executableElement;
      }

      return env.getElementUtils().overrides(overrider, this.executableElement, type);
    }

    @Override
    public String getDocComment() {
      return env.getElementUtils().getDocComment(executableElement);
    }

    @Override
    public boolean isDeprecated() {
      return env.getElementUtils().isDeprecated(executableElement);
    }

    @Override
    public PackageElement getPackage() {
      return env.getElementUtils().getPackageOf(executableElement);
    }

    @Override
    public List<? extends AnnotationMirror> getAllAnnotationMirrors() {
      return env.getElementUtils().getAllAnnotationMirrors(executableElement);
    }

    @Override
    public boolean hides(Element hidden) {
      while (hidden instanceof InterfaceExecutable) {
        hidden = ((InterfaceExecutable) hidden).executableElement;
      }
      return env.getElementUtils().hides(executableElement, hidden);
    }

    @Override
    public boolean isHiddenBy(Element hider) {
      while (hider instanceof InterfaceExecutable) {
        hider = ((InterfaceExecutable) hider).executableElement;
      }

      return env.getElementUtils().hides(hider, executableElement);
    }

    @Override
    public SourcePosition getSourcePosition() {
      return null;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
      return executableElement.getTypeParameters();
    }

    @Override
    public TypeMirror getReturnType() {
      return executableElement.getReturnType();
    }

    @Override
    public List<? extends VariableElement> getParameters() {
      return executableElement.getParameters();
    }

    @Override
    public boolean isVarArgs() {
      return executableElement.isVarArgs();
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
      return executableElement.getThrownTypes();
    }

    @Override
    public AnnotationValue getDefaultValue() {
      return executableElement.getDefaultValue();
    }

    @Override
    public Name getSimpleName() {
      return executableElement.getSimpleName();
    }

    @Override
    public TypeMirror asType() {
      return executableElement.asType();
    }

    @Override
    public ElementKind getKind() {
      return executableElement.getKind();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return executableElement.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return executableElement.getAnnotation(annotationType);
    }

    @Override
    public Set<Modifier> getModifiers() {
      EnumSet<Modifier> modifiers = EnumSet.noneOf(Modifier.class);
      modifiers.addAll(executableElement.getModifiers());
      modifiers.add(Modifier.ABSTRACT);
      return modifiers;
    }

    @Override
    public Element getEnclosingElement() {
      return InterfaceJacksonTypeElement.this;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
      return executableElement.getEnclosedElements();
    }

    @Override
    public TypeMirror getReceiverType() {
      return env.getTypeUtils().getNoType(TypeKind.NONE);
    }

    @Override
    public boolean isDefault() {
      return false;
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return executableElement.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitExecutable(this, p);
    }
  }
}
