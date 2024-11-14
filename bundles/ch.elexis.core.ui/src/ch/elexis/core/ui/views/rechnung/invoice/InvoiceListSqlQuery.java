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
import java.util.Arrays;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.ac.ACEAccessBitMapConstraint;
import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.status.ElexisStatus;
import ch.elexis.data.DBConnection;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.JdbcLink;

public class InvoiceListSqlQuery {

	public static final String VIEW_FLD_INVOICENO = "InvoiceNo"; //$NON-NLS-1$
	public static final String VIEW_FLD_INVOICETOTAL = "InvoiceTotal"; //$NON-NLS-1$
	public static final String VIEW_FLD_OPENAMOUNT = "openAmount"; //$NON-NLS-1$
	public static final String VIEW_FLD_INVOICESTATE = "InvoiceState"; //$NON-NLS-1$
	public static final String VIEW_FLD_INVOICEDATE = Rechnung.BILL_DATE;
	public static final String VIEW_FLD_INVOICESTATEDATE = Rechnung.BILL_STATE_DATE;

	public static final String REPLACEMENT_INVOICE_INNER_CONDITION = "REPLACE_WITH_INVOICE_INNER_CONDITION"; //$NON-NLS-1$
	public static final String REPLACEMENT_OUTER_CONDITION = "REPLACE_WITH_OUTER_CONDITION"; //$NON-NLS-1$

	public static String getSqlCountStats(boolean withConditions) {
		return "SELECT COUNT(InvoiceId), COUNT(DISTINCT (patientid)), SUM(invoiceTotal), SUM(openAmount) FROM " //$NON-NLS-1$
				+ InvoiceListSqlQuery.getSqlInvoice(withConditions);
	}

	public static String getSqlInvoice(boolean withConditions) {
		String query = null;
		DBConnection dbConnection = PersistentObject.getDefaultConnection();
		if (dbConnection != null && JdbcLink.DBFLAVOR_POSTGRESQL.equalsIgnoreCase(dbConnection.getDBFlavor())) {
			query = "( SELECT rz.id AS InvoiceId, rz.RnNummer AS InvoiceNo, rz.rndatum, rz.rndatumvon, rz.rndatumbis, rz.statusdatum, rz.InvoiceState, rz.InvoiceTotal, rz.MandantId, f.patientid AS PatientId, k.bezeichnung1 AS PatName1, k.bezeichnung2 AS PatName2, k.geschlecht AS PatSex, k.geburtsdatum AS PatDob, f.id AS FallId, f.gesetz AS FallGesetz, CASE WHEN (f.garantID IS NULL) THEN f.patientid ELSE f.garantID END FallGarantId, f.KostentrID AS FallKostentrID, rz.paymentCount, rz.paidAmount, rz.openAmount FROM (SELECT r.id, r.rnnummer, r.rndatum, r.rndatumvon, r.rndatumbis, r.statusdatum, r.fallid, r.MandantId, CAST(r.rnstatus AS NUMERIC) AS InvoiceState, CAST(r.betrag AS NUMERIC) AS InvoiceTotal, COUNT(z.id) AS paymentCount, CASE WHEN COUNT(z.id) = '0' THEN 0 ELSE SUM(CAST(z.betrag AS NUMERIC)) END paidAmount, CASE WHEN COUNT(z.id) = '0' THEN CAST(r.betrag AS NUMERIC) ELSE (CAST(r.betrag AS NUMERIC) - SUM(CAST(z.betrag AS NUMERIC))) END openAmount FROM RECHNUNGEN r LEFT JOIN zahlungen z ON z.rechnungsID = r.id AND z.deleted = '0' WHERE r.deleted = '0'" //$NON-NLS-1$
					+ StringUtils.SPACE + InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION + addAobo()
					+ " GROUP BY r.id) rz LEFT JOIN faelle f ON rz.FallID = f.ID LEFT JOIN kontakt k ON f.PatientID = k.id)x " //$NON-NLS-1$
					+ InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION + StringUtils.SPACE;
		} else {
			query = "( SELECT rz.id AS InvoiceId, rz.RnNummer AS InvoiceNo, rz.rndatum, rz.rndatumvon, rz.rndatumbis, rz.statusdatum, rz.InvoiceState, rz.InvoiceTotal, rz.MandantId, f.patientid AS PatientId, k.bezeichnung1 AS PatName1, k.bezeichnung2 AS PatName2, k.geschlecht AS PatSex, k.geburtsdatum AS PatDob, f.id AS FallId, f.gesetz AS FallGesetz, CASE WHEN (f.garantID IS NULL) THEN f.patientid ELSE f.garantID END FallGarantId, f.KostentrID AS FallKostentrID, rz.paymentCount, rz.paidAmount, rz.openAmount FROM (SELECT r.id, r.rnnummer, r.rndatum, r.rndatumvon, r.rndatumbis, r.statusdatum, r.fallid, r.MandantId, CAST(r.rnstatus AS SIGNED) AS InvoiceState, CAST(r.betrag AS SIGNED) AS InvoiceTotal, COUNT(z.id) AS paymentCount, CASE WHEN COUNT(z.id) = 0 THEN 0 ELSE SUM(CAST(z.betrag AS SIGNED)) END paidAmount, CASE WHEN COUNT(z.id) = 0 THEN CAST(r.betrag AS SIGNED) ELSE (CAST(r.betrag AS SIGNED) - SUM(CAST(z.betrag AS SIGNED))) END openAmount FROM RECHNUNGEN r LEFT JOIN zahlungen z ON z.rechnungsID = r.id AND z.deleted = 0 WHERE r.deleted = 0" //$NON-NLS-1$
					+ StringUtils.SPACE + InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION + addAobo()
					+ " GROUP BY r.id) rz LEFT JOIN faelle f ON rz.FallID = f.ID LEFT JOIN kontakt k ON f.PatientID = k.id )x " //$NON-NLS-1$
					+ InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION + StringUtils.SPACE;
		}
		if (!withConditions && query != null) {
			query = query.replaceAll(InvoiceListSqlQuery.REPLACEMENT_INVOICE_INNER_CONDITION, StringUtils.EMPTY);
			query = query.replace(InvoiceListSqlQuery.REPLACEMENT_OUTER_CONDITION, StringUtils.EMPTY);
		}
		return query;
	}

