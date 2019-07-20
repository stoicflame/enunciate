/**
 * Copyright © 2006-2016 Web Cohesion (info@webcohesion.com)
 * <p>
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.webcohesion.enunciate;

import java.util.*;
import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.ProcessingEnvironment;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedOptions;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;

import com.webcohesion.enunciate.javac.decorations.AnnotationMirrorDecoration;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.DecoratedRoundEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecoration;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecoration;
import com.webcohesion.enunciate.module.ContextModifyingModule;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.jgrapht.Graph;
import org.jgrapht.graph.DefaultEdge;

import static com.webcohesion.enunciate.util.IgnoreUtils.isIgnored;

/**
 * @author Ryan Heaton
 */
@SupportedOptions({})
@SupportedAnnotationTypes("*")
public class EnunciateAnnotationProcessor extends AbstractProcessor {

  private final Enunciate enunciate;
  private final Set<String> includedTypes;
  private EnunciateContext context;
  protected boolean processed = false;

  public EnunciateAnnotationProcessor(Enunciate enunciate, Set<String> includedTypes) {
    this.enunciate = enunciate;
    this.includedTypes = includedTypes;
  }

  @Override
  public SourceVersion getSupportedSourceVersion() {
    return SourceVersion.latestSupported();
  }

  @Override
  public synchronized void init(ProcessingEnvironment processingEnv) {
    super.init(processingEnv);

    //set up the processing environment.
    ArrayList<ElementDecoration> elementDecorations = new ArrayList<ElementDecoration>();
    ArrayList<TypeMirrorDecoration> typeMirrorDecorations = new ArrayList<TypeMirrorDecoration>();
    ArrayList<AnnotationMirrorDecoration> annotationMirrorDecorations = new ArrayList<AnnotationMirrorDecoration>();
    DecoratedProcessingEnvironment processingEnvironment = new DecoratedProcessingEnvironment(processingEnv, elementDecorations, typeMirrorDecorations, annotationMirrorDecorations);

    //construct a context.
    this.context = new EnunciateContext(processingEnvironment, this.enunciate.getLogger(), this.enunciate.getApiRegistry(), this.enunciate.getConfiguration(), this.enunciate.getIncludePatterns(), this.enunciate.getExcludePatterns(), this.enunciate.getClasspath());

    //initialize the modules.
    for (EnunciateModule module : this.enunciate.getModules()) {
      module.init(this.context);

      if (module instanceof ContextModifyingModule) {
        ContextModifyingModule contextModifier = (ContextModifyingModule) module;
        elementDecorations.addAll(contextModifier.getElementDecorations());
        typeMirrorDecorations.addAll(contextModifier.getTypeMirrorDecorations());
        annotationMirrorDecorations.addAll(contextModifier.getAnnotationMirrorDecorations());
      }
    }
  }

  @Override
  public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
    if (!roundEnv.processingOver()) { // (heatonra) I still don't understand why this check is needed. But if I don't do the check, the processing happens twice.

      //find all the processing elements and set them on the context.
      Set<Element> apiElements = new HashSet<Element>();
      Set<Element> localApiElements = new HashSet<Element>();
      for (Element element : roundEnv.getRootElements()) {
        Element el = ElementDecorator.decorate(element, this.context.getProcessingEnvironment());
        apiElements.add(el);
        localApiElements.add(el);
      }
      Elements elementUtils = this.context.getProcessingEnvironment().getElementUtils();
      for (String includedType : this.includedTypes) {
        TypeElement typeElement = elementUtils.getTypeElement(includedType);
        if (typeElement != null) {
          apiElements.add(typeElement);
        } else {
          this.enunciate.getLogger().debug("Unable to load type element %s.", includedType);
        }
      }

      applyExcludeFilter(apiElements);
      applyIncludeFilter(apiElements);
      this.enunciate.getLogger().debug("API Elements: %s", new EnunciateLogger.ListWriter(apiElements));

      applyExcludeFilter(localApiElements);
      this.enunciate.getLogger().debug("Local API Elements: %s", new EnunciateLogger.ListWriter(localApiElements));

      this.context.setRoundEnvironment(new DecoratedRoundEnvironment(roundEnv, this.context.getProcessingEnvironment()));
      this.context.setLocalApiElements(localApiElements);
      this.context.setApiElements(apiElements);

      //compose the engine.
      Map<String, ? extends EnunciateModule> enabledModules = this.enunciate.findEnabledModules();
      this.enunciate.getLogger().info("Enabled modules: %s", enabledModules.keySet());
      Graph<String, DefaultEdge> graph = this.enunciate.buildModuleGraph(enabledModules);
      this.enunciate.invokeModules(this.context, enabledModules, graph);

      this.processed = true;
    }

    return false; //always return 'false' in case other annotation processors want to continue.
  }

  protected void applyExcludeFilter(Set<Element> apiElements) {
    Iterator<Element> elementIterator = apiElements.iterator();
    while (elementIterator.hasNext()) {
      Element next = elementIterator.next();
      try {
        if (this.context.isExcluded(next) || isIgnored(next)) {
          elementIterator.remove();
        }
      }
      catch (RuntimeException e) {
        if (e.getClass().getName().endsWith("CompletionFailure")) {
          throw new CompletionFailureException(Collections.singletonList(next), e);
        }

        throw e;
      }
    }
  }

  protected void applyIncludeFilter(Set<Element> apiElements) {
    if (this.context.hasExplicitIncludes()) {
      Iterator<Element> elementIterator = apiElements.iterator();
      while (elementIterator.hasNext()) {
        Element next = elementIterator.next();
        try {
          if (!this.context.isExplicitlyIncluded(next)) {
            elementIterator.remove();
          }
        }
        catch (RuntimeException e) {
          if (e.getClass().getName().endsWith("CompletionFailure")) {
            throw new CompletionFailureException(Collections.singletonList(next), e);
          }

          throw e;
        }
      }
    }
  }

}
