/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.views.artikel;

import javax.inject.Inject;
import javax.inject.Named;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.data.Artikel;

public class Artikeldetail extends ViewPart implements IActivationListener {
	public static final String ID = "ch.elexis.ArtikelDetail"; //$NON-NLS-1$

	static final public InputData[] getFieldDefs(final Shell shell) {
		InputData[] ret = new InputData[] {
				new InputData(Messages.Core_Type, Artikel.FLD_TYP, Typ.STRING, null),
				new InputData(Messages.Artikeldetail_EAN, Artikel.FLD_EAN, Typ.STRING, null),
				new InputData(Messages.Core_Phamacode, Artikel.FLD_EXTINFO, Typ.STRING, "Pharmacode"), // $NON-NLS-2$ //$NON-NLS-1$
				new InputData(Messages.Artikeldetail_Einkaufspreis, Artikel.FLD_EK_PREIS, Typ.CURRENCY, null),
				new InputData(Messages.Artikeldetail_Verkaufspreis, Artikel.FLD_VK_PREIS, Typ.CURRENCY, null),
				new InputData(Messages.Core_Pieces_per_pack, Artikel.FLD_EXTINFO, Typ.INT,
						"VerpackungsEinheit"), // $NON-NLS-2$ //$NON-NLS-1$
				new InputData(Messages.Core_Pieces_by_dose, Artikel.FLD_EXTINFO, Typ.INT,
						"Verkaufseinheit") }; //$NON-NLS-1$
		return ret;
	}

	static final public InputData[] getModelFieldDefs(final Shell shell) {
		InputData[] ret = new InputData[] { new InputData(Messages.Core_Type, "typ", Typ.STRING, null), //$NON-NLS-1$
				new InputData(Messages.Artikeldetail_EAN, "gtin", Typ.STRING, null), //$NON-NLS-1$
				new InputData(Messages.Core_Phamacode, "extInfo", Typ.STRING, "Pharmacode"), // $NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-2$
				new InputData(Messages.Artikeldetail_Einkaufspreis, "purchasePrice", Typ.CURRENCY, null), //$NON-NLS-1$
				new InputData(Messages.Artikeldetail_Verkaufspreis, "sellingPrice", Typ.CURRENCY, null), //$NON-NLS-1$
				new InputData(Messages.Core_Pieces_per_pack, "extInfo", Typ.INT, "VerpackungsEinheit"), // $NON-NLS-2$ //$NON-NLS-1$ //$NON-NLS-2$
				new InputData(Messages.Core_Pieces_by_dose, "extInfo", Typ.INT, "Verkaufseinheit") }; //$NON-NLS-1$ //$NON-NLS-2$
		return ret;
	}

	FormToolkit tk = UiDesk.getToolkit();
	ScrolledForm form;
	LabeledInputField.AutoForm tblArtikel;

	private ElexisEventListenerImpl eeli_art = new ElexisUiEventListenerImpl(Artikel.class) {

		public void runInUi(ElexisEvent ev) {
			form.setText(ev.getObject().getLabel());
			tblArtikel.reload(ev.getObject());
		}
	};

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new FillLayout());
		form = tk.createScrolledForm(parent);
		TableWrapLayout twl = new TableWrapLayout();
		form.getBody().setLayout(twl);

		tblArtikel = new LabeledInputField.AutoForm(form.getBody(), getFieldDefs(parent.getShell()));

		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		tblArtikel.setLayoutData(twd);
		GlobalEventDispatcher.addActivationListener(this, this);

	}

	@Override
	public void setFocus() {

	}

	@Override
	public void dispose() {
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}

	public void activation(boolean mode) {
	}

	public void visible(boolean mode) {
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_art);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_art);
		}
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}
}
