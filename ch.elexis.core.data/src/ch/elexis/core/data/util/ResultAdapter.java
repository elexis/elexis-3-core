/*******************************************************************************
 * Copyright (c) 2008-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.data.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.MultiStatus;
import org.eclipse.core.runtime.Status;

import ch.elexis.core.data.interfaces.events.MessageEvent;
import ch.rgw.tools.Result;

/**
 * Helper class to adapt Result's into Eclipse structures.
 * 
 * @author Gerry
 * 
 */
public class ResultAdapter {

	private static final String PLUGIN_ID = "ch.elexis"; //$NON-NLS-1$

	/**
	 * Den Status als Eclipse IStatus bzw. MultiStatus abholen
	 * 
	 * @return
	 */

	@SuppressWarnings("unchecked")
	public static IStatus getResultAsStatus(Result result) {
		if (result.isOK()) {
			return org.eclipse.core.runtime.Status.OK_STATUS;
		} else {
			List<Result.msg> list = result.getMessages();
			if (list.size() == 1) {
				Result.msg r = list.get(0);
				return new org.eclipse.core.runtime.Status(
						getSeverityAsStatus(r.getSeverity()), PLUGIN_ID,
						r.getCode(),
						r.getText() == null ? "?" : r.getText(), null); //$NON-NLS-1$
			} else {
				ArrayList<IStatus> as = new ArrayList<IStatus>();
				Result.msg r = list.get(0);
				for (Result.msg m : list) {
					as.add(new org.eclipse.core.runtime.Status(
							getSeverityAsStatus(m.getSeverity()), PLUGIN_ID, m
									.getCode(), m.getText(), null)); //$NON-NLS-1$
					if (m.getSeverity().ordinal() > r.getSeverity().ordinal()) {
						r = m;
					}
				}
				return new MultiStatus(PLUGIN_ID, r.getCode(),
						as.toArray(new IStatus[0]),
						r.getText() == null ? "?" : r.getText(), null); //$NON-NLS-1$
			}
		}
	}

	/**
	 * Mapping zwischen Severity und RCP Status Severity (Status.OK,
	 * Stauts.INFO, usw)
	 */
	public static int getSeverityAsStatus(final Result.SEVERITY severity) {
		switch (severity.ordinal()) {
		case 0:
			return Status.OK;
		case 3:
			return Status.ERROR;
		case 2:
			return Status.ERROR;
		case 1:
			return Status.INFO;
		}
		return Status.ERROR;
	}

	public static void displayResult(final Result result, final String title) {
		MessageEvent.fireError(title, result.toString());
	}
}
