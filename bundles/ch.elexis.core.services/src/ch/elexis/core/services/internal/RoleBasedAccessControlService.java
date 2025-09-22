package ch.elexis.core.services.internal;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.net.http.HttpResponse.BodyHandlers;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.gson.Gson;

import ch.elexis.core.ac.ACEAccessBitMap;
import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.AccessControlList;
import ch.elexis.core.ac.AccessControlListUtil;
import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.ac.SystemCommandEvaluatableACE;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.eenv.AccessToken;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.core.utils.OsgiServiceUtil;

@Component
public class RoleBasedAccessControlService implements IAccessControlService {

	private Logger logger;

	private final boolean logDenials;

	@Reference
	Gson gson;

	@Reference
	HttpClient httpClient;

	@Reference
	IContextService contextService;

	private ThreadLocal<Boolean> privileged;

	private Map<Integer, AccessControlList> combinedRolesAclMap;
	private Map<String, AccessControlList> roleAclMap;

	private IUserService userService;

	private final String[] aoboObjects = { "IEncounter", "IInvoice" };

	public RoleBasedAccessControlService() {
		logger = LoggerFactory.getLogger(getClass());
		logDenials = ElexisSystemPropertyConstants.VERBOSE_ACL_NOTIFICATION;
		combinedRolesAclMap = Collections.synchronizedMap(new HashMap<>());
		roleAclMap = Collections.synchronizedMap(new HashMap<>());

		privileged = ThreadLocal.withInitial(() -> Boolean.FALSE);
	}

	@Override
	public boolean evaluate(EvaluatableACE evaluatableAce) {
		if (isPrivileged()) {
			return true;
		}

		String activeUserId = contextService.getActiveUser().map(IUser::getId).orElse(null);
		if (activeUserId != null) {
			int combinedRolesHashCode = contextService.getActiveUser().map(IUser::getRoleIds).get().hashCode();
			if (!combinedRolesAclMap.containsKey(combinedRolesHashCode)) {
				refresh(activeUserId, contextService.getActiveUser().map(IUser::getRoleIds).get());
			}

			boolean result = evaluateACE(combinedRolesAclMap.get(combinedRolesHashCode),
					evaluatableAce);
			if (logDenials && !result) {
				logger.info("User %s denied %s ", activeUserId, evaluatableAce.toString());
				logger.info("Combined Roles: %s", gson.toJson(combinedRolesAclMap.get(combinedRolesHashCode)));
			}

			return result;
		} else {
			logger.warn("No active user to evalute");
		}
		return false;
	}

	@Override
	public void refresh(IUser user) {
		refresh(user.getId(), user.getRoleIds());
	}

	/**
	 * 
	 * @param userId
	 * @param roles  ALL roles to be applied to user, we do not care about
	 *               usergroups - this set is imperative
	 */
	public void refresh(String userId, List<String> roles) {
		// calculate user ACL by combining the users roles

		AccessControlList userAccessControlList = determineUserAccessControlList(roles);
		combinedRolesAclMap.put(roles.hashCode(), userAccessControlList);
		if (userAccessControlList.getRolesRepresented().isEmpty()) {
			logger.warn("ACE User=[{}] Empty Role Set", userId);
		} else {
			logger.info("Refresh ACE User=[{}] Roles={}", userId, userAccessControlList.getRolesRepresented());
		}
	}

	@Override
	public boolean isPrivileged() {
		// privileged set, or in test mode
		return privileged.get() || (CoreUtil.isTestMode() && contextService.getNamed("testAccessControl").isEmpty());
	}

	private AccessControlList determineUserAccessControlList(List<String> roles) {
		if (roles.isEmpty()) {
			return new AccessControlList();
		}

		AccessControlList accessControlList = null;
		for (String roleId : roles) {
			if (accessControlList == null) {
				accessControlList = getOrLoadRoleAccessControlList(roleId);
			} else {
				AccessControlList _accessControlList = roleAclMap.get(roleId.toLowerCase());
				if (_accessControlList == null) {
					_accessControlList = getOrLoadRoleAccessControlList(roleId);
				}
				if (_accessControlList != null) {
					accessControlList = AccessControlListUtil.merge(accessControlList, _accessControlList);
				} else {
					logger.warn("Unknown role [" + roleId + "]");
				}
			}
		}

		if (accessControlList == null) {
			// we might have only roles that are not represented in Elexis
			// e.g. only fhir-r4-access
			accessControlList = new AccessControlList();
		}

		return accessControlList;
	}

