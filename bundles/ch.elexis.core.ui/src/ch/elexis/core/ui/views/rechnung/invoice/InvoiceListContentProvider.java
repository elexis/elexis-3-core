package ch.elexis.core.ui.views.rechnung.invoice;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.BusyIndicator;
import org.eclipse.swt.widgets.Control;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.views.rechnung.Messages;
import ch.elexis.core.ui.views.rechnung.RnFilterDialog;
import ch.elexis.core.ui.views.rechnung.invoice.InvoiceListContentProvider.InvoiceEntry.QueryBuilder;
import ch.elexis.data.DBConnection;
import ch.elexis.data.Fall;
import ch.elexis.data.Kontakt;
import ch.elexis.data.Mandant;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.JdbcLink;
import ch.rgw.tools.Money;
import ch.rgw.tools.TimeTool;

public class InvoiceListContentProvider implements IStructuredContentProvider {
	
	private List<InvoiceEntry> currentContent = new ArrayList<InvoiceEntry>();
	private TableViewer structuredViewer;
	private InvoiceListHeaderComposite invoiceListHeaderComposite;
	private InvoiceListBottomComposite invoiceListBottomComposite;
	
	private TimeTool invoiceDateFrom;
	private TimeTool invoiceDateTo;
	private TimeTool invoiceStateDateFrom;
	private TimeTool invoiceStateDateTo;
	
	public InvoiceListContentProvider(TableViewer tableViewerInvoiceList,
		InvoiceListHeaderComposite invoiceListHeaderComposite,
		InvoiceListBottomComposite invoiceListBottomComposite){
		this.structuredViewer = tableViewerInvoiceList;
		this.invoiceListHeaderComposite = invoiceListHeaderComposite;
		this.invoiceListBottomComposite = invoiceListBottomComposite;
	}
	
	@Override
	public void dispose(){
		structuredViewer = null;
		currentContent = null;
	}
	
	private static final String SQL_CONDITION_INVOICE_FALL_PATIENT =
		"r.fallid IN (SELECT fa.id FROM faelle fa WHERE fa.PatientID = ?)";
	private static final String SQL_CONDITION_INVOICE_NUMBER = "r.RnNummer = ?";
	private static final String SQL_CONDITION_INVOICE_MANDANT = "r.MandantId = ?";
	private static final String SQL_CONDITION_INVOICE_DATE_SINCE = "r.RnDatum >= ?";
	private static final String SQL_CONDITION_INVOICE_DATE_UNTIL =
		"r.RnDatum >= ? AND r.RnDatum <= ?";
	private static final String SQL_CONDITION_INVOICE_STATEDATE_SINCE = "r.StatusDatum >= ?";
	private static final String SQL_CONDITION_INVOICE_STATEDATE_UNTIL =
		"r.StatusDatum >= ? AND r.StatusDatum <= ?";
	private static final String SQL_CONDITION_INVOICE_STATE_IN = "r.RnStatus IN ( ? )";
	private static final String SQL_CONDITION_INVOICE_AMOUNT_UNTIL =
		"CAST(r.betrag AS SIGNED) >= ? AND CAST(r.betrag AS SIGNED) <= ?";
	private static final String SQL_CONDITION_INVOICE_AMOUNT_GREATER =
		"CAST(r.betrag AS SIGNED) >= ?";
	private static final String SQL_CONDITION_INVOICE_AMOUNT_LESSER =
		"CAST(r.betrag AS SIGNED) <= ?";
	private static final String SQL_CONDITION_INVOICE_TYPE_TP = "FallGarantId = FallKostentrID";
	private static final String SQL_CONDITION_INVOICE_TYPE_TG = "NOT(FallGarantId = FallKostentrID)";
	private static final String SQL_CONDITION_BILLING_SYSTEM = "FallGesetz = ?";
	//@formatter:on
	
	public static String orderBy = "";
	private static int queryLimit = 1000;
	
