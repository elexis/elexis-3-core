package ch.elexis.core.jaxrs.filter;

import java.io.IOException;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.JsonArray;
import com.google.gson.JsonElement;

import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IPerson;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.model.builder.IContactBuilder;
import ch.elexis.core.model.builder.IUserBuilder;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.ITraceService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.time.TimeUtil;
import ch.elexis.core.types.Gender;
import ch.elexis.core.utils.OsgiServiceUtil;
import io.curity.oauth.AuthenticatedUser;
import io.curity.oauth.OAuthFilter;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

public class ContextSettingFilter implements Filter {

	private Logger logger;

	private final boolean IS_DISABLED_WEB_SECURITY;

	private IContextService contextService;
	private IModelService coreModelService;
	private IAccessControlService accessControlService;

	private LimitedLinkedHashMap<String, CacheEntry> verificationCache;

	private IUser disabledWebSecurityContextUser;

	public ContextSettingFilter(boolean disableWebSecurity) {
		this.IS_DISABLED_WEB_SECURITY = disableWebSecurity;
		logger = LoggerFactory.getLogger(getClass());
		verificationCache = new LimitedLinkedHashMap<>(100);
	}

	private record CacheEntry(IUser user, ch.elexis.core.eenv.AccessToken accessToken) {
		// represent verificationCache entry
	};

	@Override
	public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
			throws IOException, ServletException {

		if (contextService == null) {
			logger.debug("Initializing");
			initializeConsumedServices();
		}

		HttpServletResponse servletResponse = (HttpServletResponse) response;
		HttpServletRequest servletRequest = (HttpServletRequest) request;

		clearContext();

		// assert user and assignedContact are valid
		// adapt user roles to assigned roles
		AuthenticatedUser authenticatedUser = (AuthenticatedUser) servletRequest
				.getAttribute(OAuthFilter.PRINCIPAL_ATTRIBUTE_NAME);
		if (authenticatedUser != null) {
			String jti = authenticatedUser.getClaim("jti").getAsString();

			if (!verificationCache.containsKey(jti)) {
				// if exp is before now, we should not come here
				Long exp = authenticatedUser.getClaim("exp").getAsLong();
				String preferredUsername = authenticatedUser.getClaim("preferred_username").getAsString();
				String email = authenticatedUser.getClaim("email").getAsString();
				JsonElement realmAccess = authenticatedUser.getClaim("realm_access");
				JsonArray roles = realmAccess.getAsJsonObject().get("roles").getAsJsonArray();
				HashSet<String> rolesSet = new HashSet<>(roles.size());
				roles.forEach(e -> rolesSet.add(e.getAsString()));

				ch.elexis.core.eenv.AccessToken keycloakAccessToken = new ch.elexis.core.eenv.AccessToken(null,
						TimeUtil.toDate(exp), preferredUsername, null, null);

				accessControlService.doPrivileged(() -> {
					Optional<IUser> user = coreModelService.load(preferredUsername, IUser.class);
					if (user.isEmpty()) {
						JsonElement elexisContactId = authenticatedUser.getClaim("elexisContactId");
						if (elexisContactId != null) {
							IUser dynamicCreatedUser = performDynamicUserCreationIfApplicable(preferredUsername,
									rolesSet, elexisContactId.getAsString(), email);
							user = Optional.ofNullable(dynamicCreatedUser);
						}
					}

					user.ifPresent(u -> verificationCache.put(jti, new CacheEntry(u, keycloakAccessToken)));
				});

				CacheEntry cacheEntry = verificationCache.get(jti);
				if (cacheEntry == null || cacheEntry.user == null) {
					logger.warn("User [{}] not found in local database. Access denied.\"", preferredUsername);
					servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No matching local user");
					return;
				}

				IContact userContact = cacheEntry.user.getAssignedContact();
				if (userContact == null) {
					logger.warn("User [{}] has no assigned contact. Access denied.", preferredUsername);
					servletResponse.sendError(HttpServletResponse.SC_UNAUTHORIZED, "No assigned user contact");
					return;
				}

				assertRoles(accessControlService, cacheEntry.user, rolesSet);
			}

			CacheEntry cacheEntry = verificationCache.get(jti);
			contextService.setActiveUser(cacheEntry.user);
			contextService.setTyped(cacheEntry.accessToken);

		} else {
			if (IS_DISABLED_WEB_SECURITY) {
				activateDisabledWebSecurityUserContext();
			} else {
				throw new IllegalStateException("Web security enabled. No Authentication Info found.");
			}
		}

		// TODO set selected mandator via request header

		chain.doFilter(request, response);
	}

