/*******************************************************************************
 * Copyright (c) 2009-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************
 */
package ch.elexis.core.constants;

/**
 * Utility Class for different constants. To ensure that same things are named identically in
 * different program parts
 * 
 * @author gerry
 * 
 */
public class StringConstants {
	public static final String SLASH = "/"; //$NON-NLS-1$
	public static final String BACKSLASH = "\\"; //$NON-NLS-1$
	public static final String SPACE = " "; //$NON-NLS-1$
	public static final String EMPTY = ""; //$NON-NLS-1$
	public static final String COMMA = ","; //$NON-NLS-1$
	public static final String COLON = ":"; //$NON-NLS-1$
	public static final String DASH = "-";
	public static final String DOUBLECOLON = "::"; //$NON-NLS-1$
	public static final String SEMICOLON = ";";
	public static final String CRLF = "\r\n";
	public static final String LF = "\n";
	
	public static final String VERSION_LITERAL = "VERSION"; //$NON-NLS-1$
	
	public static final String ONE = "1";
	public static final String ZERO = "0";
	public static final String FLOAT_ZERO = "0.0";
	public static final String DOUBLE_ZERO = "0.00";
	public static final String MANDATOR = Messages.StringConstants_mandator;
	public static final String USER = Messages.StringConstants_user;
	
	public static final String ROLE_NAMING = Messages.StringConstants_role;
	public static final String ROLES_NAMING = Messages.StringConstants_roles;
	
	public static final String ROLE_ADMIN = Messages.StringConstants_admin;
	public static final String ROLE_USERS = Messages.StringConstants_user;
	public static final String ROLE_ALL = Messages.StringConstants_all;
	public static final String ROLES_DEFAULT = ROLE_ADMIN + "," + ROLE_USERS + "," + ROLE_ALL; //$NON-NLS-1$ //$NON-NLS-2$
	public static final String SWTBOT_ID = "org.eclipse.swtbot.widget.key";
	public static final String OPENBRACKET = "(";
	public static final String CLOSEBRACKET = ")";
	
}
