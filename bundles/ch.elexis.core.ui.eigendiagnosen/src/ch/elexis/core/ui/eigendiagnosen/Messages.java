/*******************************************************************************
 * Copyright (c) 2005-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.eigendiagnosen;

import org.eclipse.osgi.util.NLS;

public class Messages extends NLS {
	private static final String BUNDLE_NAME = "ch.elexis.core.ui.eigendiagnosen.messages"; //$NON-NLS-1$
	public static String Eigendiagnosen_CodeSystemName;
	public static String Eigendiagnosen_allFilesDescription;
	public static String Eigendiagnosen_BadFileFormat;
	public static String Eigendiagnosen_CantRead;
	public static String Eigendiagnosen_csvDescription;
	public static String Eigendiagnosen_ImportFromCsvAndExcel;
	public static String Eigendiagnosen_msExcelDescription;
	public static String Eigendiagnosen_UnsupportedFileFormat;
	public static String EigendiagnoseSelector_Shortcut_Label;
	public static String EigendiagnoseSelector_Text_Label;
	static {
		// initialize resource bundle
		NLS.initializeMessages(BUNDLE_NAME, Messages.class);
	}
	
	private Messages(){}
}
