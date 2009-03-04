package org.codehaus.enunciate.modules.rest;

import org.codehaus.enunciate.rest.annotations.RESTError;

@RESTError(errorCode = 400)
public class KeyParameterConversionException extends ParameterConversionException {
	private static final long serialVersionUID = -6251687018013131573L;

	private final String key;

	public KeyParameterConversionException(final String key, final String value) {
		super(value);
		this.key = key;
	}

	public String getKey() {
		return key;
	}

	@Override
	public String getMessage() {
		return "Invalid value '" + getValue() + "' for parameter '" + getKey() + "'.";
	}
}
