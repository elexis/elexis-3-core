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
package ch.elexis.admin;

import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.model.IRole;
import ch.elexis.core.model.IUser;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Role;
import ch.elexis.data.User;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;

/**
 * @since 3.1
 */
public class RoleBasedAccessControl extends AbstractAccessControl {
	
	private static Logger log = LoggerFactory.getLogger(RoleBasedAccessControl.class);
	
	public static final String QUERY_RIGHT_FOR_USER =
		"SELECT COUNT(*) FROM RIGHTS_PER_USER WHERE USER_ID LIKE %s AND (";
	public static final String QUERY_RIGHT_FOR_ROLE =
		"SELECT COUNT(*) FROM RIGHTS_PER_ROLE WHERE ROLE_ID LIKE %s AND (";
	
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
	protected static boolean queryRightForUser(@NonNull User u, @NonNull ACE ace){
		return queryRight(QUERY_RIGHT_FOR_USER, u.getWrappedId(), ace);
	}
	
	protected static boolean queryRightForRole(Role r, ACE ace){
		return queryRight(QUERY_RIGHT_FOR_ROLE, r.getWrappedId(), ace);
	}
	
	protected static boolean queryRightForRoles(List<IRole> roles, @NonNull ACE ace){
		if (roles != null) {
			for (IRole role : roles) {
				if (queryRightForRole(Role.load(role.getId()), ace)) {
					return true;
				}
			}
		}
		return false;
	}
	
	private static boolean queryRight(String qs, String objId, ACE ace){
		// TODO cache?
		String queryString = String.format(qs, objId);
		StringBuilder sb = new StringBuilder(queryString);
		List<ACE> parentACEs = ace.getParentChainIncludingSelf();
		for (int i = 0; i < parentACEs.size(); i++) {
			ACE a = parentACEs.get(i);
			if (i > 0) {
				sb.append(" OR ");
			}
			sb.append(" RIGHT_ID = " + JdbcLink.wrap(a.getUniqueHashFromACE()));
		}
		sb.append(StringConstants.CLOSEBRACKET + StringConstants.SEMICOLON);
		
		Stm stm = PersistentObject.getConnection().getStatement();
		boolean ret = false;
		try {
			ResultSet result = stm.query(sb.toString());
			boolean step = result.next();
			if (!step) {
				return ret;
			}
			int counts = result.getInt(1);
			ret = (counts > 0);
		} catch (SQLException e) {
			log.error("Error querying access right ", e);
		} finally {
			PersistentObject.getConnection().releaseStatement(stm);
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
		return request((User) null, ace);
	}
	
	@Override
	public boolean request(String canonicalName){
		if (canonicalName == null || canonicalName.length() < 1)
			return false;
		
		return request(ACE.getACEByCanonicalName(canonicalName));
	}
	
	/**
	 * 
	 * @param user
	 *            if <code>null</code> the user stored in the context is applied
	 * @param ace
	 *            if <code>null</code> always returns <code>false</code>
	 * @return <code>true</code> if access granted
	 */
	public boolean request(@Nullable User user, @Nullable ACE ace){
		if (ace == null)
			return false;
		
		if (user == null) {
			IUser iUser = ContextServiceHolder.get().getActiveUser().orElse(null);
			if (iUser == null) {
				return false;
			}
			if (!iUser.isInternal()) {
				return iUser.isAdministrator()
					|| RoleBasedAccessControl.queryRightForRoles(iUser.getRoles(), ace);
			}
			user = User.load(iUser.getId());
		}
		
		if (user.isAdministrator())
			return true;
		
		return RoleBasedAccessControl.queryRightForUser(user, ace);
	}
	
	/**
	 * 
	 * @param r
	 * @param ace
	 * @return
	 */
	public boolean request(@NonNull Role r, @Nullable ACE ace){
		if (ace == null)
			return false;
		
		return RoleBasedAccessControl.queryRightForRole(r, ace);
	}
	
	@Override
	public void grant(Role r, ACE ace){
		r.grantAccessRight(ace);
	}
	
	@Override
	public void revoke(Role r, ACE ace){
		r.revokeAccessRight(ace);
	}
	
	@Override
	public void grant(String id, ACE ace){
		Role r = Role.load(id);
		grant(r, ace);
	}
	
}
