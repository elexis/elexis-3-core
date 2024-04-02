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
package ch.elexis.data;

import java.util.List;
import java.util.Locale;

import ch.elexis.core.ac.Right;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.model.IRole;
import ch.rgw.tools.JdbcLink.Stm;

public class Role extends PersistentObject {

	public static final String TABLENAME = "ROLE";
	public static final String FLD_SYSTEM_ROLE = "ISSYSTEMROLE";
	public static final String FLD_EXT_I18N_LABEL = "LAB_" + Locale.getDefault().getLanguage();
	public static final String FLD_JOINT_RIGHTS = "Rights";

	static {
		addMapping(TABLENAME, FLD_ID, FLD_SYSTEM_ROLE, FLD_EXTINFO,
				FLD_JOINT_RIGHTS + "=LIST:ROLE_ID:ROLE_RIGHT_JOINT");
	}

	public Role() {
	}

	public Role(boolean isSystemRole) {
		create(null);

		setSystemRole(false);
	}

	protected Role(final String id) {
		super(id);
	}

	public static Role load(final String id) {
		return new Role(id);
	}

	@Override
	public String getLabel() {
		return get(FLD_ID);
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	public boolean isSystemRole() {
		return getBoolean(FLD_SYSTEM_ROLE);
	}

	public void setSystemRole(boolean val) {
		// ignored, for databinding only
	}

	public String getRoleName() {
		return get(FLD_ID);
	}

	/**
	 * verify whether the proposed rolename is not already in use
	 *
	 * @param rolename
	 * @return <code>true</code> if the given rolename is available for use
	 */
	public static boolean verifyRoleNameNotTaken(String rolename) {
		return new Query<Role>(Role.class, FLD_ID, rolename).execute().isEmpty();
	}

	public String getTranslatedLabel() {
		return (String) getExtInfoStoredObjectByKey(FLD_EXT_I18N_LABEL);
	}

	public void setTranslatedLabel(String translatedLabel) {
		setExtInfoStoredObjectByKey(FLD_EXT_I18N_LABEL, translatedLabel);
	}

	/**
	 * @return the {@link Right#ID} of all rights permitted to this role
	 */
	private List<String> getAssignedRightsIds() {
		return getList(FLD_JOINT_RIGHTS, false);
	}

	/**
	 * Revokes all rights of this role
	 */
	public void revokeAllRightsForRole() {
		Stm stm = getConnection().getStatement();
		stm.exec("DELETE FROM ROLE_RIGHT_JOINT WHERE ROLE_ID=" + getWrappedId());
		getConnection().releaseStatement(stm);
	}

	/**
	 * Convenience conversion method, loads object via model service
	 *
	 * @return
	 * @since 3.11
	 * @throws IllegalStateException if entity could not be loaded
	 */
	public IRole toIRole() {
		return CoreModelServiceHolder.get().load(getId(), IRole.class)
				.orElseThrow(() -> new IllegalStateException("Could not convert contact [" + getId() + "]"));
	}
}
