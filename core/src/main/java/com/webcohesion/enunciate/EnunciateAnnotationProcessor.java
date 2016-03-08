package com.webcohesion.enunciate;

import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.DecoratedRoundEnvironment;
import com.webcohesion.enunciate.javac.decorations.ElementDecorator;
import com.webcohesion.enunciate.module.EnunciateModule;
import org.jgrapht.DirectedGraph;
import org.jgrapht.graph.DefaultEdge;
import rx.Observable;

import javax.annotation.processing.*;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import javax.lang.model.util.Elements;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;

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

    //construct a context.
    this.context = new EnunciateContext(new DecoratedProcessingEnvironment(processingEnv), this.enunciate.getLogger(), this.enunciate.getApiRegistry(), this.enunciate.getConfiguration(), this.enunciate.getIncludePatterns(), this.enunciate.getExcludePatterns());

    //initialize the modules.
    for (EnunciateModule module : this.enunciate.getModules()) {
      module.init(this.context);
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
        }
        else {
          this.enunciate.getLogger().debug("Unable to load type element %s.", includedType);
        }
      }

      applyElementFilter(localApiElements);
      applyElementFilter(apiElements);

      this.context.setRoundEnvironment(new DecoratedRoundEnvironment(roundEnv, this.context.getProcessingEnvironment()));
      this.context.setLocalApiElements(localApiElements);
      this.context.setApiElements(apiElements);

      //compose the engine.
      Map<String, ? extends EnunciateModule> enabledModules = this.enunciate.findEnabledModules();
      DirectedGraph<String, DefaultEdge> graph = this.enunciate.buildModuleGraph(enabledModules);
      Observable<EnunciateContext> engine = this.enunciate.composeEngine(this.context, enabledModules, graph);

      //fire off (and block on) the engine.
      engine.toList().toBlocking().single();

      this.processed = true;
    }

    return false; //always return 'false' in case other annotation processors want to continue.
  }

  protected void applyElementFilter(Set<Element> apiElements) {
    Iterator<Element> elementIterator = apiElements.iterator();
    while (elementIterator.hasNext()) {
      Element next = elementIterator.next();
      if (this.context.isExcluded(next)) {
        elementIterator.remove();
      }
    }
  }

}
