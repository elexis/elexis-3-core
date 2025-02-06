/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.constants;

/**
 * This class provides preference constants to be referenced by the UI parts
 * requiring configuration values.
 *
 * @since 3.0.0
 */
public class UiPreferenceConstants {
	public static final String USERSETTINGS2_EXPANDABLE_COMPOSITES_BASE = "view/expandableComposites"; //$NON-NLS-1$
	public static final String USERSETTINGS2_EXPANDABLE_COMPOSITES = USERSETTINGS2_EXPANDABLE_COMPOSITES_BASE
			+ "/setting"; //$NON-NLS-1$
	public static final String USERSETTINGS2_EXPANDABLE_COMPOSITES_STATES = USERSETTINGS2_EXPANDABLE_COMPOSITES_BASE
			+ "/states/"; //$NON-NLS-1$
	public static final String USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_OPEN = "1"; //$NON-NLS-1$
	public static final String USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_CLOSED = "2"; //$NON-NLS-1$
	public static final String USERSETTINGS2_EXPANDABLECOMPOSITE_STATE_REMEMBER_STATE = "3"; //$NON-NLS-1$
	public static final String DEFAULT_BASE_URL = "http://wiki.elexis.info/"; //$NON-NLS-1$
	public static final String DEFAULT_START_PAGE = "Hauptseite"; //$NON-NLS-1$
	public static final String DEFAULT_HANDBOOK = "https://support.medelexis.ch/"; //$NON-NLS-1$
	public static final String CFG_BASE = "org.iatrix.help.wiki"; //$NON-NLS-1$
	public static final String CFG_HANDBOOK = CFG_BASE + "/handbook"; //$NON-NLS-1$
}