	private void clearContext() {
		// info.elexis.server.core.internal.service.ContextService is ThreadLocal
		contextService.setActiveCoverage(null);
		contextService.setActiveMandator(null);
		contextService.setActiveUser(null);
		contextService.setActivePatient(null);
		contextService.removeTyped(ch.elexis.core.eenv.AccessToken.class);
	}

	private void initializeConsumedServices() {
		coreModelService = OsgiServiceUtil
				.getService(IModelService.class, "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)").get();
		contextService = OsgiServiceUtil.getService(IContextService.class).get();
		// FIXME check if the context service is thread local??
		accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).get();
	}

	/**
	 * Creates (if required) and sets a special user to the context, that holds the
	 * maximum required rights, in order to "mimic" disabled web security.
	 */
	private synchronized void activateDisabledWebSecurityUserContext() {
		if (disabledWebSecurityContextUser == null) {
			accessControlService.doPrivileged(() -> {
				disabledWebSecurityContextUser = coreModelService.load("disabled-web-sec-user", IUser.class)
						.orElse(null);
				if (disabledWebSecurityContextUser == null) {
					IPerson webSecContact = new IContactBuilder.PersonBuilder(coreModelService, "disabled-web-sec-user",
							"delete-me", LocalDate.now(), Gender.MALE).buildAndSave();
					disabledWebSecurityContextUser = new IUserBuilder(coreModelService, "disabled-web-sec-user",
							webSecContact).build();
					coreModelService.load(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_USER, IRole.class)
							.ifPresent(disabledWebSecurityContextUser::addRole);
					coreModelService.save(disabledWebSecurityContextUser);
				}
			});
		}
		contextService.setActiveUser(disabledWebSecurityContextUser);
	}

	/**
	 * Dynamically creates user if applicable
	 * 
	 * @param email
	 */
	private IUser performDynamicUserCreationIfApplicable(String preferredUsername, Set<String> roles,
			String elexisContactId, String email) {
		boolean isElexisUser = roles.contains("bot") || roles.contains("user");
		if (!isElexisUser) {
			return null;
		}
		// if an elexisContactId is set, and it is valid - dynamically create user
		Optional<IContact> assignedContact = coreModelService.load(elexisContactId, IContact.class);
		if (!assignedContact.isPresent()) {
			logger.warn("[{}] Dynamic user create failed. Invalid or missing attribute elexisContactId [{}]",
					preferredUsername, elexisContactId);
			return null;
		}
		logger.info("[{}] Dynamic user/bot create with assigned contact [{}]", preferredUsername, elexisContactId);
		IUser _user = new IUserBuilder(coreModelService, preferredUsername, assignedContact.get()).buildAndSave();

		ITraceService traceService = OsgiServiceUtil.getService(ITraceService.class).orElse(null);
		if (traceService != null) {
			traceService.addTraceEntry(preferredUsername, contextService.getRootContext().getStationIdentifier(),
					" Dynamic user creation [" + email + "] via ContextSettingFilter");
			OsgiServiceUtil.ungetService(traceService);
		} else {
			logger.warn("TraceService not available. Could not trace dynamic user creation [" + email + "]");
		}

		return _user;
	}

	/**
	 * If Keycloak grants a user specific roles, we set this as new role total for
	 * the user
	 * 
	 * @param accessControlService
	 * @param user
	 * @param token
	 */
	private void assertRoles(IAccessControlService accessControlService, IUser user, Set<String> roles) {
		accessControlService.doPrivileged(() -> {
			Set<String> allAvailableRoles = coreModelService.getQuery(IRole.class).execute().stream()
					.map(r -> r.getId()).collect(Collectors.toSet());

			Set<String> targetUserRoleSet = new HashSet<String>(allAvailableRoles);
			targetUserRoleSet.retainAll(roles);
			Set<String> currentUserRoleSet = user.getRoles().stream().map(r -> r.getId()).collect(Collectors.toSet());

			if (!Objects.equals(currentUserRoleSet, targetUserRoleSet)) {
				IUserService userService = OsgiServiceUtil.getService(IUserService.class).get();
				Set<String> effectiveUserRoles = userService.setUserRoles(user, targetUserRoleSet);
				accessControlService.refresh(user);
				logger.warn("[{}] Updated user/bot role set to {}", user, effectiveUserRoles);
			}
		});
	}

	@Override
	public void destroy() {
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
	}

	private class LimitedLinkedHashMap<K, V> extends LinkedHashMap<K, V> {

		private static final long serialVersionUID = -4811170640063577667L;
		private final int maxSize;

		public LimitedLinkedHashMap(int maxSize) {
			super(16, 0.75f, false);
			this.maxSize = maxSize;
		}

		@Override
		protected boolean removeEldestEntry(java.util.Map.Entry<K, V> eldest) {
			return size() > maxSize;
		}
	}

}
