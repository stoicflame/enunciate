package com.webcohesion.enunciate.modules.jackson.javac;

import java.lang.annotation.Annotation;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.TreeSet;
import javax.lang.model.element.AnnotationMirror;
import javax.lang.model.element.AnnotationValue;
import javax.lang.model.element.Element;
import javax.lang.model.element.ElementKind;
import javax.lang.model.element.ElementVisitor;
import javax.lang.model.element.ExecutableElement;
import javax.lang.model.element.Modifier;
import javax.lang.model.element.Name;
import javax.lang.model.element.NestingKind;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.lang.model.element.TypeParameterElement;
import javax.lang.model.element.VariableElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.facets.Facet;
import com.webcohesion.enunciate.facets.HasFacets;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.SourcePosition;
import com.webcohesion.enunciate.javac.decorations.adaptors.ExecutableElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.adaptors.TypeElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.adaptors.VariableElementAdaptor;
import com.webcohesion.enunciate.javac.decorations.type.TypeVariableContext;

/**
 * @author Ryan Heaton
 */
public class ParameterizedJacksonTypeElement implements TypeElementAdaptor, HasFacets {

  private final DeclaredType root;
  private final TypeElement element;
  private final DecoratedProcessingEnvironment env;
  private final EnunciateContext context;
  private final Name fqn;
  private final Name simpleName;
  private final TypeVariableContext variableContext;

  public ParameterizedJacksonTypeElement(DeclaredType root, EnunciateContext context) {
    this.root = root;
    this.context = context;
    this.env = context.getProcessingEnvironment();
    this.element = (TypeElement) root.asElement();

    List<? extends TypeMirror> typeArgs = root.getTypeArguments();
    StringBuilder fqn = new StringBuilder(element.getQualifiedName().toString());
    StringBuilder simpleName = new StringBuilder(element.getSimpleName().toString());
    makeName(typeArgs, fqn, simpleName);
    Elements elementUtils = context.getProcessingEnvironment().getElementUtils();
    this.fqn = elementUtils.getName(fqn);
    this.simpleName = elementUtils.getName(simpleName);
    this.variableContext = new TypeVariableContext().push(element.getTypeParameters(), root.getTypeArguments());
  }

  private void makeName(List<? extends TypeMirror> typeArgs, StringBuilder fqn, StringBuilder simpleName) {
    String nameSeparator = "Of";
    for (TypeMirror typeArg : typeArgs) {
      if (typeArg instanceof DeclaredType) {
        TypeElement argType = (TypeElement) ((DeclaredType) typeArg).asElement();
        StringBuilder simpleInner = new StringBuilder();
        simpleInner.append(argType.getSimpleName());
        makeName(((DeclaredType) typeArg).getTypeArguments(), new StringBuilder(), simpleInner);
        fqn.append(nameSeparator).append(simpleInner.toString());
        simpleName.append(nameSeparator).append(simpleInner.toString());
        nameSeparator = "And";
      }
    }
  }

  @Override
  public Set<Facet> getFacets() {
    TreeSet<Facet> facets = null;
    List<? extends TypeMirror> typeArgs = this.root.getTypeArguments();
    for (TypeMirror typeArg : typeArgs) {
      if (typeArg instanceof DeclaredType) {
        Set<Facet> containedFacets = Facet.gatherFacets(((DeclaredType) typeArg).asElement(), context);
        if (facets == null) {
          facets = new TreeSet<>(containedFacets);
        }
        else {
          facets.retainAll(containedFacets);
        }
      }
    }
    return facets;
  }

  @Override
  public Name getBinaryName() {
    return this.fqn;
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
    return decorate(env.getElementUtils().getAllMembers(element));
  }

