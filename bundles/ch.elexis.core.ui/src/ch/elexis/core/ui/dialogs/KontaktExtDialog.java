/*******************************************************************************
 * Copyright (c) 2006-2019, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    Niklaus Giger - refactoring to make rcptt happy and get rid of Enter/modify/Enter cycle
 *    
 *******************************************************************************/

package ch.elexis.core.ui.dialogs;

import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.ResourceBundle;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;

/**
 * Dialog to view/modify identifiers such as EAN, AHV, SSN, OID on objects
 * 
 * @author Gerry
 * 
 */
public class KontaktExtDialog extends TitleAreaDialog {
	private String[] fieldDefinitions;
	private Kontakt actKontact;
	private ExtInfoTable infoTable;
	/*
	 * private HashMap<String, String> xids; private String[] fields; private Text[]
	 * values;
	 */
	private static final org.slf4j.Logger logger = LoggerFactory.getLogger(KontaktExtDialog.class);

	public KontaktExtDialog(Shell shell, Kontakt k, String[] defvalues) {
		super(shell);
		this.actKontact = k;
		logger.info("{} fields are: {}", defvalues.length, defvalues);
		fieldDefinitions = defvalues;
		Arrays.sort(fieldDefinitions, new Comparator<String>() {
			public int compare(String o1, String o2) {
				return o1.compareTo(o2);
			}
		});
		for (int i = 0; i < fieldDefinitions.length; i++) {
			logger.trace("field with id {}: {}", i, fieldDefinitions[i]);
		}
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		infoTable = new ExtInfoTable(parent, fieldDefinitions);
		infoTable.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		infoTable.setLayout(new GridLayout(2, false));
		infoTable.setKontakt(actKontact);
		infoTable.pack();
		return infoTable;
	}

	@Override
	public void create() {
		super.create();
		setTitle(actKontact.getLabel());
		getShell().setText(Messages.KontaktExtDialog_indetityDetails); // $NON-NLS-1$
	}

	@Override
	protected void okPressed() {
		infoTable.okPressed(actKontact);
		super.okPressed();
	}

	/**
	 * // deprecated will no longer work correctly with Elexis >= 3.8
	 * 
	 * @author niklaus
	 *
	 */
	public static class ExtInfoTable extends Composite {
		private String[] fieldDefinitions;
		private HashMap<String, String> xids;
		private String[] fields;
		private String[] savedValues;
		private Text[] values;
		private Composite savedParent;

		public ExtInfoTable(Composite parent, String[] f) {
			super(parent, SWT.NONE);
			logger.info("{} fields are: {}", f.length, f);
			fieldDefinitions = f;
			Group params = new Group(parent, SWT.NONE);
			params.setText(Messages.KontaktExtDialog_pleaseENterDetails);
			params.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			params.setLayout(new GridLayout(2, false));
			savedParent = params;
		}

		public void setKontakt(Kontakt k) {
			xids = new HashMap<String, String>();
			fields = new String[fieldDefinitions.length];
			savedValues = new String[fieldDefinitions.length];
			values = new Text[fieldDefinitions.length];
			for (int i = 0; i < fieldDefinitions.length; i++) {
				String[] val = fieldDefinitions[i].split("="); //$NON-NLS-1$
				String key = val[0];
				fields[i] = key;
				if (val.length == 2) {
					xids.put(key, val[1]);
				}
				String value = StringConstants.EMPTY;
				String xid = xids.get(fields[i]);
				if (xid != null) {
					value = k.getXid(xid);
				}
				if (value.length() == 0) {
					value = (String) k.getInfoElement(fields[i]);
				}
				String msg_key_label = "KontaktExtInfo_" + key; //$NON-NLS-1$
				msg_key_label = msg_key_label.replaceAll("[^a-zA-Z0-9_]", "_");
				String msg_key_tooltip = "KontaktExtInfo_" + key + "_tooltip"; //$NON-NLS-1$ //$NON-NLS-2$
				msg_key_tooltip = msg_key_tooltip.replaceAll("[^a-zA-Z0-9_]", "_");
				String label_text = key;
				String tooltip_text = "";
				try {
					label_text = ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
							.getString(msg_key_label);
				} catch (java.util.MissingResourceException ex) {
					logger.info("init: missing_l10n_key {}", msg_key_label);
				}
				try {
					tooltip_text = ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
							.getString(msg_key_tooltip);
				} catch (java.util.MissingResourceException ex) {
					logger.info("init: missing_l10n_key {}", msg_key_tooltip);
				}
				new Label(savedParent, SWT.NONE).setText(label_text);
				values[i] = new Text(savedParent, SWT.BORDER | SWT.SINGLE);
				values[i].setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
				values[i].setText(value == null ? StringConstants.EMPTY : value);
				values[i].setToolTipText(tooltip_text);
				savedValues[i] = value;
				logger.trace("init: addXid {} field {} key  {} value  {}\nlabel {} tooltip {}", i, fields[i],
						xids.get(i), values[i].getText(), label_text, msg_key_tooltip);
			}
		}

		public void okPressed(Kontakt k) {
			for (int i = 0; i < fieldDefinitions.length; i++) {
				String value = values[i].getText();
				if ((savedValues == null && !value.isEmpty())
						|| (savedValues[i] != null && !savedValues[i].equals(value))) {
					k.setInfoElement(fields[i], value);
					String xid = xids.get(fields[i]);
					if (xid != null) {
						logger.debug("okPressed: Updating field {}: {} key  {} value {} -> {}", i, fields[i], xid,
								savedValues[i], value);
						k.addXid(xid, value, true);
					}
				}
			}
		}
	}
}
