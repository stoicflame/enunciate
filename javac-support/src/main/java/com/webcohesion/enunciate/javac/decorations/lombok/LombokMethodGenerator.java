package com.webcohesion.enunciate.javac.decorations.lombok;

import com.webcohesion.enunciate.javac.decorations.element.DecoratedExecutableElement;

import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.element.*;
import java.util.List;

/**
 * @author Tomasz Kalkosiński
 */
public class LombokMethodGenerator {
    private static final String LOMBOK_DATA_FQN = "lombok.Data";
    private static final String LOMBOK_GETTER_FQN = "lombok.Getter";
    private static final String LOMBOK_SETTER_FQN = "lombok.Setter";

    private List<? extends AnnotationMirror> annotationMirrors;
    private List<ExecutableElement> methods;
    private List<? extends VariableElement> fields;
    private ProcessingEnvironment env;

    public LombokMethodGenerator(List<? extends AnnotationMirror> annotationMirrors, List<ExecutableElement> methods, List<? extends VariableElement> fields, ProcessingEnvironment env) {
        this.annotationMirrors = annotationMirrors;
        this.methods = methods;
        this.fields = fields;
        this.env = env;
    }

    public void generateLombokGettersAndSetters() {
        for (VariableElement field : fields) {
            if (shouldGenerateGetter(field)) {
                methods.add(new LombokPropertyDecoratedExecutableElement(field, env, true));
            }
            if (shouldGenerateSetter(field)) {
                methods.add(new LombokPropertyDecoratedExecutableElement(field, env, false));
            }
        }
    }

    private boolean shouldGenerateGetter(VariableElement field) {
        String fieldSimpleName = field.getSimpleName().toString();
        for (ExecutableElement method : methods) {
            DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
            if (decoratedMethod.getPropertyName() != null && decoratedMethod.getPropertyName().equals(fieldSimpleName) && decoratedMethod.isGetter()) {
                return false;
            }
        }

        if (isAnnotated(field.getAnnotationMirrors(), LOMBOK_GETTER_FQN)) {
            return true;
        }

        if (isAnnotated(annotationMirrors, LOMBOK_DATA_FQN)) {
            return true;
        }

        return false;
    }

    private boolean shouldGenerateSetter(VariableElement field) {
        String fieldSimpleName = field.getSimpleName().toString();
        for (ExecutableElement method : methods) {
            DecoratedExecutableElement decoratedMethod = (DecoratedExecutableElement) method;
            if (decoratedMethod.getPropertyName() != null && decoratedMethod.getPropertyName().equals(fieldSimpleName) && decoratedMethod.isSetter()) {
                return false;
            }
        }

        if (isAnnotated(field.getAnnotationMirrors(), LOMBOK_SETTER_FQN)) {
            return true;
        }

        if (isAnnotated(annotationMirrors, LOMBOK_DATA_FQN)) {
            return true;
        }

        return false;
    }

    private boolean isAnnotated(List<? extends AnnotationMirror> annotationMirrors, String expectedAnnotationFqn) {
        for (AnnotationMirror annotationMirror : annotationMirrors) {
            Element annotationDeclaration = annotationMirror.getAnnotationType().asElement();
            if (annotationDeclaration != null) {
                String fqn = annotationDeclaration instanceof TypeElement ? ((TypeElement) annotationDeclaration).getQualifiedName().toString() : "";
                if (fqn.equals(expectedAnnotationFqn)) {
                    return true;
                }
            }
        }
        return false;
    }
}