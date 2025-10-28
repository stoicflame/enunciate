package com.webcohesion.enunciate.modules.jackson.model.adapters;

import com.google.testing.compile.Compilation;
import com.google.testing.compile.Compiler;
import com.google.testing.compile.JavaFileObjects;
import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.modules.jackson.EnunciateJacksonContext;
import org.junit.Test;
import org.mockito.Mockito;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.TypeElement;
import javax.lang.model.type.DeclaredType;
import javax.lang.model.type.TypeMirror;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import javax.tools.JavaFileObject;
import java.util.List;
import java.util.Set;

import static org.junit.Assert.*;

public class AdapterTypeTest {

    @Test
    public void classImplementingConverterInterface() {
        String source =
            "package test;\n"
            + "import com.fasterxml.jackson.databind.JavaType;\n"
            + "import com.fasterxml.jackson.databind.type.TypeFactory;\n"
            + "import com.fasterxml.jackson.databind.util.Converter;\n"
            + "public class MyConverter implements Converter<String, Integer> {\n"
            + "  public Integer convert(String value) { return null; }\n"
            + "  public JavaType getInputType(TypeFactory typeFactory) { return typeFactory.constructType(String.class); }\n"
            + "  public JavaType getOutputType(TypeFactory typeFactory) { return typeFactory.constructType(Integer.class); }\n"
            + "}\n";
        runAdapterTypeTest("MyConverter", source, (adapterType, types, elements) -> {
            assertNotNull(adapterType);
            TypeElement typeElement = elements.getTypeElement("test.MyConverter");
            DeclaredType iface = (DeclaredType) typeElement.getInterfaces().get(0);
            List<? extends TypeMirror> typeArgs = iface.getTypeArguments();
            assertEquals(typeArgs.get(1).toString(), adapterType.getAdaptingType().toString());
            assertEquals(types.erasure(typeArgs.get(0)).toString(), adapterType.getAdaptedType().toString());
        });
    }

    @Test
    public void classExtendingImplementionOfConverterInterface() {
        String source =
            "package test;\n"
            + "import com.fasterxml.jackson.databind.util.StdConverter;\n"
            + "public class MyStdConverter extends StdConverter<String, Integer> {\n"
            + "  @Override\n"
            + "  public Integer convert(String value) { return null; }\n"
            + "}\n";
        runAdapterTypeTest("MyStdConverter", source, (adapterType, types, elements) -> {
            assertNotNull(adapterType);
            TypeElement typeElement = elements.getTypeElement("test.MyStdConverter");
            DeclaredType superclass = (DeclaredType) typeElement.getSuperclass();
            List<? extends TypeMirror> typeArgs = superclass.getTypeArguments();
            assertEquals(typeArgs.get(1).toString(), adapterType.getAdaptingType().toString());
            assertEquals(types.erasure(typeArgs.get(0)).toString(), adapterType.getAdaptedType().toString());
        });
    }

    @Test
    public void classNotAChildOfConverter() {
        String source =
            "package test;\n"
            + "public class NotAConverter {\n"
            + "  public String convert(String value) { return value; }\n"
            + "}\n";
        runAdapterTypeTest("NotAConverter", source, (adapterType, types, elements) -> {
            assertNull(adapterType);
        });
    }

    private void runAdapterTypeTest(String className, String source, TriConsumer<AdapterType, Types, Elements> assertion) {
        JavaFileObject file = JavaFileObjects.forSourceString("test." + className, source);

        class AdapterTypeTestProcessor extends AbstractProcessor {
            private boolean tested = false;
            @Override
            public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
                if (tested) return false;
                Elements elements = processingEnv.getElementUtils();
                Types types = processingEnv.getTypeUtils();
                TypeElement typeElement = elements.getTypeElement("test." + className);
                DeclaredType declaredType = (DeclaredType) typeElement.asType();

                EnunciateContext enunciateContext = Mockito.mock(EnunciateContext.class);
                DecoratedProcessingEnvironment env = Mockito.mock(DecoratedProcessingEnvironment.class);
                Mockito.when(enunciateContext.getProcessingEnvironment()).thenReturn(env);
                EnunciateJacksonContext jacksonContext = Mockito.mock(EnunciateJacksonContext.class);
                Mockito.when(jacksonContext.getContext()).thenReturn(enunciateContext);
                Mockito.when(env.getTypeUtils()).thenReturn(types);

                AdapterType adapterType = null;
                try {
                    adapterType = new AdapterType(declaredType, jacksonContext);
                } catch (Exception e) {
                    assertion.accept(null, types, elements);
                    tested = true;
                    return false;
                }
                assertion.accept(adapterType, types, elements);
                tested = true;
                return false;
            }
            @Override
            public Set<String> getSupportedAnnotationTypes() { return Set.of("*"); }
            @Override
            public SourceVersion getSupportedSourceVersion() { return SourceVersion.latestSupported(); }
        }

        Compilation compilation = Compiler.javac()
                .withProcessors(new AdapterTypeTestProcessor())
                .compile(file);

        if (compilation.status() != Compilation.Status.SUCCESS) {
            System.err.println("Compilation failed. Diagnostics:");
            compilation.diagnostics().forEach(System.err::println);
        }
        assertEquals(Compilation.Status.SUCCESS, compilation.status());
    }

    @FunctionalInterface
    private interface TriConsumer<A, B, C> {
        void accept(A a, B b, C c);
    }
}