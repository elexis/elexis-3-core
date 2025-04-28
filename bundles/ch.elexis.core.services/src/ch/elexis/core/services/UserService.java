package ch.elexis.core.services;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.codec.DecoderException;
import org.apache.commons.lang3.StringUtils;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import com.google.common.cache.CacheBuilder;
import com.google.common.cache.CacheLoader;
import com.google.common.cache.LoadingCache;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.IMandator;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.IUserGroup;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.utils.OsgiServiceUtil;
import ch.rgw.tools.PasswordEncryptionService;

@Component
public class UserService implements IUserService {

	@Reference(target = "(" + IModelService.SERVICEMODELNAME + "=ch.elexis.core.model)")
	private IModelService modelService;

	private LoadingCache<IUser, Set<IMandator>> userExecutiveDoctorsWorkingForCache;
	private LoadingCache<IUserGroup, Set<IMandator>> groupExecutiveDoctorsWorkingForCache;

	private IAccessControlService accessControlService;

	@Activate()
	public void activate() {
		userExecutiveDoctorsWorkingForCache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS)
				.build(new UserExecutiveDoctorsLoader());
		groupExecutiveDoctorsWorkingForCache = CacheBuilder.newBuilder().expireAfterAccess(15, TimeUnit.SECONDS)
				.build(new GroupExecutiveDoctorsLoader());
	}

	@Override
	public boolean verifyPassword(IUser user, char[] attemptedPassword) {
		boolean ret = false;

		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				ret = pes.authenticate(attemptedPassword, user.getHashedPassword(), user.getSalt());
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				LoggerFactory.getLogger(getClass()).warn("Error verifying password for user [{}].", user.getLabel(), e);
			}
		}

		return ret;
	}

	@Override
	public void setPasswordForUser(IUser user, String password) {
		if (user != null) {
			PasswordEncryptionService pes = new PasswordEncryptionService();
			try {
				String salt = pes.generateSaltAsHexString();
				String hashed_pw = pes.getEncryptedPasswordAsHexString(password, salt);
				user.setSalt(salt);
				user.setHashedPassword(hashed_pw);
				modelService.save(user);
			} catch (NoSuchAlgorithmException | InvalidKeySpecException | DecoderException e) {
				LoggerFactory.getLogger(getClass()).warn("Error setting password for user [{}].", user.getLabel(), e);
			}
		}

	}

	@Override
	public Set<IMandator> getExecutiveDoctorsWorkingFor(IUser user, boolean includeNonActive) {
		try {
			if (includeNonActive) {
				return userExecutiveDoctorsWorkingForCache.get(user);
			} else {
				return userExecutiveDoctorsWorkingForCache.get(user).stream().filter(IMandator::isActive)
						.collect(Collectors.toSet());
			}
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("Error getting executive doctors", e);
		}
		return Collections.emptySet();
	}

	@Override
	public Set<IMandator> getExecutiveDoctorsWorkingFor(IUserGroup group, boolean includeNonActive) {
		try {
			if (group != null) {
				if (includeNonActive) {
					return groupExecutiveDoctorsWorkingForCache.get(group);
				} else {
					return groupExecutiveDoctorsWorkingForCache.get(group).stream().filter(IMandator::isActive)
							.collect(Collectors.toSet());
				}
			}
		} catch (ExecutionException e) {
			LoggerFactory.getLogger(getClass()).error("Error getting executive doctors", e);
		}
		return Collections.emptySet();
	}

	@Override
	public Optional<IMandator> getDefaultExecutiveDoctorWorkingFor(IUser user) {
		if (user.getAssignedContact() != null) {
			String defaultMandatorId = (String) user.getAssignedContact().getExtInfo("StdMandant");
			if (StringUtils.isNotEmpty(defaultMandatorId)) {
				return modelService.load(defaultMandatorId, IMandator.class);
			}

			if (user.getAssignedContact().isMandator()) {
				return modelService.load(user.getAssignedContact().getId(), IMandator.class);
			}
		}
		return Optional.empty();
	}

	@Override
	public void setDefaultExecutiveDoctorWorkingFor(IUser user, IMandator mandator) {
		if (user.getAssignedContact() != null) {
			user.getAssignedContact().setExtInfo("StdMandant", mandator.getId());
			CoreModelServiceHolder.get().save(user.getAssignedContact());
		} else {
			LoggerFactory.getLogger(getClass())
					.warn("Can not set executive doctors for user [" + user + "] with no assigned contact");
		}
	}

	@Override
	public void addOrRemoveExecutiveDoctorWorkingFor(IUser user, IMandator mandator, boolean add) {
		HashSet<IMandator> mandators = new HashSet<>(getExecutiveDoctorsWorkingFor(user, true));
		if (add) {
			mandators.add(mandator);
		} else {
			mandators.remove(mandator);
		}
		List<String> edList = mandators.stream().map(p -> p.getLabel()).collect(Collectors.toList());
		user.getAssignedContact().setExtInfo("Mandant",
				edList.isEmpty() ? StringUtils.EMPTY
						: (String) edList.stream().map(o -> o.toString())
								.reduce((u, t) -> u + StringConstants.COMMA + t).get());
		CoreModelServiceHolder.get().save(user.getAssignedContact());
		userExecutiveDoctorsWorkingForCache.invalidateAll();
	}

	@Override
	public void addOrRemoveExecutiveDoctorWorkingFor(IUserGroup userGroup, IMandator mandator, boolean add) {
		HashSet<IMandator> mandators = new HashSet<>(getExecutiveDoctorsWorkingFor(userGroup, true));
		if (add) {
			mandators.add(mandator);
		} else {
			mandators.remove(mandator);
		}
		List<String> edList = mandators.stream().map(p -> p.getLabel()).collect(Collectors.toList());
		userGroup.setExtInfo("Mandant",
				edList.isEmpty() ? StringUtils.EMPTY
						: (String) edList.stream().map(o -> o.toString())
								.reduce((u, t) -> u + StringConstants.COMMA + t).get());
		CoreModelServiceHolder.get().save(userGroup);
		userExecutiveDoctorsWorkingForCache.invalidateAll();
		groupExecutiveDoctorsWorkingForCache.invalidateAll();
	}

	@Override
	public List<IUser> getUsersByAssociatedContact(IContact contact) {
		if (contact == null) {
			return Collections.emptyList();
		}
		IQuery<IUser> qre = modelService.getQuery(IUser.class);
		qre.and(ModelPackage.Literals.IUSER__ASSIGNED_CONTACT, COMPARATOR.EQUALS, contact);
		qre.and(ModelPackage.Literals.IUSER__ACTIVE, COMPARATOR.EQUALS, true);
		return qre.execute();
	}

	private class UserExecutiveDoctorsLoader extends CacheLoader<IUser, Set<IMandator>> {

		@Override
		public Set<IMandator> load(IUser user) throws Exception {
			List<IUserGroup> groups = getUserGroups(user);
			if (!groups.isEmpty()) {
				Set<IMandator> ret = new HashSet<>();
				groups.forEach(gr -> {
					ret.addAll(getExecutiveDoctorsWorkingFor(gr, true));
				});
				return ret;
			} else if (user.getAssignedContact() != null) {
				String mandators = (String) user.getAssignedContact().getExtInfo("Mandant");
				if (mandators == null) {
					return Collections.emptySet();
				}
				List<IMandator> allMandators = modelService.getQuery(IMandator.class).execute().stream()
						.collect(Collectors.toList());

				List<String> mandatorsIdList = Arrays.asList(mandators.split(","));
				return allMandators.stream().filter(p -> mandatorsIdList.contains(p.getLabel()))
						.collect(Collectors.toSet());
			}
			return Collections.emptySet();
		}
	}

	private class GroupExecutiveDoctorsLoader extends CacheLoader<IUserGroup, Set<IMandator>> {

		@Override
		public Set<IMandator> load(IUserGroup userGroup) throws Exception {
			String mandators = (String) userGroup.getExtInfo("Mandant");
			if (mandators == null) {
				return Collections.emptySet();
			}

			List<IMandator> allMandators = modelService.getQuery(IMandator.class).execute().stream()
					.collect(Collectors.toList());

			List<String> mandatorsIdList = Arrays.asList(mandators.split(","));
			return allMandators.stream().filter(p -> mandatorsIdList.contains(p.getLabel()))
					.collect(Collectors.toSet());
		}
	}

	@Override
	public boolean verifyUsernameNotTaken(String username) {
		IQuery<IUser> query = modelService.getQuery(IUser.class, true);
		query.and("id", COMPARATOR.EQUALS, username);
		return query.execute().isEmpty();
	}

	@Override
	public boolean verifyGroupnameNotTaken(String groupname) {
		IQuery<IUserGroup> query = modelService.getQuery(IUserGroup.class, true);
		query.and("id", COMPARATOR.EQUALS, groupname);
		return query.execute().isEmpty();
	}

	@Override
	public List<IUserGroup> getUserGroups(IUser user) {
		List<IUserGroup> ret = new ArrayList<>();
		INativeQuery nativeQuery = modelService
				.getNativeQuery("SELECT USERGROUP_ID FROM USERGROUP_USER_JOINT WHERE ID = ?1");
		Iterator<?> result = nativeQuery
				.executeWithParameters(nativeQuery.getIndexedParameterMap(Integer.valueOf(1), user.getId())).iterator();
		IAccessControlService accessControlService = getAccessControlService();
		while (result.hasNext()) {
			String next = result.next().toString();
			if (accessControlService != null) {
				accessControlService
						.doPrivileged(() -> ret.add(CoreModelServiceHolder.get().load(next, IUserGroup.class).get()));
			} else {
				ret.add(CoreModelServiceHolder.get().load(next, IUserGroup.class).get());
			}
		}
		return ret;
	}

	private IAccessControlService getAccessControlService() {
		if (accessControlService == null) {
			accessControlService = OsgiServiceUtil.getService(IAccessControlService.class).orElse(null);
		}
		return accessControlService;
	}

	@Override
	public List<IRole> getUserRoles(IUser user) {
		List<IUserGroup> groups = getUserGroups(user);
		if (!groups.isEmpty()) {
			Set<IRole> roles = new HashSet<>();
			groups.forEach(g -> roles.addAll(g.getRoles()));
			return new ArrayList<>(roles);
		}
		return user.getRoles();
	}

	@Override
	public Set<String> setUserRoles(IUser user, Set<String> userRoles) {
		List<IRole> targetUserRoleSet = new LinkedList<>();
		for (String roleId : userRoles) {
			Optional<IRole> _role = modelService.load(roleId, IRole.class);
			if (_role.isPresent()) {
				targetUserRoleSet.add(_role.get());
			}
		}
		user.setRoles(targetUserRoleSet);
		modelService.save(user);
		return targetUserRoleSet.stream().map(r -> r.getId()).collect(Collectors.toSet());
	}

	@Override
	public boolean hasRole(IUser user, Set<String> roleIds) {
		return getUserRoles(user).stream().anyMatch(role -> roleIds.contains(role.getId()));
	}
}
