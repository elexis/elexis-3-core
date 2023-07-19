package ch.elexis.core.services.internal;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

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
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
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
		if(user.isPresent()) {
			if (!userAclMap.containsKey(user.get())) {
				// calculate user ACL by combining the users roles
				userAclMap.put(user.get(), determineUserAccessControlList(user.get().getRoles()));
				logger.debug("ACE User=[{}] Roles=[{}]", user.get().getId(), userAclMap.get(user.get()).getRolesRepresented());
			}
			return evaluateACE(userAclMap.get(user.get()), evaluatableAce);			
		} else {
			logger.warn("No active user to evalute");
		}
		return false;
	}

	private boolean isPrivileged() {
		// privileged set, or in test mode
		return privileged.get() || (CoreUtil.isTestMode() && contextService.getNamed("testAccessControl").isEmpty());
	}

	private AccessControlList determineUserAccessControlList(List<IRole> roles) {
		if (roles.isEmpty()) {
			return null;
		}

		AccessControlList accessControlList = getOrLoadRoleAccessControlList(roles.get(0));
		if (roles.size() > 1) {
			for (int i = 1; i < roles.size(); i++) {
				AccessControlList _accessControlList = roleAclMap.get(roles.get(i).getId().toLowerCase());
				accessControlList = AccessControlListUtil.merge(_accessControlList, _accessControlList);
			}
		}
		return accessControlList;
	}

	private AccessControlList getOrLoadRoleAccessControlList(IRole iRole) {
		String _role = iRole.getId().toLowerCase();
		if (!roleAclMap.containsKey(_role)) {
			InputStream roleAccessDefaultUserFile = AccessControlList.class.getClassLoader()
					.getResourceAsStream("/rsc/acl/" + _role + ".json");
			try {
				AccessControlList acl = new ObjectMapper().configure(Feature.ALLOW_COMMENTS, true)
						.readValue(roleAccessDefaultUserFile, AccessControlList.class);
				roleAclMap.put(_role, acl);
			} catch (Exception e) {
				logger.error("Error loading role acl [{}]", _role, e);
			}
		}
		return roleAclMap.get(_role);
	}

	private boolean evaluateACE(AccessControlList acl, EvaluatableACE ace) {
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
						if (_ace.getObject() != null) {
							// TODO determine this specific right --> CACHED!!
						} else {
							aceBitMap[i] = (byte) 0;
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

}