	public Action rnFilterAction =
		new Action(Messages.RnActions_filterListAction, Action.AS_CHECK_BOX) {
			{
				setImageDescriptor(Images.IMG_FILTER.getImageDescriptor());
				setToolTipText(Messages.RnActions_filterLIstTooltip);
			}
			
			public void run(){
				if (isChecked()) {
					RnFilterDialog rfd = new RnFilterDialog(UiDesk.getTopShell(), false);
					if (rfd.open() == Dialog.OK) {
						invoiceDateFrom = rfd.getInvoiceDateFrom();
						invoiceDateTo = rfd.getInvoiceDateTo();
						invoiceStateDateFrom = rfd.getInvoiceStateDateFrom();
						invoiceStateDateTo = rfd.getInvoiceStateDateTo();
					}
				} else {
					invoiceDateFrom = null;
					invoiceDateTo = null;
					invoiceStateDateFrom = null;
					invoiceDateTo = null;
				}
				reload();
			};
		};
	
	private final Runnable reloadRunnable = new Runnable() {
		
		@Override
		public void run(){
			currentContent.clear();
			
			DBConnection dbConnection = PersistentObject.getDefaultConnection();
			
			int countInvoicesWoLimit = 0;
			int countPatientsWoLimit = 0;
			
			QueryBuilder queryBuilder = performPreparedStatementReplacements(
				InvoiceListSqlQuery.getSqlCountStats(true), false, false);
			PreparedStatement ps = queryBuilder.createPreparedStatement(dbConnection);
			System.out.println(ps);
			try (ResultSet res = ps.executeQuery()) {
				while (res.next()) {
					countInvoicesWoLimit = res.getInt(1);
					countPatientsWoLimit = res.getInt(2);
				}
			} catch (SQLException e) {
				ElexisStatus elexisStatus = new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
					CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, "Count stats failed", e);
				ElexisEventDispatcher.fireElexisStatusEvent(elexisStatus);
				
				System.out.println(ps); // to ease on-premise debugging
				return;
			} finally {
				dbConnection.releasePreparedStatement(ps);
			}
			
			boolean limitReached = (queryLimit > 0 && countInvoicesWoLimit >= queryLimit);
			if (limitReached) {
				invoiceListHeaderComposite.setLimitWarning(queryLimit);
				MessageDialog.openInformation(UiDesk.getTopShell(), "Query-Limit reached",
					"Query limit of set. You will only see the first " + queryLimit + " results.");
			} else {
				invoiceListHeaderComposite.setLimitWarning(null);
				structuredViewer.getTable().setItemCount(countInvoicesWoLimit);
			}
			
			queryBuilder =
				performPreparedStatementReplacements(InvoiceListSqlQuery.getSqlFetch(), true, true);
			ps = queryBuilder.createPreparedStatement(dbConnection);
			System.out.println(ps);
			int openAmounts = 0;
			int owingAmounts = 0;
			Set<String> countPatients = new HashSet<>();
			
			try (ResultSet res = ps.executeQuery()) {
				while (res.next()) {
					String invoiceId = res.getString(1);
					String invoiceNumber = res.getString(2);
					String dateFrom = res.getString(3);
					String dateTo = res.getString(4);
					int invoiceStatus = res.getInt(5);
					int totalAmount = res.getInt(6);
					String patientId = res.getString(7);
					countPatients.add(patientId);
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
					
					String invoiceStateDate = res.getString(19);
					
					InvoiceEntry ie = new InvoiceEntry(structuredViewer, invoiceId, patientId,
						garantId, invoiceNumber, invoiceStatus, dateFrom, dateTo, totalAmount,
						openAmount, patientName, invoiceStateDate);
					currentContent.add(ie);
				}
			} catch (SQLException e) {
				ElexisStatus elexisStatus = new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
					CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, "Fetch results failed", e);
				ElexisEventDispatcher.fireElexisStatusEvent(elexisStatus);
				System.out.println(ps); // to ease on-premise debugging
			} finally {
				dbConnection.releasePreparedStatement(ps);
			}
			
			if (limitReached) {
				invoiceListBottomComposite.update(
					countPatients.size() + " (" + Integer.toString(countPatientsWoLimit) + ")",
					queryLimit + " (" + Integer.toString(countInvoicesWoLimit) + ")",
					new Money(openAmounts).getAmountAsString(),
					new Money(owingAmounts).getAmountAsString());
			} else {
				invoiceListBottomComposite.update(Integer.toString(countPatientsWoLimit),
					Integer.toString(countInvoicesWoLimit),
					new Money(openAmounts).getAmountAsString(),
					new Money(owingAmounts).getAmountAsString());
			}
			
