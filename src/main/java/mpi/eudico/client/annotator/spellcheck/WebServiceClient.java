package mpi.eudico.client.annotator.spellcheck;

import org.apache.http.HttpHost;

public interface WebServiceClient {
	public void setHost(HttpHost host);
	
	public HttpHost getHost();

	public void setPath(String path);
	
	public String getPath();

	public void setUsername(String username);
	
	public String getUsername();

	public void setPassword(String password);

	public String getPassword();
	
	public void setSecure(Boolean ssl);
	
	public Boolean getSecure();
	
	enum AuthenticationProtocol {
		DIGEST,
		OAUTH
	}
	
	public void setAuthenticationProtocol(AuthenticationProtocol protocol);
	
	public AuthenticationProtocol getAuthenticationProtocol();
	
	public void setAuthentication(AuthenticationProtocol protocol, String username, String password);
}
