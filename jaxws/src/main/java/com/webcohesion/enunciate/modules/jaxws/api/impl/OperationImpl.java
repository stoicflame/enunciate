package com.webcohesion.enunciate.modules.jaxws.api.impl;

import com.webcohesion.enunciate.api.Styles;
import com.webcohesion.enunciate.api.datatype.DataTypeReference;
import com.webcohesion.enunciate.api.services.Fault;
import com.webcohesion.enunciate.api.services.Operation;
import com.webcohesion.enunciate.api.services.Parameter;
import com.webcohesion.enunciate.javac.decorations.element.ElementUtils;
import com.webcohesion.enunciate.javac.decorations.type.DecoratedTypeMirror;
import com.webcohesion.enunciate.javac.javadoc.JavaDoc;
import com.webcohesion.enunciate.modules.jaxb.api.impl.DataTypeReferenceImpl;
import com.webcohesion.enunciate.modules.jaxws.model.*;

import javax.lang.model.element.AnnotationMirror;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * @author Ryan Heaton
 */
public class OperationImpl implements Operation {

  private final WebMethod webMethod;
  private final ServiceImpl service;

  public OperationImpl(WebMethod webMethod, ServiceImpl service) {
    this.webMethod = webMethod;
    this.service = service;
  }

  @Override
  public String getName() {
    return this.webMethod.getOperationName();
  }

  @Override
  public String getSlug() {
    return this.service.getSlug() + "_method_" + getName();
  }

  @Override
  public String getDescription() {
    return this.webMethod.getJavaDoc().toString();
  }

  @Override
  public String getDeprecated() {
    return ElementUtils.findDeprecationMessage(this.webMethod);
  }

  @Override
  public String getSince() {
    JavaDoc.JavaDocTagList tags = this.webMethod.getJavaDoc().get("since");
    return tags == null ? null : tags.toString();
  }

  @Override
  public String getVersion() {
    JavaDoc.JavaDocTagList tags = this.webMethod.getJavaDoc().get("version");
    return tags == null ? null : tags.toString();
  }

  @Override
  public DataTypeReference getReturnType() {
    if (this.webMethod.isOneWay() || ((DecoratedTypeMirror) this.webMethod.getReturnType()).isVoid()) {
      return null;
    }
    else {
      return new DataTypeReferenceImpl(this.webMethod.getWebResult().getXmlType(), false);
    }
  }

  @Override
  public List<? extends Parameter> getInputParameters() {
    List<Parameter> params = new ArrayList<Parameter>();
    for (WebParam param : this.webMethod.getWebParameters()) {
      if (param.isInput()) {
        params.add(new ParameterImpl(param));
      }
    }

    return params;
  }

  @Override
  public List<? extends Parameter> getOutputParameters() {
    List<Parameter> params = new ArrayList<Parameter>();
    for (WebParam param : this.webMethod.getWebParameters()) {
      if (param.isOutput()) {
        params.add(new ParameterImpl(param));
      }
    }

    return params;
  }

  @Override
  public String getReturnDescription() {
    DecoratedTypeMirror returnType = (DecoratedTypeMirror) this.webMethod.getReturnType();
    return returnType.getDocValue();
  }

  @Override
  public List<? extends Fault> getFaults() {
    List<Fault> faults = new ArrayList<Fault>();
    for (WebFault webFault : this.webMethod.getWebFaults()) {
      faults.add(new FaultImpl(webFault));
    }

    return faults;
  }

  @Override
  public Map<String, AnnotationMirror> getAnnotations() {
    return this.webMethod.getAnnotations();
  }

  @Override
  public JavaDoc getJavaDoc() {
    return this.webMethod.getJavaDoc();
  }

  @Override
  public Set<String> getStyles() {
    return Styles.gatherStyles(this.webMethod, this.webMethod.getContext().getContext().getConfiguration().getAnnotationStyles());
  }
}
