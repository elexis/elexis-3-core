package ch.elexis.core.services.internal;

import java.io.InputStream;
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

import com.fasterxml.jackson.core.JsonParser.Feature;
import com.fasterxml.jackson.databind.ObjectMapper;

import ch.elexis.core.ac.ACEAccessBitMap;
import ch.elexis.core.ac.AccessControlList;
import ch.elexis.core.ac.AccessControlListUtil;
import ch.elexis.core.ac.EvaluatableACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.ac.SystemCommandEvaluatableACE;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.IStoreToStringService;
import ch.elexis.core.services.IUserService;
import ch.elexis.core.utils.CoreUtil;

@Component
public class RoleBasedAccessControlService implements IAccessControlService {

	private Logger logger;

	@Reference
	private IContextService contextService;

	@Reference
	private IUserService userService;

	@Reference
	private IStoreToStringService storeToStringService;

	private Map<String, AccessControlList> roleAclMap;
	private Map<IUser, AccessControlList> userAclMap;

	private ThreadLocal<Boolean> privileged;

	private String[] aoboObjects = { "IEncounter", "IDocumentLetter", "IInvoice" };

	public RoleBasedAccessControlService() {
		logger = LoggerFactory.getLogger(getClass());
		roleAclMap = Collections.synchronizedMap(new HashMap<>());
		userAclMap = Collections.synchronizedMap(new HashMap<>());

		privileged = ThreadLocal.withInitial(() -> Boolean.FALSE);
	}

	@Override
	public boolean evaluate(EvaluatableACE evaluatableAce) {
		if (isPrivileged()) {
			return true;
		}
		Optional<IUser> user = contextService.getActiveUser();
		if (user.isPresent()) {
			if (!userAclMap.containsKey(user.get())) {
				refresh(user.get());
			}
			boolean result = evaluateACE(user.get(), userAclMap.get(user.get()), evaluatableAce);
			if (ElexisSystemPropertyConstants.VERBOSE_ACL_NOTIFICATION && !result) {
				String message = "(ACL " + System.currentTimeMillis() + ") User has no right ["
						+ evaluatableAce.toString() + "]";
				logger.info("", new Throwable(message));
			}
			return result;
		} else {
			logger.warn("No active user to evalute");
		}
		return false;
	}

	@Override
	public void refresh(IUser user) {
		// calculate user ACL by combining the users roles
		AccessControlList userAccessControlList = determineUserAccessControlList(userService.getUserRoles(user));
		userAclMap.put(user, userAccessControlList);
		logger.info("ACE User=[{}] Roles=[{}]", user.getId(),
				userAclMap.get(user) != null ? userAclMap.get(user).getRolesRepresented() : "");
	}

	@Override
	public boolean isPrivileged() {
		// privileged set, or in test mode
		return privileged.get() || (CoreUtil.isTestMode() && contextService.getNamed("testAccessControl").isEmpty());
	}

	private AccessControlList determineUserAccessControlList(List<IRole> roles) {
		if (roles.isEmpty()) {
			return null;
		}

		AccessControlList accessControlList = null;
		for (IRole role : roles) {
			if (accessControlList == null) {
				accessControlList = getOrLoadRoleAccessControlList(role);
			} else {
				AccessControlList _accessControlList = roleAclMap.get(role.getId().toLowerCase());
				if (_accessControlList == null) {
					_accessControlList = getOrLoadRoleAccessControlList(role);
				}
				if (_accessControlList != null) {
					accessControlList = AccessControlListUtil.merge(accessControlList, _accessControlList);
				} else {
					logger.warn("Unknown role [" + role.getId() + "]");
				}
			}
		}
		return accessControlList;
	}

	private AccessControlList getOrLoadRoleAccessControlList(IRole iRole) {
		String _role = iRole.getId().toLowerCase();
		if (!roleAclMap.containsKey(_role)) {
			InputStream roleAccessDefaultUserFile = AccessControlList.class.getClassLoader()
					.getResourceAsStream("/rsc/acl/" + _role + ".json");
			if (roleAccessDefaultUserFile != null) {
				try {
					AccessControlList acl = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true)
							.readValue(roleAccessDefaultUserFile, AccessControlList.class);
					roleAclMap.put(_role, acl);
				} catch (Exception e) {
					logger.error("Error loading role acl [{}]", _role, e);
				}
			} else {
				logger.warn("No role acl [{}] file", _role);
			}
		}
		return roleAclMap.get(_role);
	}

	private boolean evaluateACE(IUser user, AccessControlList acl, EvaluatableACE ace) {
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
					} else if (aceBitMap[i] == (byte) 2) {
						if (StringUtils.isNotEmpty(_ace.getStoreToString()) && isAoboObject(_ace.getObject())) {
							if (evaluateAobo(user, _ace)) {
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

	private boolean evaluateAobo(IUser user, ObjectEvaluatableACE _ace) {
		List<String> aoboIds = getAoboMandatorIds(user);
		Optional<Identifiable> object = storeToStringService.loadFromString(_ace.getStoreToString());
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
			} else if (object.get() instanceof IDocumentLetter) {
				if (((IDocumentLetter) object.get()).getAuthor() != null) {
					id = ((IDocumentLetter) object.get()).getAuthor().getId();
				}
			} else {
				logger.warn("Unknown aobo object [{}]", _ace.getStoreToString());
			}
			return id != null ? aoboIds.contains(id) : true;
		} else {
			logger.warn("Could not load aobo object [{}]", _ace.getStoreToString());
		}
		return false;
	}

	private List<String> getAoboMandatorIds(IUser user) {
		List<String> ret = new ArrayList<>();
		if (user.getAssignedContact() != null) {
			if (user.getAssignedContact().isMandator()) {
				ret.add(user.getAssignedContact().getId());
			}
			userService.getExecutiveDoctorsWorkingFor(user).stream().forEach(m -> ret.add(m.getId()));
		}
		return ret;
	}

	@Override
	public List<String> getAoboMandatorIds() {
		Optional<IUser> user = contextService.getActiveUser();
		if (user.isPresent()) {
			return getAoboMandatorIds(user.get());
		}
		return Collections.emptyList();
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
			logger.info("Executing priviledged [" + runnable + "]");
			runnable.run();
		} finally {
			privileged.set(Boolean.FALSE);
		}
	}

	@Override
	public boolean isAobo(ObjectEvaluatableACE evaluatableAce) {
		if (isPrivileged()) {
			return false;
		}
		if (!isAoboObject(evaluatableAce.getObject())) {
			return false;
		}
		Optional<IUser> user = contextService.getActiveUser();
		if (user.isPresent()) {
			if (!userAclMap.containsKey(user.get())) {
				refresh(user.get());
			}
			AccessControlList acl = userAclMap.get(user.get());
			ACEAccessBitMap useracebm = acl.getObject().get(evaluatableAce.getObject());
			if (useracebm != null) {
				byte[] aceBitMap = useracebm.getAccessRightMap();
				byte[] requested = evaluatableAce.getRequestedRightMap();
				for (int i = 0; i < requested.length; i++) {
					if (requested[i] == 1 && aceBitMap[i] != 2) {
						return false;
					}
				}
			}
		} else {
			logger.warn("No active user to test aobo");
		}
		return true;
	}
}
