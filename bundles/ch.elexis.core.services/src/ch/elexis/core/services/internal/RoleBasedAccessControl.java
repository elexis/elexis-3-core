/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.services.internal;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

import org.osgi.service.useradmin.User;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.ACE;
import ch.elexis.core.ac.AbstractAccessControl;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IRight;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.core.services.IModelService;
import ch.elexis.core.services.holder.ContextServiceHolder;

/**
 * @since 3.1
 */
public class RoleBasedAccessControl extends AbstractAccessControl {
	
	private Logger log = LoggerFactory.getLogger(getClass());
	
	public static final String QUERY_RIGHT_FOR_USER =
		"SELECT COUNT(*) FROM RIGHTS_PER_USER WHERE USER_ID LIKE '%s' AND (";
	public static final String QUERY_RIGHT_FOR_ROLE =
		"SELECT COUNT(*) FROM RIGHTS_PER_ROLE WHERE ROLE_ID LIKE '%s' AND (";
	
	private IModelService modelService;
	
	public RoleBasedAccessControl(IModelService modelService){
		this.modelService = modelService;
	}
	
	/**
	 * Query if user has the the provided right. Considers the entire rights chain, that is, if user
	 * u has the parent right of a given ACE the right is granted.
	 * 
	 * @param u
	 *            the {@link User}
	 * @param ace
	 *            the {@link ACE}
	 * @return <code>true</code> if {@link User} u is granted the provided {@link ACE} ace
	 */
	protected boolean queryRightForUser(@NonNull IUser u, @NonNull ACE ace){
		return queryRight(QUERY_RIGHT_FOR_USER, u.getId(), ace);
	}
	
	protected boolean queryRightForRole(IRole r, ACE ace){
		return queryRight(QUERY_RIGHT_FOR_ROLE, r.getId(), ace);
	}
	
	private boolean queryRight(String qs, String objId, ACE ace){
		// TODO cache?
		String queryString = String.format(qs, objId);
		StringBuilder sb = new StringBuilder(queryString);
		List<ACE> parentACEs = ace.getParentChainIncludingSelf();
		for (int i = 0; i < parentACEs.size(); i++) {
			ACE a = parentACEs.get(i);
			if (i > 0) {
				sb.append(" OR ");
			}
			sb.append(" RIGHT_ID = '" + a.getUniqueHash() + "'");
		}
		sb.append(StringConstants.CLOSEBRACKET + StringConstants.SEMICOLON);
		
		boolean ret = false;
		try {
			List<?> result =
				modelService.executeNativeQuery(sb.toString()).collect(Collectors.toList());
			long counts = (long) result.get(0);
			ret = (counts > 0);
		} catch (NumberFormatException e) {
			log.error("Error querying access right ", e);
		}
		
		return ret;
	}
	
	/**
	 * convenience method calling {@link #request(User, ACE)}
	 * 
	 * @param ace
	 * @return
	 * @see #request(User, ACE)
	 */
	public boolean request(@Nullable ACE ace){
		return request((IUser) null, ace);
	}
	
	/**
	 * 
	 * @param user
	 *            if <code>null</code> the user stored in the context is applied
	 * @param ace
	 *            if <code>null</code> always returns <code>false</code>
	 * @return <code>true</code> if access granted
	 */
	public boolean request(@Nullable IUser user, @Nullable ACE ace){
		if (ace == null) {
			return false;
		}
		
		if (user == null) {
			Optional<IUser> _user = ContextServiceHolder.get().getActiveUser();
			if (!_user.isPresent()) {
				return false;
			}
			user = _user.get();
		}
		
		if (user.isAdministrator()) {
			return true;
		}
		
		return queryRightForUser(user, ace);
	}
	
	/**
	 * 
	 * @param r
	 * @param ace
	 * @return
	 */
	public boolean request(@NonNull IRole r, @Nullable ACE ace){
		if (ace == null) {
			return false;
		}
		
		return queryRightForRole(r, ace);
	}
	
	private static final String INSERT_ROLE_RIGHT_JOINT =
		"INSERT INTO ROLE_RIGHT_JOINT (ID, ROLE_ID) VALUES ('%s', '%s')";
	
	@Override
	public void grant(IRole r, ACE ace){
		IRight right = getOrCreateRightByACE(ace);
		if (r.getAssignedRights().contains(right)) {
			return;
		}
		
		String sqlString = String.format(INSERT_ROLE_RIGHT_JOINT, ace.getUniqueHash(), r.getId());
		int result = modelService.executeNativeUpdate(sqlString);
		if (result != 1) {
			log.warn("Error in [{}] result size is [{}]", sqlString, result);
		}
	}
	
	private static final String DELETE_ROLE_RIGHT_JOINT =
		"DELETE FROM ROLE_RIGHT_JOINT WHERE (ID='%s') AND (ROLE_ID='%s')";
	
	@Override
	public void revoke(IRole r, ACE ace){
		String sqlString = String.format(DELETE_ROLE_RIGHT_JOINT, ace.getUniqueHash(), r.getId());
		int result = modelService.executeNativeUpdate(sqlString);
		if (result != 1) {
			log.warn("Error in [{}] result size is [{}]", sqlString, result);
		}
	}
	
	private IRight getOrCreateRightByACE(ACE ace){
		Optional<IRight> right = modelService.load(ace.getUniqueHash(), IRight.class);
		if (right.isPresent()) {
			return right.get();
		}
		
		IRight _right = modelService.create(IRight.class);
		_right.setId(ace.getUniqueHash());
		_right.setName(ace.getName());
		_right.setLocalizedName(ace.getLocalizedName());
		IRight _parentRight = getOrCreateRightByACE(ace.getParent());
		_right.setParent(_parentRight);
		
		modelService.save(_right);
		return _right;
	}
	
}