	private static String addAobo() {
		Optional<ACEAccessBitMapConstraint> aoboOrSelf = AccessControlServiceHolder.get()
				.isAoboOrSelf(EvACE.of(IInvoice.class, Right.READ));
		if (aoboOrSelf.isPresent()) {
			if (aoboOrSelf.get() == ACEAccessBitMapConstraint.AOBO) {
				String list = ConfigServiceHolder.get().getActiveUserContact("rechnungsliste/mandantenfiltered", //$NON-NLS-1$
						StringUtils.EMPTY);
				if (!list.isBlank()) {
					return " AND (r.MandantID IN (" //$NON-NLS-1$
							+ Arrays.stream(list.split(",")).map(s -> "'" + s.trim() + "'") //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
									.collect(Collectors.joining(",")) //$NON-NLS-1$
							+ ") OR r.MandantID is null)"; //$NON-NLS-1$

				}
				return " AND (r.MandantID IN (" + AccessControlServiceHolder.get().getAoboMandatorIdsForSqlIn().stream() //$NON-NLS-1$
						.map(s -> "\'" + s + "\'").collect(Collectors.joining(",")) + ") OR r.MandantID is null)"; //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
			} else if (aoboOrSelf.get() == ACEAccessBitMapConstraint.SELF) {
				return " AND (r.MandantID = \'" + AccessControlServiceHolder.get().getSelfMandatorId() //$NON-NLS-1$
						+ "\' OR r.MandantID is null)"; //$NON-NLS-1$
			}
		}
		return StringUtils.EMPTY;
	}

	public static String getSqlFetch() { //@formatter:off
		 return 	" SELECT " + //$NON-NLS-1$
					"    InvoiceId," + //$NON-NLS-1$
					InvoiceListSqlQuery.VIEW_FLD_INVOICENO+"," + //$NON-NLS-1$
					"    rndatumvon," + //$NON-NLS-1$
					"    rndatumbis," + //$NON-NLS-1$
					InvoiceListSqlQuery.VIEW_FLD_INVOICESTATE+"," + //$NON-NLS-1$
					InvoiceListSqlQuery.VIEW_FLD_INVOICETOTAL+"," + //$NON-NLS-1$
					"    PatientId," + //$NON-NLS-1$
					"    PatName1," + //$NON-NLS-1$
					"    PatName2," + //$NON-NLS-1$
					"    PatSex," + //$NON-NLS-1$
					"    PatDob," + //$NON-NLS-1$
					"    FallId," + //$NON-NLS-1$
					"    FallGesetz," + //$NON-NLS-1$
					"    FallGarantId," + //$NON-NLS-1$
					"    FallKostentrID," + //$NON-NLS-1$
					"    paymentCount," + //$NON-NLS-1$
					"    paidAmount," + //$NON-NLS-1$
					InvoiceListSqlQuery.VIEW_FLD_OPENAMOUNT + "," + //$NON-NLS-1$
					InvoiceListSqlQuery.VIEW_FLD_INVOICESTATEDATE +
					" FROM" + //$NON-NLS-1$
					StringUtils.SPACE +getSqlInvoice(true)+
					"REPLACE_WITH_ORDER " + //$NON-NLS-1$
					"REPLACE_WITH_LIMIT"; //$NON-NLS-1$
		//@formatter:on
	}

	public static void fetchNumberOfPatientsAndInvoices(AtomicInteger countPatients, AtomicInteger countInvoices)
			throws IOException {
		DBConnection dbConnection = PersistentObject.getDefaultConnection();

		PreparedStatement ps = dbConnection.getPreparedStatement(getSqlCountStats(false));
		try (ResultSet res = ps.executeQuery()) {
			while (res.next()) {
				countInvoices.set(res.getInt(1));
				countPatients.set(res.getInt(2));
			}
		} catch (SQLException e) {
			ElexisStatus elexisStatus = new ElexisStatus(org.eclipse.core.runtime.Status.ERROR, CoreHub.PLUGIN_ID,
					ElexisStatus.CODE_NONE, "Count stats failed", e); //$NON-NLS-1$
			ElexisStatus.fire(elexisStatus);
		} finally {
			dbConnection.releasePreparedStatement(ps);
		}
	}
}
