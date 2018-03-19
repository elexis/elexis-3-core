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

package ch.elexis.core.ui.contacts.views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.forms.widgets.ScrolledForm;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.model.IXid;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.dialogs.AnschriftEingabeDialog;
import ch.elexis.core.ui.dialogs.KontaktExtDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.ToggleCurrentKontaktLockHandler;
import ch.elexis.core.ui.util.LabeledInputField;
import ch.elexis.core.ui.util.LabeledInputField.AutoForm;
import ch.elexis.core.ui.util.LabeledInputField.InputData;
import ch.elexis.core.ui.util.LabeledInputField.InputData.Typ;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.Messages;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Labor;
import ch.elexis.data.Organisation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Person;
import ch.elexis.data.Xid;
import ch.elexis.data.Xid.XIDDomain;

public class KontaktBlatt extends Composite implements IActivationListener, IUnlockable {
	
	private static final String IS_USER = "istAnwender";
	
	private static final String MOBIL = Messages.KontaktBlatt_MobilePhone; //$NON-NLS-1$
	private static final String VORNAME = Messages.KontaktBlatt_FirstName; //$NON-NLS-1$
	private static final String NAME = Messages.KontaktBlatt_LastName; //$NON-NLS-1$
	private static final String TEL_DIREKT = Messages.KontaktBlatt_OhoneDirect; //$NON-NLS-1$
	private static final String ANSPRECHPERSON = Messages.KontaktBlatt_ContactPerson; //$NON-NLS-1$
	private static final String ZUSATZ = Messages.KontaktBlatt_Addidtional; //$NON-NLS-1$
	private static final String BEZEICHNUNG = Messages.KontaktBlatt_Name; //$NON-NLS-1$
	static final String[] types = {
		"istOrganisation", "istLabor", "istPerson", "istPatient", IS_USER, "istMandant" //$NON-NLS-1$//$NON-NLS-2$//$NON-NLS-3$//$NON-NLS-4$//$NON-NLS-5$
	}; //$NON-NLS-6$
	static final String[] typLabels = {
		Messages.KontaktBlatt_Organization, Messages.KontaktBlatt_Laboratory,
		Messages.KontaktBlatt_Person, Messages.KontaktBlatt_Patient, Messages.KontaktBlatt_User,
		Messages.KontaktBlatt_Mandator
	}; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
	private final Button[] bTypes = new Button[types.length];
	private final TypButtonAdapter tba = new TypButtonAdapter();
	private final IViewSite site;
	private final ScrolledForm form;
	private final FormToolkit tk;
	AutoForm afDetails;
	
	static final InputData[] def = new InputData[] {
		new InputData(Messages.KontaktBlatt_Bez1, Kontakt.FLD_NAME1, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Bez2, Kontakt.FLD_NAME2, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Bez3, Kontakt.FLD_NAME3, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Sex, Person.SEX, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_LawCode, Person.FLD_TITLE_SUFFIX, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Street, Kontakt.FLD_STREET, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Zip, Kontakt.FLD_ZIP, Typ.STRING, null, 6),
		new InputData(Messages.KontaktBlatt_Place, Kontakt.FLD_PLACE, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Country, Kontakt.FLD_COUNTRY, Typ.STRING, null, 3),
		new InputData(Messages.KontaktBlatt_XMLName, Patient.FLD_ALLERGIES, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Phone1, Kontakt.FLD_PHONE1, Typ.STRING, null, 30),
		new InputData(Messages.KontaktBlatt_Phone2, Kontakt.FLD_PHONE2, Typ.STRING, null, 30),
		new InputData(Messages.KontaktBlatt_Mobile, Kontakt.FLD_MOBILEPHONE, Typ.STRING, null, 30),
		new InputData(Messages.KontaktBlatt_Fax, Kontakt.FLD_FAX, Typ.STRING, null, 30),
		new InputData(Messages.KontaktBlatt_MediportSupport, Patient.FLD_GROUP, Typ.CHECKBOX, null),
		new InputData(Messages.KontaktBlatt_Mail, Kontakt.FLD_E_MAIL, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_www, Kontakt.FLD_WEBSITE, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_shortLabel, Kontakt.FLD_SHORT_LABEL, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_remark, Kontakt.FLD_REMARK, Typ.STRING, null),
		new InputData(Messages.KontaktBlatt_Bez1, Kontakt.FLD_NAME1, Typ.STRING, null), // helper field (non-visible) but needs a resolvable value to avoid exception
		new InputData(Messages.KontaktBlatt_title, Person.TITLE, Typ.STRING, null), new InputData(
			Messages.KontaktBlatt_extid, "UUID", new LabeledInputField.IContentProvider() { //$NON-NLS-1$ //$NON-NLS-2$
				
				public void displayContent(PersistentObject po, InputData ltf){
					StringBuilder sb = new StringBuilder();
					IXid xid = po.getXid();
					String dom = Xid.getSimpleNameForXIDDomain(xid.getDomain());
					sb.append(dom).append(": ").append(xid.getDomainId()); //$NON-NLS-1$
					ltf.setText(sb.toString());
				}
				
				public void reloadContent(PersistentObject po, InputData ltf){
					ArrayList<String> extFlds = new ArrayList<String>();
					Kontakt k = (Kontakt) po;
					for (String dom : Xid.getXIDDomains()) {
						XIDDomain xd = Xid.getDomain(dom);
						if ((k.istPerson() && xd.isDisplayedFor(Person.class))
							|| (k.istOrganisation() && xd.isDisplayedFor(Organisation.class))) {
							extFlds.add(Xid.getSimpleNameForXIDDomain(dom) + "=" + dom); //$NON-NLS-1$
						} else if (k.istOrganisation() && xd.isDisplayedFor(Labor.class)) {
							extFlds.add(Xid.getSimpleNameForXIDDomain(dom) + "=" + dom);
						}
					}
					KontaktExtDialog dlg = new KontaktExtDialog(UiDesk.getTopShell(), (Kontakt) po,
						extFlds.toArray(new String[0]));
					dlg.open();
					
				}
				
			}),
	};
	private Kontakt actKontakt;
	private final Label lbAnschrift;
	
