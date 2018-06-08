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
package ch.elexis.core.ui.views.rechnung.invoice;

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
import ch.rgw.tools.JdbcLink;

public class InvoiceListSqlQuery {
	
	public static final String VIEW_FLD_INVOICENO = "InvoiceNo";
	public static final String VIEW_FLD_INVOICETOTAL = "InvoiceTotal";
	public static final String VIEW_FLD_OPENAMOUNT = "openAmount";
	public static final String VIEW_FLD_INVOICESTATE = "InvoiceState";
	public static final String VIEW_FLD_INVOICEDATE = Rechnung.BILL_DATE;
	public static final String VIEW_FLD_INVOICESTATEDATE = Rechnung.BILL_STATE_DATE;
	
	public static final String REPLACEMENT_INVOICE_INNER_CONDITION =
		"REPLACE_WITH_INVOICE_INNER_CONDITION";
	public static final String REPLACEMENT_OUTER_CONDITION = "REPLACE_WITH_OUTER_CONDITION";
	
	public static String getSqlCountStats(boolean withConditions){
		return "SELECT COUNT(InvoiceId), COUNT(DISTINCT (patientid)), SUM(invoiceTotal), SUM(openAmount) FROM "
			+ InvoiceListSqlQuery.getSqlInvoice(withConditions);
	}
	
	public static String getSqlInvoice(boolean withConditions)
	{
		String query = null;
		DBConnection dbConnection = PersistentObject.getDefaultConnection();
		if (dbConnection != null && JdbcLink.DBFLAVOR_POSTGRESQL.equalsIgnoreCase(dbConnection.getDBFlavor())) {
			query =
				"( SELECT rz.id AS InvoiceId, rz.RnNummer AS InvoiceNo, rz.rndatum, rz.rndatumvon, rz.rndatumbis, rz.statusdatum, rz.InvoiceState, rz.InvoiceTotal, rz.MandantId, f.patientid AS PatientId, k.bezeichnung1 AS PatName1, k.bezeichnung2 AS PatName2, k.geschlecht AS PatSex, k.geburtsdatum AS PatDob, f.id AS FallId, f.gesetz AS FallGesetz, CASE WHEN (f.garantID IS NULL) THEN f.patientid ELSE f.garantID END FallGarantId, f.KostentrID AS FallKostentrID, rz.paymentCount, rz.paidAmount, rz.openAmount FROM (SELECT r.id, r.rnnummer, r.rndatum, r.rndatumvon, r.rndatumbis, r.statusdatum, r.fallid, r.MandantId, CAST(r.rnstatus AS NUMERIC) AS InvoiceState, CAST(r.betrag AS NUMERIC) AS InvoiceTotal, COUNT(z.id) AS paymentCount, CASE WHEN COUNT(z.id) = '0' THEN 0 ELSE SUM(CAST(z.betrag AS NUMERIC)) END paidAmount, CASE WHEN COUNT(z.id) = '0' THEN CAST(r.betrag AS NUMERIC) ELSE (CAST(r.betrag AS NUMERIC) - SUM(CAST(z.betrag AS NUMERIC))) END openAmount FROM RECHNUNGEN r LEFT JOIN zahlungen z ON z.rechnungsID = r.id AND z.deleted = '0' WHERE r.deleted = '0'"
				+ " " + InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION
					+ " GROUP BY r.id) rz LEFT JOIN faelle f ON rz.FallID = f.ID LEFT JOIN kontakt k ON f.PatientID = k.id)x "
					+ InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION+ " ";
		}
		else
		{
			query = "( SELECT rz.id AS InvoiceId, rz.RnNummer AS InvoiceNo, rz.rndatum, rz.rndatumvon, rz.rndatumbis, rz.statusdatum, rz.InvoiceState, rz.InvoiceTotal, rz.MandantId, f.patientid AS PatientId, k.bezeichnung1 AS PatName1, k.bezeichnung2 AS PatName2, k.geschlecht AS PatSex, k.geburtsdatum AS PatDob, f.id AS FallId, f.gesetz AS FallGesetz, CASE WHEN (f.garantID IS NULL) THEN f.patientid ELSE f.garantID END FallGarantId, f.KostentrID AS FallKostentrID, rz.paymentCount, rz.paidAmount, rz.openAmount FROM (SELECT r.id, r.rnnummer, r.rndatum, r.rndatumvon, r.rndatumbis, r.statusdatum, r.fallid, r.MandantId, CAST(r.rnstatus AS SIGNED) AS InvoiceState, CAST(r.betrag AS SIGNED) AS InvoiceTotal, COUNT(z.id) AS paymentCount, CASE WHEN COUNT(z.id) = 0 THEN 0 ELSE SUM(CAST(z.betrag AS SIGNED)) END paidAmount, CASE WHEN COUNT(z.id) = 0 THEN CAST(r.betrag AS SIGNED) ELSE (CAST(r.betrag AS SIGNED) - SUM(CAST(z.betrag AS SIGNED))) END openAmount FROM RECHNUNGEN r LEFT JOIN zahlungen z ON z.rechnungsID = r.id AND z.deleted = 0 WHERE r.deleted = 0"
			+ " " + InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION
					+ " GROUP BY r.id) rz LEFT JOIN faelle f ON rz.FallID = f.ID LEFT JOIN kontakt k ON f.PatientID = k.id )x "
			+ InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION+ " ";
		}
		if (!withConditions && query != null) {
			query = query.replaceAll(InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION, "");
			query = query.replace(InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION, "");
		}
		return query;
	}
	
	public static String getSqlFetch()
	{ //@formatter:off
		 return 	" SELECT " + 
					"    InvoiceId," + 
					InvoiceListSqlQuery.VIEW_FLD_INVOICENO+"," + 
					"    rndatumvon," + 
					"    rndatumbis," + 
					InvoiceListSqlQuery.VIEW_FLD_INVOICESTATE+"," + 
					InvoiceListSqlQuery.VIEW_FLD_INVOICETOTAL+"," + 
					"    PatientId," + 
					"    PatName1," + 
					"    PatName2," + 
					"    PatSex," + 
					"    PatDob," + 
					"    FallId," + 
					"    FallGesetz," + 
					"    FallGarantId," + 
					"    FallKostentrID," + 
					"    paymentCount," + 
					"    paidAmount," + 
					InvoiceListSqlQuery.VIEW_FLD_OPENAMOUNT + "," +
					InvoiceListSqlQuery.VIEW_FLD_INVOICESTATEDATE +
					" FROM" + 
					" " +getSqlInvoice(true)+
					"REPLACE_WITH_ORDER " + 
					"REPLACE_WITH_LIMIT";
		//@formatter:on
	}
	
	public static void fetchNumberOfPatientsAndInvoices(AtomicInteger countPatients,
		AtomicInteger countInvoices) throws IOException{
		DBConnection dbConnection = PersistentObject.getDefaultConnection();
		
		PreparedStatement ps = dbConnection.getPreparedStatement(getSqlCountStats(false));
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
