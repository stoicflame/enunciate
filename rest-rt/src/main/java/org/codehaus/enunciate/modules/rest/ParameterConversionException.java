package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.RESTError;

@RESTError(errorCode = 400)
public class ParameterConversionException extends RuntimeException {
	private static final long serialVersionUID = -8619182811461806508L;

	private String value;

	public ParameterConversionException(final String value) {
		super();
		this.value = value;
	}

	@Override
	public String getMessage() {
		return "Invalid parameter value '" + value + "' on URL.";
	}

	public String getValue() {
		return value;
	}

	public void setValue(final String value) {
		this.value = value;
	}

}
