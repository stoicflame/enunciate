package net.sf.enunciate.test.integration;

import org.aopalliance.intercept.MethodInterceptor;
import org.aopalliance.intercept.MethodInvocation;
import org.codehaus.xfire.handler.HandlerSupport;
import org.codehaus.xfire.handler.AbstractHandler;
import org.codehaus.xfire.MessageContext;
import org.codehaus.xfire.exchange.InMessage;
import org.springframework.aop.support.DelegatingIntroductionInterceptor;

import java.util.List;
import java.util.Arrays;

/**
 * Advice for a source service.
 *
 * @author Ryan Heaton
 */
public class SourceServiceAdvice extends DelegatingIntroductionInterceptor implements MethodInterceptor, HandlerSupport {

  public Object invoke(MethodInvocation methodInvocation) throws Throwable {
    if (("addInfoSet".equals(methodInvocation.getMethod().getName())) && ("SPECIAL".equals(methodInvocation.getArguments()[0]))) {
      return "intercepted";
    }

    return methodInvocation.proceed();
  }

  public List getInHandlers() {
    return Arrays.asList(this);
  }

  public List getOutHandlers() {
    return null;
  }

  public List getFaultHandlers() {
    return null;
  }

  public void invoke(MessageContext context) throws Exception {
    InMessage inMessage = context.getInMessage();
    //todo: I don't know... do some stuff here to prove this code gets executed.
  }
}
