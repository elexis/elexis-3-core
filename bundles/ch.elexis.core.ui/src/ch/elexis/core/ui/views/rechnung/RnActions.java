/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.views.rechnung;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.ui.commands.Handler;
import ch.elexis.core.ui.commands.MahnlaufCommand;
import ch.elexis.core.ui.constants.UiResourceConstants;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AllOrNoneLockRequestingAction;
import ch.elexis.core.ui.locks.AllOrNoneLockRequestingRestrictedAction;
import ch.elexis.core.ui.locks.LockRequestingAction;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceActions;
import ch.elexis.data.AccountTransaction;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;

/**
 * Collection of bill-related actions
 *
 * @author gerry
 *
 */
public class RnActions {
	/**
	 * @deprecated please replace with {@link InvoiceActions}
	 */
	@Deprecated
	Action rnExportAction, increaseLevelAction, addPaymentAction, addExpenseAction, changeStatusAction, stornoAction,
			addAccountExcessAction;
	Action editCaseAction, delRnAction, reactivateRnAction, patDetailAction;
	Action expandAllAction, collapseAllAction, reloadAction, mahnWizardAction;
	Action printListeAction, exportListAction, rnFilterAction;

	RnActions(final RechnungsListeView view) {

		printListeAction = new Action(Messages.Core_Print_List) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText(Messages.RnActions_printListTooltip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				Object[] sel = view.cv.getSelection();
				new RnListeDruckDialog(view.getViewSite().getShell(), sel).open();
			}
		};
		mahnWizardAction = new Action(Messages.Invoice_reminder_automatism) { // $NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_remindersTooltip); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
			}

