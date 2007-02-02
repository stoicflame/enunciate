package net.sf.enunciate.modules.xfire;

import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.core.Ordered;

/**
 * Any bean found in the context implementing this interface will be added (in order) as
 * an interceptor to the {@link net.sf.enunciate.modules.xfire.EnunciateHandlerMapping}.
 * 
 * @author Ryan Heaton
 */
public interface EnunciateHandlerInterceptor extends HandlerInterceptor, Ordered {
}
