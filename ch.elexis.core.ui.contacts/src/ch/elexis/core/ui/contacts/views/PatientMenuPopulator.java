/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.contacts.views;

import java.text.MessageFormat;
import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.elexis.core.data.Patient;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.dialogs.AssignStickerDialog;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus.IMenuPopulator;
import ch.elexis.core.ui.views.Messages;
import ch.rgw.tools.ExHandler;

public class PatientMenuPopulator implements IMenuPopulator {
	IAction exportKGAction, delPatAction, stickerAction;
	PatientenListeView mine;
	
	public IAction[] fillMenu(){
		LinkedList<IAction> ret = new LinkedList<IAction>();
		ret.add(stickerAction);
		if (CoreHub.acl.request(AccessControlDefaults.KONTAKT_DELETE)) {
			ret.add(delPatAction);
		}
		if (CoreHub.acl.request(AccessControlDefaults.KONTAKT_EXPORT)) {
			ret.add(exportKGAction);
		}
		delPatAction.setEnabled(CoreHub.acl.request(AccessControlDefaults.KONTAKT_DELETE));
		exportKGAction.setEnabled(CoreHub.acl.request(AccessControlDefaults.KONTAKT_EXPORT));
		return ret.toArray(new IAction[0]);
	}
	
	PatientMenuPopulator(PatientenListeView plv){
		mine = plv;
		stickerAction =
			new RestrictedAction(AccessControlDefaults.KONTAKT_ETIKETTE,
				Messages.PatientMenuPopulator_StickerAction) { //$NON-NLS-1$
				{
					setToolTipText(Messages.PatientMenuPopulator_StickerToolTip); //$NON-NLS-1$
				}
				
				@Override
				public void doRun(){
					Patient p = mine.getSelectedPatient();
					AssignStickerDialog aed = new AssignStickerDialog(Hub.getActiveShell(), p);
					aed.open();
				}
				
			};
		delPatAction = new Action(Messages.PatientMenuPopulator_DeletePatientAction) { //$NON-NLS-1$
				@Override
				public void run(){
					// access rights guard
					if (!CoreHub.acl.request(AccessControlDefaults.KONTAKT_DELETE)) {
						SWTHelper.alert(Messages.PatientMenuPopulator_DeletePatientRefusalCaption,
							Messages.PatientMenuPopulator_DeletePatientRefusalBody); //$NON-NLS-1$ //$NON-NLS-2$
						return;
					}
					
					Patient p = mine.getSelectedPatient();
					if (p != null) {
						if (MessageDialog.openConfirm(mine.getViewSite().getShell(),
							Messages.PatientMenuPopulator_DeletePatientConfirm, p.getLabel()) == true) { //$NON-NLS-1$
							if (p.delete(false) == false) {
								SWTHelper.alert(
									Messages.PatientMenuPopulator_DeletePatientRejectCaption, //$NON-NLS-1$
									Messages.PatientMenuPopulator_DeletePatientRejectBody); //$NON-NLS-1$
							} else {
								mine.reload();
							}
						}
					}
				}
				
			};
		exportKGAction =
			new Action(Messages.PatientMenuPopulator_ExportEMRAction, Action.AS_DROP_DOWN_MENU) { //$NON-NLS-1$
				Menu menu = null;
				{
					setToolTipText(Messages.PatientMenuPopulator_ExportEMRToolTip); //$NON-NLS-1$
					setMenuCreator(new IMenuCreator() {
						
						public void dispose(){
							if (menu != null) {
								menu.dispose();
								menu = null;
							}
							
						}
						
						public Menu getMenu(Control parent){
							menu = new Menu(parent);
							createMenu();
							return menu;
						}
						
						public Menu getMenu(Menu parent){
							menu = new Menu(parent);
							createMenu();
							return menu;
						}
						
					});
				}
				
				void createMenu(){
					Patient p = mine.getSelectedPatient();
					if (p != null) {
						List<IConfigurationElement> list =
							Extensions.getExtensions("ch.elexis.Transporter"); //$NON-NLS-1$
						for (final IConfigurationElement ic : list) {
							String name = ic.getAttribute("name"); //$NON-NLS-1$
							System.out.println(name);
							String handler = ic.getAttribute("AcceptableTypes"); //$NON-NLS-1$
							if (handler == null) {
								continue;
							}
							if (handler.contains("ch.elexis.data.Patient") //$NON-NLS-1$
								|| (handler.contains("ch.elexis.data.*"))) { //$NON-NLS-1$
								MenuItem it = new MenuItem(menu, SWT.NONE);
								it.setText(ic.getAttribute("name")); //$NON-NLS-1$
								it.addSelectionListener(new SelectionAdapter() {
									@Override
									public void widgetSelected(SelectionEvent e){
										Patient pat = mine.getSelectedPatient();
										try {
											IDataSender sender =
												(IDataSender) ic
													.createExecutableExtension("ExporterClass"); //$NON-NLS-1$
											sender.store(pat);
											sender.finalizeExport();
											SWTHelper.showInfo(
												Messages.PatientMenuPopulator_EMRExported,//$NON-NLS-1$ 
												MessageFormat.format(
													Messages.PatientMenuPopulator_ExportEmrSuccess, //$NON-NLS-1$
													pat.getLabel()));
										} catch (CoreException e1) {
											ExHandler.handle(e1);
										} catch (XChangeException xx) {
											SWTHelper.showError(
												Messages.PatientMenuPopulator_ErrorCaption, //$NON-NLS-1$ 
												MessageFormat.format(
													Messages.PatientMenuPopulator_ExportEmrFailure, //$NON-NLS-1$
													pat.getLabel()));
											
										}
									}
								});
								
							}
						}
					}
				}
			};
	}
}
