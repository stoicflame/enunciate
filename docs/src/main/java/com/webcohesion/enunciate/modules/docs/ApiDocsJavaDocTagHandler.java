package com.webcohesion.enunciate.modules.docs;

import com.webcohesion.enunciate.api.ApiRegistrationContext;
import com.webcohesion.enunciate.api.ApiRegistry;
import com.webcohesion.enunciate.api.datatype.DataType;
import com.webcohesion.enunciate.api.datatype.Property;
import com.webcohesion.enunciate.api.datatype.Syntax;
import com.webcohesion.enunciate.api.datatype.Value;
import com.webcohesion.enunciate.api.resources.Method;
import com.webcohesion.enunciate.api.resources.ResourceApi;
import com.webcohesion.enunciate.api.resources.ResourceGroup;
import com.webcohesion.enunciate.api.services.Operation;
import com.webcohesion.enunciate.api.services.Service;
import com.webcohesion.enunciate.api.services.ServiceApi;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedPackageElement;
import com.webcohesion.enunciate.javac.decorations.element.DecoratedTypeElement;
import com.webcohesion.enunciate.javac.javadoc.JavaDocLink;
import com.webcohesion.enunciate.javac.javadoc.JavaDocTagHandler;

import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

/**
 * @author Ryan Heaton
 */
public class ApiDocsJavaDocTagHandler implements JavaDocTagHandler {

  static final Pattern RAW_LINK_PATTERN = Pattern.compile("(?:^|[^>=\"'])(http.[^\"'<\\s]+)(?![^<>]*>|[^\"]*?<\\/a)");

  private final ApiRegistry registry;
  private final ApiRegistrationContext context;

  public ApiDocsJavaDocTagHandler(ApiRegistry registry, ApiRegistrationContext context) {
    this.registry = registry;
    this.context = context;
  }

  @Override
  public String getTypeId() {
    return "api-docs";
  }

  @Override
  public String onInlineTag(String tagName, String tagText, DecoratedElement context) {
    if ("link".equals(tagName)) {
      JavaDocLink link = JavaDocLink.parse(tagText);
      String classRef = link.getClassName();
      String subelementRef = link.getMemberName();
      String value = link.getLabel();

      //use the current context as the class ref.
      if ("".equals(classRef)) {
        DecoratedElement type = context;
        while (true) {
          if (type == null || type instanceof PackageElement) {
            break;
          }
          else if (type instanceof DecoratedTypeElement) {
            classRef = ((DecoratedTypeElement) type).getQualifiedName().toString();
            break;
          }
          type = (DecoratedElement) type.getEnclosingElement();
        }
      }

      List<String> possibleResolutions = new ArrayList<>();
      if (!"".equals(classRef)) {
        if (classRef.indexOf('.') < 0) {
          //if it's a local reference, it could either be a class in the same package or an inner class of the current type.
          DecoratedElement pckg = context;

          while (true) {
            if (pckg == null) {
              break;
            }
            else if (pckg instanceof TypeElement) {
              possibleResolutions.add(((TypeElement) pckg).getQualifiedName().toString() + "." + classRef);
            }
            else if (pckg instanceof DecoratedPackageElement) {
              possibleResolutions.add(((DecoratedPackageElement) pckg).getQualifiedName() + "." + classRef);
              break;
            }
            pckg = (DecoratedElement) pckg.getEnclosingElement();
          }
        }
        else {
          possibleResolutions.add(classRef);
        }
      }

      return resolveJavadocLink(possibleResolutions, tagText, subelementRef, value);
    }
    else if ("code".equals(tagName)) {
      return "<code>" + tagText + "</code>";
    }

    return tagText;
  }

  private String resolveJavadocLink(List<String> possibleResolutions, String tagText, String subelementRef, String value) {
    if (!possibleResolutions.isEmpty()) {
      //now find the reference
      Set<Syntax> syntaxes = this.registry.getSyntaxes(this.context);
      for (Syntax syntax : syntaxes) {
        for (String candidate : possibleResolutions) {
          List<DataType> dataTypes = syntax.findDataTypes(candidate);
          if (dataTypes != null && !dataTypes.isEmpty()) {
            DataType dataType = dataTypes.get(0);
            Value dataTypeValue = dataType.findValue(subelementRef);
            if (dataTypeValue != null) {
              return "<a href=\"" + dataType.getSlug() + ".html#" + dataTypeValue.getValue() + "\">"
                 + (value != null ? value : dataTypeValue.getValue())
                 + "</a>";
            }
            Property property = dataType.findProperty(subelementRef);
            if (property != null) {
              return "<a href=\"" + dataType.getSlug() + ".html#prop-" + property.getName() + "\">"
                 + (value != null ? value : property.getName())
                 + "</a>";
            }
            return "<a href=\"" + dataType.getSlug() + ".html\">"
               + (value != null ? value : (subelementRef.isEmpty() ? dataType.getLabel() : subelementRef))
               + "</a>";
          }
        }
      }

      List<ResourceApi> resourceApis = this.registry.getResourceApis(this.context);
      for (ResourceApi resourceApi : resourceApis) {
        for (String candidate : possibleResolutions) {
          Method method = resourceApi.findMethodFor(candidate, subelementRef);
          if (method != null) {
            if (value == null) {
              value = method.getLabel() + " " + method.getResource().getGroup().getLabel();
            }

            return "<a href=\"" + method.getResource().getGroup().getSlug() + ".html#" + method.getSlug() + "\">" + value + "</a>";
          }
          else {
            ResourceGroup resourceGroup = resourceApi.findResourceGroupFor(candidate);
            if (resourceGroup != null) {
              if (value == null) {
                value = resourceGroup.getLabel();
              }

              return "<a href=\"" + resourceGroup.getSlug() + ".html\">" + value + "</a>";
            }
          }
        }
      }

      List<ServiceApi> serviceApis = this.registry.getServiceApis(this.context);
      for (ServiceApi serviceApi : serviceApis) {
        for (String candidate : possibleResolutions) {
          Operation operation = serviceApi.findOperationFor(candidate, subelementRef);
          if (operation != null) {
            if (value == null) {
              value = operation.getName();
            }

            return "<a href=\"" + operation.getService().getSlug() + ".html#" + operation.getSlug() + "\">" + value + "</a>";
          }
          else {
            Service service = serviceApi.findServiceFor(candidate);
            if (service != null) {
              if (value == null) {
                value = service.getLabel();
              }

              return "<a href=\"" + service.getSlug() + ".html\">" + value + "</a>";
            }
          }
        }
      }
    }

    return value != null ? value : tagText.trim();
  }

  @Override
  public String onBlockTag(String tagName, String value, DecoratedElement context) {
    if ("see".equals(tagName)) {
      if (value.startsWith("\"")) {
        return value;
      }
      else if (value.startsWith("<")) {
        return value;
      }
      else if (value.startsWith("http")) {
        return "<a target=\"_blank\" href=\"" + value + "\">" + value + "</a>";
      }
      else {
        //process 'see' block tags as if they were 'link' inline tags.
        return onInlineTag("link", value, context);
      }
    }
    else {
      return RAW_LINK_PATTERN.matcher(value).replaceAll(" <a target=\"_blank\" href=\"$1\">$1</a>");
    }
  }
}
