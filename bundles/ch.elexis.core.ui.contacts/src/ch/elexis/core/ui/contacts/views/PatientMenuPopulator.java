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
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.dialogs.AssignStickerDialog;
import ch.elexis.core.ui.exchange.IDataSender;
import ch.elexis.core.ui.exchange.XChangeException;
import ch.elexis.core.ui.locks.LockRequestingRestrictedAction;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus.IMenuPopulator;
import ch.rgw.tools.ExHandler;

public class PatientMenuPopulator implements IMenuPopulator, IMenuListener {
	IAction exportKGAction, stickerAction;
	RestrictedAction delPatAction;
	PatientenListeView mine;

	@Override
	public IAction[] fillMenu() {
		LinkedList<IAction> ret = new LinkedList<>();
		ret.add(stickerAction);
		ret.add(delPatAction);
		if (AccessControlServiceHolder.get().evaluate(EvACE.of(IContact.class, Right.EXPORT))) {
			ret.add(exportKGAction);
		}
		delPatAction.reflectRight();
		exportKGAction.setEnabled(AccessControlServiceHolder.get().evaluate(EvACE.of(IContact.class, Right.EXPORT)));
		return ret.toArray(new IAction[0]);
	}

	PatientMenuPopulator(PatientenListeView plv, final StructuredViewer structuredViewer) {
		mine = plv;
		stickerAction = new RestrictedAction(EvACE.of(ISticker.class, Right.CREATE),
				Messages.Core_Sticker_ellipsis) { // $NON-NLS-1$
			{
				setToolTipText(Messages.PatientMenuPopulator_StickerToolTip); // $NON-NLS-1$
			}

			@Override
			public void doRun() {
				IPatient p = mine.getSelectedPatient();
				AssignStickerDialog aed = new AssignStickerDialog(Hub.getActiveShell(), p);
				aed.open();
			}

		};
		delPatAction = new LockRequestingRestrictedAction<IPatient>(EvACE.of(IContact.class, Right.DELETE),
				Messages.PatientMenuPopulator_DeletePatientAction) {

			@Override
			public void doRun(IPatient p) {
				if (MessageDialog.openConfirm(mine.getViewSite().getShell(),
						Messages.Core_Really_delete_caption, p.getLabel()) == true) {
					List<ICoverage> coverages = p.getCoverages();
					if (coverages.isEmpty()) {
						if (ContextServiceHolder.get().getActivePatient().isPresent()
								&& ContextServiceHolder.get().getActivePatient().get().getId().equals(p.getId())) {
							ContextServiceHolder.get().setActivePatient(null);
						}
						CoreModelServiceHolder.get().delete(p);
						mine.reload();
					} else {
						SWTHelper.alert(Messages.PatientMenuPopulator_DeletePatientRejectCaption,
								Messages.PatientMenuPopulator_DeletePatientRejectBody);
					}
				}
			}

			@Override
			public IPatient getTargetedObject() {
				return mine.getSelectedPatient();
			}
		};
		exportKGAction = new Action(Messages.PatientMenuPopulator_ExportEMRAction, Action.AS_DROP_DOWN_MENU) { // $NON-NLS-1$
			Menu menu = null;

			{
				setToolTipText(Messages.PatientMenuPopulator_ExportEMRToolTip); // $NON-NLS-1$
				setMenuCreator(new IMenuCreator() {

					@Override
					public void dispose() {
						if (menu != null) {
							menu.dispose();
							menu = null;
						}
					}

					@Override
					public Menu getMenu(Control parent) {
						menu = new Menu(parent);
						createMenu();
						return menu;
					}

					@Override
					public Menu getMenu(Menu parent) {
						menu = new Menu(parent);
						createMenu();
						return menu;
					}

				});
			}

			void createMenu() {
				IPatient p = mine.getSelectedPatient();
				if (p != null) {
					List<IConfigurationElement> list = Extensions.getExtensions(ExtensionPointConstantsUi.TRANSPORTER); // $NON-NLS-1$
					for (final IConfigurationElement ic : list) {
						// TODO "Acceptable Types" is not part of
						// ch.elexis.Transporter
						// never was?! Should we remove this code? - mde
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
								public void widgetSelected(SelectionEvent e) {
									IPatient pat = mine.getSelectedPatient();
									try {
										IDataSender sender = (IDataSender) ic
												.createExecutableExtension("ExporterClass"); //$NON-NLS-1$
										sender.store(pat);
										sender.finalizeExport();
										SWTHelper.showInfo(Messages.PatientMenuPopulator_EMRExported, // $NON-NLS-1$
												MessageFormat.format(Messages.PatientMenuPopulator_ExportEmrSuccess, // $NON-NLS-1$
														pat.getLabel()));
									} catch (CoreException e1) {
										ExHandler.handle(e1);
									} catch (XChangeException xx) {
										SWTHelper.showError(Messages.Core_Error, // $NON-NLS-1$
												MessageFormat.format(Messages.PatientMenuPopulator_ExportEmrFailure, // $NON-NLS-1$
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

	@Override
	public void menuAboutToShow(IMenuManager manager) {
		delPatAction.setEnabled(delPatAction.isEnabled());
		manager.update(true);
	}
}