	/**
	 * We use another approach than Elexis: We assume that no roles are required in
	 * the Elexis database. If however a role entry for the id exists AND it is not
	 * a system role, we only check for the acl override.
	 * 
	 * @param roleId
	 * @return
	 */
	public AccessControlList getOrLoadRoleAccessControlList(String roleId) {
		if (!roleAclMap.containsKey(roleId)) {

			if (ElexisSystemPropertyConstants.IS_EE_DEPENDENT_OPERATION_MODE) {
				loadRoleAccessControlListDependent(roleId);
			} else {
				doPrivileged(() -> {
					IRole role = CoreModelServiceHolder.get().load(roleId, IRole.class).orElseThrow();
					loadRoleAccessControlListLegacy(role);
				});
			}
		}
		return roleAclMap.get(roleId);
	}

	private void loadRoleAccessControlListDependent(String roleId) {
		String accessToken = contextService.getTyped(AccessToken.class).get().getToken();
		var request = HttpRequest
				.newBuilder(URI.create("https://" + ElexisSystemPropertyConstants.GET_EE_HOSTNAME
						+ "/api/v1/ops/elexis-rcp/acl/" + roleId))
				.header("Authorization", "Bearer " + accessToken).header("accept", "application/json").build();
		try {
			HttpResponse<String> response = httpClient.send(request, BodyHandlers.ofString());
			if (204 == response.statusCode()) {
				logger.info("No acl file for role [{}]", roleId);
				return;
			}
			AccessControlList acl = gson.fromJson(response.body(), AccessControlList.class);
			roleAclMap.put(roleId, acl);
		} catch (Exception e) {
			logger.error("Error loading role acl [{}]", roleId, e);
		}

	}

	private void loadRoleAccessControlListLegacy(IRole iRole) {
		String _role = iRole.getId().toLowerCase();
		if (!roleAclMap.containsKey(_role)) {
			InputStream jsonStream = null;
			if (iRole.isSystemRole()) {
				jsonStream = AccessControlList.class.getClassLoader()
						.getResourceAsStream("/rsc/acl/" + _role + ".json");
			} else {
				String jsonValue = (String) iRole.getExtInfo("json");
				if (StringUtils.isNotBlank(jsonValue)) {
					try {
						jsonStream = new ByteArrayInputStream(jsonValue.getBytes("UTF-8"));
					} catch (UnsupportedEncodingException e) {
						logger.error("Invalid role custom json acl [{}]", _role, e);
					}
				}
			}
			if (jsonStream != null) {
				Optional<AccessControlList> acl = readAccessControlList(jsonStream);
				if (acl.isPresent()) {
					roleAclMap.put(_role, acl.get());
				} else {
					logger.error("Error loading role acl [{}]", _role);
				}
			} else {
				logger.warn("No role acl [{}] file", _role);
			}
		}
	}

	@Override
	public Optional<AccessControlList> readAccessControlList(InputStream jsonStream) {
		try (InputStreamReader inputStreamReader = new InputStreamReader(jsonStream)) {
			AccessControlList acl = gson.fromJson(inputStreamReader, AccessControlList.class);
			return Optional.of(acl);
		} catch (Exception e) {
			logger.error("Error reading acl json", e);
		}
		return Optional.empty();
	}

	private boolean evaluateACE(AccessControlList acl, EvaluatableACE ace) {
		if (ace instanceof ObjectEvaluatableACE) {
			ObjectEvaluatableACE _ace = (ObjectEvaluatableACE) ace;
			ACEAccessBitMap useracebm = acl.getObject().get(_ace.getObject());
			if (useracebm != null) {
				byte[] aceBitMap = useracebm.getAccessRightMap();
				// e.g. { 2, 4, 2, 2, 0, 4, 0, 0 } aceBitMap
				// cud:aobo,rv:*
				byte[] requested = _ace.getRequestedRightMap();
				// e.g. { 0, 1, 0, 0, 0, 0, 0 } requested

				byte[] evaluated = new byte[Right.values().length];
				short flattenedbitmap = 0;

				for (int i = 0; i < evaluated.length; i++) {
					if (aceBitMap[i] == (byte) 4) {
						aceBitMap[i] = (byte) 1;
						flattenedbitmap |= 1 << i;
					} else if (aceBitMap[i] == (byte) 2 || aceBitMap[i] == (byte) 1) {
						if (StringUtils.isNotEmpty(_ace.getStoreToString()) && isAoboObject(_ace.getObject())) {
							List<String> aoboMandatorIds = getAoboMandatorIds();
							if (evaluateAobo(aoboMandatorIds, _ace)) {
								aceBitMap[i] = (byte) 1;
								flattenedbitmap |= 1 << i;
							} else {
								aceBitMap[i] = (byte) 0;
							}
						} else {
							aceBitMap[i] = (byte) 1;
							flattenedbitmap |= 1 << i;
						}
					}
					evaluated[i] = (byte) (aceBitMap[i] & requested[i]);
				}
				// e.g. { 0, 1, 0, 0, 0, 0, 0} firstRun

				boolean result = (flattenedbitmap & _ace.getRequested()) == _ace.getRequested();
				return result;
			}
		} else if (ace instanceof SystemCommandEvaluatableACE) {
			SystemCommandEvaluatableACE _ace = (SystemCommandEvaluatableACE) ace;
			return evaluateSystemCommandACE(acl, _ace);
		}
		return false;
	}

