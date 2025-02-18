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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.services.IEvaluationService;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IArticleDefaultSignature;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.builder.IPrescriptionBuilder;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.LocalLockServiceHolder;
import ch.elexis.core.services.holder.MedicationServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.dialogs.MediDetailDialog;
import ch.elexis.core.ui.dialogs.PrescriptionSignatureTitleAreaDialog;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.medication.handlers.PrintRecipeHandler;
import ch.elexis.core.ui.medication.handlers.PrintTakingsListHandler;
import ch.elexis.core.ui.util.CreatePrescriptionHelper;
import ch.elexis.core.ui.util.GenericObjectDragSource;
import ch.elexis.core.ui.util.GenericObjectDropTarget;
import ch.elexis.core.ui.util.ListDisplay;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.views.codesystems.LeistungenView;
import ch.elexis.core.ui.views.controls.InteractionLink;
import ch.rgw.tools.ExHandler;
import jakarta.inject.Inject;

/**
 * Display and let the user modify the medication of the currently selected
 * patient This is a pop-in-Replacement for DauerMediDisplay. To calculate the
 * daily cost wie accept the forms 1-1-1-1 and 1x1, 2x3 and so on
 *
 * @author gerry
 *
 */
public class FixMediDisplay extends ListDisplay<IPrescription> {
	public static final String ID = "ch.elexis.FixMediDisplay"; //$NON-NLS-1$
	private final LDListener dlisten;
	private IAction stopMedicationAction, changeMedicationAction, removeMedicationAction, addDefaultSignatureAction;
	FixMediDisplay self;
	Label lCost;
	InteractionLink interactionLink;
	GenericObjectDropTarget target;
	private MenuManager menuManager;
	private IViewSite viewSite;
	static final String REZEPT = Messages.FixMediDisplay_Prescription; // $NON-NLS-1$
	static final String LISTE = Messages.Core_List_ellipsis; // $NON-NLS-1$
	static final String HINZU = Messages.Core_Add; // $NON-NLS-1$
	static final String KOPIEREN = Messages.Core_Copy; // $NON-NLS-1$

