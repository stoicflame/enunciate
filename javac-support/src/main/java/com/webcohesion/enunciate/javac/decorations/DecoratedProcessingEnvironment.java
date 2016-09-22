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
import java.util.List;
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
  private final List<ElementDecoration> elementDecorations;
  private final List<TypeMirrorDecoration> typeMirrorDecorations;
  private final List<AnnotationMirrorDecoration> annotationMirrorDecorations;

  public DecoratedProcessingEnvironment(ProcessingEnvironment delegate, List<ElementDecoration> elementDecorations, List<TypeMirrorDecoration> typeMirrorDecorations, List<AnnotationMirrorDecoration> annotationMirrorDecorations) {
    this.elementDecorations = elementDecorations;
    this.typeMirrorDecorations = typeMirrorDecorations;
    this.annotationMirrorDecorations = annotationMirrorDecorations;
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
      return new SourcePosition(path, cu.getSourceFile(), position, line, column);
    }
    else {
      return null;
    }
  }

  public List<ElementDecoration> getElementDecorations() {
    return elementDecorations;
  }

  public List<TypeMirrorDecoration> getTypeMirrorDecorations() {
    return typeMirrorDecorations;
  }

  public List<AnnotationMirrorDecoration> getAnnotationMirrorDecorations() {
    return annotationMirrorDecorations;
  }
}
