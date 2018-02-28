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

package ch.elexis.core.ui.views;

import java.text.ParseException;
import java.util.Collections;
import java.util.List;

import org.eclipse.core.runtime.IStatus;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.KeyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.model.IDiagnose;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.locks.LockDeniedNoActionLockHandler;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.Artikel;
import ch.elexis.data.Eigenleistung;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.elexis.data.Verrechnet;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;
import ch.rgw.tools.StringTool;

public class VerrechnungsDisplay extends Composite implements IUnlockable {
	Table tVerr;
	TableViewer viewer;
	MenuManager contextMenuManager;
	private String defaultRGB;
	private IWorkbenchPage page;
	private final Hyperlink hVer;
	private final PersistentObjectDropTarget dropTarget;
	private IAction applyMedicationAction, chPriceAction, chCountAction,
			chTextAction, removeAction,
			removeAllAction;
	private static final String INDICATED_MEDICATION = Messages.VerrechnungsDisplay_indicatedMedication;
	private static final String APPLY_MEDICATION = Messages.VerrechnungsDisplay_applyMedication;
	private static final String CHPRICE = Messages.VerrechnungsDisplay_changePrice;
	private static final String CHCOUNT = Messages.VerrechnungsDisplay_changeNumber;
	private static final String REMOVE = Messages.VerrechnungsDisplay_removeElements;
	private static final String CHTEXT = Messages.VerrechnungsDisplay_changeText;
	private static final String REMOVEALL = Messages.VerrechnungsDisplay_removeAll;
	