	@Inject
	void activePatient(@Optional IPatient patient) {
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(list)) {
				reload();
			}
		});
	}

	@Optional
	@Inject
	void updatePrescription(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IPrescription prescription) {
		if (CoreUiUtil.isActiveControl(list)) {
			if (prescription != null) {
				reload();
			}
		}
	}

	@Inject
	void createPrescription(@Optional @UIEventTopic(ElexisEventTopics.EVENT_CREATE) IPrescription prescription) {
		updatePrescription(prescription);
	}

	@Inject
	void reloadPrescription(@Optional @UIEventTopic(ElexisEventTopics.EVENT_CREATE) Class<?> clazz) {
		if (clazz == IPrescription.class) {
			if (CoreUiUtil.isActiveControl(list)) {
				reload();
			}
		}
	}

	public FixMediDisplay(Composite parent, IViewSite viewSite) {
		super(parent, SWT.NONE, null);
		this.viewSite = viewSite;
		lCost = new Label(this, SWT.NONE);
		lCost.setText(Messages.FixMediDisplay_DailyCost);
		lCost.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		interactionLink = new InteractionLink(this, SWT.NONE);
		interactionLink.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));

		dlisten = new DauerMediListener(viewSite);
		self = this;
		addHyperlinks(HINZU, LISTE, REZEPT);
		makeActions();
		ViewMenus menu = new ViewMenus(viewSite);
		menu.createControlContextMenu(list, stopMedicationAction, changeMedicationAction, addDefaultSignatureAction,
				null, removeMedicationAction);
		menuManager = menu.getContextMenu();
		setDLDListener(dlisten);
		target = new GenericObjectDropTarget(Messages.Core_Fixed_medication, this,
				new GenericObjectDropTarget.IReceiver() {

					@Override
					public void dropped(List<Object> list, DropTargetEvent e) {
						for (Object object : list) {
							if (object instanceof IArticle) {
								CreatePrescriptionHelper prescriptionHelper = new CreatePrescriptionHelper(
										(IArticle) object, getShell());
								prescriptionHelper.setMedicationTypeFix(true);
								prescriptionHelper.createPrescription();

								ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD,
										IPrescription.class);
							} else if (object instanceof IPrescription) {
								List<IPrescription> existing = Collections.emptyList();
								java.util.Optional<IPatient> activePatient = ContextServiceHolder.get()
										.getActivePatient();
								if (activePatient.isPresent()) {
									existing = activePatient.get()
											.getMedication(Arrays.asList(EntryType.FIXED_MEDICATION,
													EntryType.RESERVE_MEDICATION, EntryType.SYMPTOMATIC_MEDICATION));
									IPrescription pre = (IPrescription) object;
									for (IPrescription pe : existing) {
										if (pe.equals(pre)) {
											return;
										}
									}
									IPrescription prescription = new IPrescriptionBuilder(CoreModelServiceHolder.get(),
											ContextServiceHolder.get(), pre.getArticle(), activePatient.get(),
											pre.getDosageInstruction()).build();
									prescription.setRemark(pre.getRemark());
									CoreModelServiceHolder.get().save(prescription);
									ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_RELOAD,
											IPrescription.class);
								}
							}
						}
					}

					@Override
					public boolean accept(List<Object> list) {
						for (Object object : list) {
							if (!(object instanceof IPrescription) && !(object instanceof IArticle)) {
								return false;
							}
						}
						return true;
					}
				});

		new GenericObjectDragSource(list, new GenericObjectDragSource.ISelectionRenderer() {
			@Override
			public List<Object> getSelection() {
				IPrescription pr = FixMediDisplay.this.getSelection();
				if (pr != null) {
					return Collections.singletonList(pr);
				}
				return Collections.emptyList();
			}
		});

		list.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ContextServiceHolder.get().getRootContext().setTyped(getSelection());
			}
		});

		CoreUiUtil.injectServicesWithContext(this);
	}

	public MenuManager getMenuManager() {
		return menuManager;
	}

	public void sortList() {
		String[] items = list.getItems();
		Arrays.sort(items);
		list.removeAll();
		list.setItems(items);

		update();
		redraw();
	}

	public void reload() {
		if (!isDisposed()) {
			clear();
			java.util.Optional<IPatient> patient = ContextServiceHolder.get().getActivePatient();
			if (patient.isPresent()) {
				List<IPrescription> fix = patient.get().getMedication(Arrays.asList(EntryType.FIXED_MEDICATION));
				fix.stream().forEach(p -> add(p));

				lCost.setText(MedicationViewHelper.calculateDailyCostAsString(fix));
				interactionLink.updateAtcs(MedicationViewHelper.getAllGtins(fix));
			}
			sortList();
		}
	}

	class DauerMediListener implements LDListener {
		IViewSite site;

		DauerMediListener(IViewSite s) {
			site = s;
		}

		public void hyperlinkActivated(String l) {
			try {
				if (l.equals(HINZU)) {
					site.getPage().showView(LeistungenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(target);
				} else if (l.equals(LISTE)) {
					HashMap<String, String> parameterMap = new HashMap<>();
					parameterMap.put("ch.elexis.core.ui.medication.commandParameter.medication", "fix"); //$NON-NLS-1$ //$NON-NLS-2$
					IEvaluationService evaluationService = (IEvaluationService) viewSite
							.getService(IEvaluationService.class);
					new PrintTakingsListHandler()
							.execute(new ExecutionEvent(null, parameterMap, null, evaluationService.getCurrentState()));
				} else if (l.equals(REZEPT)) {
					HashMap<String, String> parameterMap = new HashMap<>();
					parameterMap.put("ch.elexis.core.ui.medication.commandParameter.medication", "fix"); //$NON-NLS-1$ //$NON-NLS-2$
					IEvaluationService evaluationService = (IEvaluationService) viewSite
							.getService(IEvaluationService.class);
					new PrintRecipeHandler()
							.execute(new ExecutionEvent(null, parameterMap, null, evaluationService.getCurrentState()));
				} else if (l.equals(KOPIEREN)) {
					toClipBoard(true);
				}
			} catch (Exception ex) {
				ExHandler.handle(ex);
			}

		}

		public String getLabel(Object o) {
			if (o instanceof IPrescription) {
				IPrescription presc = (IPrescription) o;
				return (presc.getEntryType() == EntryType.RESERVE_MEDICATION) ? presc.getLabel() + " Res." //$NON-NLS-1$
						: presc.getLabel();
			}
			return o.toString();
		}
	}

	private void makeActions() {

		changeMedicationAction = new RestrictedAction(EvACE.of(IPrescription.class, Right.UPDATE),
				Messages.Core_DoChange_ellipsis) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.FixMediDisplay_Modify); // $NON-NLS-1$
			}

			public void doRun() {
				IPrescription pr = getSelection();
				if (pr != null) {
					MediDetailDialog md = new MediDetailDialog(getShell(), pr, true);
					md.setExecutedFrom(FixMediDisplay.class.getSimpleName());
					md.open();
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, pr);
				}
			}
		};

		stopMedicationAction = new RestrictedAction(EvACE.of(IPrescription.class, Right.UPDATE), Messages.Core_DoStop) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_REMOVEITEM.getImageDescriptor());
				setToolTipText(Messages.FixMediDisplay_StopThisMedicament); // $NON-NLS-1$
			}

			public void doRun() {
				IPrescription pr = getSelection();
				if (pr != null) {
					remove(pr);
					AcquireLockUi.aquireAndRun(pr, new ILockHandler() {
						@Override
						public void lockFailed() {
							// do nothing
						}

						@Override
						public void lockAcquired() {
							pr.setStopReason("Geändert durch " + CoreHub.getLoggedInContact().getLabel());
							CoreModelServiceHolder.get().delete(pr);
						}
					});
					ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, pr);
				}
			}
		};

		addDefaultSignatureAction = new Action(Messages.FixMediDisplay_AddDefaultSignature) {
			{
				setImageDescriptor(Images.IMG_BOOKMARK_PENCIL.getImageDescriptor());
				setToolTipText(Messages.FixMediDisplay_AddDefaultSignature_Tooltip);
			}

			@Override
			public void run() {
				IPrescription pr = getSelection();
				if (pr != null) {
					PrescriptionSignatureTitleAreaDialog adtad = new PrescriptionSignatureTitleAreaDialog(
							UiDesk.getTopShell(), pr.getArticle());
					adtad.setMedicationTypeFix(true);
					adtad.lookup();
					if (adtad.open() == Window.OK) {
						IArticleDefaultSignature signature = adtad.getSignature();
						if (signature != null) {
							applySignature(signature, pr);
						}
					}
				}
			}

			private void applySignature(IArticleDefaultSignature signature, IPrescription prescription) {
				AcquireLockBlockingUi.aquireAndRun(prescription, new ILockHandler() {
					@Override
					public void lockAcquired() {

						// creates a history entry for a prescription, stops the old one with current
						// date
						IPrescription oldPrescription = prescription;
						IPrescription newPrescription = MedicationServiceHolder.get()
								.createPrescriptionCopy(oldPrescription);
						if (LocalLockServiceHolder.get().acquireLock(newPrescription).isOk()) {

							newPrescription.setDosageInstruction(signature.getSignatureAsDosisString());
							newPrescription.setRemark(signature.getComment());
							newPrescription.setEntryType(signature.getMedicationType());
							CoreModelServiceHolder.get().save(newPrescription);

							MedicationServiceHolder.get().stopPrescription(oldPrescription, LocalDateTime.now(),
									"Geändert durch " + CoreHub.getLoggedInContact().getLabel());
							CoreModelServiceHolder.get().save(oldPrescription);
							LocalLockServiceHolder.get().releaseLock(newPrescription);

							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, oldPrescription);
							ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, newPrescription);
						}
					}

					@Override
					public void lockFailed() {
						// do nothing

					}
				});

			}
		};

		removeMedicationAction = new RestrictedAction(EvACE.of(IPrescription.class, Right.DELETE), Messages.Core_Delete) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.FixMediDisplay_DeleteUnrecoverable); // $NON-NLS-1$
			}

			public void doRun() {
				IPrescription pr = getSelection();
				if (pr != null) {
					if (MessageDialog.openQuestion(getShell(), Messages.FixMediDisplay_DeleteUnrecoverable,
							Messages.FixMediDisplay_DeleteUnrecoverable)) {
						remove(pr);
						AcquireLockUi.aquireAndRun(pr, new ILockHandler() {

							@Override
							public void lockFailed() {
								// do nothing
							}

							@Override
							public void lockAcquired() {
								CoreModelServiceHolder.get().remove(pr);
							}
						});
						ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, pr);
					}
				}
			}
		};

	}
}
