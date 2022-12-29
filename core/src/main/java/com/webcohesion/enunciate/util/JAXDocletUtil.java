/*
 * © 2020 by Intellectual Reserve, Inc. All rights reserved.
 */
package com.webcohesion.enunciate.util;

import com.webcohesion.enunciate.EnunciateLogger;
import com.webcohesion.enunciate.javac.decorations.DecoratedProcessingEnvironment;
import com.webcohesion.enunciate.javac.decorations.TypeMirrorDecorator;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.metadata.rs.TypeHint;
import org.apache.commons.lang3.StringUtils;

import javax.lang.model.element.TypeElement;
import javax.lang.model.type.TypeKind;
import javax.lang.model.type.TypeMirror;

public class JAXDocletUtil {

  public static DecoratedTypeMirror getReturnWrapped(String comment, DecoratedProcessingEnvironment env, EnunciateLogger logger) {
    DecoratedTypeMirror returnType = null;
    JavaDoc localDoc = new JavaDoc(comment, null, null, env);
    if (localDoc.get("returnWrapped") != null) {
      String returnWrapped = localDoc.get("returnWrapped").get(0);
      String fqn = returnWrapped.substring(0, JavaDoc.indexOfFirstWhitespace(returnWrapped)).trim();

      boolean array = false;
      if (fqn.endsWith("[]")) {
        array = true;
        fqn = fqn.substring(0, fqn.length() - 2);
      }

      TypeMirror[] typeArgs = new TypeMirror[0];
      if (StringUtils.contains(fqn, '<') && StringUtils.contains(fqn, '>')) {
        int lt = StringUtils.indexOf(fqn, '<');
        int gt = StringUtils.lastIndexOf(fqn, '>');
        String baseFqn = StringUtils.substring(fqn, 0, lt).trim();
        String[] args = StringUtils.split(StringUtils.substring(fqn, lt + 1, gt), ',');
        typeArgs = new TypeMirror[args.length];
        for (int i = 0; i < args.length; i++) {
          String arg = args[i].trim();
          TypeElement el = env.getElementUtils().getTypeElement(arg);
          typeArgs[i] = TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(el), env);
        }
        fqn = baseFqn;
      }

      TypeElement type = env.getElementUtils().getTypeElement(fqn);
      if (type != null) {
        if (!array && isNoContentType(fqn)) {
          returnType = (DecoratedTypeMirror) env.getTypeUtils().getNoType(TypeKind.VOID);
        }
        else {
          returnType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(env.getTypeUtils().getDeclaredType(type, typeArgs), env);

          if (array) {
            returnType = (DecoratedTypeMirror) TypeMirrorDecorator.decorate(env.getTypeUtils().getArrayType(returnType), env);
          }
        }
      }
      else {
        logger.info("Invalid @returnWrapped type: \"%s\" (doesn't resolve to a type).", fqn);
      }
    }

    return returnType;
  }

  //the following name denotes 'no content' semantics:
  //- com.webcohesion.enunciate.metadata.rs.TypeHint.NO_CONTENT
  private static boolean isNoContentType(String fqn) {
    String noContentClassName = TypeHint.NO_CONTENT.class.getName();
    return fqn.equals(noContentClassName.replace('$', '.'));
  }
}