	private ElexisEventListener eeli_kontakt = new ElexisUiEventListenerImpl(Kontakt.class) {
		public void runInUi(ElexisEvent ev){
			Kontakt kontakt = (Kontakt) ev.getObject();
			
			switch (ev.getType()) {
			case ElexisEvent.EVENT_SELECTED:
				Kontakt deselectedKontakt = actKontakt;
				setKontakt(kontakt);
				if (deselectedKontakt != null) {
					if (CoreHub.getLocalLockService().isLockedLocal(deselectedKontakt)) {
						CoreHub.getLocalLockService().releaseLock(deselectedKontakt);
					}
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);
					commandService.refreshElements(ToggleCurrentKontaktLockHandler.COMMAND_ID,
						null);
				}
				break;
			case ElexisEvent.EVENT_DESELECTED:
				setEnabled(false);
				break;
			case ElexisEvent.EVENT_LOCK_AQUIRED:
			case ElexisEvent.EVENT_LOCK_RELEASED:
				if (kontakt.equals(actKontakt)) {
					save();
					setUnlocked(ev.getType() == ElexisEvent.EVENT_LOCK_AQUIRED);
				}
				break;
			default:
				break;
			}
		}
	};
	
	public KontaktBlatt(Composite parent, int style, IViewSite vs){
		super(parent, style);
		site = vs;
		tk = UiDesk.getToolkit();
		setLayout(new FillLayout());
		form = tk.createScrolledForm(this);
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		Composite cTypes = tk.createComposite(body, SWT.BORDER);
		for (int i = 0; i < types.length; i++) {
			bTypes[i] = tk.createButton(cTypes, typLabels[i], SWT.CHECK);
			bTypes[i].addSelectionListener(tba);
			bTypes[i].setData(types[i]);
			if (types[i].equalsIgnoreCase(IS_USER)) {
				bTypes[i].setEnabled(false);
			}
		}
		cTypes.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cTypes.setLayout(new FillLayout());
		
		Composite bottom = tk.createComposite(body);
		bottom.setLayout(new FillLayout());
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		actKontakt = (Kontakt) ElexisEventDispatcher.getSelected(Kontakt.class);
		afDetails = new AutoForm(bottom, def);
		Composite cAnschrift = tk.createComposite(body);
		cAnschrift.setLayout(new GridLayout(2, false));
		cAnschrift.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		Hyperlink hAnschrift =
			tk.createHyperlink(cAnschrift, Messages.KontaktBlatt_Postal, SWT.NONE); //$NON-NLS-1$
		hAnschrift.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(HyperlinkEvent e){
				if (actKontakt != null) {
					new AnschriftEingabeDialog(getShell(), actKontakt).open();
					ElexisEventDispatcher.fireSelectionEvent(actKontakt);
				}
			}
			
		});
		lbAnschrift = tk.createLabel(cAnschrift, StringConstants.EMPTY, SWT.WRAP);
		lbAnschrift.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		setOrganisationFieldsVisible(false);
		def[19].getWidget().setVisible(false); //field is only added for UI presentation reasons
		GlobalEventDispatcher.addActivationListener(this, site.getPart());
		setUnlocked(false);
	}
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, site.getPart());
		super.dispose();
	}
	
	private final class TypButtonAdapter extends SelectionAdapter {
		ArrayList<String> alTypes = new ArrayList<String>();
		ArrayList<String> alValues = new ArrayList<String>();
		
		@Override
		public void widgetSelected(SelectionEvent e){
			Button b = (Button) e.getSource();
			String type = (String) b.getData();
			
			if (b.getSelection() == true) {
				if (type.equals("istOrganisation")) { //$NON-NLS-1$
					select("1", "x", "0", "0", "0", "0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					def[0].setLabel(BEZEICHNUNG);
					def[1].setLabel(ZUSATZ);
					def[2].setLabel(ANSPRECHPERSON);
					def[3].setText(""); //$NON-NLS-1$
					def[10].setLabel(TEL_DIREKT);
					setOrganisationFieldsVisible(true);
				} else if (type.equals("istLabor")) { //$NON-NLS-1$
					select("1", "1", "0", "0", "0", "0"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					def[0].setLabel(BEZEICHNUNG);
					def[1].setLabel(ZUSATZ);
					def[2].setLabel(Messages.KontaktBlatt_LabAdmin); //$NON-NLS-1$
					def[10].setLabel(TEL_DIREKT);
				} else {
					def[0].setLabel(NAME);
					def[1].setLabel(VORNAME);
					def[2].setLabel(ZUSATZ);
					def[10].setLabel(MOBIL);
					setOrganisationFieldsVisible(false);
					if ("istPerson".equals(type)) { //$NON-NLS-1$
						select("0", "0", "1", "x", "x", "x"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					} else if (type.equals("istPatient")) { //$NON-NLS-1$
						select("0", "0", "1", "1", "x", "x"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					} else if (type.equals(IS_USER)) { //$NON-NLS-1$
						select("0", "0", "1", "x", "1", "x"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					} else if (type.equals("istMandant")) { //$NON-NLS-1$
						select("0", "0", "1", "x", "1", "1"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$ //$NON-NLS-6$
					}
				}
			} else {
				actKontakt.set(type, "0"); //$NON-NLS-1$
			}
		}
		
		void select(String... fields){
			alTypes.clear();
			alValues.clear();
			for (int i = 0; i < fields.length; i++) {
				if (fields[i].equals("x")) { //$NON-NLS-1$
					continue;
				}
				alTypes.add(types[i]);
				alValues.add(fields[i]);
				bTypes[i].setSelection(fields[i].equals(StringConstants.ONE));
			}
			actKontakt.set(alTypes.toArray(new String[0]), alValues.toArray(new String[0]));
		}
	}
	
	private void setOrganisationFieldsVisible(boolean visible){
		def[4].getWidget().setVisible(visible);
		def[9].getWidget().setVisible(visible);
		def[14].getWidget().setVisible(visible);
	}
	
	public void activation(boolean mode){
		if (ElexisEventDispatcher.getSelected(Kontakt.class) == null) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
		
	}
	
	private void setKontakt(Kontakt kontakt){
		if (!isEnabled()) {
			setEnabled(true);
		}
		actKontakt = kontakt;
		afDetails.reload(actKontakt);
		if (actKontakt != null) {
			String[] ret = new String[types.length];
			actKontakt.get(types, ret);
			for (int i = 0; i < types.length; i++) {
				bTypes[i]
					.setSelection((ret[i] == null) ? false : StringConstants.ONE.equals(ret[i]));
				if (CoreHub.acl.request(AccessControlDefaults.KONTAKT_MODIFY) == false) {
					bTypes[i].setEnabled(false);
				}
			}
			if (bTypes[3].getSelection() == true) {
				// isPatient
				def[17].getWidget().setEnabled(false);
			} else {
				def[17].getWidget().setEnabled(true);
			}
			if (bTypes[0].getSelection() == true) {
				// isOrganisation
				def[0].setLabel(BEZEICHNUNG);
				def[1].setLabel(ZUSATZ);
				def[2].setLabel(ANSPRECHPERSON);
				def[3].setEditable(false);
				def[3].setText(StringConstants.EMPTY);
				def[10].setLabel(TEL_DIREKT);
				setOrganisationFieldsVisible(true);
			} else {
				def[0].setLabel(NAME);
				def[1].setLabel(VORNAME);
				def[2].setLabel(ZUSATZ);
				def[3].setEditable(true);
				setOrganisationFieldsVisible(false);
			}
			lbAnschrift.setText(actKontakt.getPostAnschrift(false));
		}
		form.reflow(true);
		setUnlocked(CoreHub.getLocalLockService().isLockedLocal(kontakt));
	}
	
	public void visible(boolean mode){
		if (mode == true) {
			setKontakt((Kontakt) ElexisEventDispatcher.getSelected(Kontakt.class));
			ElexisEventDispatcher.getInstance().addListeners(eeli_kontakt);
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_kontakt);
		}
		
	}
	
	private final ElexisEvent eetemplate = new ElexisEvent(null, Kontakt.class,
		ElexisEvent.EVENT_SELECTED | ElexisEvent.EVENT_DESELECTED);
	
	public ElexisEvent getElexisEventFilter(){
		return eetemplate;
	}
	
	private void save(){
		afDetails.save();
	}
	
	@Override
	public void setUnlocked(boolean unlocked){
		afDetails.setUnlocked(unlocked);
	}
}
