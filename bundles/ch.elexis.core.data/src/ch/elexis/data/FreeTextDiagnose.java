/*******************************************************************************
 * Copyright (c) 2005-2014, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    T. Huster - initial implementation
 *
 *******************************************************************************/
package ch.elexis.data;

import ch.elexis.core.data.interfaces.IDiagnose;

public class FreeTextDiagnose extends PersistentObject implements IDiagnose {

	public static final String TABLENAME = "at_medevit_elexis_freetextdiagnose"; //$NON-NLS-1$

	public static final String FLD_TEXT = "text"; //$NON-NLS-1$

	static {
		addMapping(TABLENAME, FLD_TEXT);
	}

	public FreeTextDiagnose() {
	}

	private FreeTextDiagnose(String id) {
		super(id);
	}

	public static FreeTextDiagnose load(final String id) {
		return new FreeTextDiagnose(id);
	}

	public FreeTextDiagnose(String text, boolean create) {
		create(null, new String[] { FLD_TEXT }, new String[] { text });
	}

	@Override
	public String getLabel() {
		String ret = getText();
		if (ret.length() > 80) {
			ret = ret.substring(0, 77) + "..."; //$NON-NLS-1$
		}
		return ret;
	}

	@Override
	protected String getTableName() {
		return TABLENAME;
	}

	@Override
	public String getCodeSystemName() {
		return "freetext"; //$NON-NLS-1$
	}

	@Override
	public String getCodeSystemCode() {
		return "freetext"; //$NON-NLS-1$
	}

	@Override
	public String getCode() {
		return getId();
	}

	@Override
	public String getText() {
		return get(FLD_TEXT);
	}

}