			@Override
			public void run() {
				if (!MessageDialog.openConfirm(view.getViewSite().getShell(), Messages.RnActions_reminderConfirmCaption, // $NON-NLS-1$
						Messages.RnActions_reminderConfirmMessage)) { // $NON-NLS-1$
					return;
				}
				Handler.execute(view.getViewSite(), MahnlaufCommand.ID, null);
				view.cfp.clearValues();
				view.cfp.cbStat.setText(RnControlFieldProvider.stats[RnControlFieldProvider.stats.length - 5]);
				view.cfp.fireChangedEvent();
			}
		};
		rnExportAction = new Action(Messages.RechnungsListeView_printAction) { // $NON-NLS-1$
			{
				setToolTipText(Messages.RechnungsListeView_printToolTip); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_GOFURTHER.getImageDescriptor());
			}

			@Override
			public void run() {
				List<Rechnung> list = view.createList();
				new RnOutputDialog(view.getViewSite().getShell(), list).open();
			}
		};

		patDetailAction = new Action(Messages.Core_Patientdetails) { // $NON-NLS-1$
			@Override
			public void run() {
				IWorkbenchPage rnPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					/* PatientDetailView fdv=(PatientDetailView) */rnPage
							.showView(UiResourceConstants.PatientDetailView2_ID);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}

		};
		editCaseAction = new Action(Messages.Core_Edit_Case) { // $NON-NLS-1$

			@Override
			public void run() {
				IWorkbenchPage rnPage = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				try {
					rnPage.showView(FallDetailView.ID);
				} catch (Exception ex) {
					ExHandler.handle(ex);
				}
			}

		};
		delRnAction = new AllOrNoneLockRequestingAction<Rechnung>(Messages.RnActions_deleteBillAction) {

			@Override
			public List<Rechnung> getTargetedObjects() {
				return view.createList();
			}

			@Override
			public void doRun(List<Rechnung> lockedElements) {
				for (Rechnung rn : lockedElements) {
					rn.storno(true);
				}
			}
		};
		reactivateRnAction = new AllOrNoneLockRequestingAction<Rechnung>(Messages.RnActions_reactivateBillAction) {

			@Override
			public List<Rechnung> getTargetedObjects() {
				return view.createList();
			}

			@Override
			public void doRun(List<Rechnung> lockedElements) {
				for (Rechnung rn : lockedElements) {
					rn.setStatus(InvoiceState.OPEN);
				}
			}
		};
		expandAllAction = new Action(Messages.Core_Expand_All) { // $NON-NLS-1$
			@Override
			public void run() {
				view.cv.getViewerWidget().getControl().setRedraw(false);
				((TreeViewer) view.cv.getViewerWidget()).expandAll();
				view.cv.getViewerWidget().getControl().setRedraw(true);
			}
		};
		collapseAllAction = new Action(Messages.RnActions_collapseAllAction) { // $NON-NLS-1$
			@Override
			public void run() {
				view.cv.getViewerWidget().getControl().setRedraw(false);
				((TreeViewer) view.cv.getViewerWidget()).collapseAll();
				view.cv.getViewerWidget().getControl().setRedraw(true);
			}
		};
		reloadAction = new Action(Messages.Core_Reload) { // $NON-NLS-1$
			{
				setToolTipText(Messages.Core_Reread_List); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
			}

			@Override
			public void run() {
				view.cfp.fireChangedEvent();
			}
		};

		addPaymentAction = new Action(Messages.Invoice_Add_Payment) { // $NON-NLS-1$
			{
				setToolTipText(Messages.Invoice_add_amount_as_payment); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
			}

			@Override
			public void run() {
				List<Rechnung> list = view.createList();
				if (!list.isEmpty()) {
					Rechnung actRn = list.get(0);
					try {
						if (new RnDialogs.BuchungHinzuDialog(view.getViewSite().getShell(), actRn)
								.open() == Dialog.OK) {
							ElexisEventDispatcher.update(actRn);
						}
					} catch (ElexisException e) {
						SWTHelper.showError("Zahlung hinzufügen ist nicht möglich", e.getLocalizedMessage());
					}
				}
			}
		};

		addExpenseAction = new Action(Messages.RnActions_addFineAction) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_REMOVEITEM.getImageDescriptor());
			}

			@Override
			public void run() {
				List<Rechnung> list = view.createList();
				if (!list.isEmpty()) {
					try {
						if (list.size() == 1) {
							Rechnung actRn = list.get(0);
							if (new RnDialogs.GebuehrHinzuDialog(view.getViewSite().getShell(), actRn)
									.open() == Dialog.OK) {
								ElexisEventDispatcher.update(actRn);
							}
						} else {
							if (new RnDialogs.MultiGebuehrHinzuDialog(view.getViewSite().getShell(), list)
									.open() == Dialog.OK) {
								for (Rechnung rn : list) {
									ElexisEventDispatcher.update(rn);
								}
							}
						}
					} catch (ElexisException e) {
						SWTHelper.showError("Zahlung hinzufügen ist nicht möglich", e.getLocalizedMessage());
					}
				}
			}
		};

		changeStatusAction = new AllOrNoneLockRequestingRestrictedAction<Rechnung>(
				EvACE.of(IInvoice.class, Right.UPDATE), Messages.RnActions_changeStateAction) { // $NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_changeStateTooltip); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
			}

			@Override
			public List<Rechnung> getTargetedObjects() {
				return view.createList();
			}

			@Override
			public void doRun(List<Rechnung> list) {
				if (list.size() == 1) {
					Rechnung actRn = list.get(0);
					if (new RnDialogs.StatusAendernDialog(view.getViewSite().getShell(), actRn).open() == Dialog.OK) {
						ElexisEventDispatcher.update(actRn);
					}
				} else {
					if (new RnDialogs.MultiStatusAendernDialog(view.getViewSite().getShell(), list)
							.open() == Dialog.OK) {
						for (Rechnung rn : list) {
							ElexisEventDispatcher.update(rn);
						}
					}
				}
			}
		};
		stornoAction = new LockRequestingAction<Rechnung>(Messages.RnActions_stornoAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.RnActions_stornoActionTooltip);
			}

			@Override
			public Rechnung getTargetedObject() {
				List<Rechnung> list = view.createList();
				if (!list.isEmpty()) {
					return list.get(0);
				}
				return null;
			}

			@Override
			public void doRun(Rechnung actRn) {
				if (new RnDialogs.StornoDialog(view.getViewSite().getShell(), actRn).open() == Dialog.OK) {
					ElexisEventDispatcher.update(actRn);
				}
			}
		};
		increaseLevelAction = new Action(Messages.RnActions_increaseReminderLevelAction) { // $NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_increadeReminderLevelTooltip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				List<Rechnung> list = view.createList();
				if (!list.isEmpty()) {
					for (Rechnung actRn : list) {
						InvoiceState invoiceState = actRn.getInvoiceState();
						switch (invoiceState) {
						case OPEN_AND_PRINTED -> actRn.setStatus(InvoiceState.DEMAND_NOTE_1);
						case DEMAND_NOTE_1_PRINTED -> actRn.setStatus(InvoiceState.DEMAND_NOTE_2);
						case DEMAND_NOTE_2_PRINTED -> actRn.setStatus(InvoiceState.DEMAND_NOTE_3);
						default -> SWTHelper.showInfo(Messages.RnActions_changeStateErrorCaption,
								Messages.RnActions_changeStateErrorMessage);
						}
					}
				}

			}
		};
		addAccountExcessAction = new Action(Messages.RnActions_addAccountGood) { // $NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_addAccountGoodTooltip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				List<Rechnung> list = view.createList();
				if (!list.isEmpty()) {
					Rechnung actRn = list.get(0);

					// Allfaelliges Guthaben des Patienten der Rechnung als
					// Anzahlung hinzufuegen
					Fall fall = actRn.getFall();
					Patient patient = fall.getPatient();
					Money prepayment = patient.getAccountExcess();
					if (prepayment.getCents() > 0) {
						// make sure prepayment is not bigger than amount of
						// bill
						Money amount;
						if (prepayment.getCents() > actRn.getBetrag().getCents()) {
							amount = new Money(actRn.getBetrag());
						} else {
							amount = new Money(prepayment);
						}

						if (SWTHelper.askYesNo(Messages.RnActions_transferMoneyCaption, // $NON-NLS-1$
								"Das Konto von Patient \"" + patient.getLabel()
										+ "\" weist ein positives Kontoguthaben auf. Wollen Sie den Betrag von "
										+ amount.toString() + " dieser Rechnung \"" + actRn.getNr() + ": " //$NON-NLS-2$
										+ fall.getLabel() + "\" zuweisen?")) {

							// remove amount from account and transfer it to the
							// bill
							Money accountAmount = new Money(amount);
							accountAmount.negate();
							new AccountTransaction(patient, null, accountAmount, null,
									"Anzahlung von Kontoguthaben auf Rechnung " + actRn.getNr());
							actRn.addZahlung(amount, "Anzahlung von Kontoguthaben", null);
						}
					}
				}
			}
		};
		rnFilterAction = new Action(Messages.Core_Filter_List, Action.AS_CHECK_BOX) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
				setToolTipText(Messages.RnActions_filterLIstTooltip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				if (isChecked()) {
					RnFilterDialog rfd = new RnFilterDialog(view.getViewSite().getShell());
					if (rfd.open() == Dialog.OK) {
						view.cntp.setConstraints(rfd.ret);
						view.cfp.fireChangedEvent();
					}
				} else {
					view.cntp.setConstraints(null);
					view.cfp.fireChangedEvent();
				}

			}
		};
		exportListAction = new Action(Messages.RnActions_exportListAction) {
			{
				setToolTipText(Messages.RnActions_exportListTooltip);
				setImageDescriptor(Images.IMG_DOCUMENT_EXCEL.getImageDescriptor());
			}

			@Override
			public void run() {
				Object[] sel = view.cv.getSelection();
				new RnListeExportDialog(view.getViewSite().getShell(), sel).open();
			}
		};
	}
}
