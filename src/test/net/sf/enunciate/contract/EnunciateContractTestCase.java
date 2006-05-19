package net.sf.enunciate.contract;

import com.sun.mirror.apt.AnnotationProcessor;
import com.sun.mirror.declaration.AnnotationTypeDeclaration;
import com.sun.mirror.declaration.TypeDeclaration;
import net.sf.enunciate.EnunciateTestCase;
import net.sf.jelly.apt.Context;
import net.sf.jelly.apt.ProcessorFactory;
import static org.testng.Assert.assertNotNull;

import java.net.URL;
import java.util.*;

/**
 * Base test case for contract classes.  Initializes the APT environment with all java files in the
 * specified subdirectory.
 *
 * @author Ryan Heaton
 */
public abstract class EnunciateContractTestCase extends EnunciateTestCase {

  protected EnunciateContractTestCase() {
    try {
      assertNotNull(Context.getCurrentEnvironment());
    }
    catch (IllegalStateException ise) {
      invokeAPT(new NoOpAPF(), getAptOptions(), getAllJavaFiles(getSubDirName()));
    }
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
    TypeDeclaration declaration = Context.getCurrentEnvironment().getTypeDeclaration(fqn);
    assertNotNull(declaration, "No source def found: " + fqn);
    return declaration;
  }

  /**
   * Used just to set the current environment.
   */
  private class NoOpAPF extends ProcessorFactory implements AnnotationProcessor {

    @Override
    public Collection<String> supportedOptions() {
      return Collections.emptyList();
    }

    @Override
    public Collection<String> supportedAnnotationTypes() {
      return Arrays.asList("*");
    }

    @Override
    protected AnnotationProcessor getProcessorFor(Set<AnnotationTypeDeclaration> annotations) {
      return this;
    }

    protected AnnotationProcessor newProcessor(URL url) {
      throw new UnsupportedOperationException();
    }

    public void process() {
      //no-op.
    }

  }

}
