package ch.elexis.core.ui.views.rechnung.invoice;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.ui.IViewSite;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.commands.Handler;
import ch.elexis.core.ui.commands.MahnlaufCommand;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AllOrNoneLockRequestingAction;
import ch.elexis.core.ui.locks.AllOrNoneLockRequestingRestrictedAction;
import ch.elexis.core.ui.locks.LockRequestingAction;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.rechnung.Messages;
import ch.elexis.core.ui.views.rechnung.RnDialogs;
import ch.elexis.core.ui.views.rechnung.RnListeDruckDialog;
import ch.elexis.core.ui.views.rechnung.RnOutputDialog;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListContentProvider.InvoiceEntry;
import ch.elexis.data.AccountTransaction;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.rgw.tools.Money;

public class InvoiceActions {
	
	public Action addPaymentAction, rnExportAction, increaseLevelAction, addExpenseAction,
			stornoAction, addAccountExcessAction, printListeAction, mahnWizardAction,
			exportListAction, changeStatusAction, deleteAction, reactivateAction;
	
	private final StructuredViewer viewer;
	private final IViewSite iViewSite;
	
	public InvoiceActions(StructuredViewer structuredViewer, IViewSite iViewSite){
		this.viewer = structuredViewer;
		this.iViewSite = iViewSite;
		
		rnExportAction = new Action(Messages.RechnungsListeView_printAction) {
			{
				setToolTipText(Messages.RechnungsListeView_printToolTip); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_GOFURTHER.getImageDescriptor());
			}
			
			@Override
			public void run(){
				List<Rechnung> invoiceSelections = getInvoiceSelections(viewer);
				new RnOutputDialog(UiDesk.getTopShell(), invoiceSelections).open();
			}
		};
		
		addPaymentAction = new Action(Messages.RnActions_addBookingAction) { //$NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_addBookingTooltip); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
			}
			
