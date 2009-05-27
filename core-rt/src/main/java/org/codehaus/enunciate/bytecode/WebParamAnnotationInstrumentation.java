package org.codehaus.enunciate.bytecode;

import org.objectweb.asm.*;

import javax.jws.WebParam;

/**
 * Instrumentation for a class that changes the default value of the {@link WebParam @WebParam} annotation
 * to the name of the parameter.
 *
 * @author Ryan Heaton
 */
public class WebParamAnnotationInstrumentation extends ClassAdapter {

  private static final String WEB_PARAM_DESCRIPTOR = Type.getDescriptor(WebParam.class);
  private static final ThreadLocal<String> CURRENT_CLASS_DESCRIPTOR = new ThreadLocal<String>();
  private final InstrumentationInfo instrumentation;

  /**
   * @param cv The delegate.
   * @param instrumentation The instrumentation information
   */
  public WebParamAnnotationInstrumentation(ClassVisitor cv, InstrumentationInfo instrumentation) {
    super(cv);
    this.instrumentation = instrumentation;
  }

  @Override
  public void visit(int version, int access, String name, String signature, String superName, String[] interfaces) {
    CURRENT_CLASS_DESCRIPTOR.set(name);
    super.visit(version, access, name, signature, superName, interfaces);
  }

  @Override
  public void visitEnd() {
    CURRENT_CLASS_DESCRIPTOR.remove();
  }

  @Override
  public MethodVisitor visitMethod(int access, String name, String desc, String signature, String[] exceptions) {
    MethodKey mk = new MethodKey(CURRENT_CLASS_DESCRIPTOR.get(), name, desc);
    MethodVisitor mv = super.visitMethod(access, name, desc, signature, exceptions);
    String[] parameterNames = this.instrumentation.getParameterNames().get(mk);
    if (parameterNames != null) {
      mv = new WebParamAnnotationMethodVisitor(mv, parameterNames);
    }
    return mv;
  }

  private static class WebParamAnnotationMethodVisitor extends MethodAdapter {

    private int currentParameter = 0;
    private final String[] parameterNames;

    // Inherited.
    public WebParamAnnotationMethodVisitor(MethodVisitor mv, String[] parameterNames) {
      super(mv);
      this.parameterNames = parameterNames;
    }

    @Override
    public AnnotationVisitor visitParameterAnnotation(int parameter, String desc, boolean visible) {
      while (currentParameter < parameter) {
        AnnotationVisitor paramVisitor = super.visitParameterAnnotation(currentParameter, WEB_PARAM_DESCRIPTOR, true);
        paramVisitor.visit("name",this.parameterNames[currentParameter]);
        paramVisitor.visitEnd();
        currentParameter++;
      }

      AnnotationVisitor av = super.visitParameterAnnotation(parameter, desc, visible);
      if (visible && WEB_PARAM_DESCRIPTOR.equals(desc)) {
        av = new WebParamAnnotationVisitor(av, this.parameterNames[parameter]);
      }
      currentParameter++;
      return av;
    }

    @Override
    public void visitCode() {
      while (currentParameter < this.parameterNames.length) {
        AnnotationVisitor paramVisitor = super.visitParameterAnnotation(currentParameter, WEB_PARAM_DESCRIPTOR, true);
        paramVisitor.visit("name",this.parameterNames[currentParameter]);
        paramVisitor.visitEnd();
        currentParameter++;
      }

      super.visitCode();
    }

    @Override
    public void visitEnd() {
      while (currentParameter < this.parameterNames.length) {
        AnnotationVisitor paramVisitor = super.visitParameterAnnotation(currentParameter, WEB_PARAM_DESCRIPTOR, true);
        paramVisitor.visit("name",this.parameterNames[currentParameter]);
        paramVisitor.visitEnd();
        currentParameter++;
      }

      super.visitEnd();
    }
  }

  private static class WebParamAnnotationVisitor implements AnnotationVisitor {

    private final String parameterName;
    private final AnnotationVisitor delegate;
    private boolean nameVisited = false;

    public WebParamAnnotationVisitor(AnnotationVisitor delegate, String parameterName) {
      this.parameterName = parameterName;
      this.delegate = delegate;
    }

    public void visit(String name, Object value) {
      if ("name".equals(name) && "".equals(value)) {
        delegate.visit(name, this.parameterName);
      }
      else {
        delegate.visit(name, value);
      }
      nameVisited = true;
    }

    public void visitEnum(String name, String desc, String value) {
      delegate.visitEnum(name, desc, value);
    }

    public AnnotationVisitor visitAnnotation(String name, String desc) {
      return delegate.visitAnnotation(name, desc);
    }

    public AnnotationVisitor visitArray(String name) {
      return delegate.visitArray(name);
    }

    public void visitEnd() {
      if (!nameVisited) {
        delegate.visit("name", this.parameterName);
      }
      delegate.visitEnd();
    }
  }
}
