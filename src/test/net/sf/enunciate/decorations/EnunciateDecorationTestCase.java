package net.sf.enunciate.decorations;

import static org.testng.Assert.*;
import org.testng.IHookable;
import org.testng.IHookCallBack;
import com.sun.mirror.apt.AnnotationProcessorFactory;
import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;

import java.util.*;

import net.sf.enunciate.EnunciateTestCase;

/**
 * Base test case for decorations.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciateDecorationTestCase extends EnunciateTestCase implements IHookable, AnnotationProcessorFactory, AnnotationProcessor {

  private IHookCallBack callback;
  protected AnnotationProcessorEnvironment env;

  public void run(IHookCallBack callback) {
    this.callback = callback;
    invokeAPT(this, getAptOptions(), getAllJavaFiles(getSubDirName()));
  }

  public Collection<String> supportedOptions() {
    return Collections.emptyList();
  }

  public Collection<String> supportedAnnotationTypes() {
    return Arrays.asList("*");
  }

  public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set, AnnotationProcessorEnvironment env) {
    this.env = env;
    return this;
  }

  public void process() {
    assertNotNull(this.callback, "Uninitialized callback.");
    assertNotNull(this.env, "Uninitialized environment.");
    this.callback.runTestMethod();
  }

  protected ArrayList<String> getAptOptions() {
    ArrayList<String> aptOpts = new ArrayList<String>();
    aptOpts.add("-cp");
    aptOpts.add(System.getProperty("java.class.path"));
    aptOpts.add("-nocompile");
    return aptOpts;
  }

  protected String getSubDirName() {
    return "decorations";
  }

}
