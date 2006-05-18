package net.sf.enunciate.contract;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.apt.AnnotationProcessorEnvironment;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.EnunciateTestCase;
import net.sf.jelly.apt.ProcessorFactory;
import static org.testng.Assert.assertNotNull;
import org.testng.IHookCallBack;
import org.testng.IHookable;

import java.net.URL;
import java.util.*;

/**
 * Base test case for contract.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciateContractTestCase extends EnunciateTestCase implements IHookable {

  private IHookCallBack callback;
  protected AnnotationProcessorEnvironment env;

  public void run(IHookCallBack callback) {
    this.callback = callback;
    invokeAPT(new APFInternal(), getAptOptions(), getAllJavaFiles(getSubDirName()));
  }

  protected ArrayList<String> getAptOptions() {
    ArrayList<String> aptOpts = new ArrayList<String>();
    aptOpts.add("-cp");
    aptOpts.add(System.getProperty("java.class.path"));
    aptOpts.add("-nocompile");
    return aptOpts;
  }

  protected String getSubDirName() {
    return "contract";
  }

  /**
   * Gets the declaration given the fully-qualified name.  Asserts it exists.
   *
   * @param fqn The fqn.
   * @return The declaration.
   */
  protected TypeDeclaration getDeclaration(String fqn) {
    TypeDeclaration declaration = this.env.getTypeDeclaration(fqn);
    assertNotNull(declaration, "No source def found: " + fqn);
    return declaration;
  }

  private class APFInternal extends ProcessorFactory implements AnnotationProcessor {
    @Override
    public Collection<String> supportedOptions() {
      return Collections.emptyList();
    }

    @Override
    public Collection<String> supportedAnnotationTypes() {
      return Arrays.asList("*");
    }

    @Override
    public AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> set, AnnotationProcessorEnvironment ape) {
      env = ape;
      return super.getProcessorFor(set, ape);
    }

    @Override
    protected AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> annotations) {
      return this;
    }

    protected AnnotationProcessor newProcessor(URL url) {
      throw new UnsupportedOperationException();
    }

    public void process() {
      assertNotNull(callback, "Uninitialized callback.");
      assertNotNull(env, "Uninitialized environment.");
      callback.runTestMethod();
    }

  }

}
