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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.Artikel;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListenerImpl;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.GlobalEventDispatcher.IActivationListener;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;

public class Artikeldetail extends ViewPart implements IActivationListener, ISaveablePart2 {
	public static final String ID = "ch.elexis.ArtikelDetail"; //$NON-NLS-1$
	
	static final public InputData[] getFieldDefs(final Shell shell){
		InputData[] ret =
			new InputData[] {
				new InputData(Messages.Artikeldetail_typ, Artikel.FLD_TYP, Typ.STRING, null),
				new InputData(Messages.Artikeldetail_EAN, Artikel.FLD_EAN, Typ.STRING, null),
				new InputData(Messages.Artikeldetail_Pharmacode, Artikel.FLD_EXTINFO, Typ.STRING,
					"Pharmacode"), //$NON-NLS-2$
				new InputData(Messages.Artikeldetail_Einkaufspreis, Artikel.FLD_EK_PREIS,
					Typ.CURRENCY, null),
				new InputData(Messages.Artikeldetail_Verkaufspreis, Artikel.FLD_VK_PREIS,
					Typ.CURRENCY, null),
				new InputData(Messages.Artikeldetail_maxbestand, Artikel.MAXBESTAND, Typ.STRING,
					null),
				new InputData(Messages.Artikeldetail_minbestand, Artikel.MINBESTAND, Typ.STRING,
					null),
				new InputData(Messages.Artikeldetail_istbestand, Artikel.ISTBESTAND, Typ.STRING,
					null),
				new InputData(Messages.Artikeldetail_istbestandAnbruch, Artikel.FLD_EXTINFO,
					Typ.INT, "Anbruch"), //$NON-NLS-2$
				new InputData(Messages.Artikeldetail_verpackungseinheit, Artikel.FLD_EXTINFO,
					Typ.INT, "Verpackungseinheit"), //$NON-NLS-2$
				new InputData(Messages.Artikeldetail_stueckProAbgabe, Artikel.FLD_EXTINFO, Typ.INT,
					"Verkaufseinheit"), //$NON-NLS-2$
				new InputData(Messages.Artikeldetail_lieferant, Artikel.FLD_LIEFERANT_ID,
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
									Messages.Artikeldetail_lieferant,
									Messages.Artikeldetail_LieferantWaehlen, Kontakt.DEFAULT_SORT);
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
	
	private ElexisEventListenerImpl eeli_art = new ElexisEventListenerImpl(Artikel.class) {
		
		public void runInUi(ElexisEvent ev){
			form.setText(ev.getObject().getLabel());
			tblArtikel.reload(ev.getObject());
		}
	};
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		form = tk.createScrolledForm(parent);
		TableWrapLayout twl = new TableWrapLayout();
		form.getBody().setLayout(twl);
		
		tblArtikel =
			new LabeledInputField.AutoForm(form.getBody(), getFieldDefs(parent.getShell()));
		
		TableWrapData twd = new TableWrapData(TableWrapData.FILL_GRAB);
		twd.grabHorizontal = true;
		tblArtikel.setLayoutData(twd);
		GlobalEventDispatcher.addActivationListener(this, this);
		
	}
	
	@Override
	public void setFocus(){
		
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
	public void activation(boolean mode){
		// TODO Auto-generated method stub
		
	}
	
	public void visible(boolean mode){
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_art);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_art);
		}
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
}
