package water.webserver.jetty8;

import org.eclipse.jetty.security.Authenticator;
import org.eclipse.jetty.security.ServerAuthException;
import org.eclipse.jetty.security.authentication.FormAuthenticator;
import org.eclipse.jetty.server.Authentication;

import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;

/**
 * Dynamically switches between Form-based authentication
 * and Basic Access authentication.
 * The decision is made based on user's "User-Agent". Browser clients will use Form based
 * authentication, all other clients will use basic auth.
 */
class Jetty8DelegatingAuthenticator implements Authenticator {

  private Authenticator _primaryAuth;
  private FormAuthenticator _formAuth;

  Jetty8DelegatingAuthenticator(Authenticator primaryAuth, FormAuthenticator formAuth) {
    _primaryAuth = primaryAuth;
    _formAuth = formAuth;
  }

  @Override
  public void setConfiguration(AuthConfiguration configuration) {
    _primaryAuth.setConfiguration(configuration);
    _formAuth.setConfiguration(configuration);
  }

  @Override
  public String getAuthMethod() {
    return "FORM_PREFERRED";
  }

  @Override
  public Authentication validateRequest(ServletRequest request, ServletResponse response,
                                        boolean mandatory) throws ServerAuthException {
    if (isBrowserAgent((HttpServletRequest) request))
      return _formAuth.validateRequest(request, response, mandatory);
    else
      return _primaryAuth.validateRequest(request, response, mandatory);
  }

  private static boolean isBrowserAgent(HttpServletRequest request) {
    String userAgent = request.getHeader("User-Agent");
    // Covers all modern browsers (Firefox, Chrome, IE, Edge & Opera)
    return (userAgent != null) &&
            (userAgent.startsWith("Mozilla/") || userAgent.startsWith("Opera/"));
  }

  @Override
  public boolean secureResponse(ServletRequest request, ServletResponse response,
                                boolean mandatory, Authentication.User validatedUser) {
    return true; // both BASIC and FORM return true
  }

}
