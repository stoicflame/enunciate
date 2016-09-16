package com.webcohesion.enunciate.javac.decorations.lombok;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import java.lang.annotation.Annotation;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * @author Tomasz Kalkosiński
 */
public class LombokPropertyDecoratedExecutableElement extends DecoratedExecutableElement {

    private VariableElement variableElement;
    private boolean getter;

    public LombokPropertyDecoratedExecutableElement(VariableElement variableElement, ProcessingEnvironment env, boolean getter) {
        super(null, env);
        this.variableElement = variableElement;
        this.getter = getter;
    }

    @Override
    public List<? extends TypeParameterElement> getTypeParameters() {
        return null;
    }

    @Override
    public TypeMirror getReturnType() {
        return getter ? variableElement.asType() : null;
    }

    @Override
    public List<? extends VariableElement> getParameters() {
        return getter ? Collections.<VariableElement>emptyList() : Collections.singletonList(variableElement);
    }

    @Override
    public boolean isVarArgs() {
        return false;
    }

    @Override
    public List<? extends TypeMirror> getThrownTypes() {
        return null;
    }

    @Override
    public AnnotationValue getDefaultValue() {
        return null;
    }

    @Override
    public TypeMirror asType() {
        return variableElement.asType();
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
        HashSet<Modifier> clone = new HashSet<Modifier>(variableElement.getModifiers());
        if (clone.contains(Modifier.PRIVATE)) {
            clone.remove(Modifier.PRIVATE);
            clone.add(Modifier.PUBLIC);
        }
        return clone;
    }

    @Override
    public Name getSimpleName() {
        return variableElement.getSimpleName();
    }

    @Override
    public Element getEnclosingElement() {
        return variableElement.getEnclosingElement();
    }

    @Override
    public List<? extends Element> getEnclosedElements() {
        return variableElement.getEnclosedElements();
    }

    @Override
    public String getPropertyName() {
        return variableElement.getSimpleName().toString();
    }

    @Override
    public String toString() {
        return variableElement.toString();
    }

    @Override
    public boolean isGetter() {
        return getter;
    }

    @Override
    public boolean isSetter() {
        return !getter;
    }
}