  protected List<? extends Element> decorate(List<? extends Element> elements) {
    ArrayList<Element> members = new ArrayList<Element>();
    for (Element member : elements) {
      if (member instanceof VariableElement) {
        members.add(new ParameterizedVariable((VariableElement) member));
      } else if (member instanceof ExecutableElement) {
        members.add(new ParameterizedExecutable((ExecutableElement) member));
      } else {
        members.add(member);
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
    return this.fqn;
  }

  @Override
  public Name getSimpleName() {
    return this.simpleName;
  }

  @Override
  public TypeMirror getSuperclass() {
    TypeMirror superclass = this.element.getSuperclass();
    if (superclass instanceof DeclaredType) {
      return new ParameterizedJacksonDeclaredType((DeclaredType) superclass, context);
    }
    return superclass;
  }

  @Override
  public List<? extends TypeMirror> getInterfaces() {
    return this.element.getInterfaces();
  }

  @Override
  public List<? extends TypeParameterElement> getTypeParameters() {
    return Collections.emptyList();
  }

  @Override
  public Element getEnclosingElement() {
    return this.element.getEnclosingElement();
  }

  @Override
  public TypeMirror asType() {
    return new ParameterizedJacksonDeclaredType(this.root, this.context);
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
  public Set<Modifier> getModifiers() {
    return this.element.getModifiers();
  }

  @Override
  public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
    return this.element.getAnnotationsByType(annotationType);
  }

  @Override
  public <R, P> R accept(ElementVisitor<R, P> v, P p) {
    return v.visitType(this, p);
  }

  private class ParameterizedVariable implements VariableElementAdaptor {

    private final VariableElement variableElement;

    public ParameterizedVariable(VariableElement variableElement) {
      this.variableElement = variableElement;
    }

    @Override
    public String getDocComment() {
      return env.getElementUtils().getDocComment(variableElement);
    }

    @Override
    public boolean isDeprecated() {
      return env.getElementUtils().isDeprecated(variableElement);
    }

    @Override
    public PackageElement getPackage() {
      return env.getElementUtils().getPackageOf(variableElement);
    }

    @Override
    public List<? extends AnnotationMirror> getAllAnnotationMirrors() {
      return env.getElementUtils().getAllAnnotationMirrors(variableElement);
    }

    @Override
    public boolean hides(Element hidden) {
      while (hidden instanceof ParameterizedVariable) {
        hidden = ((ParameterizedVariable) hidden).variableElement;
      }
      return env.getElementUtils().hides(variableElement, hidden);
    }

    @Override
    public boolean isHiddenBy(Element hider) {
      while (hider instanceof ParameterizedVariable) {
        hider = ((ParameterizedVariable) hider).variableElement;
      }

      return env.getElementUtils().hides(hider, variableElement);
    }

    @Override
    public SourcePosition getSourcePosition() {
      return null;
    }

    @Override
    public Object getConstantValue() {
      return variableElement.getConstantValue();
    }

    @Override
    public TypeMirror asType() {
      return variableContext.resolveTypeVariables(variableElement.asType(), env);
    }

    @Override
    public ElementKind getKind() {
      return variableElement.getKind();
    }

    @Override
    public List<? extends AnnotationMirror> getAnnotationMirrors() {
      return variableElement.getAnnotationMirrors();
    }

    @Override
    public <A extends Annotation> A getAnnotation(Class<A> annotationType) {
      return variableElement.getAnnotation(annotationType);
    }

    @Override
    public Set<Modifier> getModifiers() {
      return variableElement.getModifiers();
    }

    @Override
    public Name getSimpleName() {
      return variableElement.getSimpleName();
    }

    @Override
    public Element getEnclosingElement() {
      return ParameterizedJacksonTypeElement.this;
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
      return variableElement.getEnclosedElements();
    }

    @Override
    public <A extends Annotation> A[] getAnnotationsByType(Class<A> annotationType) {
      return variableElement.getAnnotationsByType(annotationType);
    }

    @Override
    public <R, P> R accept(ElementVisitor<R, P> v, P p) {
      return v.visitVariable(this, p);
    }
  }

  private class ParameterizedExecutable implements ExecutableElementAdaptor {

    private final ExecutableElement executableElement;

    private ParameterizedExecutable(ExecutableElement executableElement) {
      this.executableElement = executableElement;
    }

    @Override
    public boolean overrides(ExecutableElement overridden, TypeElement scope) {
      while (overridden instanceof ParameterizedExecutable) {
        overridden = ((ParameterizedExecutable) overridden).executableElement;
      }
      return env.getElementUtils().overrides(this.executableElement, overridden, scope);
    }

    @Override
    public boolean isOverriddenBy(ExecutableElement overrider, TypeElement type) {
      while (overrider instanceof ParameterizedExecutable) {
        overrider = ((ParameterizedExecutable) overrider).executableElement;
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
      while (hidden instanceof ParameterizedExecutable) {
        hidden = ((ParameterizedExecutable) hidden).executableElement;
      }
      return env.getElementUtils().hides(executableElement, hidden);
    }

    @Override
    public boolean isHiddenBy(Element hider) {
      while (hider instanceof ParameterizedExecutable) {
        hider = ((ParameterizedExecutable) hider).executableElement;
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
      return variableContext.resolveTypeVariables(executableElement.getReturnType(), env);
    }

    @Override
    public List<? extends VariableElement> getParameters() {
      ArrayList<VariableElement> parameters = new ArrayList<VariableElement>();
      for (VariableElement variableElement : executableElement.getParameters()) {
        parameters.add(new ParameterizedVariable(variableElement));
      }
      return parameters;
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
      return variableContext.resolveTypeVariables(executableElement.asType(), env);
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
      return executableElement.getModifiers();
    }

    @Override
    public Element getEnclosingElement() {
      return executableElement.getEnclosingElement();
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
