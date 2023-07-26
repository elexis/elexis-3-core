package ch.elexis.core.services.internal;

import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

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
import ch.elexis.core.model.IRight;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IXid;
import ch.elexis.core.model.RoleConstants;
import ch.elexis.core.services.IAccessControlService;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.utils.CoreUtil;

@Component
public class RoleBasedAccessControlService implements IAccessControlService {

	private Logger logger;

	@Reference
	private IContextService contextService;

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
			return evaluateACE(user.get(), userAclMap.get(user.get()), evaluatableAce);
		} else {
			logger.warn("No active user to evalute");
		}
		return false;
	}

	@Override
	public void refresh(IUser user) {
		// calculate user ACL by combining the users roles
		userAclMap.put(user, determineUserAccessControlList(getUserRoles(user)));
		logger.debug("ACE User=[{}] Roles=[{}]", user.getId(), userAclMap.get(user).getRolesRepresented());
	}

	private List<IRole> getUserRoles(IUser user) {
		List<IRole> roles = user.getRoles();
		List<IRole> ret = roles.stream().map(r -> (IRole) new AccessControlRole(r)).collect(Collectors.toList());
		Optional<IRole> practitioner = ret.stream()
				.filter(r -> r.getId().equals(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_PRACTITIONER)).findFirst();
		Optional<IRole> assistant = ret.stream()
				.filter(r -> r.getId().equals(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_ASSITANT)).findFirst();
		if (practitioner.isPresent() && !assistant.isPresent()) {
			ret.add(new AccessControlRole(RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_ASSITANT));
		}
		return ret;
	}

	private boolean isPrivileged() {
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
		// TODO cache result

		if (ace instanceof ObjectEvaluatableACE) {
			ObjectEvaluatableACE _ace = (ObjectEvaluatableACE) ace;
			ACEAccessBitMap useracebm = acl.getObject().get(_ace.getObject());
			if (useracebm != null) {
				byte[] aceBitMap = useracebm.getAccessRightMap();
				// e.g. { 2, 4, 2, 2, 0, 4, 0, 0 } aceBitMap
				// cud:aobo,rv:*

				System.out.println("aceBitMap " + Arrays.toString(aceBitMap));

				byte[] requested = _ace.getRequestedRightMap();
				// e.g. { 0, 1, 0, 0, 0, 0, 0 } requested

				byte[] evaluated = new byte[Right.values().length];

				// if the requested rights can be satisfied with a bitwise AND,
				// we have a GO!

				for (int i = 0; i < evaluated.length; i++) {
					if (aceBitMap[i] == (byte) 4) {
						aceBitMap[i] = (byte) 1;
					} else if (aceBitMap[i] == (byte) 2) {
						if (StringUtils.isNotEmpty(_ace.getObjectId()) && isAoboObject(_ace.getObject())) {
							List<String> aoboIds = getAoboMandatorIds(user);
							// TODO test if object has an aobo mandator d
							aceBitMap[i] = (byte) 0;
						} else {
							aceBitMap[i] = (byte) 1;
						}
					}
					evaluated[i] = (byte) (aceBitMap[i] & requested[i]);
				}
				// e.g. { 0, 1, 0, 0, 0, 0, 0} firstRun

				System.out.println("flattenedAceBitMap " + Arrays.toString(aceBitMap) + " &  req "
						+ Arrays.toString(requested) + "  = eval " + Arrays.toString(evaluated));

				boolean result = Arrays.equals(requested, evaluated);
				if (result) {
					// TODO add to cache - ?? add also negative result to cache??
				}
				return result;
			}
		} else if (ace instanceof SystemCommandEvaluatableACE) {
			SystemCommandEvaluatableACE _ace = (SystemCommandEvaluatableACE) ace;
			return evaluateSystemCommandACE(acl, _ace);
		}

		// LOG

		return false;
	}

	private List<String> getAoboMandatorIds(IUser user) {
		List<String> ret = new ArrayList<>();
		if(user.getAssignedContact() != null && user.getAssignedContact().isMandator()) {
			ret.add(user.getAssignedContact().getId());
		}
		
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

	private class AccessControlRole implements IRole {

		private IRole role;

		private String roleId;

		public AccessControlRole(IRole role) {
			this.role = role;
		}

		public AccessControlRole(String roleId) {
			this.roleId = roleId;
		}

		@Override
		public String getId() {
			if (role != null) {
				switch (role.getId()) {
				case RoleConstants.SYSTEMROLE_LITERAL_EXECUTIVE_DOCTOR:
					return RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_PRACTITIONER;
				case RoleConstants.SYSTEMROLE_LITERAL_ASSISTANT:
					return RoleConstants.ACCESSCONTROLE_ROLE_MEDICAL_ASSITANT;
				}
				return role.getId();
			}
			return roleId;
		}

		@Override
		public String getLabel() {
			return getId();
		}

		@Override
		public boolean addXid(String domain, String id, boolean updateIfExists) {
			return false;
		}

		@Override
		public IXid getXid(String domain) {
			return null;
		}

		@Override
		public Long getLastupdate() {
			if (role != null) {
				return role.getLastupdate();
			}
			return 0L;
		}

		@Override
		public void setId(String id) {
		}

		@Override
		public boolean isSystemRole() {
			if (role != null) {
				return role.isSystemRole();
			}
			return true;
		}

		@Override
		public void setSystemRole(boolean value) {
		}

		@Override
		public List<IRight> getAssignedRights() {
			if (role != null) {
				return role.getAssignedRights();
			}
			return Collections.emptyList();
		}
	}
}
