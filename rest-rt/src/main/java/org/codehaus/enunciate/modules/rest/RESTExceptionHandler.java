package org.codehaus.enunciate.modules.rest;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.web.servlet.HandlerExceptionResolver;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.View;

public interface RESTExceptionHandler extends HandlerExceptionResolver, View {

	public final static String MODEL_EXCEPTION = "org.codehaus.enunciate.modules.rest.RESTResourceExceptionHandler#EXCEPTION";

	public abstract ModelAndView resolveException(HttpServletRequest request, HttpServletResponse response,
			Object handler, Exception exception);

	public abstract String getContentType();

	public abstract void render(Map model, HttpServletRequest request, HttpServletResponse response)
			throws Exception;

}