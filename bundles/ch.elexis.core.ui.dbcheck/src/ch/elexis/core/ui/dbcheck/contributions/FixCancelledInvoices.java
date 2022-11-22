package ch.elexis.core.ui.dbcheck.contributions;

import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.model.builder.IAccountTransactionBuilder;
import ch.elexis.core.services.IInvoiceService;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQueryCursor;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;

public class FixCancelledInvoices extends ExternalMaintenance {

	/**
	 * Lookup {@link IPayment} with Storno remark, missing corresponding
	 * {@link IAccountTransaction} with Storno. Due to bug #24778 of
	 * {@link IInvoiceService} cancel.
	 *
	 */
	@Override
	public String executeMaintenance(IProgressMonitor pm, String DBVersion) {
		StringBuilder output = new StringBuilder();

		int checked = 0;
		int fixed = 0;

		IQuery<IPayment> paymentQuery = CoreModelServiceHolder.get().getQuery(IPayment.class);
		paymentQuery.and("remark", COMPARATOR.EQUALS, "Storno");
		try (IQueryCursor<IPayment> payments = paymentQuery.executeAsCursor()) {
			pm.beginTask("Fixing cancelled invoice transactions", payments.size());
			while (payments.hasNext()) {
				checked++;
				IPayment payment = payments.next();
				IQuery<IAccountTransaction> transactionQuery = CoreModelServiceHolder.get()
						.getQuery(IAccountTransaction.class);
				transactionQuery.and("zahlung", COMPARATOR.EQUALS, payment);
				List<IAccountTransaction> matchingTransactions = transactionQuery.execute();
				if (matchingTransactions.isEmpty()) {
					new IAccountTransactionBuilder(CoreModelServiceHolder.get(), payment).buildAndSave();
					fixed++;
				}
				pm.worked(1);
				if (pm.isCanceled()) {
					break;
				}
			}
		}
		pm.done();
		output.append("Checked " + checked + " storno payments, and added " + fixed + " storno transactions");
		return output.toString();
	}

	@Override
	public String getMaintenanceDescription() {
		return "Rechnungs Storno, fehlende Buchungen zu Zahlungen erstellen";
	}

}
