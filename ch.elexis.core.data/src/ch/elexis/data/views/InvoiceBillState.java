/*******************************************************************************
 * Copyright (c) 2017 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.data.views;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.concurrent.atomic.AtomicInteger;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.data.DBConnection;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;

/**
 * Aggregates the information of all Invoices, payments and states in a single SQL view. Used by
 * InvoiceListView in core.ui.
 *
 */
public class InvoiceBillState {
	
	public static final String VIEW_NAME = "INVOICE_BILL_STATE";
	
	public static final String VIEW_FLD_INVOICENO = "InvoiceNo";
	public static final String VIEW_FLD_INVOICETOTAL = "InvoiceTotal";
	public static final String VIEW_FLD_OPENAMOUNT = "openAmount";
	public static final String VIEW_FLD_INVOICESTATE = "InvoiceState";
	public static final String VIEW_FLD_INVOICEDATE = Rechnung.BILL_DATE;
	public static final String VIEW_FLD_INVOICESTATEDATE = Rechnung.BILL_STATE_DATE;
	
	private static final String COUNT_STATS_MYSQL =
		"SELECT COUNT(InvoiceId), COUNT(DISTINCT (patientid)), SUM(invoiceTotal), SUM(openAmount) FROM "
			+ VIEW_NAME;
	
	/**
	 * Initializes the SQL view if required (i.e. not already existing)
	 */
	public static void initializeSqlViewIfRequired(){
		if (!PersistentObject.tableExists(VIEW_NAME, true)) {
			PersistentObject.executeDBInitScriptForClass(InvoiceBillState.class, null);
		}
	}
	
	public static void fetchNumberOfPatientsAndInvoices(AtomicInteger countPatients,
		AtomicInteger countInvoices) throws IOException{
		DBConnection dbConnection = PersistentObject.getDefaultConnection();
		
		PreparedStatement ps = dbConnection.getPreparedStatement(COUNT_STATS_MYSQL);
		try (ResultSet res = ps.executeQuery()) {
			while (res.next()) {
				countInvoices.set(res.getInt(1));
				countPatients.set(res.getInt(2));
			}
		} catch (SQLException e) {
			ElexisStatus elexisStatus = new ElexisStatus(org.eclipse.core.runtime.Status.ERROR,
				CoreHub.PLUGIN_ID, ElexisStatus.CODE_NONE, "Count stats failed", e);
			ElexisEventDispatcher.fireElexisStatusEvent(elexisStatus);
		} finally {
			dbConnection.releasePreparedStatement(ps);
		}
		
	}
	
}
