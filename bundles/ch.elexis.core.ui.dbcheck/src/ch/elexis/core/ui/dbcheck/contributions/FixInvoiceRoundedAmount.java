package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.model.IInvoice;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Money;

public class FixInvoiceRoundedAmount extends ExternalMaintenance {

	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		IQuery<IInvoice> invoicesQuery = CoreModelServiceHolder.get().getQuery(IInvoice.class);

		int counter = 0;

		try (IQueryCursor<IInvoice> cursor = invoicesQuery.executeAsCursor()) {
			pm.beginTask("Bitte warten, der Betrag aller Rechnungen wird überprüft", cursor.size());
			while (cursor.hasNext()) {
				IInvoice invoice = cursor.next();
				List<String> outputs = invoice.getTrace(Rechnung.OUTPUT);
				if(!outputs.isEmpty()) {
					Money openAmount = invoice.getOpenAmount();
					Money totalAmount = invoice.getTotalAmount();
					
					if (!openAmount.isZero() && Math.abs(openAmount.getCents()) < 5
							&& (Math.abs(new Money(totalAmount).roundTo5().getCents())
									- Math.abs(totalAmount.getCents()) != 0)) {
						invoice.setTotalAmount(totalAmount.roundTo5());
						CoreModelServiceHolder.get().save(invoice);
						counter++;
					}
				}
				pm.worked(1);
				if (pm.isCanceled()) {
					return "Abgebrochen ...";
				}
			}
		}
		return "Betrag von " + counter + " Rechnungen korrigiert";
	}

	@Override
	public String getMaintenanceDescription() {
		return "Rundung des Betrags bei bezahlten Rechnungen korrigieren";
	}

}
