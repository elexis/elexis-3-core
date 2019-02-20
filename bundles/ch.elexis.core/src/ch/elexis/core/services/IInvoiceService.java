package ch.elexis.core.services;

import java.util.List;

import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.rgw.tools.Result;

public interface IInvoiceService {
	
	/**
	 * Create an {@link IInvoice} from the list of {@link IEncounter}s billing information.
	 * 
	 * @param encounters
	 * @return
	 */
	public Result<IInvoice> invoice(final List<IEncounter> encounters);
	
	/**
	 * Cancel the invoice. If reopen is set the {@link IEncounter}s referenced by the
	 * {@link IInvoice} will be opened for changes.
	 * 
	 * @param invoice
	 * @param reopen
	 * @return
	 */
	public List<IEncounter> cancel(IInvoice invoice, boolean reopen);
}
