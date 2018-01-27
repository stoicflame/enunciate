/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate.modules.jackson1;

import com.webcohesion.enunciate.EnunciateContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.metadata.Ignore;
import com.webcohesion.enunciate.module.*;
import com.webcohesion.enunciate.modules.jackson1.model.AccessorVisibilityChecker;
import com.webcohesion.enunciate.modules.jackson1.model.types.KnownJsonType;
import com.webcohesion.enunciate.util.MediaTypeUtils;
import com.webcohesion.enunciate.util.OneTimeLogMessage;
import java.util.*;
import javax.lang.model.element.*;
import javax.lang.model.type.TypeMirror;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlTransient;
import javax.xml.bind.annotation.XmlType;
import org.apache.commons.configuration.HierarchicalConfiguration;
import org.codehaus.jackson.annotate.JacksonAnnotation;
import org.codehaus.jackson.annotate.JsonAutoDetect;
import org.codehaus.jackson.annotate.JsonIgnore;
import org.codehaus.jackson.annotate.JsonMethod;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.map.annotate.JsonSerialize;
import org.reflections.adapters.MetadataAdapter;

/**
 * @author Ryan Heaton
 */
@SuppressWarnings("unchecked")
public class Jackson1Module extends BasicProviderModule implements TypeDetectingModule, MediaTypeDefinitionModule, ApiRegistryProviderModule, ApiFeatureProviderModule {

    private DataTypeDetectionStrategy defaultDataTypeDetectionStrategy;
    private boolean jacksonDetected = false;
    private boolean jaxbSupportDetected = false;
    private EnunciateJackson1Context jacksonContext;
    private ApiRegistry apiRegistry;

    @Override
    public String getName() {
        return "jackson1";
    }

    public boolean isHonorJaxbAnnotations() {
        return this.config.getBoolean("[@honorJaxb]", this.jaxbSupportDetected);
    }

    public boolean isCollapseTypeHierarchy() {
        return this.config.getBoolean("[@collapse-type-hierarchy]", false);
    }

    public boolean isWrapRootValue() {
        return this.config.getBoolean("[@wrapRootValue]", false);
    }
    //ISSUE:#810

    public String getPropertyNamingStrategy() {
        return this.config.getString("[@propertyNamingStrategy]", "");
    }

    public KnownJsonType getDateFormat() {
        String dateFormatString = this.config.getString("[@dateFormat]", KnownJsonType.WHOLE_NUMBER.name());
        return KnownJsonType.valueOf(dateFormatString.toUpperCase());
    }

    public boolean isDisableExamples() {
        return this.config.getBoolean("[@disableExamples]", false);
    }

    @Override
    public ApiRegistry getApiRegistry() {
        return new Jackson1ApiRegistry(this.jacksonContext);
    }

    public EnunciateJackson1Context getJacksonContext() {
        return jacksonContext;
    }

    @Override
    public void call(EnunciateContext context) {
        this.jacksonContext = new EnunciateJackson1Context(context, isHonorJaxbAnnotations(), getDateFormat(), isCollapseTypeHierarchy(), getMixins(), getExternalExamples(), getDefaultVisibility(), isDisableExamples(), isWrapRootValue(), getPropertyNamingStrategy());
        DataTypeDetectionStrategy detectionStrategy = getDataTypeDetectionStrategy();
        switch (detectionStrategy) {
            case aggressive:
                for (Element declaration : context.getApiElements()) {
                    addPotentialJacksonElement(declaration, new LinkedList<Element>());
                }
                break;
            case local:
                for (Element declaration : context.getLocalApiElements()) {
                    addPotentialJacksonElement(declaration, new LinkedList<Element>());
                }
            //no break, add explicit includes:
            default:
                if (context.hasExplicitIncludes()) { //if we're not aggressive, we only want to add the api elements if they've been explicitly included
                    for (Element declaration : context.getApiElements()) {
                        addPotentialJacksonElement(declaration, new LinkedList<Element>());
                    }
                }
        }
    }

    @Override
    protected boolean isEnabledByDefault() {
        return jacksonDetected && super.isEnabledByDefault();
    }

    public DataTypeDetectionStrategy getDataTypeDetectionStrategy() {
        String dataTypeDetection = this.config.getString("[@datatype-detection]", null);

        if (dataTypeDetection != null) {
            try {
                return DataTypeDetectionStrategy.valueOf(dataTypeDetection);
            } catch (IllegalArgumentException e) {
                //fall through...
            }
        }

        return this.defaultDataTypeDetectionStrategy == null ? DataTypeDetectionStrategy.local : this.defaultDataTypeDetectionStrategy;
    }

    @Override
    public void setDefaultDataTypeDetectionStrategy(DataTypeDetectionStrategy strategy) {
        this.defaultDataTypeDetectionStrategy = strategy;
    }

    @Override
    public void addDataTypeDefinitions(TypeMirror type, Set<String> declaredMediaTypes, LinkedList<Element> contextStack) {
        if (MediaTypeUtils.isJsonCompatible(declaredMediaTypes)) {
            type = this.jacksonContext.resolveSyntheticType((DecoratedTypeMirror) type);
            this.jacksonContext.addReferencedTypeDefinitions(type, contextStack);
        } else {
            debug("Element %s is NOT to be added as a Jackson data type because %s doesn't seem to include JSON.", type, declaredMediaTypes);
        }
    }

    public Map<String, String> getMixins() {
        HashMap<String, String> mixins = new HashMap<String, String>();
        List<HierarchicalConfiguration> mixinElements = this.config.configurationsAt("mixin");
        for (HierarchicalConfiguration mixinElement : mixinElements) {
            mixins.put(mixinElement.getString("[@target]", ""), mixinElement.getString("[@source]", ""));
        }
        return mixins;
    }

