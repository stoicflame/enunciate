package com.webcohesion.enunciate.javac.decorations;

import com.sun.source.tree.CompilationUnitTree;
import com.sun.source.util.SourcePositions;
import com.sun.source.util.TreePath;
import com.sun.source.util.Trees;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;

import javax.annotation.processing.Filer;
import javax.annotation.processing.Messager;
import javax.annotation.processing.ProcessingEnvironment;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.util.Elements;
import javax.lang.model.util.Types;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author Ryan Heaton
 */
public class DecoratedProcessingEnvironment implements ProcessingEnvironment {

  private final ProcessingEnvironment delegate;
  private final Map<String, Object> properties = new ConcurrentHashMap<String, Object>();
  private final Trees trees;

  public DecoratedProcessingEnvironment(ProcessingEnvironment delegate) {
    while (delegate instanceof DecoratedProcessingEnvironment) {
      delegate = ((DecoratedProcessingEnvironment) delegate).delegate;
    }
    this.delegate = delegate;
    this.trees = Trees.instance(delegate);
  }

  @Override
  public Map<String, String> getOptions() {
    return delegate.getOptions();
  }

  @Override
  public Messager getMessager() {
    return delegate.getMessager();
  }

  @Override
  public Filer getFiler() {
    return delegate.getFiler();
  }

  @Override
  public Elements getElementUtils() {
    return new DecoratedElements(delegate.getElementUtils(), this);
  }

  @Override
  public Types getTypeUtils() {
    return new DecoratedTypes(delegate.getTypeUtils(), this);
  }

  @Override
  public SourceVersion getSourceVersion() {
    return delegate.getSourceVersion();
  }

  @Override
  public Locale getLocale() {
    return delegate.getLocale();
  }

  public Object getProperty(String property) {
    return this.properties.get(property);
  }

  public void setProperty(String property, Object value) {
    this.properties.put(property, value);
  }

  public SourcePosition findSourcePosition(Element element) {
    while (element instanceof DecoratedElement) {
      element = ((DecoratedElement) element).getDelegate();
    }

    TreePath path = this.trees.getPath(element);
    if (path != null) {
      CompilationUnitTree cu = path.getCompilationUnit();
      SourcePositions positions = this.trees.getSourcePositions();
      long position = positions.getStartPosition(cu, path.getLeaf());
      long line = cu.getLineMap().getLineNumber(position);
      long column = cu.getLineMap().getColumnNumber(position);
      return new SourcePosition(position, line, column);
    }
    else {
      return null;
    }
  }
}
