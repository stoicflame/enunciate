package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.services.Operation;
import com.webcohesion.enunciate.api.services.Service;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedPackageElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

import javax.lang.model.element.PackageElement;
import java.util.List;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class ApiDocsJavaDocTagHandler implements JavaDocTagHandler {

  private final ApiRegistry registry;
  private final ApiRegistrationContext context;

  public ApiDocsJavaDocTagHandler(ApiRegistry registry, ApiRegistrationContext context) {
    this.registry = registry;
    this.context = context;
  }

  @Override
  public String onInlineTag(String tagName, String tagText, DecoratedElement context) {
    if ("link".equals(tagName)) {
      tagText = tagText.trim();

      int fragmentStart = tagText.indexOf('#'); //the start index of where we need to start looking for the value.
      int fragmentEnd = fragmentStart;
      int firstParen = -1;
      if (fragmentStart >= 0) {
        //if there's a '#' char, we have to check for a left-right paren pair before checking for the space.
        firstParen = tagText.indexOf('(', fragmentStart);
        if (firstParen >= 0) {
          fragmentEnd = tagText.indexOf(')', firstParen);
        }
      }

      String value = tagText;
      int valueStart = tagText.indexOf(' ', fragmentStart < 0 ? 0 : fragmentEnd);
      if (valueStart >= 0 && valueStart + 1 < tagText.length()) {
        value = tagText.substring(valueStart + 1, tagText.length());
      }

      String classRef = "";
      String subelementRef = "";
      if (fragmentStart > 0) {
        classRef = tagText.substring(0, fragmentStart).trim();

        if (firstParen >= 0) {
          subelementRef = tagText.substring(fragmentStart + 1, firstParen - fragmentStart).trim();
        }
        else if (valueStart >= 0) {
          subelementRef = tagText.substring(fragmentStart + 1, valueStart - fragmentStart).trim();
        }
      }
      else if (valueStart > 0){
        classRef = tagText.substring(0, valueStart).trim();
      }

      //use the current context as the class ref.
      if ("".equals(classRef)){
        DecoratedElement type = context;
        while (!(type instanceof DecoratedTypeElement)) {
          type = (DecoratedElement) type.getEnclosingElement();

          if (type == null || type instanceof PackageElement) {
            break;
          }
        }

        if (type instanceof DecoratedTypeElement) {
          classRef = ((DecoratedTypeElement) type).getQualifiedName().toString();
        }
      }

      if (!"".equals(classRef)) {
        if (classRef.indexOf('.') < 0) {
          //if it's a local reference, assume it's in the current package.
          DecoratedElement pckg = context;

          while (!(pckg instanceof DecoratedPackageElement)) {
            pckg = (DecoratedElement) pckg.getEnclosingElement();
            if (pckg == null) {
              break;
            }
          }

          if (pckg != null) {
            classRef = ((DecoratedPackageElement) pckg).getQualifiedName() + "." + classRef;
          }
        }

        //now find the reference
        Set<Syntax> syntaxes = this.registry.getSyntaxes(this.context);
        for (Syntax syntax : syntaxes) {
          List<DataType> dataTypes = syntax.findDataTypes(classRef);
          if (dataTypes != null && !dataTypes.isEmpty()) {
            if (value.equals(tagText)) {
              value = dataTypes.get(0).getLabel();
            }

            return "<a href=\"" + dataTypes.get(0).getSlug() + ".html\">" + value + "</a>";
          }
        }

        List<ResourceApi> resourceApis = this.registry.getResourceApis(this.context);
        for (ResourceApi resourceApi : resourceApis) {
          Method method = resourceApi.findMethodFor(classRef, subelementRef);
          if (method != null) {
            if (value.equals(tagText)) {
              value = method.getLabel() + " " + method.getResource().getGroup().getLabel();
            }

            return "<a href=\"" + method.getResource().getGroup().getSlug() + ".html#" + method.getSlug() + "\">" + value + "</a>";
          }
          else {
            ResourceGroup resourceGroup = resourceApi.findResourceGroupFor(classRef);
            if (resourceGroup != null) {
              if (value.equals(tagText)) {
                value = resourceGroup.getLabel();
              }

              return "<a href=\"" + resourceGroup.getSlug() + ".html\">" + value + "</a>";
            }
          }
        }

        List<ServiceApi> serviceApis = this.registry.getServiceApis(this.context);
        for (ServiceApi serviceApi : serviceApis) {
          Operation operation = serviceApi.findOperationFor(classRef, subelementRef);
          if (operation != null) {
            if (value.equals(tagText)) {
              value = operation.getName();
            }

            return "<a href=\"" + operation.getService().getSlug() + ".html#" + operation.getSlug() + "\">" + value + "</a>";
          }
          else {
            Service service = serviceApi.findServiceFor(classRef);
            if (service != null) {
              if (value.equals(tagText)) {
                value = service.getLabel();
              }

              return "<a href=\"" + service.getSlug() + ".html\">" + value + "</a>";
            }
          }
        }
      }

      return value;
    }

    return tagText;
  }

}
