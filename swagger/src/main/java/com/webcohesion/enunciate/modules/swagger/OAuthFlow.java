package com.webcohesion.enunciate.modules.swagger;

import java.util.Map;

/**
 * @author FabianHalbmann
 */
public class OAuthFlow {

	private final String authorizationUrl;
	private final String tokenUrl;
	private final String refreshUrl;
	private final Map<String, String> flows;

	public OAuthFlow(String authorizationUrl, String tokenUrl, String refreshUrl, Map<String, String> flows) {
		this.authorizationUrl = authorizationUrl;
		this.tokenUrl = tokenUrl;
		this.refreshUrl = refreshUrl;
		this.flows = flows;
	}

	public String getAuthorizationUrl() {
		return authorizationUrl;
	}

	public String getTokenUrl() {
		return tokenUrl;
	}

	public String getRefreshUrl() {
		return refreshUrl;
	}

	public Map<String, String> getFlows() {
		return flows;
	}

}