    public Map<String, String> getExternalExamples() {
        HashMap<String, String> examples = new HashMap<String, String>();
        List<HierarchicalConfiguration> exampleElements = this.config.configurationsAt("examples.example");
        for (HierarchicalConfiguration exampleElement : exampleElements) {
            examples.put(exampleElement.getString("[@type]", ""), exampleElement.getString("[@example]", "..."));
        }
        return examples;
    }

    public AccessorVisibilityChecker getDefaultVisibility() {
        List<HierarchicalConfiguration> visibilityElements = this.config.configurationsAt("accessor-visibility");
        AccessorVisibilityChecker checker = AccessorVisibilityChecker.DEFAULT_CHECKER;
        for (HierarchicalConfiguration visibilityElement : visibilityElements) {
            JsonMethod method = JsonMethod.valueOf(visibilityElement.getString("[@type]", "").toUpperCase());
            JsonAutoDetect.Visibility visibility = JsonAutoDetect.Visibility.valueOf(visibilityElement.getString("[@visibility]", "").toUpperCase());
            checker = checker.withVisibility(method, visibility);
        }
        return checker;
    }

    protected void addPotentialJacksonElement(Element declaration, LinkedList<Element> contextStack) {
        if (declaration instanceof TypeElement) {
            if (!this.jacksonContext.isKnownTypeDefinition((TypeElement) declaration) && isExplicitTypeDefinition(declaration, this.jacksonContext.isHonorJaxb())) {
                OneTimeLogMessage.JACKSON_1_DEPRECATED.log(this.context);
                this.jacksonContext.add(this.jacksonContext.createTypeDefinition((TypeElement) declaration), contextStack);
            }
        }
    }

    protected boolean isExplicitTypeDefinition(Element declaration, boolean honorJaxb) {
        if (declaration.getKind() != ElementKind.CLASS && declaration.getKind() != ElementKind.ENUM) {
            debug("%s isn't a potential Jackson type because it's not a class or an enum.", declaration);
            return false;
        }

        PackageElement pckg = this.context.getProcessingEnvironment().getElementUtils().getPackageOf(declaration);
        if ((pckg != null) && (pckg.getAnnotation(Ignore.class) != null)) {
            debug("%s isn't a potential Jackson type because its package is annotated as to be ignored.", declaration);
            return false;
        }

        if (isThrowable(declaration)) {
            debug("%s isn't a potential Jackson type because it's an instance of java.lang.Throwable.", declaration);
            return false;
        }

        List<? extends AnnotationMirror> annotationMirrors = declaration.getAnnotationMirrors();
        boolean explicitXMLTypeOrElement = false;
        for (AnnotationMirror mirror : annotationMirrors) {
            Element annotationDeclaration = mirror.getAnnotationType().asElement();
            if (annotationDeclaration != null) {
                String fqn = annotationDeclaration instanceof TypeElement ? ((TypeElement) annotationDeclaration).getQualifiedName().toString() : "";
                //exclude all XmlTransient types and all jaxws types.
                if (JsonIgnore.class.getName().equals(fqn)
                        || fqn.startsWith("javax.xml.ws")
                        || fqn.startsWith("javax.ws.rs")
                        || fqn.startsWith("javax.jws")) {
                    debug("%s isn't a potential Jackson type because of annotation %s.", declaration, fqn);
                    return false;
                } else {
                    if (honorJaxb) {
                        if (XmlTransient.class.getName().equals(fqn)) {
                            debug("%s isn't a potential Jackson type because of annotation %s.", declaration, fqn);
                            return false;
                        }

                        if ((XmlType.class.getName().equals(fqn)) || (XmlRootElement.class.getName().equals(fqn))) {
                            debug("%s will be considered a Jackson type because we're honoring the %s annotation.", declaration, fqn);
                            explicitXMLTypeOrElement = true;
                        }
                    }

                    explicitXMLTypeOrElement = explicitXMLTypeOrElement || isJacksonSerializationAnnotation(fqn);
                }
            }

            if (explicitXMLTypeOrElement) {
                break;
            }
        }

        return explicitXMLTypeOrElement;
    }

    /**
     * Whether the specified declaration is throwable.
     *
     * @param declaration The declaration to determine whether it is throwable.
     * @return Whether the specified declaration is throwable.
     */
    protected boolean isThrowable(Element declaration) {
        return declaration.getKind() == ElementKind.CLASS && ((DecoratedTypeMirror) declaration.asType()).isInstanceOf(Throwable.class);
    }

    @Override
    public boolean typeDetected(Object type, MetadataAdapter metadata) {
        String classname = metadata.getClassName(type);
        this.jacksonDetected |= ObjectMapper.class.getName().equals(classname);
        this.jaxbSupportDetected |= "org.codehaus.jackson.xc.JaxbAnnotationIntrospector".equals(classname);

        if (classname.startsWith("org.codehaus.jackson")) {
            //don't accept jackson system specific types
            return false;
        }

        List<String> classAnnotations = metadata.getClassAnnotationNames(type);
        if (classAnnotations != null) {
            for (String fqn : classAnnotations) {
                if (isJacksonSerializationAnnotation(fqn)) {
                    return true;
                }
            }
        }
        return false;
    }

    boolean isJacksonSerializationAnnotation(String fqn) {
        return !JacksonAnnotation.class.getName().equals(fqn) && (JsonSerialize.class.getName().equals(fqn));
    }
}
