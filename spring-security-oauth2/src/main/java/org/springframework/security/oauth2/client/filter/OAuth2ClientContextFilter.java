package org.springframework.security.oauth2.client.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.springframework.beans.factory.InitializingBean;
import org.springframework.security.oauth2.client.resource.UserRedirectRequiredException;
import org.springframework.security.oauth2.common.DefaultThrowableAnalyzer;
import org.springframework.security.web.DefaultRedirectStrategy;
import org.springframework.security.web.RedirectStrategy;
import org.springframework.security.web.util.ThrowableAnalyzer;
import org.springframework.util.Assert;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;
import org.springframework.web.util.NestedServletException;
import org.springframework.web.util.UriComponents;

/**
 * Security filter for an OAuth2 client.
 * 
 * @author Ryan Heaton
 * @author Dave Syer
 */
public class OAuth2ClientContextFilter implements Filter, InitializingBean {

	/**
	 * Key in request attributes for the current URI in case it is needed by rest client code that needs to send a
	 * redirect URI to an authorization server.
	 */
	public static final String CURRENT_URI = "currentUri";

	private ThrowableAnalyzer throwableAnalyzer = new DefaultThrowableAnalyzer();

	private RedirectStrategy redirectStrategy = new DefaultRedirectStrategy();

	public void afterPropertiesSet() throws Exception {
		Assert.notNull(redirectStrategy, "A redirect strategy must be supplied.");
	}

	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {
		HttpServletRequest request = (HttpServletRequest) servletRequest;
		HttpServletResponse response = (HttpServletResponse) servletResponse;
		request.setAttribute(CURRENT_URI, calculateCurrentUri(request));

		try {
			chain.doFilter(servletRequest, servletResponse);
		}
		catch (IOException ex) {
			throw ex;
		}
		catch (Exception ex) {
			// Try to extract a SpringSecurityException from the stacktrace
			Throwable[] causeChain = throwableAnalyzer.determineCauseChain(ex);
			UserRedirectRequiredException redirect = (UserRedirectRequiredException) throwableAnalyzer
					.getFirstThrowableOfType(UserRedirectRequiredException.class, causeChain);
			if (redirect != null) {
				redirectUser(redirect, request, response);
			}
			else {
				if (ex instanceof ServletException) {
					throw (ServletException) ex;
				}
				if (ex instanceof RuntimeException) {
					throw (RuntimeException) ex;
				}
				throw new NestedServletException("Unhandled exception", ex);
			}
		}
	}

	/**
	 * Redirect the user according to the specified exception.
	 * 
	 * @param resourceThatNeedsAuthorization
	 * @param e The user redirect exception.
	 * @param request The request.
	 * @param response The response.
	 */
	protected void redirectUser(UserRedirectRequiredException e, HttpServletRequest request,
			HttpServletResponse response) throws IOException {

		String redirectUri = e.getRedirectUri();
		//vika - enhance it to support fragment
		String pathQueryUri = redirectUri;
		String fragment = "";
		int fragInx = redirectUri.indexOf('#');
		if (fragInx > 0) {
		  pathQueryUri = redirectUri.substring(0, fragInx);
		  fragment = redirectUri.substring(fragInx);
		}
		
		
		StringBuilder builder = new StringBuilder(pathQueryUri);
		Map<String, String> requestParams = e.getRequestParams();
		char appendChar = redirectUri.indexOf('?') < 0 ? '?' : '&';
		for (Map.Entry<String, String> param : requestParams.entrySet()) {
			try {
				builder.append(appendChar).append(param.getKey()).append('=')
						.append(URLEncoder.encode(param.getValue(), "UTF-8"));
			}
			catch (UnsupportedEncodingException uee) {
				throw new IllegalStateException(uee);
			}
			appendChar = '&';
		}

		if (e.getStateKey() != null) {
			builder.append(appendChar).append("state").append('=').append(e.getStateKey());
		}
		builder.append(fragment);
		this.redirectStrategy.sendRedirect(request, response, builder.toString());

	}

	/**
	 * Calculate the current URI given the request.
	 * 
	 * @param request The request.
	 * @return The current uri.
	 */
	protected String calculateCurrentUri(HttpServletRequest request) throws UnsupportedEncodingException {
		ServletUriComponentsBuilder builder = ServletUriComponentsBuilder.fromRequest(request);
		// Now work around SPR-10172...
		String queryString = request.getQueryString();
		boolean legalSpaces = queryString != null && queryString.contains("+");
		if (legalSpaces) {
			builder.replaceQuery(queryString.replace("+", "%20"));
		}
		UriComponents uri = null;
        try {
		    uri = builder.replaceQueryParam("code").build(true);
        } catch (IllegalArgumentException ex) {
            // ignore failures to parse the url (including query string). does't make sense
            // for redirection purposes anyway.
            return null;
        }
		String query = uri.getQuery();
		if (legalSpaces) {
			query = query.replace("%20", "+");
		}
		return ServletUriComponentsBuilder.fromUri(uri.toUri()).replaceQuery(query).build().toString();
	}

	public void init(FilterConfig filterConfig) throws ServletException {
	}

	public void destroy() {
	}

	public void setThrowableAnalyzer(ThrowableAnalyzer throwableAnalyzer) {
		this.throwableAnalyzer = throwableAnalyzer;
	}

	public void setRedirectStrategy(RedirectStrategy redirectStrategy) {
		this.redirectStrategy = redirectStrategy;
	}

}