	private boolean evaluateAobo(List<String> aoboMandatorIds, ObjectEvaluatableACE _ace) {
		Optional<Identifiable> object = StoreToStringServiceHolder.get().loadFromString(_ace.getStoreToString());
		if (object.isPresent()) {
			String id = null;
			if (object.get() instanceof IEncounter) {
				if (((IEncounter) object.get()).getMandator() != null) {
					id = ((IEncounter) object.get()).getMandator().getId();
				}
			} else if (object.get() instanceof IInvoice) {
				if (((IInvoice) object.get()).getMandator() != null) {
					id = ((IInvoice) object.get()).getMandator().getId();
				}
			} else {
				logger.warn("Unknown aobo object [{}]", _ace.getStoreToString());
			}
			return id != null ? aoboMandatorIds.contains(id) : true;
		} else {
			logger.warn("Could not load aobo object [{}]", _ace.getStoreToString());
		}
		return false;
	}

	@Override
	public String getSelfMandatorId() {
		return contextService.getActiveUser().map(IUser::getAssociatedContactId).orElse("-1");
	}

	public List<String> getAoboMandatorIds() {
		if (userService == null) {
			userService = OsgiServiceUtil.getService(IUserService.class).get();
		}
		List<String> ret = new ArrayList<>();
		contextService.getActiveUser().ifPresent(user -> {
			ret.add(user.getAssociatedContactId());
			userService.getExecutiveDoctorsWorkingFor(user, true).stream().forEach(m -> ret.add(m.getId()));
		});
		return ret;
	}

	@Override
	public List<String> getAoboMandatorIdsForSqlIn() {
		List<String> ret = new ArrayList<>();
		ret.add("-1");
		ret.addAll(getAoboMandatorIds());
		return ret;
	}

	private boolean isAoboObject(String object) {
		return Arrays.asList(aoboObjects).stream().filter(aobo -> object.endsWith(aobo)).findFirst().isPresent();
	}

	private boolean evaluateSystemCommandACE(AccessControlList acl, SystemCommandEvaluatableACE _ace) {
		if (acl.getSystemCommand().containsKey(_ace.getSystemCommandId())) {
			ACEAccessBitMap aceAccessBitMap = acl.getSystemCommand().get(_ace.getSystemCommandId());
			return aceAccessBitMap.grants(Right.EXECUTE);
		}
		return false;
	}

	@Override
	public void doPrivileged(Runnable runnable) {
		try {
			privileged.set(Boolean.TRUE);
			logger.trace("Executing priviledged [" + runnable + "]");
			runnable.run();
		} finally {
			privileged.set(Boolean.FALSE);
		}
	}

	@Override
	public Optional<ACEAccessBitMapConstraint> isAoboOrSelf(ObjectEvaluatableACE evaluatableAce) {
		if (isPrivileged()) {
			return Optional.empty();
		}
		if (!isAoboObject(evaluatableAce.getObject())) {
			return Optional.empty();
		}

		int combinedRolesHashCode = contextService.getActiveUser().map(IUser::getRoleIds).get().hashCode();
		if (!combinedRolesAclMap.containsKey(combinedRolesHashCode)) {
			refresh(contextService.getActiveUser().map(IUser::getId).get(),
					contextService.getActiveUser().map(IUser::getRoleIds).get());
		}
		AccessControlList acl = combinedRolesAclMap.get(combinedRolesHashCode);

		ACEAccessBitMap useracebm = acl.getObject().get(evaluatableAce.getObject());
		if (useracebm != null) {
			byte[] aceBitMap = useracebm.getAccessRightMap();
			byte[] requested = evaluatableAce.getRequestedRightMap();
			for (int i = 0; i < requested.length; i++) {
				if (requested[i] == 1 && (aceBitMap[i] == 1)) {
					return Optional.of(ACEAccessBitMapConstraint.SELF);
				} else if (requested[i] == 1 && (aceBitMap[i] == 2)) {
					return Optional.of(ACEAccessBitMapConstraint.AOBO);
				}
			}
		}
		return Optional.empty();
	}
}
