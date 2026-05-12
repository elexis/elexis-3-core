package ch.elexis.core.jaxrs.filter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.regex.Pattern;

import org.slf4j.LoggerFactory;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;

public abstract class AbstractCombinedOauthJwtContextSettingFilter implements Filter {

	private static final boolean IS_DISABLE_WEBSEC = Boolean.valueOf(System.getProperty("disable.web.security"));

	private ContextSettingFilter contextSettingFilter;

	private Pattern skipPattern;
	private Filter oAuthJwtFilter;

	@Override
	public void init(FilterConfig filterConfig) throws ServletException {

		if (IS_DISABLE_WEBSEC) {
			LoggerFactory.getLogger(getClass()).error("!!! UNPROTECTED API !!!");
		} else {
			initializeOAuthFilter(filterConfig);
			LoggerFactory.getLogger(getClass()).debug("Filter initialized");
		}

		contextSettingFilter = new ContextSettingFilter(IS_DISABLE_WEBSEC);

		String skipPatternDefinition = filterConfig.getInitParameter("skipPattern");
		if (skipPatternDefinition != null) {
			skipPattern = Pattern.compile(skipPatternDefinition, Pattern.DOTALL);
		}
	}

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		HttpServletRequest servletRequest = (HttpServletRequest) request;
		if (shouldSkip(servletRequest)) {
			// NO SECURITY, NO CONTEXT
			chain.doFilter(request, response);
			return;
		}

		if (!IS_DISABLE_WEBSEC) {
			oAuthJwtFilter.doFilter(servletRequest, response,
					(req, res) -> contextSettingFilter.doFilter(req, res, chain));
		} else {
			contextSettingFilter.doFilter(request, response, chain);
		}
	}

	private void initializeOAuthFilter(FilterConfig filterConfig) throws ServletException {
		oAuthJwtFilter = new io.curity.oauth.OAuthJwtFilter();
		oAuthJwtFilter.init(new EnvironmentVariablesExtendedFilterConfig(filterConfig));
	}

	/**
	 * @see org.keycloak.adapters.servlet.KeycloakOIDCFilter#shouldSkip
	 */
	private boolean shouldSkip(HttpServletRequest request) {

		if (skipPattern == null) {
			return false;
		}

		String requestPath = request.getRequestURI().substring(request.getContextPath().length());
		return skipPattern.matcher(requestPath).matches();
	}

	/**
	 * Consider environment variables as well as init parameters for FilterConfig
	 */
	private class EnvironmentVariablesExtendedFilterConfig implements FilterConfig {

		private final String ENV_PREFIX;

		private FilterConfig filterConfig;

		public EnvironmentVariablesExtendedFilterConfig(FilterConfig filterConfig) {
			this.filterConfig = filterConfig;
			String filterId = filterConfig.getInitParameter("filter-id");
			if (filterId != null) {
				ENV_PREFIX = "OAUTH_FILTER_" + filterId + "_";
			} else {
				ENV_PREFIX = "OAUTH_FILTER_";
			}
		}

		@Override
		public String getFilterName() {
			return filterConfig.getFilterName();
		}

		@Override
		public String getInitParameter(String name) {
			String initParameter = filterConfig.getInitParameter(name);
			if (initParameter == null) {
				initParameter = System.getenv(ENV_PREFIX + name);
			}
			return initParameter;
		}

		@Override
		public Enumeration<String> getInitParameterNames() {
			Enumeration<String> initParameterNames = filterConfig.getInitParameterNames();
			List<String> list = System.getenv().keySet().stream().filter(key -> key.startsWith(ENV_PREFIX))
					.map(key -> key.substring(ENV_PREFIX.length())).toList();

			ArrayList<String> lt = new ArrayList<>(list);
			initParameterNames.asIterator().forEachRemaining(lt::add);
			return Collections.enumeration(lt);
		}

		@Override
		public ServletContext getServletContext() {
			return filterConfig.getServletContext();
		}

	}

}
