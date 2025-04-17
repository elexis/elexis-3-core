/*******************************************************************************
 * Copyright (c) 2005-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MouseAdapter;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.IContentProvider;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

public class Mandanten extends PreferencePage implements IWorkbenchPreferencePage {
	private LabeledInputField.AutoForm lfa;
	private InputData[] def;
	private Label lColor;

	private Hashtable<String, Mandant> hMandanten = new Hashtable<>();

	@Override
	protected Control createContents(final Composite parent) {
			FormToolkit tk = UiDesk.getToolkit();
			Form form = tk.createForm(parent);
			final Composite body = form.getBody();
			body.setLayout(new GridLayout(1, false));
			final Combo mandanten = new Combo(body, SWT.DROP_DOWN | SWT.READ_ONLY);
			Query<Mandant> qbe = new Query<>(Mandant.class);
			List<Mandant> list = qbe.execute();
			for (Mandant m : (List<Mandant>) list) {
				mandanten.add(m.getLabel());
				hMandanten.put(m.getLabel(), m);
			}
			mandanten.addSelectionListener(new SelectionAdapter() {

				@Override
				public void widgetSelected(SelectionEvent e) {
					Combo source = (Combo) e.getSource();
					String m = (source.getItem(source.getSelectionIndex()));
					Mandant man = hMandanten.get(m);
					lColor.setBackground(UiDesk.getColorFromRGB(ConfigServiceHolder
							.getGlobal(Preferences.USR_MANDATOR_COLORS_PREFIX + m, UiDesk.COL_GREY60)));
					lfa.reload(man);
				}

			});
			GridData gd = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
			// gd.horizontalSpan=2;
			mandanten.setLayoutData(gd);
			tk.adapt(mandanten);
			lfa = new LabeledInputField.AutoForm(body, def);
			lfa.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			lColor = new Label(body, SWT.NONE);
			lColor.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			lColor.setText("Color for mandator");
			lColor.addMouseListener(new MouseAdapter() {
				@Override
				public void mouseDoubleClick(MouseEvent e) {
					ColorDialog cd = new ColorDialog(getShell());
					Label l = (Label) e.getSource();
					RGB selected = cd.open();
					String symbolic = UiDesk.createColor(selected);
					l.setBackground(UiDesk.getColorFromRGB(symbolic));
					ConfigServiceHolder.setGlobal(
							Preferences.USR_MANDATOR_COLORS_PREFIX + mandanten.getItem(mandanten.getSelectionIndex()),
							symbolic);
				}
			});
			tk.paintBordersFor(body);
			return form;
	}

	public void init(IWorkbench workbench) {
		String grp = ConfigServiceHolder.getGlobal(Preferences.ACC_GROUPS, Messages.Core_Admin);

		def = new InputData[] { new InputData(Messages.Core_Short_Label, "Label", Typ.STRING, null), //$NON-NLS-1$
				new InputData(Messages.Core_Password, PersistentObject.FLD_EXTINFO, Typ.STRING, "UsrPwd"), //$NON-NLS-1$
				// -> KSK, NIF und EAN gehören zu Tarmed.
				// new InputData("KSK-Nr","ExtInfo",Typ.STRING,"KSK"),
				// new InputData("NIF","ExtInfo",Typ.STRING,"NIF"),
				// new InputData("EANr","ExtInfo",Typ.STRING,"EAN"),
				new InputData(Messages.Core_Groups, PersistentObject.FLD_EXTINFO, "Groups", grp.split(",")), //$NON-NLS-1$ //$NON-NLS-2$
				new InputData(Messages.Core_Invoicingparty, PersistentObject.FLD_EXTINFO, new IContentProvider() {

					public void displayContent(Object po, InputData ltf) {
						Mandant m = (Mandant) po;
						Kontakt r = m.getRechnungssteller();
						ltf.setText(r.getLabel());
					}

					public void reloadContent(Object po, InputData ltf) {
						Kontakt rsi = (Kontakt) po;
						KontaktSelektor ksl = new KontaktSelektor(getShell(), Kontakt.class,
								Messages.Mandanten_selectBiller, Messages.Mandanten_pleaseSelectBiller,
								new String[] { Kontakt.FLD_NAME1, Kontakt.FLD_NAME2 });
						if (ksl.open() == Dialog.OK) {
							rsi = (Kontakt) ksl.getSelection();
						}
						((Mandant) po).setRechnungssteller(rsi);
						ltf.setText(rsi.getLabel());
					}
				}) };
	}

}