			@Override
			public void run(){
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (list.size() > 0) {
					Rechnung actRn = list.get(0);
					try {
						if (new RnDialogs.BuchungHinzuDialog(UiDesk.getTopShell(), actRn)
							.open() == Dialog.OK) {
							ElexisEventDispatcher.update(actRn);
						}
					} catch (ElexisException e) {
						SWTHelper.showError("Zahlung hinzufügen ist nicht möglich",
							e.getLocalizedMessage());
					}
				}
			}
		};
		
		increaseLevelAction = new Action(Messages.RnActions_increaseReminderLevelAction) { //$NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_increadeReminderLevelTooltip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (list.size() > 0) {
					for (Rechnung actRn : list) {
						switch (actRn.getStatus()) {
						case RnStatus.OFFEN_UND_GEDRUCKT:
							actRn.setStatus(RnStatus.MAHNUNG_1);
							break;
						case RnStatus.MAHNUNG_1_GEDRUCKT:
							actRn.setStatus(RnStatus.MAHNUNG_2);
							break;
						case RnStatus.MAHNUNG_2_GEDRUCKT:
							actRn.setStatus(RnStatus.MAHNUNG_3);
							break;
						default:
							SWTHelper.showInfo(Messages.RnActions_changeStateErrorCaption,
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
			public void run(){
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (!list.isEmpty()) {
					try {
						if (list.size() == 1) {
							Rechnung actRn = list.get(0);
							if (new RnDialogs.GebuehrHinzuDialog(UiDesk.getTopShell(), actRn)
								.open() == Dialog.OK) {
								ElexisEventDispatcher.update(actRn);
							}
						} else {
							if (new RnDialogs.MultiGebuehrHinzuDialog(UiDesk.getTopShell(), list)
								.open() == Dialog.OK) {
								for (Rechnung rn : list) {
									ElexisEventDispatcher.update(rn);
								}
							}
						}
					} catch (ElexisException e) {
						SWTHelper.showError("Zahlung hinzufügen ist nicht möglich",
							e.getLocalizedMessage());
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
			public Rechnung getTargetedObject(){
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (!list.isEmpty()) {
					return list.get(0);
				}
				return null;
			}
			
			@Override
			public void doRun(Rechnung actRn){
				if (new RnDialogs.StornoDialog(UiDesk.getTopShell(), actRn).open() == Dialog.OK) {
					ElexisEventDispatcher.update(actRn);
				}
			}
		};
		
		addAccountExcessAction = new Action(Messages.RnActions_addAccountGood) {
			{
				setToolTipText(Messages.RnActions_addAccountGoodTooltip);
			}
			
			@Override
			public void run(){
				List<Rechnung> list = getInvoiceSelections(viewer);
				if (list.size() > 0) {
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
						
						if (SWTHelper.askYesNo(Messages.RnActions_transferMoneyCaption, //$NON-NLS-1$
							"Das Konto von Patient \"" + patient.getLabel()
								+ "\" weist ein positives Kontoguthaben auf. Wollen Sie den Betrag von "
								+ amount.toString() + " dieser Rechnung \"" + actRn.getNr() + ": "
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
		
		printListeAction = new Action(Messages.RnActions_printListAction) { //$NON-NLS-1$
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText(Messages.RnActions_printListTooltip); //$NON-NLS-1$
			}
			
			@Override
			public void run(){
				List<Rechnung> invoiceSelections = getInvoiceSelections(viewer);
				new RnListeDruckDialog(UiDesk.getTopShell(), invoiceSelections).open();
			}
		};
		
		mahnWizardAction = new Action(Messages.RnActions_remindersAction) { //$NON-NLS-1$
			{
				setToolTipText(Messages.RnActions_remindersTooltip); //$NON-NLS-1$
				setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
			}
			
			@Override
			public void run(){
				if (!MessageDialog.openConfirm(UiDesk.getTopShell(),
					Messages.RnActions_reminderConfirmCaption,
					Messages.RnActions_reminderConfirmMessage)) {
					return;
				}
				Handler.execute(iViewSite, MahnlaufCommand.ID, null);
			}
		};
		exportListAction = new Action(Messages.RnActions_exportListAction) {
			{
				setToolTipText(Messages.RnActions_exportListTooltip);
				setImageDescriptor(Images.IMG_EXPORT.getImageDescriptor());
			}
			
			@Override
			public void run(){
				List<Rechnung> invoiceSelections = getInvoiceSelections(viewer);
				new RnDialogs.RnListeExportDialog(UiDesk.getTopShell(), invoiceSelections).open();
			}
		};
		
		changeStatusAction = new AllOrNoneLockRequestingRestrictedAction<Rechnung>(
			AccessControlDefaults.ADMIN_CHANGE_BILLSTATUS_MANUALLY,
			Messages.RnActions_changeStateAction) {
			{
				setToolTipText(Messages.RnActions_changeStateTooltip);
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
			}
			
			@Override
			public List<Rechnung> getTargetedObjects(){
				return getInvoiceSelections(viewer);
			}
			
			@Override
			public void doRun(List<Rechnung> list){
				if (list.size() == 1) {
					Rechnung actRn = list.get(0);
					if (new RnDialogs.StatusAendernDialog(viewer.getControl().getShell(), actRn)
						.open() == Dialog.OK) {
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
		
		deleteAction =
			new AllOrNoneLockRequestingAction<Rechnung>(Messages.RnActions_deleteBillAction) {
				
				@Override
				public List<Rechnung> getTargetedObjects(){
					return getInvoiceSelections(viewer);
				}
				
				@Override
				public void doRun(List<Rechnung> lockedElements){
					for (Rechnung rn : lockedElements) {
						rn.stornoBill(true);
					}
				}
			};
		
		reactivateAction =
			new AllOrNoneLockRequestingAction<Rechnung>(Messages.RnActions_reactivateBillAction) {
				
				@Override
				public List<Rechnung> getTargetedObjects(){
					return getInvoiceSelections(viewer);
				}
				
				@Override
				public void doRun(List<Rechnung> lockedElements){
					for (Rechnung rn : lockedElements) {
						rn.setStatus(RnStatus.OFFEN);
					}
				}
			};
	}
	
	private List<Rechnung> getInvoiceSelections(StructuredViewer viewer){
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
