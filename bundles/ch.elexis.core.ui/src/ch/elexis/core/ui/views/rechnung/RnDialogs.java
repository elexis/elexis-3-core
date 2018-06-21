/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
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

import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Properties;

import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.DirectoryDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.tiff.common.ui.datepicker.DatePickerCombo;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ExtensionPointConstantsData;
import ch.elexis.core.data.interfaces.IRnOutputter;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.AccountTransaction;
import ch.elexis.data.AccountTransaction.Account;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.elexis.data.Zahlung;
import ch.elexis.scripting.CSVWriter;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class RnDialogs {
	public static final int ERR_STORNO = 1;
	private static final String RECHNUNG_IST_STORNIERT = "Rechnung ist storniert";
	
	public static class GebuehrHinzuDialog extends TitleAreaDialog {
		Rechnung rn;
		DatePickerCombo dp;
		Text amount;
		Text bemerkung;
		
		public GebuehrHinzuDialog(Shell shell, Rechnung r) throws ElexisException{
			super(shell);
			if (r.getStatus() == RnStatus.STORNIERT) {
				throw new ElexisException(getClass(), RECHNUNG_IST_STORNIERT, ERR_STORNO);
			}
			rn = r;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_date); //$NON-NLS-1$
			dp = new DatePickerCombo(ret, SWT.NONE);
			dp.setDate(new Date());
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_amount); //$NON-NLS-1$
			// nf=NumberFormat.getCurrencyInstance();
			amount = new Text(ret, SWT.BORDER);
			// amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_remark); //$NON-NLS-1$
			bemerkung = new Text(ret, SWT.MULTI | SWT.BORDER);
			bemerkung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			amount.setFocus();
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.RnDialogs_invoice + rn.getNr()); //$NON-NLS-1$
			getShell().setText(Messages.RnDialogs_addExpense); //$NON-NLS-1$
			setMessage(Messages.RnDialogs_enterAmount); //$NON-NLS-1$
			setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		}
		
		@Override
		protected void okPressed(){
			
			// Number num=df.parse(amount.getText());
			Money ret = MoneyInput.getFromTextField(amount);
			if (ret != null) {
				ret = ret.multiply(-1.0);
				rn.addZahlung(ret, bemerkung.getText(), new TimeTool(dp.getDate().getTime()));
				super.okPressed();
			} else {
				ErrorDialog.openError(getShell(), Messages.RnDialogs_amountInvalid,
					Messages.RnDialogs_invalidFormat, //$NON-NLS-1$ //$NON-NLS-2$
					new Status(1, "ch.elexis", 1, "CurrencyFormat", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		}
	}
	
	public static class MultiGebuehrHinzuDialog extends TitleAreaDialog {
		private List<Rechnung> rechnungen;
		private List<String> rnNumbers;
		
		private DatePickerCombo dp;
		private Text amount;
		private Text bemerkung;
		private TableViewer tableViewer;
		
		public MultiGebuehrHinzuDialog(Shell shell, List<Rechnung> rechnungen)
			throws ElexisException{
			super(shell);
			this.rechnungen = rechnungen;
			
			rnNumbers = new ArrayList<String>();
			for (Rechnung rn : rechnungen) {
				if (rn.getStatus() == RnStatus.STORNIERT) {
					throw new ElexisException(getClass(), RECHNUNG_IST_STORNIERT, ERR_STORNO);
				}
				rnNumbers.add(rn.getNr());
			}
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_date); //$NON-NLS-1$
			dp = new DatePickerCombo(ret, SWT.NONE);
			dp.setDate(new Date());
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_amount); //$NON-NLS-1$
			// nf=NumberFormat.getCurrencyInstance();
			amount = new Text(ret, SWT.BORDER);
			// amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_remark); //$NON-NLS-1$
			bemerkung = new Text(ret, SWT.MULTI | SWT.BORDER);
			bemerkung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			amount.setFocus();
			
			tableViewer = new TableViewer(ret, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
			GridData gd_Table = new GridData();
			gd_Table.grabExcessHorizontalSpace = true;
			gd_Table.horizontalSpan = 1;
			gd_Table.minimumHeight = 100;
			gd_Table.heightHint = 100;
			tableViewer.getTable().setLayoutData(gd_Table);
			tableViewer.getTable().setHeaderVisible(true);
			tableViewer.getTable().setLinesVisible(false);
			tableViewer.setContentProvider(new ArrayContentProvider());
			TableViewerColumn colRnNumber = new TableViewerColumn(tableViewer, SWT.NONE);
			colRnNumber.getColumn().setWidth(200);
			colRnNumber.getColumn().setText(Messages.RnDialogs_invoiceNumber);
			colRnNumber.setLabelProvider(new ColumnLabelProvider());
			
			tableViewer.setInput(rnNumbers);
			
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.RnDialogs_addExpenseMulti); //$NON-NLS-1$
			getShell().setText(Messages.RnDialogs_addExpense); //$NON-NLS-1$
			setMessage(Messages.RnDialogs_enterAmount); //$NON-NLS-1$
			setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		}
		
		@Override
		protected void okPressed(){
			
			// Number num=df.parse(amount.getText());
			Money ret = MoneyInput.getFromTextField(amount);
			if (ret != null) {
				ret = ret.multiply(-1.0);
				TimeTool ttDate = new TimeTool(dp.getDate().getTime());
				for (Rechnung rn : rechnungen) {
					rn.addZahlung(ret, bemerkung.getText(), ttDate);
				}
				super.okPressed();
			} else {
				ErrorDialog.openError(getShell(), Messages.RnDialogs_amountInvalid,
					Messages.RnDialogs_invalidFormat, //$NON-NLS-1$ //$NON-NLS-2$
					new Status(1, "ch.elexis", 1, "CurrencyFormat", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		}
	}
	
	public static class BuchungHinzuDialog extends TitleAreaDialog {
		
		Rechnung rn;
		DatePickerCombo dp;
		Text amount, bemerkung;
		
		ComboViewer viewer;
		
		public BuchungHinzuDialog(Shell shell, Rechnung r) throws ElexisException{
			this(shell, r, false);
		}
		
		/**
		 * 
		 * @param shell
		 * @param invoice
		 * @param allowCancelledInvoices
		 * @throws ElexisException
		 * @since 3.6
		 */
		public BuchungHinzuDialog(Shell shell, Rechnung invoice, boolean allowCancelledInvoices)
			throws ElexisException{
			super(shell);
			if (invoice.getStatus() == InvoiceState.CANCELLED.numericValue()) {
				if (!allowCancelledInvoices) {
					throw new ElexisException(getClass(), RECHNUNG_IST_STORNIERT, ERR_STORNO);
				}
			}
			rn = invoice;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_date); //$NON-NLS-1$
			dp = new DatePickerCombo(ret, SWT.NONE);
			dp.setDate(new Date());
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_amount); //$NON-NLS-1$
			// nf=NumberFormat.getCurrencyInstance();
			amount = new Text(ret, SWT.BORDER);
			// amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.NONE).setText(Messages.RnDialogs_remark); //$NON-NLS-1$
			bemerkung = new Text(ret, SWT.MULTI | SWT.BORDER);
			bemerkung.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			amount.setText(rn.getOffenerBetrag().getAmountAsString());
			amount.setFocus();
			new Label(ret, SWT.NONE).setText("Konto"); //$NON-NLS-1$
			viewer = new ComboViewer(ret, SWT.BORDER);
			viewer.setContentProvider(ArrayContentProvider.getInstance());
			viewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					if (element instanceof Account) {
						return ((Account) element).getNumeric() + " - " + ((Account) element).getName();
					}
					return super.getText(element);
				}
			});
			List<Account> accounts = new ArrayList<>();
			accounts.addAll(Account.getAccounts().values());
			accounts.sort(new Comparator<Account>() {
				@Override
				public int compare(Account left, Account right){
					return Integer.valueOf(left.getNumeric())
						.compareTo(Integer.valueOf(right.getNumeric()));
				}
			});
			viewer.setInput(accounts);
			viewer.getCombo().setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			setTitle(Messages.RnDialogs_invoice + rn.getNr()); //$NON-NLS-1$
			getShell().setText(Messages.RnDialogs_addTransaction); //$NON-NLS-1$
			setMessage(Messages.RnDialogs_enterAmount); //$NON-NLS-1$
			setTitleImage(Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize));
		}
		
		@Override
		protected void okPressed(){
			// Number num=df.parse(amount.getText());
			Money ret = MoneyInput.getFromTextField(amount);
			if (ret != null) {
				Account account = null;
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection != null && !selection.isEmpty()) {
					account = (Account) selection.getFirstElement();
				}
				String text = bemerkung.getText();
				if ((text == null || text.isEmpty()) && account != null) {
					text = account.getName();
				}
				Zahlung zahlung = rn.addZahlung(ret, text, new TimeTool(dp.getDate().getTime()));
				if (zahlung != null) {
					AccountTransaction transaction = zahlung.getTransaction();
					if (transaction != null) {
						if (account != null) {
							transaction.setAccount(account);
						}
					}
				}
				super.okPressed();
			} else {
				ErrorDialog.openError(getShell(), Messages.RnDialogs_amountInvalid,
					Messages.RnDialogs_invalidFormat, //$NON-NLS-1$ //$NON-NLS-2$
					new Status(1, "ch.elexis", 1, "CurrencyFormat", null)); //$NON-NLS-1$ //$NON-NLS-2$
			}
			
		}
		
	}
	
	public static class StatusAendernDialog extends TitleAreaDialog {
		Rechnung rn;
		Combo cbStates;
		
		// RnStatus[] states=RnStatus.Text;
		
		public StatusAendernDialog(Shell shell, Rechnung r){
			super(shell);
			rn = r;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			cbStates = new Combo(ret, SWT.READ_ONLY);
			cbStates.setItems(RnStatus.getStatusTexts());
			cbStates.setVisibleItemCount(RnStatus.getStatusTexts().length);
			cbStates.select(rn.getStatus());
			cbStates.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			new Label(ret, SWT.WRAP).setText(Messages.RnDialogs_warningDontChangeManually); //$NON-NLS-1$
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.RnDialogs_invoiceNumber + rn.getNr()); //$NON-NLS-1$
			setTitle(Messages.RnDialogs_modifyInvoiceState); //$NON-NLS-1$
			
			setMessage(rn.getFall().getPatient().getLabel() + Messages.RnDialogs_pleaseNewState); //$NON-NLS-1$
		}
		
		@Override
		protected void okPressed(){
			int idx = cbStates.getSelectionIndex();
			if (idx != -1) {
				rn.setStatus(idx);
			}
			super.okPressed();
		}
		
	}
	
	public static class MultiStatusAendernDialog extends TitleAreaDialog {
		private List<Rechnung> rechnungen;
		private List<String> rnNumbers;
		private Combo cbStates;
		private TableViewer tableViewer;
		
		public MultiStatusAendernDialog(Shell shell, List<Rechnung> rechnungen){
			super(shell);
			this.rechnungen = rechnungen;
			
			rnNumbers = new ArrayList<String>();
			for (Rechnung rn : rechnungen) {
				rnNumbers.add(rn.getNr());
			}
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			Label lblSelectState = new Label(ret, SWT.NONE);
			lblSelectState.setText(Messages.RnDialogs_pleaseNewStateForMulti);
			
			cbStates = new Combo(ret, SWT.READ_ONLY);
			cbStates.setItems(RnStatus.getStatusTexts());
			cbStates.setVisibleItemCount(RnStatus.getStatusTexts().length);
			cbStates.select(rechnungen.get(0).getStatus());
			cbStates.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			
			tableViewer = new TableViewer(ret, SWT.V_SCROLL | SWT.BORDER | SWT.FULL_SELECTION);
			GridData gd_Table = new GridData();
			gd_Table.grabExcessHorizontalSpace = true;
			gd_Table.horizontalSpan = 1;
			gd_Table.minimumHeight = 150;
			gd_Table.heightHint = 150;
			tableViewer.getTable().setLayoutData(gd_Table);
			tableViewer.getTable().setHeaderVisible(true);
			tableViewer.getTable().setLinesVisible(false);
			
			tableViewer.setContentProvider(new ArrayContentProvider());
			TableViewerColumn colRnNumber = new TableViewerColumn(tableViewer, SWT.NONE);
			colRnNumber.getColumn().setWidth(200);
			colRnNumber.getColumn().setText(Messages.RnDialogs_invoiceNumber);
			colRnNumber.setLabelProvider(new ColumnLabelProvider());
			
			tableViewer.setInput(rnNumbers);
			
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.RnDialogs_modifyInvoiceState); //$NON-NLS-1$
			setTitle(Messages.RnDialogs_modifyInvoiceStateMulti); //$NON-NLS-1$
			setMessage(Messages.RnDialogs_warningDontChangeManually);
		}
		
		@Override
		protected void okPressed(){
			int idx = cbStates.getSelectionIndex();
			if (idx != -1) {
				for (Rechnung rn : rechnungen) {
					rn.setStatus(idx);
				}
			}
			super.okPressed();
		}
	}
	
	public static class StornoDialog extends TitleAreaDialog {
		Rechnung rn;
		Button bReactivate;
		List<Button> exporters = new ArrayList<Button>();
		private List<IRnOutputter> lo;
		private boolean alwaysReactive = false;
		private List<Konsultation> konsultations;
		
		public StornoDialog(Shell shell, Rechnung r){
			super(shell);
			rn = r;
			this.alwaysReactive = false;
		}
		
		public StornoDialog(Shell shell, Rechnung r, boolean alwaysReactive){
			super(shell);
			rn = r;
			this.alwaysReactive = alwaysReactive;
		}
		
		@SuppressWarnings("unchecked")
		private List<IRnOutputter> getOutputters(){
			List<IRnOutputter> outputters = new ArrayList<>();
			List<IRnOutputter> los =
				Extensions.getClasses(ExtensionPointConstantsData.RECHNUNGS_MANAGER, "outputter"); //$NON-NLS-1$ //$NON-NLS-2$
			for (IRnOutputter rno : los) {
				if (rno.canStorno(null) && hasTrace(rno.getDescription())) {
					outputters.add(rno);
				}
			}
			return outputters;
		}
		
		@SuppressWarnings("unchecked")
		@Override
		protected Control createDialogArea(Composite parent){
			lo = Extensions.getClasses(ExtensionPointConstantsData.RECHNUNGS_MANAGER, "outputter"); //$NON-NLS-1$ //$NON-NLS-2$
			if (lo.isEmpty()) {
				String msg = "Elexis has no textplugin configured for outputting bills!"; //$NON-NLS-1$ 
				SWTHelper.alert(msg, msg);
				return null;
			}
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new GridLayout());
			Label lbLocal = new Label(ret, SWT.NONE);
			lbLocal.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
			lbLocal.setText(Messages.RnDialogs_stornoOnlyLocal); //$NON-NLS-1$
			for (IRnOutputter rno : lo) {
				if (rno.canStorno(null) && hasTrace(rno.getDescription())) {
					Button cbStorno = new Button(ret, SWT.CHECK);
					cbStorno.setData(rno);
					cbStorno.setText(rno.getDescription());
					cbStorno.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
					cbStorno.setSelection(true);
					exporters.add(cbStorno);
				}
			}
			if (exporters.size() > 0) {
				lbLocal.setText(Messages.RnDialogs_stornoPropagate); //$NON-NLS-1$
			}
			new Label(ret, SWT.SEPARATOR | SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(
				1, false, 1, false));
			
			bReactivate = new Button(ret, SWT.CHECK);
			bReactivate.setText(Messages.RnDialogs_reactivateConsultations); //$NON-NLS-1$
			bReactivate.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			bReactivate.setSelection(true);
			
			if (alwaysReactive) {
				bReactivate.setVisible(false);
			}
			
			/*
			 * bYes=new Button(ret,SWT.RADIO); bNo=new Button(ret,SWT.RADIO);
			 * bYes.setText(Messages.getString("RnDialogs.yes")); //$NON-NLS-1$
			 * bNo.setText(Messages.getString("RnDialogs.no")); //$NON-NLS-1$
			 */
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			return ret;
		}
		
		private boolean hasTrace(String msg){
			List<String> msgs = rn.getTrace(Rechnung.OUTPUT);
			for (String m : msgs) {
				if (m.indexOf(msg) > -1) {
					return true;
				}
			}
			return false;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.RnDialogs_invoice + rn.getNr()); //$NON-NLS-1$
			setTitle(Messages.RnDialogs_reallyCancel); //$NON-NLS-1$
			//setMessage(Messages.getString("RnDialogs.reactivateConsultations")); //$NON-NLS-1$
		}
		
		@Override
		protected void okPressed(){
			konsultations = rn.stornoBill(bReactivate.getSelection() || alwaysReactive);
			for (Button exporter : exporters) {
				if (exporter.getSelection()) {
					IRnOutputter iro = (IRnOutputter) exporter.getData();
					if (iro != null) {
						iro.doOutput(IRnOutputter.TYPE.STORNO, Arrays.asList(new Rechnung[] {
							rn
						}), new Properties());
					}
				}
			}
			super.okPressed();
		}
		
		public List<Konsultation> getKonsultations(){
			return konsultations;
		}
		
		/**
		 * Opens a dialog and closes it with OK if no rnoutputters are registered. Otherwise the
		 * dialog will stay opened.
		 * 
		 * @param closeWithOKIfNoExporterExists
		 * @return
		 */
		public int openDialog(){
			List<IRnOutputter> rnOutputters = getOutputters();
			if (rnOutputters != null && rnOutputters.isEmpty()) {
				super.setBlockOnOpen(false);
				super.open();
				okPressed();
				return getReturnCode();
			}
			return super.open();
		}
	}
	
	public static class RnListeExportDialog extends TitleAreaDialog {
		ArrayList<Rechnung> rnn;
		String RnListExportDirname = CoreHub.localCfg.get("rechnung/RnListExportDirname", null);
		Text tDirName;
		// RnStatus[] states=RnStatus.Text;
		private Logger log = LoggerFactory.getLogger(RnActions.class);
		private String RnListExportFileName =
				new SimpleDateFormat("'RnListExport-'yyyyMMddHHmmss'.csv'").format(new Date());
		
		public RnListeExportDialog(Shell shell, List<Rechnung> rechnungen){
			super(shell);
			
			rnn = new ArrayList<Rechnung>();
			for (Rechnung rn : rechnungen) {
				rnn.add(rn);
			}
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite ret = new Composite(parent, SWT.NONE);
			ret.setLayout(new FillLayout());
			ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
			
			//201512211432js: Siehe auch Rechnungsdrucker.java public class RechnungsDrucker.createSettingsControl()
			//TODO: Auf Konstante umstellen, dann braucht's allerdings den Austausch weiterer Module bei Installation!!!
			
			Group cSaveCopy = new Group(ret, SWT.NONE);
			cSaveCopy.setText(String.format(Messages.RnActions_exportSaveHelp, RnListExportFileName));
			cSaveCopy.setLayout(new GridLayout(2, false));
			Button bSelectFile = new Button(cSaveCopy, SWT.PUSH);
			bSelectFile.setText(Messages.RnActions_exportListDirName);
			bSelectFile.setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
			bSelectFile.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					DirectoryDialog ddlg = new DirectoryDialog(parent.getShell());
					RnListExportDirname = ddlg.open();
					if (RnListExportDirname == null) {
						SWTHelper.alert(Messages.RnActions_exportListDirNameMissingCaption,
							Messages.RnActions_exportListDirNameMissingText);
					} else {
						//ToDo: Umstellen auf externe Konstante!
						CoreHub.localCfg.set("rechnung/RnListExportDirname", RnListExportDirname);
						tDirName.setText(RnListExportDirname);
					}
				}
			});
			tDirName = new Text(cSaveCopy, SWT.BORDER | SWT.READ_ONLY);
			tDirName.setText(CoreHub.localCfg.get("rechnung/RnListExportDirname", "")); //$NON-NLS-1$
			tDirName.setLayoutData(SWTHelper.getFillGridData(2, true, 1, false));
			return ret;
		}
		
		@Override
		public void create(){
			super.create();
			getShell().setText(Messages.RnActions_billsList);
			setTitle(Messages.RnActions_exportListCaption);
			setMessage(Messages.RnActions_exportListMessage);
			getShell().setSize(900, 700);
			SWTHelper.center(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
				getShell());
		}
		
		@Override
		protected void okPressed(){
			super.okPressed();
			CSVWriteTable();
		}
		
		public void CSVWriteTable(){
			String pathToSave = RnListExportDirname + "/" + RnListExportFileName;
			CSVWriter csv = null;
			int nrLines = 0;
			try {
				csv = new CSVWriter(new FileWriter(pathToSave));
				// @formatter:off
					String[] header = new String[] {
						"Aktion?", // line 0 
						"Re.Nr", // line 1
						"Re.DatumRn", // line 2
						"Re.DatumVon", // line 3
						"Re.DatumBis", // line 4
						"Re.Garant", // line 5
						"Re.Total", // line 6
						"Re.Offen", // line 7
						"Re.StatusLastUpdate", // line 8
						"Re.Status", // line 9
						"Re.StatusIsActive", // line 10
						"Re.StatusText", // line 11
						"Re.StatusChanges", // line 12
						"Re.Rejecteds", // line 13
						"Re.Outputs", // line 14
						"Re.Payments", // line 15
						"Fall.AbrSystem", // line 16
						"Fall.Bezeichnung", // line 17
						"Fall.Grund", // line 18
						"Pat.Nr", // line 10
						"Pat.Name", // line 20
						"Pat.Vorname", // line 21
						"Pat.GebDat", // line 22
						"Pat.LztKonsDat", // line 23
						"Pat.Balance", // line 24
						"Pat.GetAccountExcess", // line 25
						"Pat.BillSummary.Total.", // line 26
						"Pat.BillSummary.Paid", // line 27
						"Pat.BillSummary.Open" // line 28
					};
					// @formatter:on
				log.debug("csv export started for {} with {} fields for {} invoices", pathToSave,
					header.length, rnn.size());
				csv.writeNext(header);
				nrLines++;
				int i;
				for (i = 0; i < rnn.size(); i++) {
					Rechnung rn = rnn.get(i);
					Fall fall = rn.getFall();
					Patient p = fall.getPatient();
					String[] line = new String[header.length];
					line[0] = ""; //201512210402js: Leere Spalte zum Eintragen der gewünschten Aktion.
					line[1] = rn.getNr();
					line[2] = rn.getDatumRn();
					line[3] = rn.getDatumVon();
					line[4] = rn.getDatumBis();
					line[5] = fall.getGarant().getLabel();
					line[6] = rn.getBetrag().toString();
					line[7] = rn.getOffenerBetrag().toString();
					long luTime = rn.getLastUpdate();
					Date date = new Date(luTime);
					// TODO: Support other date formats based upon location or configured settings
					SimpleDateFormat df2 = new SimpleDateFormat("dd.MM.yyyy");
					String dateText = df2.format(date);
					line[8] = dateText.toString();
					int st = rn.getStatus();
					line[9] = Integer.toString(st);
					if (RnStatus.isActive(st)) {
						line[10] = "True";
					} else {
						line[10] = "False";
					}
					line[11] = RnStatus.getStatusText(st);
					// 201512210310js: New: produce 4 fields, each with multiline content.
					List<String> statuschgs = rn.getTrace(Rechnung.STATUS_CHANGED);
					String a = statuschgs.toString();
					if (a != null && a.length() > 1) {
						//Die Uhrzeiten rauswerfen:
						a = a.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
						//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
						a = a.replaceAll(", ", "\n");
						//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
						line[12] = a.substring(1, a.length() - 1);
					}
					if (rn.getStatus() == RnStatus.FEHLERHAFT) {
						List<String> rejects = rn.getTrace(Rechnung.REJECTED);
						String rnStatus = rejects.toString();
						if (rnStatus != null && rnStatus.length() > 1) {
							//Die Uhrzeiten rauswerfen:
							rnStatus =
								rnStatus.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
							//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
							rnStatus = rnStatus.replaceAll(", ", "\n");
							//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
							line[13] = rnStatus.substring(1, rnStatus.length() - 1);
						}
					}
					List<String> outputs = rn.getTrace(Rechnung.OUTPUT);
					String rnOutput = outputs.toString();
					if (rnOutput != null && rnOutput.length() > 1) {
						//Die Uhrzeiten rauswerfen:
						rnOutput = rnOutput.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
						//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
						rnOutput = rnOutput.replaceAll(", ", "\n");
						//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
						line[14] = rnOutput.substring(1, rnOutput.length() - 1);
					}
					List<String> payments = rn.getTrace(Rechnung.PAYMENT);
					String rnPayment = payments.toString();
					if (rnPayment != null && rnPayment.length() > 1) {
						//Die Uhrzeiten rauswerfen:
						rnPayment = rnPayment.replaceAll(", [0-9][0-9]:[0-9][0-9]:[0-9][0-9]", "");
						//", " durch "\n" ersetzen (Man könnte auch noch prüfen, ob danach eine Zahl/ein Datum kommt - die dann aber behalten werden muss.)
						rnPayment = rnPayment.replaceAll(", ", "\n");
						//Führende und Trailende [] bei der Ausgabe (!) rauswerfen
						line[15] = rnPayment.substring(1, rnPayment.length() - 1);
					}
					// Jetzt alles zum betroffenen Fall:
					line[16] = fall.getAbrechnungsSystem();
					line[17] = fall.getBezeichnung();
					line[18] = fall.getGrund();
					// Jetzt alles zum betroffenen Patienten:
					line[19] = p.getKuerzel();
					line[20] = p.getName();
					line[21] = p.getVorname();
					line[22] = p.getGeburtsdatum();
					// TODO: allenfalls wieder: auf n.a. oder so setzen...
					// TODO: Ich möcht aber wissen, ob p (dürfte eigentlich nie der Fall sein) oder nk schuld sind, wenn nichts rauskommt.
					// TODO: Na ja, eigentlich würd ich noch lieber wissen, WARUM da manchmal nichts rauskommt, obwohl eine kons sicher vhd ist.
					String lkDatum = "p==null";
					if (p != null) {
						Konsultation lk = p.getLetzteKons(false);
						if (lk != null) {
							lkDatum = (lk.getDatum());
						} else {
							lkDatum = "lk==null";
						}
					}
					line[23] = lkDatum;
					line[24] = p.getBalance(); //returns: String
					line[25] = p.getAccountExcess().toString(); //returns: Money
					//201512210146js: Das Folgende ist aus BillSummary - dort wird dafür keine Funktion bereitgestellt,
					// TODO: Prüfen, ob das eine Redundanz DORT und HIER ist vs. obenn erwähnter getKontostand(), getAccountExcess() etc.
					// maybe called from foreign thread
					String totalText = ""; //$NON-NLS-1$
					String paidText = ""; //$NON-NLS-1$
					String openText = ""; //$NON-NLS-1$
					// Davon, dass p != null ist, darf man eigentlich ausgehen, da ja Rechnungen zu p gehören etc.
					if (p != null) {
						Money total = new Money(0);
						Money paid = new Money(0);
						List<Rechnung> rechnungen = p.getRechnungen();
						for (Rechnung rechnung : rechnungen) {
							// don't consider canceled bills
							if (rechnung.getStatus() != RnStatus.STORNIERT) {
								total.addMoney(rechnung.getBetrag());
								for (Zahlung zahlung : rechnung.getZahlungen()) {
									paid.addMoney(zahlung.getBetrag());
								}
							}
						}
						Money open = new Money(total);
						open.subtractMoney(paid);
						totalText = total.toString();
						paidText = paid.toString();
						openText = open.toString();
					}
					line[26] = totalText;
					line[27] = paidText;
					line[28] = openText;
					csv.writeNext(line);
					nrLines++;
				}
				csv.close();
				log.debug("{}: Wrote {} lines for {} invoices", pathToSave, nrLines, rnn.size());
			} catch (Exception ex) {
				ExHandler.handle(ex);
				log.error("csv exporter error", ex);
				SWTHelper.showError("Fehler", ex.getMessage());
			} finally {
				if (csv != null) {
					try {
						csv.close();
					} catch (IOException e) {
						log.error("cannot close csv exporter", e);
					}
				}
			}
		}
	}
}
