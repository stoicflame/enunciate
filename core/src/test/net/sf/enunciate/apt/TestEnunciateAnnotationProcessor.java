package net.sf.enunciate.apt;

import net.sf.enunciate.EnunciateException;
import net.sf.enunciate.InAPTTestCase;
import net.sf.enunciate.OutsideAPTOkay;
import net.sf.enunciate.contract.jaxb.*;
import net.sf.enunciate.contract.jaxws.EndpointInterface;
import net.sf.enunciate.contract.validation.BaseValidator;
import net.sf.enunciate.contract.validation.ValidationResult;
import net.sf.enunciate.contract.validation.Validator;
import net.sf.enunciate.config.EnunciateConfiguration;
import net.sf.enunciate.modules.BasicDeploymentModule;
import net.sf.enunciate.modules.DeploymentModule;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.Collection;
import java.util.ArrayList;

import junit.framework.Test;
import junit.framework.AssertionFailedError;
import com.sun.mirror.declaration.TypeDeclaration;
import com.sun.mirror.declaration.ClassDeclaration;

/**
 * @author Ryan Heaton
 */
public class TestEnunciateAnnotationProcessor extends InAPTTestCase {

  /**
   * Tests the process.
   */
  @OutsideAPTOkay
  public void testProcess() throws Exception {
    final MockDeploymentModule mockModule = new MockDeploymentModule();
    EnunciateConfiguration config = new EnunciateConfiguration() {
      @Override
      public List<DeploymentModule> getEnabledModules() {
        return Arrays.asList(new DeploymentModule[]{mockModule});
      }
    };

    EnunciateAnnotationProcessor processor = new EnunciateAnnotationProcessor(config) {
      @Override
      protected EnunciateFreemarkerModel getRootModel() {
        mockModule.model = new EnunciateFreemarkerModel();
        return null;
      }
    };

    processor.process();
    processor.throwAnyErrors();
    assertTrue(mockModule.generated);
    assertNotNull(mockModule.model);
  }

