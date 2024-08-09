package ch.elexis.core.ui.views.rechnung.invoice;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IViewSite;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.NoPoUtil;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.services.holder.InvoiceServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.commands.Handler;
import ch.elexis.core.ui.commands.MahnlaufCommand;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AllOrNoneLockRequestingAction;
import ch.elexis.core.ui.locks.AllOrNoneLockRequestingRestrictedAction;
import ch.elexis.core.ui.locks.MultiLockRequestingAction;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.rechnung.RnDialogs;
import ch.elexis.core.ui.views.rechnung.RnDialogs.StornoDialog;
import ch.elexis.core.ui.views.rechnung.RnListeDruckDialog;
import ch.elexis.core.ui.views.rechnung.RnOutputDialog;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListContentProvider.InvoiceEntry;
import ch.elexis.data.AccountTransaction;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class InvoiceActions {

	public Action addPaymentAction, rnExportAction, increaseLevelAction, addExpenseAction, stornoAction,
			stornoRecreateAction, addAccountExcessAction, printListeAction, mahnWizardAction, exportListAction,
			changeStatusAction, deleteAction, reactivateAction;

	private final StructuredViewer viewer;
	private final IViewSite iViewSite;

	public InvoiceActions(StructuredViewer structuredViewer, IViewSite iViewSite) {
		this.viewer = structuredViewer;
		this.iViewSite = iViewSite;

		rnExportAction = new Action(Messages.RechnungsListeView_printAction) {
			{
				setToolTipText(Messages.RechnungsListeView_printToolTip); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_GOFURTHER.getImageDescriptor());
			}

			@Override
			public void run() {
				List<Rechnung> invoiceSelections = getInvoiceSelections(viewer);
				new RnOutputDialog(UiDesk.getTopShell(), invoiceSelections).open();
			}
		};

		addPaymentAction = new Action(Messages.Invoice_Add_Payment) { // $NON-NLS-1$
			{
				setToolTipText(Messages.Invoice_add_amount_as_payment); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
			}

			@Override
			public void run() {
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (!list.isEmpty()) {
					Rechnung actRn = list.get(0);
					try {
						if (new RnDialogs.BuchungHinzuDialog(UiDesk.getTopShell(), actRn).open() == Dialog.OK) {
							ElexisEventDispatcher.update(actRn);
						}
					} catch (ElexisException e) {
						SWTHelper.showError("Zahlung hinzufügen ist nicht möglich", e.getLocalizedMessage());
					}
				}
			}
		};

		increaseLevelAction = new Action(Messages.RnActions_increaseReminderLevelAction) { // $NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_increadeReminderLevelTooltip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (!list.isEmpty()) {
					for (Rechnung actRn : list) {
						switch (actRn.getInvoiceState()) {
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

		addExpenseAction = new Action(Messages.RnActions_addFineAction) {
			{
				setImageDescriptor(Images.IMG_REMOVEITEM.getImageDescriptor());
			}

			@Override
			public void run() {
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (!list.isEmpty()) {
					try {
						if (list.size() == 1) {
							Rechnung actRn = list.get(0);
							if (new RnDialogs.GebuehrHinzuDialog(UiDesk.getTopShell(), actRn).open() == Dialog.OK) {
								ElexisEventDispatcher.update(actRn);
							}
						} else {
							if (new RnDialogs.MultiGebuehrHinzuDialog(UiDesk.getTopShell(), list).open() == Dialog.OK) {
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

		stornoAction = new MultiLockRequestingAction<List<Rechnung>>(Messages.RnActions_stornoAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.RnActions_stornoActionTooltip);
			}

			private int dialogResult = -1;
			private boolean dialogReopen = false;
			private List<IRnOutputter> dialogExporters;

			@Override
			public List<? extends PersistentObject> getTargetedObjects() {
				// reset dialog result for new selection
				dialogResult = -1;
				dialogReopen = false;
				return getInvoiceSelections(viewer);
			}

			@Override
			public void doRun(PersistentObject po) {
				Rechnung actRn = (Rechnung) po;
				// only show dialog for new selection
				if (dialogResult == -1) {
					StornoDialog stornoDialog = new RnDialogs.StornoDialog(UiDesk.getTopShell(), actRn);
					dialogResult = stornoDialog.open();
					dialogReopen = stornoDialog.getReopen();
					dialogExporters = stornoDialog.getSelectedExporters();
				} else if (dialogResult == Dialog.OK) {
					if (Rechnung.isStorno(actRn) || Rechnung.hasStornoBeforeDate(actRn, new TimeTool())) {
						SWTHelper.alert(Messages.RnActions_stornoAction,
								Messages.RnActions_stornoActionNotPossibleText);
					} else {
						NoPoUtil.loadAsIdentifiable(actRn, IInvoice.class).ifPresent(invoice -> {
							InvoiceServiceHolder.get().cancel(invoice, dialogReopen);
							if (dialogExporters != null) {
								for (IRnOutputter iro : dialogExporters) {
									iro.doOutput(IRnOutputter.TYPE.STORNO, Arrays.asList(new Rechnung[] { actRn }),
											new Properties());
								}
							}
						});
					}
				}
				ElexisEventDispatcher.update(actRn);
			}

		};

		stornoRecreateAction = new MultiLockRequestingAction<List<Rechnung>>(Messages.RnActions_stornoRecreateAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(Messages.RnActions_stornoRecreateActionTooltip);
			}

			private Map<Rechnung, List<Konsultation>> invoiceEncounterMap;

			@Override
			public List<? extends PersistentObject> getTargetedObjects() {
				List<Rechnung> ret = getInvoiceSelections(viewer);
				// collect encounters to recreate after storno
				invoiceEncounterMap = ret.stream()
						.collect(Collectors.toMap(Function.identity(), Rechnung::getKonsultationen));
				// use existing storno action before sequential recreate
				stornoAction.run();
				return ret;
			}

			@Override
			public void doRun(PersistentObject po) {
				Rechnung actRn = (Rechnung) po;
				List<Konsultation> actRnEncounters = invoiceEncounterMap.get(actRn);
				// test if cancelled and encounters open for new invoice
				if (actRn.getInvoiceState() == InvoiceState.CANCELLED) {
					if (actRnEncounters.stream().allMatch(e -> e.getRechnung() == null)) {
						InvoiceServiceHolder.get()
								.invoice(NoPoUtil.loadAsIdentifiable(actRnEncounters, IEncounter.class));
					}
				}
			}
		};

		addAccountExcessAction = new Action(Messages.RnActions_addAccountGood) {
			{
				setToolTipText(Messages.RnActions_addAccountGoodTooltip);
			}

			@Override
			public void run() {
				List<Rechnung> list = getInvoiceSelections(viewer);
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

		printListeAction = new Action(Messages.Core_Print_List) { // $NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText(Messages.RnActions_printListTooltip); // $NON-NLS-1$
			}

			@Override
			public void run() {
				List<Rechnung> invoiceSelections = getInvoiceSelections(viewer);
				new RnListeDruckDialog(UiDesk.getTopShell(), invoiceSelections).open();
			}
		};

		mahnWizardAction = new Action(Messages.Invoice_reminder_automatism) { // $NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_remindersTooltip); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
			}

			@Override
			public void run() {
				if (!MessageDialog.openConfirm(UiDesk.getTopShell(), Messages.RnActions_reminderConfirmCaption,
						Messages.RnActions_reminderConfirmMessage)) {
					return;
				}
				Handler.execute(iViewSite, MahnlaufCommand.ID, null);
			}
		};
		exportListAction = new Action(Messages.RnActions_exportListAction) {
			{
				setToolTipText(Messages.RnActions_exportListTooltip);
				setImageDescriptor(Images.IMG_PAGE_EXCEL.getImageDescriptor());
			}

			@Override
			public void run() {
				List<Rechnung> invoiceSelections = getInvoiceSelections(viewer);
				new RnDialogs.RnListeExportDialog(UiDesk.getTopShell(), invoiceSelections).open();
			}
		};

		changeStatusAction = new AllOrNoneLockRequestingRestrictedAction<Rechnung>(
				EvACE.of(IInvoice.class, Right.UPDATE), Messages.RnActions_changeStateAction) {
			{
				setToolTipText(Messages.RnActions_changeStateTooltip);
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
			}

			@Override
			public List<Rechnung> getTargetedObjects() {
				return getInvoiceSelections(viewer);
			}

			@Override
			public void doRun(List<Rechnung> list) {
				if (list.size() == 1) {
					Rechnung actRn = list.get(0);
					if (new RnDialogs.StatusAendernDialog(viewer.getControl().getShell(), actRn).open() == Dialog.OK) {
						ElexisEventDispatcher.update(actRn);
					}
				} else {
					if (new RnDialogs.MultiStatusAendernDialog(viewer.getControl().getShell(), list)
							.open() == Dialog.OK) {
						for (Rechnung rn : list) {
							ElexisEventDispatcher.update(rn);
						}
					}
				}
			}
		};

		deleteAction = new AllOrNoneLockRequestingAction<Rechnung>(Messages.RnActions_deleteBillAction) {

			@Override
			public List<Rechnung> getTargetedObjects() {
				return getInvoiceSelections(viewer);
			}

			@Override
			public void doRun(List<Rechnung> lockedElements) {
				for (Rechnung rn : lockedElements) {
					rn.stornoBill(true);
				}
			}
		};

		reactivateAction = new AllOrNoneLockRequestingAction<Rechnung>(Messages.RnActions_reactivateBillAction) {

			@Override
			public List<Rechnung> getTargetedObjects() {
				return getInvoiceSelections(viewer);
			}

			@Override
			public void doRun(List<Rechnung> lockedElements) {
				for (Rechnung rn : lockedElements) {
					rn.setStatus(InvoiceState.OPEN);
				}
			}
		};
	}

	private List<Rechnung> getInvoiceSelections(StructuredViewer viewer) {
		IStructuredSelection sel = (IStructuredSelection) viewer.getSelection();
		List<Rechnung> ret = new ArrayList<>();
		if (sel != null) {
			Object[] array = sel.toArray();
			for (Object object : array) {
				if (object instanceof Rechnung) {
					ret.add((Rechnung) object);
				} else if (object instanceof InvoiceEntry) {
					String invoiceId = ((InvoiceEntry) object).getInvoiceId();
					ret.add(Rechnung.load(invoiceId));
				}
			}
			return ret;
		}
		return Collections.emptyList();

	}

}
