package ch.elexis.core.ui.views.rechnung.invoice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.data.DBConnection;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class InvoiceListContentProvider implements IStructuredContentProvider {
	
	private List<InvoiceEntry> currentContent = new ArrayList<InvoiceEntry>();
	private StructuredViewer structuredViewer;
	private InvoiceListHeaderComposite invoiceListHeaderComposite;
	private InvoiceListBottomComposite invoiceListBottomComposite;
	
	public InvoiceListContentProvider(TableViewer tableViewerInvoiceList,
		InvoiceListHeaderComposite invoiceListHeaderComposite,
		InvoiceListBottomComposite invoiceListBottomComposite){
		this.structuredViewer = tableViewerInvoiceList;
		this.invoiceListHeaderComposite = invoiceListHeaderComposite;
		this.invoiceListBottomComposite = invoiceListBottomComposite;
	}
	
	@Override
	public void dispose(){}
	
	//@formatter:off
	public static final String FETCH_PS_MYSQL =
		" SELECT \n" + 
		"    InvoiceId,\n" + 
		"    InvoiceNo,\n" + 
		"    rndatumvon,\n" + 
		"    rndatumbis,\n" + 
		"    InvoiceState,\n" + 
		"    InvoiceTotal,\n" + 
		"    PatientId,\n" + 
		"    PatName1,\n" + 
		"    PatName2,\n" + 
		"    PatSex,\n" + 
		"    PatDob,\n" + 
		"    FallId,\n" + 
		"    FallGesetz,\n" + 
		"    FallGarantId,\n" + 
		"    FallKostentrID,\n" + 
		"    paymentCount,\n" + 
		"    paidAmount,\n" + 
		"    openAmount\n" + 
		"FROM" + 
		"    invoice_list_view" + 
		"REPLACE_WITH_CONDITIONALS " + 
		"REPLACE_WITH_ORDER " + 
		"REPLACE_WITH_LIMIT";
	
	public static final String COUNT_STATS_MYSQL = "SELECT \n" + 
		"    COUNT(InvoiceId),\n" + 
		"    COUNT(DISTINCT (patientid)),\n" + 
		"    SUM(invoiceTotal),\n" + 
		"    SUM(openAmount)\n" + 
		"FROM\n" + 
		"    invoice_list_view\n" + 
		"REPLACE_WITH_CONDITIONALS\n" + 
		"REPLACE_WITH_LIMIT";
	//@formatter:on
	
	public static String orderBy = "";
	private static int queryLimit = 1000;
	
	public void reload(){
		
		currentContent.clear();
		
		DBConnection dbConnection = PersistentObject.getDefaultConnection();
		
		int countInvoices = 0;
		int countPatients = 0;
		
		String statement = performPreparedStatementReplacements(COUNT_STATS_MYSQL, false);
		PreparedStatement ps = dbConnection.getPreparedStatement(statement);
		try (ResultSet res = ps.executeQuery()) {
			while (res.next()) {
				countInvoices = res.getInt(1);
				countPatients = res.getInt(2);
			}
		} catch (SQLException e) {
			ElexisStatus elexisStatus = new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
				CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, "Count stats failed", e);
			ElexisEventDispatcher.fireElexisStatusEvent(elexisStatus);
		} finally {
			dbConnection.releasePreparedStatement(ps);
		}
		
		boolean limitReached = (queryLimit > 0 && countInvoices >= queryLimit);
		if (limitReached) {
			invoiceListHeaderComposite.setLimitWarning(queryLimit);
			MessageDialog.openInformation(UiDesk.getTopShell(), "Query-Limit reached",
				"Query limit of set. You will only see the first " + queryLimit + " results.");
		} else {
			invoiceListHeaderComposite.setLimitWarning(null);
		}
		
		String preparedStatement = performPreparedStatementReplacements(FETCH_PS_MYSQL, true);
		ps = dbConnection.getPreparedStatement(preparedStatement);
		
		int openAmounts = 0;
		int owingAmounts = 0;
		System.out.println(ps);
		try (ResultSet res = ps.executeQuery()) {
			while (res.next()) {
				String invoiceId = res.getString(1);
				String invoiceNumber = res.getString(2);
				String dateFrom = res.getString(3);
				String dateTo = res.getString(4);
				int invoiceStatus = res.getInt(5);
				int totalAmount = res.getInt(6);
				String patientId = res.getString(7);
				String patientName =
					res.getString(8) + " " + res.getString(9) + " (" + res.getString(10) + ")";
				String dob = res.getString(11);
				if (StringUtils.isNumeric(dob)) {
					patientName += ", " + new TimeTool(dob).toString(TimeTool.DATE_GER);
				}
				String garantId = res.getString(14);
				int openAmount = res.getInt(18);
				
				openAmounts += openAmount;
				owingAmounts += (totalAmount - openAmount);
				
				InvoiceEntry ie = new InvoiceEntry(structuredViewer, invoiceId, patientId, garantId,
					invoiceNumber, invoiceStatus, dateFrom, dateTo, totalAmount, openAmount,
					patientName);
				currentContent.add(ie);
			}
		} catch (SQLException e) {
			ElexisStatus elexisStatus = new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
				CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, "Fetch results failed", e);
			ElexisEventDispatcher.fireElexisStatusEvent(elexisStatus);
		} finally {
			dbConnection.releasePreparedStatement(ps);
		}
		
		if (limitReached) {
			invoiceListBottomComposite.update(Integer.toString(countPatients),
				queryLimit + " (" + Integer.toString(countInvoices) + ")",
				new Money(openAmounts).getAmountAsString(),
				new Money(owingAmounts).getAmountAsString());
		} else {
			invoiceListBottomComposite.update(Integer.toString(countPatients),
				Integer.toString(countInvoices), new Money(openAmounts).getAmountAsString(),
				new Money(owingAmounts).getAmountAsString());
		}
		
		structuredViewer.setInput(currentContent);
		
		resolveAll();
	}
	
	private void resolveAll(){
		currentContent.parallelStream().forEach(invoiceEntry -> {
			// touch patient number
			if (!invoiceEntry.isResolved()) {
				invoiceEntry.resolve();
			}
		});
	}
	
	private String performPreparedStatementReplacements(String original,
		boolean includeOrderReplacement){
		String conditionals = determinePreparedStatementConditionals();
		String preparedStatement = original.replace("REPLACE_WITH_CONDITIONALS", conditionals);
		
		if (includeOrderReplacement) {
			preparedStatement = preparedStatement.replaceAll("REPLACE_WITH_ORDER", orderBy);
		}
		
		if (queryLimit > 0) {
			return preparedStatement.replaceAll("REPLACE_WITH_LIMIT",
				" LIMIT 0," + Integer.toString(queryLimit));
		} else {
			return preparedStatement.replaceAll("REPLACE_WITH_LIMIT", "");
		}
	}
	
	private String determinePreparedStatementConditionals(){
		List<String> conditionalTokens = new ArrayList<>();
		
		Integer invoiceStateNo = invoiceListHeaderComposite.getSelectedInvoiceStateNo();
		if (invoiceStateNo != null) {
			conditionalTokens.add("InvoiceState" + Query.EQUALS + Integer.toString(invoiceStateNo));
		}
		
		String invoiceId = invoiceListHeaderComposite.getSelectedInvoiceId();
		if (StringUtils.isNumeric(invoiceId)) {
			conditionalTokens.add("InvoiceNo LIKE '" + invoiceId + "%'");
		}
		
		String patientId = invoiceListHeaderComposite.getSelectedPatientId();
		if (patientId != null) {
			conditionalTokens.add(Fall.PATIENT_ID + Query.EQUALS + JdbcLink.wrap(patientId));
		}
		
		String totalAmount = invoiceListHeaderComposite.getSelectedTotalAmount();
		if (StringUtils.isNotBlank(totalAmount)) {
			if (totalAmount.contains(",") || totalAmount.contains(".")) {
				totalAmount = totalAmount.replaceAll(",", ".");
				try {
					Double amount = new Double(totalAmount);
					Money money = new Money();
					money.addAmount(amount);
					conditionalTokens.add("InvoiceTotal LIKE '" + money.getCents() + "%'");
				} catch (NumberFormatException nfe) {
					nfe.printStackTrace();
				}
			} else {
				conditionalTokens.add("InvoiceTotal LIKE '" + totalAmount + "%'");
			}
		}
		
		if (conditionalTokens.size() == 0) {
			return "";
		}
		
		return " WHERE " + conditionalTokens.stream().reduce((u, t) -> u + " AND " + t).get();
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){
		
	}
	
	@Override
	public Object[] getElements(Object inputElement){
		if (inputElement instanceof List<?>) {
			return currentContent.toArray();
		}
		return Collections.emptyList().toArray();
	}
	
	public void setSortOrderAndDirection(Object data, int sortDirection){
		String sortDirectionString = (SWT.UP == sortDirection) ? "ASC" : "DESC";
		if (Rechnung.BILL_NUMBER.equals(data)) {
			orderBy = "ORDER BY LENGTH(RnNummer) " + sortDirectionString + ",RnNummer "
				+ sortDirectionString;
		} else if (Rechnung.BILL_DATE_FROM.equals(data)) {
			orderBy = "ORDER BY " + data + " " + sortDirectionString;
		} else if (Kontakt.FLD_NAME1.equals(data)) {
			orderBy =
				"ORDER BY PatName1 " + sortDirectionString + ", PatName2 " + sortDirectionString;
		} else {
			orderBy = "";
		}
		
		reload();
	}
	
	public void setQueryLimit(int queryLimit){
		InvoiceListContentProvider.queryLimit = queryLimit;
	}
	
	/**
	 * View specific model class, including multi threaded property loading.
	 */
	public static class InvoiceEntry {
		
		private static ExecutorService executorService = Executors.newFixedThreadPool(8);
		private volatile boolean resolved = false;
		private volatile boolean resolving = false;
		private StructuredViewer viewer;
		
		private final String invoiceId;
		private final String patientId;
		private final String garantId;
		private String invoiceNumber;
		private InvoiceState invoiceState;
		private int totalAmount;
		private int openAmount;
		private TimeTool dateFrom;
		private TimeTool dateTo;
		private String patientName;
		
		private String payerType; // req resolv
		private String law; // req resolv
		private String garantLabel; // req resolv
		
		public InvoiceEntry(StructuredViewer viewer, String invoiceId, String patientId,
			String garantId, String invoiceNumber, int invoiceStatus, String dateFrom,
			String dateTo, int totalAmount, int openAmount, String patientName){
			this.viewer = viewer;
			
			this.invoiceId = invoiceId;
			this.patientId = patientId;
			if (garantId == null) {
				this.garantId = patientId;
			} else {
				this.garantId = garantId;
			}
			setInvoiceNumber(invoiceNumber);
			setInvoiceState(InvoiceState.fromState(invoiceStatus));
			setTotalAmount(totalAmount);
			setOpenAmount(openAmount);
			this.patientName = patientName;
			
			if (StringUtils.isNumeric(dateFrom)) {
				this.dateFrom = new TimeTool(dateFrom);
			}
			if (StringUtils.isNumeric(dateTo)) {
				this.dateTo = new TimeTool(dateTo);
			}
		}
		
		public synchronized boolean isResolved(){
			if (!resolved && !resolving) {
				resolving = true;
				executorService.execute(new ResolveLazyFieldsRunnable(viewer, this));
			}
			return resolved;
		}
		
		public void resolve(){
			executorService.execute(new ResolveLazyFieldsRunnable(null, this));
			while (!isResolved()) {
				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					// ignore
				}
			}
		}
		
		public synchronized void refresh(){
			resolved = false;
		}
		
		public String getInvoiceNumber(){
			return invoiceNumber;
		}
		
		public void setInvoiceNumber(String invoiceNumber){
			this.invoiceNumber = invoiceNumber;
		}
		
		public void setInvoiceState(InvoiceState invoiceState){
			this.invoiceState = invoiceState;
		}
		
		public InvoiceState getInvoiceState(){
			return invoiceState;
		}
		
		public void setOpenAmount(int openAmount){
			this.openAmount = openAmount;
		}
		
		public int getOpenAmount(){
			return openAmount;
		}
		
		public int getTotalAmount(){
			return totalAmount;
		}
		
		public void setTotalAmount(int totalAmount){
			this.totalAmount = totalAmount;
		}
		
		public String getTreatmentPeriod(){
			if (dateFrom != null && dateTo != null) {
				return dateFrom.toString(TimeTool.DATE_GER_SHORT) + " - "
					+ dateTo.toString(TimeTool.DATE_GER_SHORT);
			}
			return null;
		}
		
		public String getReceiverLabel(){
			if (garantId.equals(patientId)) {
				garantLabel = getPatientName();
			}
			return garantLabel;
		}
		
		public String getPayerType(){
			if (!isResolved()) {
				return "...";
			}
			return payerType;
		}
		
		public String getLaw(){
			if (!isResolved()) {
				return "...";
			}
			return law;
		}
		
		public String getPatientName(){
			return patientName;
		}
		
		public String getInvoiceId(){
			return invoiceId;
		}
		
		private class ResolveLazyFieldsRunnable implements Runnable {
			
			private StructuredViewer viewer;
			private InvoiceEntry invoiceEntry;
			
			public ResolveLazyFieldsRunnable(StructuredViewer viewer, InvoiceEntry invoiceEntry){
				this.viewer = viewer;
				this.invoiceEntry = invoiceEntry;
			}
			
			@Override
			public void run(){
				resolvePayerType();
				resolveLaw();
				resolveGarantLabel();
				invoiceEntry.resolved = true;
				invoiceEntry.resolving = false;
				if (viewer != null) {
					updateViewer();
				}
			}
			
			private void resolveGarantLabel(){
				if (garantLabel == null) {
					Kontakt garant = Kontakt.load(garantId);
					if (garant.exists()) {
						garantLabel = garant.getLabel();
					}
				}
			}
			
			private void resolveLaw(){
				Rechnung r = Rechnung.load(invoiceId);
				if (r.exists()) {
					Fall fall = r.getFall();
					if (fall.exists()) {
						law = fall.getAbrechnungsSystem();
					}
				}
			}
			
			private void resolvePayerType(){
				payerType = "TG";
				
				Rechnung r = Rechnung.load(invoiceId);
				if (r.exists()) {
					Fall fall = r.getFall();
					if (fall.exists()) {
						String kostentraeger = (String) fall.getInfoElement("Kostenträger");
						if (kostentraeger != null) {
							if (garantId != null && garantId.equals(kostentraeger)) {
								payerType = "TP";
								return;
							}
						}
					}
				}
			}
			
			private void updateViewer(){
				Control control = viewer.getControl();
				if (control != null && !control.isDisposed()) {
					control.getDisplay().asyncExec(new Runnable() {
						@Override
						public void run(){
							if (!control.isDisposed() && control.isVisible()) {
								viewer.refresh(invoiceEntry, true);
							}
						}
					});
				}
			}
		}
	}
}