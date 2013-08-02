/*******************************************************************************
 * Copyright (c) 2006-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - extracted from elexis main and adapted for usage
 * 
 *******************************************************************************/
package ch.elexis.core.ui.eigenartikel;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.core.data.Artikel;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.eigenartikel.Eigenartikel;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.core.ui.views.IDetailDisplay;

public class EigenartikelDetailDisplay implements IDetailDisplay {
	
	private static InputData inoffPharmacode;
	private static boolean warningAccepted = false;
	
	static final public InputData[] getFieldDefs(final Shell shell){
		inoffPharmacode =
			new InputData(Messages.EigenartikelDisplay_Pharmacode, Artikel.FLD_SUB_ID, Typ.STRING,
				null);
		
		InputData[] ret =
			new InputData[] {
				new InputData(Messages.EigenartikelDisplay_typ, Artikel.FLD_TYP, Typ.STRING, null),
				inoffPharmacode,
				new InputData(Messages.EigenartikelDisplay_group, Artikel.FLD_CODECLASS,
					Typ.STRING, null),
				new InputData(Messages.EigenartikelDisplay_buyPrice, Artikel.FLD_EK_PREIS,
					Typ.CURRENCY, null),
				new InputData(Messages.EigenartikelDisplay_sellPrice, Artikel.FLD_VK_PREIS,
					Typ.CURRENCY, null),
				new InputData(Messages.EigenartikelDisplay_maxOnStock, Artikel.MAXBESTAND,
					Typ.STRING, null),
				new InputData(Messages.EigenartikelDisplay_minOnStock, Artikel.MINBESTAND,
					Typ.STRING, null),
				new InputData(Messages.EigenartikelDisplay_actualOnStockPacks, Artikel.ISTBESTAND,
					Typ.STRING, null),
				new InputData(Messages.EigenartikelDisplay_actualOnStockPieces,
					Artikel.FLD_EXTINFO, Typ.INT, Artikel.ANBRUCH),
				new InputData(Messages.EigenartikelDisplay_PiecesPerPack, Artikel.FLD_EXTINFO,
					Typ.INT, Artikel.VERPACKUNGSEINHEIT),
				new InputData(Messages.EigenartikelDisplay_PiecesPerDose, Artikel.FLD_EXTINFO,
					Typ.INT, Artikel.VERKAUFSEINHEIT),
				new InputData(Messages.EigenartikelDisplay_dealer, Artikel.FLD_LIEFERANT_ID,
					new LabeledInputField.IContentProvider() {
						public void displayContent(PersistentObject po, InputData ltf){
							String lbl = ((Artikel) po).getLieferant().getLabel();
							if (lbl.length() > 15) {
								lbl = lbl.substring(0, 12) + "..."; //$NON-NLS-1$
							}
							ltf.setText(lbl);
						}
						
						public void reloadContent(PersistentObject po, InputData ltf){
							KontaktSelektor ksl =
								new KontaktSelektor(shell, Kontakt.class,
									Messages.EigenartikelDisplay_dealer,
									Messages.EigenartikelDisplay_pleaseChooseDealer,
									Kontakt.DEFAULT_SORT);
							if (ksl.open() == Dialog.OK) {
								Kontakt k = (Kontakt) ksl.getSelection();
								((Artikel) po).setLieferant(k);
								String lbl = ((Artikel) po).getLieferant().getLabel();
								if (lbl.length() > 15) {
									lbl = lbl.substring(0, 12) + "..."; //$NON-NLS-1$
								}
								ltf.setText(lbl);
								ElexisEventDispatcher.reload(Artikel.class);
							}
						}
						
					})
			};
		return ret;
	}
	
	FormToolkit tk = UiDesk.getToolkit();
	ScrolledForm form;
	LabeledInputField.AutoForm tblArtikel;
	
	@Override
	public Composite createDisplay(Composite parent, IViewSite site){
		parent.setLayout(new FillLayout());
		form = tk.createScrolledForm(parent);
		Composite ret = form.getBody();
		TableWrapLayout twl = new TableWrapLayout();
		ret.setLayout(twl);
		
		tblArtikel = new LabeledInputField.AutoForm(ret, getFieldDefs(parent.getShell()));
		
		LabeledInputField lif = inoffPharmacode.getWidget();
		final Text inoffPharmacodeText = (Text) lif.getControl();
		inoffPharmacodeText.addFocusListener(new FocusAdapter() {
			@Override
			public void focusGained(FocusEvent e){
				if (warningAccepted)
					return;
				MessageDialog.openInformation(PlatformUI.getWorkbench().getDisplay()
					.getActiveShell(), Messages.Eigenartikel_WarningPharmacodeChange_Title,
					Messages.Eigenartikel_WarningPharmacodeChange);
				warningAccepted = true;
				inoffPharmacodeText.setFocus();
			}
		});
		
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		tblArtikel.setLayoutData(twd);
		return ret;
	}
	
	@Override
	public Class<? extends PersistentObject> getElementClass(){
		return Eigenartikel.class;
	}
	
	@Override
	public void display(Object obj){
		if (obj instanceof Eigenartikel) {
			Eigenartikel m = (Eigenartikel) obj;
			form.setText(m.getLabel());
			tblArtikel.reload(m);
		}
	}
	
	@Override
	public String getTitle(){
		return Messages.EigenartikelDisplay_displayTitle;
	}
	
}
