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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.data.AccountTransaction;
import ch.elexis.core.data.Brief;
import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Rechnung;
import ch.elexis.core.data.RnStatus;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.commands.Handler;
import ch.elexis.core.ui.commands.MahnlaufCommand;
import ch.elexis.core.ui.text.ITextPlugin.ICallback;
import ch.elexis.core.ui.text.TextContainer;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.FallDetailView;
import ch.elexis.core.ui.views.PatientDetailView2;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.Tree;

/**
 * Collection of bill-related actions
 * 
 * @author gerry
 * 
 */
public class RnActions {
	Action rnExportAction, editCaseAction, delRnAction, reactivateRnAction, patDetailAction;
	Action expandAllAction, collapseAllAction, reloadAction, mahnWizardAction;
	Action addPaymentAction, addExpenseAction, changeStatusAction, stornoAction;
	Action increaseLevelAction, printListeAction, rnFilterAction;
	Action addAccountExcessAction;
	
	RnActions(final RechnungsListeView view){
		
		printListeAction = new Action(Messages.getString("RnActions.printListAction")) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
					setToolTipText(Messages.getString("RnActions.printListTooltip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					Object[] sel = view.cv.getSelection();
					new RnListeDruckDialog(view.getViewSite().getShell(), sel).open();
				}
			};
		mahnWizardAction = new Action(Messages.getString("RnActions.remindersAction")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RnActions.remindersTooltip")); //$NON-NLS-1$
					setImageDescriptor(Images.IMG_WIZARD.getImageDescriptor());
				}
				
				@Override
				public void run(){
					if (!MessageDialog.openConfirm(view.getViewSite().getShell(),
						Messages.getString("RnActions.reminderConfirmCaption"), //$NON-NLS-1$
						Messages.getString("RnActions.reminderConfirmMessage"))) { //$NON-NLS-1$
						return;
					}
					Handler.execute(view.getViewSite(), MahnlaufCommand.ID, null);
					view.cfp.clearValues();
					view.cfp.cbStat
						.setText(RnControlFieldProvider.stats[RnControlFieldProvider.stats.length - 3]);
					view.cfp.fireChangedEvent();
				}
			};
		rnExportAction = new Action(Messages.getString("RechnungsListeView.printAction")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RechnungsListeView.printToolTip")); //$NON-NLS-1$
					setImageDescriptor(Images.IMG_GOFURTHER.getImageDescriptor());
				}
				
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					new RnOutputDialog(view.getViewSite().getShell(), list).open();
				}
			};
		
		patDetailAction = new Action(Messages.getString("RnActions.patientDetailsAction")) { //$NON-NLS-1$
				@Override
				public void run(){
					IWorkbenchPage rnPage =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						/* PatientDetailView fdv=(PatientDetailView) */rnPage
							.showView(PatientDetailView2.ID);
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
				
			};
		editCaseAction = new Action(Messages.getString("RnActions.edirCaseAction")) { //$NON-NLS-1$
			
				@Override
				public void run(){
					IWorkbenchPage rnPage =
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
					try {
						/* FallDetailView fdv=(FallDetailView) */rnPage.showView(FallDetailView.ID);
					} catch (Exception ex) {
						ExHandler.handle(ex);
					}
				}
				
			};
		delRnAction = new Action(Messages.getString("RnActions.deleteBillAction")) { //$NON-NLS-1$
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					for (Rechnung rn : list) {
						rn.storno(true);
					}
				}
			};
		reactivateRnAction = new Action(Messages.getString("RnActions.reactivateBillAction")) { //$NON-NLS-1$
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					for (Rechnung rn : list) {
						rn.setStatus(RnStatus.OFFEN);
					}
				}
			};
		expandAllAction = new Action(Messages.getString("RnActions.expandAllAction")) { //$NON-NLS-1$
				@Override
				public void run(){
					view.cv.getViewerWidget().getControl().setRedraw(false);
					((TreeViewer) view.cv.getViewerWidget()).expandAll();
					view.cv.getViewerWidget().getControl().setRedraw(true);
				}
			};
		collapseAllAction = new Action(Messages.getString("RnActions.collapseAllAction")) { //$NON-NLS-1$
				@Override
				public void run(){
					view.cv.getViewerWidget().getControl().setRedraw(false);
					((TreeViewer) view.cv.getViewerWidget()).collapseAll();
					view.cv.getViewerWidget().getControl().setRedraw(true);
				}
			};
		reloadAction = new Action(Messages.getString("RnActions.reloadAction")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RnActions.reloadTooltip")); //$NON-NLS-1$
					setImageDescriptor(Images.IMG_REFRESH.getImageDescriptor());
				}
				
				@Override
				public void run(){
					view.cfp.fireChangedEvent();
				}
			};
		
		addPaymentAction = new Action(Messages.getString("RnActions.addBookingAction")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RnActions.addBookingTooltip")); //$NON-NLS-1$
					setImageDescriptor(Images.IMG_ADDITEM.getImageDescriptor());
				}
				
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					if (list.size() > 0) {
						Rechnung actRn = list.get(0);
						try {
							if (new RnDialogs.BuchungHinzuDialog(view.getViewSite().getShell(),
								actRn).open() == Dialog.OK) {
								ElexisEventDispatcher.update(actRn);
							}
						} catch (ElexisException e) {
							SWTHelper.showError("Zahlung hinzufügen ist nicht möglich",
								e.getLocalizedMessage());
						}
					}
				}
			};
		
		addExpenseAction = new Action(Messages.getString("RnActions.addFineAction")) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_REMOVEITEM.getImageDescriptor());
				}
				
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					if (list.size() > 0) {
						Rechnung actRn = list.get(0);
						try {
							if (new RnDialogs.GebuehrHinzuDialog(view.getViewSite().getShell(),
								actRn).open() == Dialog.OK) {
								ElexisEventDispatcher.update(actRn);
							}
						} catch (ElexisException e) {
							SWTHelper.showError("Zahlung hinzufügen ist nicht möglich",
								e.getLocalizedMessage());
						}
					}
				}
			};
		
		changeStatusAction =
			new RestrictedAction(AccessControlDefaults.ADMIN_CHANGE_BILLSTATUS_MANUALLY,
				Messages.getString("RnActions.changeStateAction")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RnActions.changeStateTooltip")); //$NON-NLS-1$
					setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				}
				
				@Override
				public void doRun(){
					List<Rechnung> list = view.createList();
					if (list.size() > 0) {
						Rechnung actRn = list.get(0);
						if (new RnDialogs.StatusAendernDialog(view.getViewSite().getShell(), actRn)
							.open() == Dialog.OK) {
							ElexisEventDispatcher.update(actRn);
						}
					}
				}
			};
		stornoAction = new Action(Messages.getString("RnActions.stornoAction")) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
					setToolTipText(Messages.getString("RnActions.stornoActionTooltip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					if (list.size() > 0) {
						Rechnung actRn = list.get(0);
						if (new RnDialogs.StornoDialog(view.getViewSite().getShell(), actRn).open() == Dialog.OK) {
							ElexisEventDispatcher.update(actRn);
						}
					}
				}
			};
		increaseLevelAction =
			new Action(Messages.getString("RnActions.increaseReminderLevelAction")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RnActions.increadeReminderLevelTooltip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
					if (list.size() > 0) {
						Rechnung actRn = list.get(0);
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
							SWTHelper.showInfo(
								Messages.getString("RnActions.changeStateErrorCaption"), //$NON-NLS-1$
								Messages.getString("RnActions.changeStateErrorMessage")); //$NON-NLS-1$
						}
					}
					
				}
			};
		addAccountExcessAction = new Action(Messages.getString("RnActions.addAccountGood")) { //$NON-NLS-1$
				{
					setToolTipText(Messages.getString("RnActions.addAccountGoodTooltip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					List<Rechnung> list = view.createList();
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
							
							if (SWTHelper
								.askYesNo(
									Messages.getString("RnActions.transferMoneyCaption"), //$NON-NLS-1$
									"Das Konto von Patient \""
										+ patient.getLabel()
										+ "\" weist ein positives Kontoguthaben auf. Wollen Sie den Betrag von "
										+ amount.toString() + " dieser Rechnung \"" + actRn.getNr()
										+ ": " + fall.getLabel() + "\" zuweisen?")) {
								
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
		rnFilterAction =
			new Action(Messages.getString("RnActions.filterListAction"), Action.AS_CHECK_BOX) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
					setToolTipText(Messages.getString("RnActions.filterLIstTooltip")); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
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
	}
	
	static class RnListeDruckDialog extends TitleAreaDialog implements ICallback {
		ArrayList<Rechnung> rnn;
		private TextContainer text;
		
		public RnListeDruckDialog(final Shell shell, final Object[] tree){
			super(shell);
			rnn = new ArrayList<Rechnung>(tree.length);
			for (Object o : tree) {
				if (o instanceof Tree) {
					Tree tr = (Tree) o;
					if (tr.contents instanceof Rechnung) {
						tr = tr.getParent();
					}
					if (tr.contents instanceof Fall) {
						tr = tr.getParent();
					}
					if (tr.contents instanceof Patient) {
						for (Tree tFall : (Tree[]) tr.getChildren().toArray(new Tree[0])) {
							Fall fall = (Fall) tFall.contents;
							for (Tree tRn : (Tree[]) tFall.getChildren().toArray(new Tree[0])) {
								Rechnung rn = (Rechnung) tRn.contents;
								rnn.add(rn);
							}
						}
					}
				}
			}
			
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(final Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			text = new TextContainer(getShell());
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			text.getPlugin().createContainer(ret, this);
			text.getPlugin().showMenu(false);
			text.getPlugin().showToolbar(false);
			text.createFromTemplateName(null,
				"Liste", Brief.UNKNOWN, CoreHub.actUser, Messages.getString("RnActions.bills")); //$NON-NLS-1$ //$NON-NLS-2$
			text.getPlugin()
				.insertText(
					"[Titel]", //$NON-NLS-1$
					Messages.getString("RnActions.billsListPrintetAt") + new TimeTool().toString(TimeTool.DATE_GER) + "\n", //$NON-NLS-1$ //$NON-NLS-2$
					SWT.CENTER);
			String[][] table = new String[rnn.size() + 1][];
			Money sum = new Money();
			int i;
			for (i = 0; i < rnn.size(); i++) {
				Rechnung rn = rnn.get(i);
				table[i] = new String[3];
				StringBuilder sb = new StringBuilder();
				Fall fall = rn.getFall();
				Patient p = fall.getPatient();
				table[i][0] = rn.getNr();
				sb.append(p.getLabel()).append(" - ").append(fall.getLabel()); //$NON-NLS-1$
				table[i][1] = sb.toString();
				Money betrag = rn.getBetrag();
				sum.addMoney(betrag);
				table[i][2] = betrag.getAmountAsString();
			}
			table[i] = new String[3];
			table[i][0] = ""; //$NON-NLS-1$
			table[i][1] = Messages.getString("RnActions.sum"); //$NON-NLS-1$
			table[i][2] = sum.getAmountAsString();
			text.getPlugin().setFont("Helvetica", SWT.NORMAL, 9); //$NON-NLS-1$
			text.getPlugin().insertTable("[Liste]", 0, table, new int[] { //$NON-NLS-1$
					10, 80, 10
				});
			text.getPlugin().showMenu(true);
			text.getPlugin().showToolbar(true);
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.getString("RnActions.billsList")); //$NON-NLS-1$
			setTitle(Messages.getString("RnActions.printListCaption")); //$NON-NLS-1$
			setMessage(Messages.getString("RnActions.printListMessage")); //$NON-NLS-1$
			getShell().setSize(900, 700);
			SWTHelper.center(Hub.plugin.getWorkbench().getActiveWorkbenchWindow().getShell(),
				getShell());
		}
		
		@Override
		protected void okPressed(){
			super.okPressed();
		}
		
		public void save(){
			// TODO Auto-generated method stub
			
		}
		
		public boolean saveAs(){
			// TODO Auto-generated method stub
			return false;
		}
	}
}
