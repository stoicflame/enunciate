package com.webcohesion.enunciate.modules.swagger;

/**
 * @author FabianHalbmann
 */
public class OAuthFlows {

	private final OAuthFlow implicit;
	private final OAuthFlow password;
	private final OAuthFlow clientCredentials;
	private final OAuthFlow authorizationCode;

	public OAuthFlows(OAuthFlow implicit, OAuthFlow password, OAuthFlow clientCredentials,
			OAuthFlow authorizationCode) {
		this.implicit = implicit;
		this.password = password;
		this.clientCredentials = clientCredentials;
		this.authorizationCode = authorizationCode;
	}

	public OAuthFlow getImplicit() {
		return implicit;
	}

	public OAuthFlow getPassword() {
		return password;
	}

	public OAuthFlow getClientCredentials() {
		return clientCredentials;
	}

	public OAuthFlow getAuthorizationCode() {
		return authorizationCode;
	}

}