	private final ElexisEventListener eeli_update = new ElexisUiEventListenerImpl(
		Konsultation.class, ElexisEvent.EVENT_UPDATE) {
		@Override
		public void runInUi(ElexisEvent ev){
			Konsultation actKons =
				(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			setLeistungen(actKons);
		}
	};
	
	public VerrechnungsDisplay(final IWorkbenchPage p, Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout());
		this.page = p;
		defaultRGB = UiDesk.createColor(new RGB(255, 255, 255));
		
		hVer =
			UiDesk.getToolkit().createHyperlink(this, Messages.VerrechnungsDisplay_billing,
				SWT.NONE); //$NON-NLS-1$
		hVer.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		hVer.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(HyperlinkEvent e){
				try {
					if (StringTool.isNothing(LeistungenView.ID)) {
						SWTHelper.alert(Messages.VerrechnungsDisplay_error, "LeistungenView.ID"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					page.showView(LeistungenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							Messages.VerrechnungsDisplay_errorStartingCodeWindow + ex.getMessage(),
							ex, ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		makeActions();
		tVerr = UiDesk.getToolkit().createTable(this, SWT.MULTI);
		tVerr.setLayoutData(new GridData(GridData.FILL_BOTH));
		tVerr.setMenu(createVerrMenu());
		// dummy table viewer needed for SelectionsProvider for Menu
		viewer = new TableViewer(tVerr);
		// add selection event to table which provides selection to ElexisEventDispatcher
		tVerr.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				TableItem[] selection = tVerr.getSelection();
				Verrechnet verrechnet = (Verrechnet) selection[0].getData();
				ElexisEventDispatcher.fireSelectionEvent(verrechnet);
			}
		});
		tVerr.addKeyListener(new KeyListener() {
			@Override
			public void keyReleased(KeyEvent e){}
			
			@Override
			public void keyPressed(KeyEvent e){
				if (e.keyCode == SWT.DEL) {
					if (tVerr.getSelectionIndices().length >= 1 && removeAction != null) {
						removeAction.run();
					}
				}
			}
		});
		dropTarget =
			new PersistentObjectDropTarget(Messages.VerrechnungsDisplay_doBill, tVerr,
				new DropReceiver()); //$NON-NLS-1$
		// refresh the table if a update to a Verrechnet occurs
		ElexisEventDispatcher.getInstance().addListeners(
			new ElexisUiEventListenerImpl(Verrechnet.class, ElexisEvent.EVENT_UPDATE) {
				@Override
				public void runInUi(ElexisEvent ev){
					Konsultation actKons =
						(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
					setLeistungen(actKons);
				}
			});
		
		ElexisEventDispatcher.getInstance().addListeners(eeli_update);
	}
	
	public void clear(){
		tVerr.removeAll();
		hVer.setText(Messages.VerrechnungsDisplay_billed + ")");
	}
	
	public void addPersistentObject(PersistentObject o){
		Konsultation actKons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
		if (actKons != null) {
			if (o instanceof Leistungsblock) {
				Leistungsblock block = (Leistungsblock) o;
				List<ICodeElement> elements = block.getElements();
				for (ICodeElement element : elements) {
					if (element instanceof PersistentObject) {
						addPersistentObject((PersistentObject) element);
					}
				}
				List<ICodeElement> diff = block.getDiffToReferences(elements);
				if (!diff.isEmpty()) {
					StringBuilder sb = new StringBuilder();
					diff.forEach(r -> {
						if (sb.length() > 0) {
							sb.append("\n");
						}
						sb.append(r);
					});
					MessageDialog.openWarning(getShell(), "Warnung",
						"Warnung folgende Leistungen konnten im aktuellen Kontext (Fall, Konsultation, Gesetz) nicht verrechnet werden.\n"
							+ sb.toString());
				}
			}
			if (o instanceof Prescription) {
				Prescription presc = (Prescription) o;
				o = presc.getArtikel();
			}
			if (o instanceof IVerrechenbar) {
				if (CoreHub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN) == false) {
					SWTHelper.alert(Messages.VerrechnungsDisplay_missingRightsCaption, //$NON-NLS-1$
						Messages.VerrechnungsDisplay_missingRightsBody); //$NON-NLS-1$
				} else {
					Result<IVerrechenbar> result = actKons.addLeistung((IVerrechenbar) o);
					
					if (!result.isOK()) {
						SWTHelper.alert(Messages.VerrechnungsDisplay_imvalidBilling,
							result.toString()); //$NON-NLS-1$
					}
					setLeistungen(actKons);
				}
			} else if (o instanceof IDiagnose) {
				actKons.addDiagnose((IDiagnose) o);
			}
		}
	}
	
	private final class DropReceiver implements PersistentObjectDropTarget.IReceiver {
		public void dropped(PersistentObject o, DropTargetEvent ev){
			if (accept(o)) {
				addPersistentObject(o);
			}
		}
		
		public boolean accept(PersistentObject o){
			if (ElexisEventDispatcher.getSelectedPatient() != null) {
				if (o instanceof Artikel) {
					return !((Artikel) o).isProduct();
				}
				if (o instanceof IVerrechenbar) {
					return true;
				}
				if (o instanceof IDiagnose) {
					return true;
				}
				if (o instanceof Leistungsblock) {
					return true;
				}
				if (o instanceof Prescription) {
					Prescription p = ((Prescription) o);
					return (p.getArtikel() != null && !p.getArtikel().isProduct());
				}
			}
			return false;
		}
	}
	
	public void setLeistungen(Konsultation b){
		List<Verrechnet> lgl = Collections.emptyList();
		if (b != null) {
			lgl = b.getLeistungen();
		}
		tVerr.setRedraw(false);
		tVerr.removeAll();
		StringBuilder sdg = new StringBuilder();
		Money sum = new Money(0);
		for (Verrechnet lst : lgl) {
			sdg.setLength(0);
			int z = lst.getZahl();
			Money preis = lst.getNettoPreis().multiply(z);
			sum.addMoney(preis);
			sdg.append(z).append(" ").append(getServiceCode(lst)).append(" ").append(lst.getText()) //$NON-NLS-1$ //$NON-NLS-2$
				.append(" (").append(preis.getAmountAsString()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			TableItem ti = new TableItem(tVerr, SWT.WRAP);
			ti.setText(sdg.toString());
			ti.setData(lst);
			
			IVerrechenbar vr = lst.getVerrechenbar();
			if (vr instanceof Artikel) {
				Artikel a = (Artikel) vr;
				int abgabeEinheit = a.getAbgabeEinheit();
				if (abgabeEinheit > 0 && abgabeEinheit < a.getPackungsGroesse()) {
					ti.setImage(Images.IMG_BLOCKS_SMALL.getImage());
				}
			}
			
			// set table item color
			IVerrechenbar verrBar = lst.getVerrechenbar();
			if (verrBar != null) {
				Color color = UiDesk.getColorFromRGB(defaultRGB);
				String codeName = verrBar.getCodeSystemName();
				
				if (codeName != null) {
					String rgbColor =
						CoreHub.globalCfg.get(Preferences.LEISTUNGSCODES_COLOR + codeName,
							defaultRGB);
					color = UiDesk.getColorFromRGB(rgbColor);
				}
				ti.setBackground(color);
			}
		}
		tVerr.setRedraw(true);
		sdg.setLength(0);
		sdg.append(Messages.VerrechnungsDisplay_billed).append(sum.getAmountAsString()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
		hVer.setText(sdg.toString());
	}
	
	/**
	 * Filter codes of {@link Verrechnet} where ID is used as code. This is relevant for {@link Eigenleistung} and Eigenartikel.
	 * 
	 * @param lst
	 * @return
	 */
	private String getServiceCode(Verrechnet verrechnet){
		String ret = verrechnet.getCode();
		IVerrechenbar verrechenbar = verrechnet.getVerrechenbar();
		if (verrechenbar != null) {
			if (verrechenbar instanceof Eigenleistung || (verrechenbar instanceof Artikel
				&& "Eigenartikel".equals(((Artikel) verrechenbar).get(Artikel.FLD_TYP)))) {
				if (verrechenbar.getId().equals(ret)) {
					ret = "";
				}
			}
		}
		return ret;
	}
	
	private Menu createVerrMenu(){
		contextMenuManager = new MenuManager();
		contextMenuManager.setRemoveAllWhenShown(true);
		contextMenuManager.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				int[] selIndices = tVerr.getSelectionIndices();
				if (selIndices.length > 1) {
					manager.add(removeAction);
				} else {
					int sel = tVerr.getSelectionIndex();
					if (sel != -1) {
						TableItem ti = tVerr.getItem(sel);
						Verrechnet v = (Verrechnet) ti.getData();
						IVerrechenbar verrechenbar = v.getVerrechenbar();
						
						manager.add(chPriceAction);
						manager.add(chCountAction);
						IVerrechenbar vbar = v.getVerrechenbar();
						List<IAction> itemActions = (List<IAction>) (List<?>) vbar.getActions(v);
						if ((itemActions != null) && (itemActions.size() > 0)) {
							manager.add(new Separator());
							for (IAction a : itemActions) {
								if (a != null) {
									manager.add(a);
								}
							}
						}
						manager.add(new Separator());
						manager.add(chTextAction);
						manager.add(removeAction);
						manager.add(new Separator());
						manager.add(removeAllAction);
						if (verrechenbar instanceof Artikel) {
							manager.add(new Separator());
							manager.add(applyMedicationAction);
							// #8796
							manager.add(new Action(INDICATED_MEDICATION, Action.AS_CHECK_BOX) {
								@Override
								public void run(){
									Verrechnet v = loadSelectedVerrechnet();
									AcquireLockUi.aquireAndRun(v,
										new LockDeniedNoActionLockHandler() {
											
											@Override
											public void lockAcquired(){
												if (isIndicated()) {
													v.setDetail(Verrechnet.INDICATED, "false");
												} else {
													v.setDetail(Verrechnet.INDICATED, "true");
												}
											}
										});
									
								}
								
								private boolean isIndicated(){
									Verrechnet v = loadSelectedVerrechnet();
									String value = v.getDetail(Verrechnet.INDICATED);
									return "true".equalsIgnoreCase(value);
								}
								
								@Override
								public boolean isChecked(){
									return isIndicated();
								}
							});
						}
					}
				}
			}
		});
		return contextMenuManager.createContextMenu(tVerr);
	}
	
	private void makeActions(){
		// #3278
		applyMedicationAction = new Action(APPLY_MEDICATION) {
			@Override
			public void run(){
				Verrechnet v = loadSelectedVerrechnet();
				AcquireLockUi.aquireAndRun(v, new LockDeniedNoActionLockHandler() {

					@Override
					public void lockAcquired(){
						v.setDetail(Verrechnet.VATSCALE, Double.toString(0.0));
						
						int packungsGroesse = ((Artikel) v.getVerrechenbar()).getPackungsGroesse();
						String proposal = (packungsGroesse > 0) ? "1/" + packungsGroesse : "1";
						changeQuantityDialog(proposal, v);
						Object prescriptionId = v.getDetail(Verrechnet.FLD_EXT_PRESC_ID);
						if (prescriptionId instanceof String) {
							Prescription prescription = Prescription.load((String) prescriptionId);
							if (prescription.getEntryType() == EntryType.SELF_DISPENSED) {
								prescription.setApplied(true);
							}
						}
					}
				});

			}
			
			@Override
			public ImageDescriptor getImageDescriptor(){
				return Images.IMG_SYRINGE.getImageDescriptor();
			}
		};
		
		removeAction = new Action(REMOVE) {
			@Override
			public void run(){
				int[] sel = tVerr.getSelectionIndices();
				for (int i : sel) {
					TableItem ti = tVerr.getItem(i);
					Verrechnet v = (Verrechnet) ti.getData();
					AcquireLockUi.aquireAndRun(v, new LockDeniedNoActionLockHandler() {
						@Override
						public void lockAcquired(){
							Result<Verrechnet> result = ((Konsultation) ElexisEventDispatcher
								.getSelected(Konsultation.class)).removeLeistung(v);
							if (!result.isOK()) {
								SWTHelper.alert(
									Messages.VerrechnungsDisplay_PositionCanootBeRemoved, result //$NON-NLS-1$
										.toString());
							}
						}
						
					});
				}
				setLeistungen((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
			}
		};
		
		removeAllAction = new Action(REMOVEALL) {
			@Override
			public void run(){
				TableItem[] items = tVerr.getItems();
				for (TableItem ti : items) {
					Verrechnet v = (Verrechnet) ti.getData();
					if (!v.getKons().isEditable(true)) {
						return;
					}
					AcquireLockUi.aquireAndRun(v, new LockDeniedNoActionLockHandler() {
						@Override
						public void lockAcquired(){
							Result<Verrechnet> result = ((Konsultation) ElexisEventDispatcher
								.getSelected(Konsultation.class)).removeLeistung(v);
							if (!result.isOK()) {
								SWTHelper.alert(
									Messages.VerrechnungsDisplay_PositionCanootBeRemoved, result //$NON-NLS-1$
										.toString());
							}
						}
					});
					
				}
				setLeistungen((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
			}
		};
		
		chPriceAction = new Action(CHPRICE) {
			
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				
				if(!v.getKons().isEditable(true)) {
					return;
				}
				
				AcquireLockUi.aquireAndRun(v, new LockDeniedNoActionLockHandler() {

					@Override
					public void lockAcquired(){
						Money oldPrice = v.getBruttoPreis();
						String p = oldPrice.getAmountAsString();
						InputDialog dlg = new InputDialog(UiDesk.getTopShell(),
							Messages.VerrechnungsDisplay_changePriceForService, //$NON-NLS-1$
							Messages.VerrechnungsDisplay_enterNewPrice, p, //$NON-NLS-1$
							null);
						if (dlg.open() == Dialog.OK) {
							try {
								String val = dlg.getValue().trim();
								Money newPrice = new Money(oldPrice);
								if (val.endsWith("%") && val.length() > 1) { //$NON-NLS-1$
									val = val.substring(0, val.length() - 1);
									double percent = Double.parseDouble(val);
									double factor = 1.0 + (percent / 100.0);
									v.setSecondaryScaleFactor(factor);
								} else {
									newPrice = new Money(val);
									v.setTP(newPrice.getCents());
									v.setSecondaryScaleFactor(1);
									// mark as changed price
									v.setDetail(Verrechnet.FLD_EXT_CHANGEDPRICE, "true");
								}
								// v.setPreis(newPrice);
								setLeistungen(
									(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
							} catch (ParseException ex) {
								SWTHelper.showError(Messages.VerrechnungsDisplay_badAmountCaption, //$NON-NLS-1$
									Messages.VerrechnungsDisplay_badAmountBody); //$NON-NLS-1$
							}
						}
					}
					
				});
			}
			
		};
		
		chCountAction = new Action(CHCOUNT) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				
				if(!v.getKons().isEditable(true)) {
					return;
				}
				
				String p = Integer.toString(v.getZahl());
				AcquireLockUi.aquireAndRun(v, new LockDeniedNoActionLockHandler() {
					
					@Override
					public void lockAcquired(){
						changeQuantityDialog(p, v);
					}
				});
			}
		};
		
		chTextAction = new Action(CHTEXT) {
			@Override
			public void run(){
				int sel = tVerr.getSelectionIndex();
				TableItem ti = tVerr.getItem(sel);
				Verrechnet v = (Verrechnet) ti.getData();
				
				if(!v.getKons().isEditable(true)) {
					return;
				}
				
				AcquireLockUi.aquireAndRun(v, new LockDeniedNoActionLockHandler() {
					@Override
					public void lockAcquired(){
						String oldText = v.getText();
						InputDialog dlg = new InputDialog(UiDesk.getTopShell(),
							Messages.VerrechnungsDisplay_changeTextCaption, //$NON-NLS-1$
							Messages.VerrechnungsDisplay_changeTextBody, //$NON-NLS-1$
							oldText, null);
						if (dlg.open() == Dialog.OK) {
							String input = dlg.getValue();
							if (input.matches("[0-9\\.,]+")) { //$NON-NLS-1$
								if (!SWTHelper.askYesNo(
									Messages.VerrechnungsDisplay_confirmChangeTextCaption, //$NON-NLS-1$
									Messages.VerrechnungsDisplay_confirmChangeTextBody)) { //$NON-NLS-1$
									return;
								}
							}
							v.setText(input);
							setLeistungen((Konsultation) ElexisEventDispatcher
								.getSelected(Konsultation.class));
						}
					}
				});
			}
		};
	}
	
	private Verrechnet loadSelectedVerrechnet(){
		int sel = tVerr.getSelectionIndex();
		TableItem ti = tVerr.getItem(sel);
		return (Verrechnet) ti.getData();
	}
	
	private void changeQuantityDialog(String p, Verrechnet v){
		InputDialog dlg =
			new InputDialog(UiDesk.getTopShell(), Messages.VerrechnungsDisplay_changeNumberCaption, //$NON-NLS-1$
				Messages.VerrechnungsDisplay_changeNumberBody, //$NON-NLS-1$
				p, null);
		if (dlg.open() == Dialog.OK) {
			try {
				String val = dlg.getValue();
				if (!StringTool.isNothing(val)) {
					int changeAnzahl;
					double secondaryScaleFactor = 1.0;
					String text = v.getVerrechenbar().getText();
					
					if (val.indexOf(StringConstants.SLASH) > 0) {
						changeAnzahl = 1;
						String[] frac = val.split(StringConstants.SLASH);
						secondaryScaleFactor =
							Double.parseDouble(frac[0]) / Double.parseDouble(frac[1]);
						text = v.getText() + " (" + val //$NON-NLS-1$
							+ Messages.VerrechnungsDisplay_Orininalpackungen;
					} else if (val.indexOf('.') > 0) {
						changeAnzahl = 1;
						secondaryScaleFactor = Double.parseDouble(val);
						text = v.getText() + " (" + Double.toString(secondaryScaleFactor) + ")";
					} else {
						changeAnzahl = Integer.parseInt(dlg.getValue());
					}
					
					IStatus ret = v.changeAnzahlValidated(changeAnzahl);
					if (ret.isOK()) {
						v.setSecondaryScaleFactor(secondaryScaleFactor);
						v.setText(text);
					} else {
						SWTHelper.showError(Messages.VerrechnungsDisplay_error, ret.getMessage());
					}
				}
				setLeistungen((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
			} catch (NumberFormatException ne) {
				SWTHelper.showError(Messages.VerrechnungsDisplay_invalidEntryCaption, //$NON-NLS-1$
					Messages.VerrechnungsDisplay_invalidEntryBody); //$NON-NLS-1$
			}
		}
	}
	
	@Override
	public void setUnlocked(boolean unlocked) {
		setEnabled(unlocked);
		redraw();
	}
}