			structuredViewer.setInput(currentContent);
		}
	};
	
	public void reload(){
		BusyIndicator.showWhile(UiDesk.getDisplay(), reloadRunnable);
	}
	
	private QueryBuilder performPreparedStatementReplacements(String preparedStatement,
		boolean includeLimitReplacement, boolean includeOrderReplacement){
		QueryBuilder queryBuilder = determinePreparedStatementConditionals();
			
		if (includeOrderReplacement) {
			preparedStatement = preparedStatement.replaceAll("REPLACE_WITH_ORDER", orderBy);
		}
		
		if (includeLimitReplacement) {
			if (queryLimit > 0) {
				queryBuilder.setMainQuery(preparedStatement.replaceAll("REPLACE_WITH_LIMIT",
					" LIMIT " + Integer.toString(queryLimit)));
			} else {
				queryBuilder.setMainQuery(preparedStatement.replaceAll("REPLACE_WITH_LIMIT", ""));
			}
		} else {
			queryBuilder.setMainQuery(preparedStatement);
		}
		return queryBuilder;
	}
	
	private QueryBuilder determinePreparedStatementConditionals(){
		QueryBuilder queryBuilder = QueryBuilder.create();
		if (CoreHub.acl.request(AccessControlDefaults.ACCOUNTING_GLOBAL) == false) {
			Mandant selectedMandator = ElexisEventDispatcher.getSelectedMandator();
			if (selectedMandator != null) {
				queryBuilder.build(SQL_CONDITION_INVOICE_MANDANT, selectedMandator.getId());
			}
		}
		
		if (invoiceDateFrom != null) {
			if (invoiceDateTo != null) {
				queryBuilder.build(SQL_CONDITION_INVOICE_DATE_UNTIL,
					invoiceDateFrom.toString(TimeTool.DATE_COMPACT),
					invoiceDateTo.toString(TimeTool.DATE_COMPACT));
			} else {
				queryBuilder.build(SQL_CONDITION_INVOICE_DATE_SINCE,
					invoiceDateFrom.toString(TimeTool.DATE_COMPACT));
			}
		}
		
		if (invoiceStateDateFrom != null) {
			if (invoiceStateDateTo != null) {
				queryBuilder.build(SQL_CONDITION_INVOICE_STATEDATE_UNTIL,
					invoiceStateDateFrom.toString(TimeTool.DATE_COMPACT),
					invoiceStateDateTo.toString(TimeTool.DATE_COMPACT));
			} else {
				queryBuilder.build(SQL_CONDITION_INVOICE_STATEDATE_SINCE,
					invoiceStateDateFrom.toString(TimeTool.DATE_COMPACT));
			}
		}
		
		Integer invoiceStateNo = invoiceListHeaderComposite.getSelectedInvoiceStateNo();
		if (invoiceStateNo != null) {
			if (InvoiceState.OWING.numericValue() == invoiceStateNo) {
				queryBuilder.build(SQL_CONDITION_INVOICE_STATEDATE_UNTIL,
					invoiceStateDateFrom.toString(TimeTool.DATE_COMPACT),
					invoiceStateDateTo.toString(TimeTool.DATE_COMPACT));
				
				String conditional = Arrays.asList(InvoiceState.owingStates()).stream()
					.map(is -> Integer.toString(is.numericValue())).reduce((u, t) -> u + " ," + t)
					.get();
				queryBuilder.build(SQL_CONDITION_INVOICE_STATE_IN, conditional);
				
			} else if (InvoiceState.TO_PRINT.numericValue() == invoiceStateNo) {
				String conditional = Arrays.asList(InvoiceState.toPrintStates()).stream()
					.map(is -> Integer.toString(is.numericValue())).reduce((u, t) -> u + " ," + t)
					.get();
				queryBuilder.build(SQL_CONDITION_INVOICE_STATE_IN, conditional);
			} else {
				queryBuilder.build(SQL_CONDITION_INVOICE_STATE_IN,
					Integer.toString(invoiceStateNo));
			}
		}
		
		String invoiceId = invoiceListHeaderComposite.getSelectedInvoiceId();
		if (StringUtils.isNumeric(invoiceId)) {
			queryBuilder.build(SQL_CONDITION_INVOICE_NUMBER, invoiceId);
		}
		
		String patientId = invoiceListHeaderComposite.getSelectedPatientId();
		if (patientId != null) {
			queryBuilder.build(SQL_CONDITION_INVOICE_FALL_PATIENT, patientId);
		}
		
		String totalAmount = invoiceListHeaderComposite.getSelectedTotalAmount();
		if (StringUtils.isNotBlank(totalAmount)) {
			String boundaryAmount = null;
			
			try {
				if (totalAmount.startsWith(">")) {
					// greater
					totalAmount = totalAmount.substring(1, totalAmount.length());
					Money totalMoney = new Money(totalAmount);
					queryBuilder.build(SQL_CONDITION_INVOICE_AMOUNT_GREATER,
						totalMoney.getCents());
				}
				else if (totalAmount.startsWith("<")) {
					// lesser
					totalAmount = totalAmount.substring(1, totalAmount.length());
					Money totalMoney = new Money(totalAmount);
					queryBuilder.build(SQL_CONDITION_INVOICE_AMOUNT_LESSER,
						totalMoney.getCents());
				}
				else if (totalAmount.contains("-")) {
					String[] split = totalAmount.split("-");
					Money totalMoney = null;
					
					if (split.length > 0) {
						totalAmount = split[0];
						totalMoney = new Money(totalAmount);
					}
					if (totalMoney != null && split.length > 1) {
						boundaryAmount = split[1];
						// until
						Money boundaryMoney = new Money(boundaryAmount);
						queryBuilder.build(SQL_CONDITION_INVOICE_AMOUNT_UNTIL,
							totalMoney.getCents(), boundaryMoney.getCents());
					}
				}
				else {
					// equal
					Money totalMoney = new Money(totalAmount);
					queryBuilder.build(SQL_CONDITION_INVOICE_AMOUNT_UNTIL,
						totalMoney.getCents(), totalMoney.getCents());
				}
			} catch (ParseException e) {
				// invalid value entered - do nothing
			}
		}
		
		String type = invoiceListHeaderComposite.getSelectedInvoiceType();
		if (type != null) {
			QueryBuilder qb;
			if ("TP".equals(type)) {
				qb = queryBuilder.build(SQL_CONDITION_INVOICE_TYPE_TP, (String) null);
			} else {
				qb = queryBuilder.build(SQL_CONDITION_INVOICE_TYPE_TG, (String) null);
			}
			qb.setInnerCondition(false);
		}
		
		String billingSystem = invoiceListHeaderComposite.getSelectedBillingSystem();
		if (billingSystem != null) {
			QueryBuilder qb = queryBuilder.build(SQL_CONDITION_BILLING_SYSTEM, billingSystem);
			qb.setInnerCondition(false);
		}
		
		return queryBuilder;
	}
	
	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput){}
	
	@Override
	public Object[] getElements(Object inputElement){
		if (inputElement instanceof List<?>) {
			return currentContent.toArray();
		}
		return Collections.emptyList().toArray();
	}
	
	public void setSortOrderAndDirection(Object data, int sortDirection){
		String sortDirectionString = (SWT.UP == sortDirection) ? "ASC" : "DESC";
		if (InvoiceListSqlQuery.VIEW_FLD_INVOICENO.equals(data)) {
			orderBy = "ORDER BY LENGTH(" + InvoiceListSqlQuery.VIEW_FLD_INVOICENO + ") "
				+ sortDirectionString + "," + InvoiceListSqlQuery.VIEW_FLD_INVOICENO + " "
				+ sortDirectionString;
		} else if (Rechnung.BILL_DATE_FROM.equals(data)
			|| InvoiceListSqlQuery.VIEW_FLD_INVOICETOTAL.equals(data)
			|| InvoiceListSqlQuery.VIEW_FLD_OPENAMOUNT.equals(data)) {
			orderBy = "ORDER BY " + data + " " + sortDirectionString;
		} else if (Kontakt.FLD_NAME1.equals(data)) {
			orderBy =
				"ORDER BY PatName1 " + sortDirectionString + ", PatName2 " + sortDirectionString;
		} else if (InvoiceListSqlQuery.VIEW_FLD_INVOICESTATEDATE.equals(data)) {
			orderBy = "ORDER BY " + data + " " + sortDirectionString;
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
		private String billingSystem; // req resolv
		private String garantLabel; // req resolv
		private int invoiceStateSinceDays;
		
		public InvoiceEntry(StructuredViewer viewer, String invoiceId, String patientId,
			String garantId, String invoiceNumber, int invoiceStatus, String dateFrom,
			String dateTo, int totalAmount, int openAmount, String patientName, String stateDate){
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
			if (StringUtils.isNumeric(stateDate)) {
				this.invoiceStateSinceDays = new TimeTool(stateDate).daysTo(new TimeTool());
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
			if (garantId != null && garantId.equals(patientId)) {
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
		
		public String getBillingSystem(){
			if (!isResolved()) {
				return "...";
			}
			return billingSystem;
		}
		
		public String getPatientName(){
			return patientName;
		}
		
		public String getInvoiceId(){
			return invoiceId;
		}
		
		public int getInvoiceStateSinceDays(){
			return invoiceStateSinceDays;
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
						billingSystem = fall.getAbrechnungsSystem();
					}
				}
			}
			
			private void resolvePayerType(){
				payerType = "TG";
				
				Rechnung r = Rechnung.load(invoiceId);
				if (r.exists()) {
					Fall fall = r.getFall();
					if (fall.exists()) {
						Kontakt costBearer = fall.getCostBearer();
						if (costBearer != null) {
							if (garantId != null && garantId.equals(costBearer.getId())) {
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
								viewer.update(invoiceEntry, null);
							}
						}
					});
				}
			}
		}
		
		static class QueryBuilder {
			private boolean isInnerCondition = true;
			private String mainQuery;
			private String condition;
			private Object[] values;
			private List<QueryBuilder> queryBuilders;
			
			public static QueryBuilder create(){
				return new QueryBuilder();
			}
			
			private QueryBuilder(){
				queryBuilders = new ArrayList<>();
			}
			
			private QueryBuilder(String condition, Object... values){
				this.condition = condition;
				this.values = values;
			}
			
			public QueryBuilder build(String query, Object... values){
				QueryBuilder queryBuilder = new QueryBuilder(query, values);
				queryBuilders.add(queryBuilder);
				return queryBuilder;
			}
			
			public void setInnerCondition(boolean isInnerCondition){
				this.isInnerCondition = isInnerCondition;
			}
			
			public boolean isInnerCondition(){
				return isInnerCondition;
			}
			
			private Object[] getValue(){
				return values;
			}
			
			private String getCondition(){
				return condition;
			}
			
			public void setMainQuery(String mainQuery){
				this.mainQuery = mainQuery;
			}
			
			public String getQuery(){
				if (queryBuilders != null && mainQuery != null) {
					StringBuilder sbInner = new StringBuilder();
					StringBuilder sbOuter = new StringBuilder();
					for (QueryBuilder builder : queryBuilders) {
						StringBuilder used = builder.isInnerCondition() ? sbInner: sbOuter;
						if (used.length() > 0) {
							used.append(" AND ");
						}
						used.append(builder.getCondition());
					}
					String mainQueryRet;
					if (sbInner.length() > 0) {
						mainQueryRet = mainQuery.replace(
							InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION,
							" AND " + sbInner.toString());
					} else {
						mainQueryRet =  mainQuery
							.replace(InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION, "");
					}
					if(sbOuter.length() > 0) {
						mainQueryRet = mainQueryRet.replace(InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION, " WHERE "+sbOuter.toString());
					} else {
						mainQueryRet = mainQueryRet.replace(InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION, "");
					}
					return mainQueryRet;
				}
				return "";
			}
			
			/**
			 * Creates a prepared statement and set all values as string with the query builder
			 * 
			 * @param dbConnection
			 * @return
			 */
			public PreparedStatement createPreparedStatement(DBConnection dbConnection){
				if (queryBuilders != null) {
					String query = getQuery();
					boolean isPostgres = JdbcLink.DBFLAVOR_POSTGRESQL
						.equalsIgnoreCase(PersistentObject.getDefaultConnection().getDBFlavor());
					if (isPostgres) {
						// replace with postgres compatible type
						query = query.replaceAll("SIGNED", "NUMERIC");
					}
					PreparedStatement ps = dbConnection.getPreparedStatement(query);
					int i = 1;
					for (QueryBuilder qb : queryBuilders) {
						for (Object s : qb.getValue()) {
							try {
								if (s instanceof String) {
									ps.setString(i++, (String) s);
								} else if (s instanceof Long) {
									ps.setLong(i++, (Long) s);
								} else if (s instanceof Integer) {
									ps.setInt(i++, (Integer) s);
								} else if (s instanceof Double) {
									ps.setDouble(i++, (Double) s);
								}
							} catch (SQLException e) {
								/** ignore **/
							}
						}
					}
					return ps;
				}
				return null;
			}
		}
	}
}