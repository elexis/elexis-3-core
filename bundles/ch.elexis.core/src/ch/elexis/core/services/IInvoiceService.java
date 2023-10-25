package ch.elexis.core.services;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import ch.elexis.core.model.IAccountTransaction;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IPayment;
import ch.elexis.core.model.InvoiceState;
import ch.rgw.tools.Money;
import ch.rgw.tools.Result;

public interface IInvoiceService {

	/**
	 * Create an {@link IInvoice} from the list of {@link IEncounter}s billing
	 * information.
	 *
	 * @param encounters
	 * @return
	 */
	public Result<IInvoice> invoice(final List<IEncounter> encounters);

	/**
	 * Cancel the invoice. If reopen is set the {@link IEncounter}s referenced by
	 * the {@link IInvoice} will be opened for changes.
	 *
	 * @param invoice
	 * @param reopen
	 * @return
	 */
	public List<IEncounter> cancel(IInvoice invoice, boolean reopen);

	/**
	 * Get all {@link IInvoice}s the provided {@link IEncounter} was part of.
	 *
	 * @return
	 */
	public List<IInvoice> getInvoices(IEncounter encounter);

	/**
	 * Get the {@link IInvoice} with matching number.
	 *
	 * @param number
	 * @return
	 */
	public Optional<IInvoice> getInvoiceWithNumber(String number);

	/**
	 * Test if the invoice was in state {@link InvoiceState#CANCELLED} before the
	 * provided date.
	 *
	 * @param invoice
	 * @param date
	 * @return
	 */
	public boolean hasStornoBeforeDate(IInvoice invoice, LocalDate date);

	/**
	 * Get a unique id by combining {@link IPatient} code and {@link IInvoice}
	 * number for the invoice. Patient code and invoice number are padded with 0
	 * characters, result will have 12 characters.
	 *
	 * @return
	 */
	public String getCombinedId(IInvoice invoice);

	/**
	 * Remove the {@link IPayment} from the {@link IInvoice}. If there is a matching
	 * {@link IAccountTransaction} for the {@link IPayment} it is removed.
	 *
	 * @param payment
	 */
	public void removePayment(IPayment payment);

	/**
	 * Add a {@link IPayment} to the {@link IInvoice}. Also creates a
	 * {@link IAccountTransaction} for the {@link IPayment}. </br>
	 * The state of the {@link IInvoice} is updated. For example if there is nothing
	 * left to pay, the invoice state is set to {@link InvoiceState#PAID}.
	 * 
	 *
	 * @param invoice
	 * @param amount
	 * @param remark
	 * @return
	 */
	public IPayment addPayment(IInvoice invoice, Money amount, String remark);

	/**
	 * Get the an {@link IAccountTransaction} matching the {@link IPayment}.
	 * 
	 * 
	 * @param payment
	 * @return
	 */
	public Optional<IAccountTransaction> getAccountTransaction(IPayment payment);
}