  /**
   * Tests getting the root model.
   */
  public void testGetRootModel() throws Exception {
    EnunciateConfiguration config = new EnunciateConfiguration();
    config.putNamespace("urn:test", "test");
    config.putNamespace("http://enunciate.sf.net/samples/contract", "tContract");

    final boolean[] validated = new boolean[] {false};
    EnunciateAnnotationProcessor processor = new EnunciateAnnotationProcessor(config) {
      @Override
      protected void validate(EnunciateFreemarkerModel model) {
        validated[0] = true;
      }

      @Override
      public boolean isEndpointInterface(TypeDeclaration declaration) {
        return "net.sf.enunciate.samples.services.NamespacedWebService".equals(declaration.getQualifiedName());
      }

      @Override
      protected boolean isPotentialSchemaType(TypeDeclaration declaration) {
        return "net.sf.enunciate.samples.schema".equals(declaration.getPackage().getQualifiedName());
      }

      @Override
      public TypeDefinition createTypeDefinition(ClassDeclaration declaration) {
        if ("net.sf.enunciate.samples.schema.BeanOne".equals(declaration.getQualifiedName())) {
          return new ComplexTypeDefinition(declaration);
        }
        else if ("net.sf.enunciate.samples.schema.BeanTwo".equals(declaration.getQualifiedName())) {
          return new ComplexTypeDefinition(declaration);
        }
        else if ("net.sf.enunciate.samples.schema.BeanThree".equals(declaration.getQualifiedName())) {
          return new ComplexTypeDefinition(declaration);
        }

        throw new AssertionFailedError(declaration.getQualifiedName() + " shouldn't have been considered as a potential schema type.");
      }

      @Override
      public RootElementDeclaration createRootElementDeclaration(ClassDeclaration declaration, TypeDefinition typeDefinition) {
        if ("net.sf.enunciate.samples.schema.BeanThree".equals(declaration.getQualifiedName())) {
          return new RootElementDeclaration(declaration, typeDefinition);
        }

        return null;
      }
    };

    EnunciateFreemarkerModel model = processor.getRootModel();
    assertEquals("One and only one endpoint interface should have been found.", 1, model.endpointInterfaces.size());
    assertEquals("Three and only three type definitions should have been found.", 3, model.typeDefinitions.size());
    assertEquals("One and only one root elements should have been found.", 1, model.rootElements.size());
    assertNotNull(model.findTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanOne")));
    assertNotNull(model.findTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanTwo")));
    assertNotNull(model.findTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanThree")));
    assertNotNull(model.findRootElementDeclaration((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanThree")));
    assertTrue(validated[0]);
    assertEquals("tContract", model.getNamespacesToPrefixes().get("http://enunciate.sf.net/samples/contract"));
    assertEquals("test", model.getNamespacesToPrefixes().get("urn:test"));
  }

  /**
   * Tests validating the model.
   */
  public void testValidate() throws Exception {
    final EndpointInterface ei = new EndpointInterface(getDeclaration("net.sf.enunciate.samples.services.NamespacedWebService"));
    final ComplexTypeDefinition beanOne = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanOne"));
    final ComplexTypeDefinition beanTwo = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanTwo"));
    final ComplexTypeDefinition beanThree = new ComplexTypeDefinition((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanThree"));
    RootElementDeclaration beanThreeElement = new RootElementDeclaration((ClassDeclaration) getDeclaration("net.sf.enunciate.samples.schema.BeanThree"), beanThree);

    EnunciateConfiguration config = new EnunciateConfiguration();
    final MockValidator validator = new MockValidator();
    config.setValidator(validator);
    config.addModule(new BasicDeploymentModule() {
      @Override
      public Validator getValidator() {
        return validator;
      }
    });

    config.addModule(new BasicDeploymentModule() {
      @Override
      public String getName() {
        return "basic2";
      }

      @Override
      public Validator getValidator() {
        return validator;
      }
    });

    EnunciateAnnotationProcessor processor = new EnunciateAnnotationProcessor(config);
    EnunciateFreemarkerModel model = new EnunciateFreemarkerModel();
    model.add(ei);
    model.add(beanOne);
    model.add(beanTwo);
    model.add(beanThree);
    model.add(beanThreeElement);

    processor.validate(model);

    for (int i = 0; i < 3; i++) {
      assertTrue("Endpoint interface should have been validated three (and only three) times.", validator.validatedObjects.remove(ei));
    }
    assertFalse("Endpoint interface should have been validated three (and only three) times.", validator.validatedObjects.remove(ei));

    for (int i = 0; i < 3; i++) {
      assertTrue("Bean one should have been validated three (and only three) times.", validator.validatedObjects.remove(beanOne));
    }
    assertFalse("Bean one should have been validated three (and only three) times.", validator.validatedObjects.remove(beanOne));

    for (int i = 0; i < 3; i++) {
      assertTrue("Bean two should have been validated three (and only three) times.", validator.validatedObjects.remove(beanTwo));
    }
    assertFalse("Bean two should have been validated three (and only three) times.", validator.validatedObjects.remove(beanTwo));

    for (int i = 0; i < 3; i++) {
      assertTrue("Bean three should have been validated three (and only three) times.", validator.validatedObjects.remove(beanThree));
    }

    for (int i = 0; i < 3; i++) {
      assertTrue("Bean three element should have been validated three (and only three) times.", validator.validatedObjects.remove(beanThreeElement));
    }

    assertFalse("Bean three should have been validated three (and only three) times.", validator.validatedObjects.remove(beanThree));
    assertFalse("Bean three element should have been validated three (and only three) times.", validator.validatedObjects.remove(beanThreeElement));

    processor = new EnunciateAnnotationProcessor(config) {
      @Override
      protected ValidationResult validate(EnunciateFreemarkerModel model, Validator validator) {
        ValidationResult result = new ValidationResult();
        result.addWarning(beanThree.getPosition(), "test warning.");
        result.addError(beanTwo.getPosition(), "test error");
        result.addError(beanTwo.getPosition(), "test error2");
        return result;
      }
    };

    try {
      processor.validate(model); //todo: make sure all warnings and errors are printed.
      fail("Should have thrown a model validation exception.");
    }
    catch (ModelValidationException e) {
      //fall through...
    }
  }

  /**
   * Whether a type is a simple type.
   */
  public void testIsSimpleType() throws Exception {
    EnunciateAnnotationProcessor processor = new EnunciateAnnotationProcessor();
    assertFalse(processor.isSimpleType(getDeclaration("net.sf.enunciate.samples.schema.BeanOne")));
    assertFalse(processor.isSimpleType(getDeclaration("net.sf.enunciate.samples.schema.BeanTwo")));
    assertFalse(processor.isSimpleType(getDeclaration("net.sf.enunciate.samples.schema.BeanThree")));
    assertFalse(processor.isSimpleType(getDeclaration("net.sf.enunciate.samples.anotherschema.SimpleTypeComplexContentBean")));
    assertTrue(processor.isSimpleType(getDeclaration("net.sf.enunciate.samples.anotherschema.SimpleTypeSimpleContentBean")));
  }

  private static class MockValidator extends BaseValidator {
    Collection<Object> validatedObjects = new ArrayList<Object>();

    @Override
    public ValidationResult validateEndpointInterface(EndpointInterface ei) {
      validatedObjects.add(ei);
      return super.validateEndpointInterface(ei);
    }

    @Override
    public ValidationResult validateComplexType(ComplexTypeDefinition complexType) {
      validatedObjects.add(complexType);
      return super.validateComplexType(complexType);
    }

    @Override
    public ValidationResult validateSimpleType(SimpleTypeDefinition simpleType) {
      validatedObjects.add(simpleType);
      return super.validateSimpleType(simpleType);
    }

    @Override
    public ValidationResult validateEnumType(EnumTypeDefinition enumType) {
      validatedObjects.add(enumType);
      return super.validateEnumType(enumType);
    }

    @Override
    public ValidationResult validateRootElement(RootElementDeclaration rootElementDeclaration) {
      validatedObjects.add(rootElementDeclaration);
      return super.validateRootElement(rootElementDeclaration);
    }
  }

  private static class MockDeploymentModule extends BasicDeploymentModule {
    boolean generated = false;

    EnunciateFreemarkerModel model = null;

    @Override
    protected void doGenerate() throws EnunciateException, IOException {
      this.generated = true;
    }
  }

  public static Test suite() {
    return createSuite(TestEnunciateAnnotationProcessor.class);
  }
}
