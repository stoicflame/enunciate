/*
 * © 2019 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate;

import javax.lang.model.element.Element;
import javax.lang.model.element.TypeElement;
import java.util.Iterator;
import java.util.List;

public class CompletionFailureException extends RuntimeException {

  public CompletionFailureException(List<Element> stack, Throwable cause) {
    super(buildMessage(stack, cause), cause);
  }

  private static String buildMessage(List<Element> stack, Throwable cause) {
    if (stack.isEmpty()) {
      return "Javac completion failure at unknown location.";
    }

    StringBuilder message = new StringBuilder(cause.getMessage()).append('\n');
    Iterator<Element> stackIt = stack.iterator();
    while (stackIt.hasNext()) {
      Element element = stackIt.next();
      String name = element instanceof TypeElement ? ((TypeElement) element).getQualifiedName().toString() : element.getSimpleName().toString();
      message.append(String.format("    referenced via %s", name));
      if (stackIt.hasNext()) {
        message.append('\n');
      }
    }

    return message.toString();
  }
}
