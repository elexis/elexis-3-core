/*******************************************************************************
 * Copyright (c) 2011, Marco Descher
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 * 	Marco Descher - initial implementation
 *
 ******************************************************************************/
package ch.elexis.core.ui.commands.sourceprovider;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.ui.AbstractSourceProvider;
import org.eclipse.ui.ISources;

import ch.elexis.core.ui.Hub;


/**
 * This class provides a sourceProvider for Eclipse command evaluation. It can
 * be used to check whether a Patient is currently selected in the system.
 * According to this command contributions can be toggled on/off.
 *
 * The value is set within the {@link Hub} class.
 *
 * @author Marco Descher
 */
public class PatientSelectionStatus extends AbstractSourceProvider {

	public final static String PATIENTACTIVE = "ch.elexis.commands.sourceprovider.patientSelectionActive"; //$NON-NLS-1$
	public final static String TRUE = "TRUE"; //$NON-NLS-1$
	public final static String FALSE = "FALSE"; //$NON-NLS-1$
	private boolean enabled = false;

	public PatientSelectionStatus() {
	}

	@Override
	public void dispose() {
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	@Override
	public Map getCurrentState() {
		Map map = new HashMap();
		String value = enabled ? TRUE : FALSE;
		map.put(PATIENTACTIVE, value);
		return map;
	}

	@Override
	public String[] getProvidedSourceNames() {
		return new String[] { PATIENTACTIVE };
	}

	public void setState(boolean state) {
		String value = FALSE;
		if (state == true) {
			value = TRUE;
		} else {
			value = FALSE;
		}
		fireSourceChanged(ISources.WORKBENCH, PATIENTACTIVE, value);
	}

}
