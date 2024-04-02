/*******************************************************************************
 * Copyright (c) 2007, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.preferences.inputs;

import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import ch.elexis.core.data.util.MultiplikatorList;
import ch.elexis.core.ui.dialogs.AddMultiplikatorDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.PersistentObject;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.JdbcLink.Stm;
import ch.rgw.tools.TimeTool;

public class MultiplikatorEditor extends Composite {
	// String myClass;
	List list;
	final Stm stm = PersistentObject.getConnection().getStatement();
	String typeName;

	public MultiplikatorEditor(final Composite prnt, final String clazz) {
		super(prnt, SWT.NONE);
		setLayout(new GridLayout());
		setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		list = new List(this, SWT.BORDER | SWT.V_SCROLL | SWT.SINGLE);
		list.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		Button bNew = new Button(this, SWT.PUSH);
		bNew.setText(Messages.Core_Add_ellipsis); // $NON-NLS-1$
		bNew.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				AddMultiplikatorDialog amd = new AddMultiplikatorDialog(getShell());
				if (amd.open() == Dialog.OK) {
					TimeTool t = amd.getBegindate();
					String mul = amd.getMult();

					MultiplikatorList multis = new MultiplikatorList("VK_PREISE", typeName);
					multis.insertMultiplikator(t, mul);
					list.add(String.format("%s %s: %s", //$NON-NLS-1$
							Messages.MultiplikatorEditor_from, t.toString(TimeTool.DATE_GER), mul));
				}
			}
		});
		reload(clazz);
	}

	@Override
	public void dispose() {
		PersistentObject.getConnection().releaseStatement(stm);
	}

	@SuppressWarnings("unchecked")
	public void reload(final String typeName) {
		this.typeName = typeName;
		ArrayList<String[]> daten = new ArrayList<>();
		try (ResultSet res = stm.query("SELECT * FROM VK_PREISE WHERE TYP=" + JdbcLink.wrap(typeName))) { //$NON-NLS-1$
			while ((res != null) && (res.next() == true)) {
				String[] row = new String[2];
				row[0] = res.getString("DATUM_VON"); //$NON-NLS-1$
				row[1] = res.getString("MULTIPLIKATOR"); //$NON-NLS-1$
				daten.add(row);
			}

			Collections.sort(daten, new Comparator() {

				public int compare(final Object o1, final Object o2) {
					String[] s1 = (String[]) o1;
					String[] s2 = (String[]) o2;
					return s1[0].compareTo(s2[0]);
				}

			});

			TimeTool dis = new TimeTool();
			list.removeAll();
			for (String[] s : daten) {
				dis.set(s[0]);
				list.add(String.format("%s %s: %s", //$NON-NLS-1$
						Messages.MultiplikatorEditor_from, dis.toString(TimeTool.DATE_GER), s[1]));
			}

		} catch (Exception ex) {
			ExHandler.handle(ex);
		}
	}
}
