/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/
package ch.elexis.core.ui.medication.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.services.IEvaluationService;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.dialogs.ArticleDefaultSignatureTitleAreaDialog;
import ch.elexis.core.ui.dialogs.MediDetailDialog;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.medication.handlers.PrintRecipeHandler;
import ch.elexis.core.ui.medication.handlers.PrintTakingsListHandler;
import ch.elexis.core.ui.util.ListDisplay;
import ch.elexis.core.ui.util.PersistentObjectDragSource;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.data.Artikel;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Prescription;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.TimeTool;

/**
 * Display and let the user modify the medication of the currently selected patient This is a
 * pop-in-Replacement for DauerMediDisplay. To calculate the daily cost wie accept the forms 1-1-1-1
 * and 1x1, 2x3 and so on
 *
 * @author gerry
 *
 */
public class FixMediDisplay extends ListDisplay<Prescription> {
	public static final String ID = "ch.elexis.FixMediDisplay";
	private final LDListener dlisten;
	private IAction stopMedicationAction, changeMedicationAction, removeMedicationAction,
			addDefaultSignatureAction;
	FixMediDisplay self;
	Label lCost;
	PersistentObjectDropTarget target;
	private MenuManager menuManager;
	private IViewSite viewSite;
	static final String REZEPT = Messages.FixMediDisplay_Prescription; //$NON-NLS-1$
	static final String LISTE = Messages.FixMediDisplay_UsageList; //$NON-NLS-1$
	static final String HINZU = Messages.FixMediDisplay_AddItem; //$NON-NLS-1$
	static final String KOPIEREN = Messages.FixMediDisplay_Copy; //$NON-NLS-1$
	
