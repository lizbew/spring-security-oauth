package org.springframework.security.oauth2.client.token.grant.wx;

import org.springframework.security.oauth2.client.token.grant.redirect.AbstractRedirectResourceDetails;

/**
 * @author Ryan Heaton
 * @author Dave Syer
 */
public class AuthorizationCodeResourceWxDetails extends AbstractRedirectResourceDetails {
  public final static String WX_GRANT_TYPE = "authorization_code_wx";

  public AuthorizationCodeResourceWxDetails() {
    setGrantType(WX_GRANT_TYPE);
  }

}
