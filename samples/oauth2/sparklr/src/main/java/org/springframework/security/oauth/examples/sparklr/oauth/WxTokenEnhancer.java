package org.springframework.security.oauth.examples.sparklr.oauth;

import java.util.LinkedHashMap;
import java.util.Map;

import org.springframework.security.oauth2.common.DefaultOAuth2AccessToken;
import org.springframework.security.oauth2.common.OAuth2AccessToken;
import org.springframework.security.oauth2.provider.OAuth2Authentication;
import org.springframework.security.oauth2.provider.token.TokenEnhancer;

public class WxTokenEnhancer implements TokenEnhancer {

  public OAuth2AccessToken enhance(OAuth2AccessToken accessToken, OAuth2Authentication authentication) {
    Map<String, Object> info = accessToken.getAdditionalInformation();
    if (info == null) {
      info = new LinkedHashMap<String, Object>();
    } else {
      info = new LinkedHashMap<String, Object>(info);
    }
    info.put("openid", "gh_spark");
    DefaultOAuth2AccessToken newToken = new DefaultOAuth2AccessToken(accessToken);
    newToken.setAdditionalInformation(info);
    return newToken;
  }

}