	public FixMediDisplay(Composite parent, IViewSite viewSite){
		super(parent, SWT.NONE, null);
		this.viewSite = viewSite;
		lCost = new Label(this, SWT.NONE);
		lCost.setText(Messages.FixMediDisplay_DailyCost);
		lCost.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		dlisten = new DauerMediListener(viewSite);
		self = this;
		addHyperlinks(HINZU, LISTE, REZEPT);
		makeActions();
		ViewMenus menu = new ViewMenus(viewSite);
		menu.createControlContextMenu(list, stopMedicationAction, changeMedicationAction,
			addDefaultSignatureAction, null, removeMedicationAction);
		menuManager = menu.getContextMenu();
		setDLDListener(dlisten);
		target = new PersistentObjectDropTarget(Messages.FixMediDisplay_FixMedikation, this, //$NON-NLS-1$
			new PersistentObjectDropTarget.IReceiver() {
				
				public boolean accept(PersistentObject o){
					if (o instanceof Prescription) {
						return true;
					}
					if (o instanceof Artikel) {
						return true;
					}
					return false;
				}
				
				public void dropped(PersistentObject o, DropTargetEvent e){
					
					if (o instanceof Artikel) {
						MediDetailDialog dlg = new MediDetailDialog(getShell(), (Artikel) o);
						if (dlg.open() == Window.OK) {
							Prescription prescription =
								new Prescription((Artikel) o, (Patient) ElexisEventDispatcher
									.getSelected(Patient.class), dlg.getDosis(), dlg.getIntakeOrder());
							// self.add(pre);
							AcquireLockUi.aquireAndRun(prescription, new ILockHandler() {
								@Override
								public void lockFailed(){
									prescription.remove();
								}
								
								@Override
								public void lockAcquired(){
									// do nothing
								}
							});
							reload();
						}
						
					} else if (o instanceof Prescription) {
						Prescription[] existing =
							((Patient) ElexisEventDispatcher.getSelected(Patient.class))
								.getFixmedikation();
						Prescription pre = (Prescription) o;
						for (Prescription pe : existing) {
							if (pe.equals(pre)) {
								return;
							}
						}
						Prescription prescription =
							new Prescription(pre.getArtikel(), ElexisEventDispatcher
							.getSelectedPatient(), pre.getDosis(), pre.getBemerkung());
						AcquireLockUi.aquireAndRun(prescription, new ILockHandler() {
							@Override
							public void lockFailed(){
								prescription.remove();
							}
							
							@Override
							public void lockAcquired(){
								// do nothing
							}
						});
						// self.add(now);
						reload();
					}
				}
			});
		new PersistentObjectDragSource(list, new PersistentObjectDragSource.ISelectionRenderer() {
			
			public List<PersistentObject> getSelection(){
				Prescription pr = FixMediDisplay.this.getSelection();
				ArrayList<PersistentObject> ret = new ArrayList<PersistentObject>(1);
				if (pr != null) {
					ret.add(pr);
				}
				return ret;
			}
		});
		
		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ElexisEventDispatcher.fireSelectionEvent(getSelection());
			}
		});
	}
	
	public MenuManager getMenuManager(){
		return menuManager;
	}
	
	public void sortList(){
		String[] items = list.getItems();
		Arrays.sort(items);
		list.removeAll();
		list.setItems(items);
		
		update();
		redraw();
	}
	
	public void reload(){
		clear();
		Patient act = ElexisEventDispatcher.getSelectedPatient();
		if (act != null) {
			List<Prescription> fix = Arrays.asList(act.getFixmedikation());
			TimeTool now = new TimeTool();
			for (Prescription pr : fix) {
				// skip stopped prescriptions 
				String endTimeStr = pr.getEndTime();
				if (!endTimeStr.isEmpty()) {
					TimeTool endTime = new TimeTool(endTimeStr);
					if (endTime.isBefore(now)) {
						continue;
					}
				}
				add(pr);
			}
			
			lCost.setText(MedicationViewHelper.calculateDailyCostAsString(fix));
		}
		sortList();
	}
	
	class DauerMediListener implements LDListener {
		IViewSite site;
		
		DauerMediListener(IViewSite s){
			site = s;
		}
		
		public void hyperlinkActivated(String l){
			try {
				if (l.equals(HINZU)) {
					site.getPage().showView(LeistungenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(target);
				} else if (l.equals(LISTE)) {
					IEvaluationService evaluationService =
						(IEvaluationService) viewSite.getService(IEvaluationService.class);
					new PrintTakingsListHandler().execute(new ExecutionEvent(null, new HashMap(),
						null, evaluationService.getCurrentState()));
				} else if (l.equals(REZEPT)) {
					IEvaluationService evaluationService =
						(IEvaluationService) viewSite.getService(IEvaluationService.class);
					new PrintRecipeHandler().execute(new ExecutionEvent(null, new HashMap(), null,
						evaluationService.getCurrentState()));
				} else if (l.equals(KOPIEREN)) {
					toClipBoard(true);
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}
			
		}
		
		public String getLabel(Object o){
			if (o instanceof Prescription) {
				return ((Prescription) o).getLabel();
			}
			return o.toString();
		}
	}
	
	private void makeActions(){
		
		changeMedicationAction =
			new RestrictedAction(AccessControlDefaults.MEDICATION_MODIFY,
				Messages.FixMediDisplay_Change) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
					setToolTipText(Messages.FixMediDisplay_Modify); //$NON-NLS-1$
				}
				
				public void doRun(){
					Prescription pr = getSelection();
					if (pr != null) {
						new MediDetailDialog(getShell(), pr).open();
						reload();
						redraw();
					}
				}
			};
		
		stopMedicationAction =
			new RestrictedAction(AccessControlDefaults.MEDICATION_MODIFY,
				Messages.FixMediDisplay_Stop) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_REMOVEITEM.getImageDescriptor());
					setToolTipText(Messages.FixMediDisplay_StopThisMedicament); //$NON-NLS-1$
				}
				
				public void doRun(){
					Prescription pr = getSelection();
					if (pr != null) {
						remove(pr);
						AcquireLockUi.aquireAndRun(pr, new ILockHandler() {
							@Override
							public void lockFailed(){
								// do nothing
							}
							
							@Override
							public void lockAcquired(){
								pr.delete(); // this does not delete but stop the Medication. Sorry for that
							}
						});
						reload();
					}
				}
			};
		
		addDefaultSignatureAction = new Action(Messages.FixMediDisplay_AddDefaultSignature) {
			{
				setImageDescriptor(Images.IMG_BOOKMARK_PENCIL.getImageDescriptor());
				setToolTipText(Messages.FixMediDisplay_AddDefaultSignature_Tooltip);
			}
			
			@Override
			public void run(){
				Prescription pr = getSelection();
				if (pr != null) {
					ArticleDefaultSignatureTitleAreaDialog adtad =
						new ArticleDefaultSignatureTitleAreaDialog(UiDesk.getTopShell(), pr);
					adtad.open();
				}
			}
		};
		
		removeMedicationAction =
			new RestrictedAction(AccessControlDefaults.DELETE_MEDICATION,
				Messages.FixMediDisplay_Delete) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
					setToolTipText(Messages.FixMediDisplay_DeleteUnrecoverable); //$NON-NLS-1$
				}
				
				public void doRun(){
					Prescription pr = getSelection();
					if (pr != null) {
						remove(pr);
						AcquireLockUi.aquireAndRun(pr, new ILockHandler() {
							
							@Override
							public void lockFailed(){
								// do nothing
							}
							
							@Override
							public void lockAcquired(){
								pr.remove(); // this does, in fact, remove the medication from the database
							}
						});
						reload();
					}
				}
			};
		
	}
	
}
