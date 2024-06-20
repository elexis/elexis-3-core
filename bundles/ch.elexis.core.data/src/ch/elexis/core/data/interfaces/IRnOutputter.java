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

package ch.elexis.core.data.interfaces;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.Properties;

import ch.elexis.core.model.IInvoice;
import ch.elexis.core.model.InvoiceState;
import ch.elexis.data.Fall;
import ch.elexis.data.Rechnung;
import ch.rgw.tools.Result;

/**
 * An Object that is able to output a bill. Can be a ptinter, a file, an
 * electronic connection or whatever else.
 *
 * @author Gerry
 *
 */
public interface IRnOutputter {
	public static enum TYPE {
		ORIG, COPY, STORNO
	};

	/**
	 * Property indicating the output method. "asDefault" -> The user requested
	 * output via default outputter "byName" -> The user requested this outputter by
	 * name. If the property is not set, byName will be assumed.
	 */
	public static final String PROP_OUTPUT_METHOD = "OutputMethod";

	public static final String PROP_OUTPUT_WITH_ESR = "OutputWithEsr";

	public static final String PROP_OUTPUT_WITH_RECLAIM = "OutputWithReclaim";

	public static final String PROP_OUTPUT_WITH_MAIL = "OutputWithMail";

	public static final String PROP_OUTPUT_MODIFY_INVOICESTATE = "OutputModifyInvoiceState";

	public static final String PROP_OUTPUT_NOUI = "OutputNoUi";

	public static final String PROP_OUTPUT_NOPRINT = "OutputNoPrint";

	/**
	 * A short textual description for this output (as Label)
	 */
	public String getDescription();

	/**
	 * Do the actual output
	 *
	 * @param type  Type of the bill
	 * @param rnn   collection with all bills to process
	 * @param props properties for various purposes. Can be null.
	 * @return a result indicating errors
	 */
	public Result<Rechnung> doOutput(final TYPE type, final Collection<Rechnung> rnn, final Properties props);

	/**
	 * Cancelling an already output bill: Depending on the type of the outputter,
	 * this might result in propagataing the cancel information to the final
	 * destination of the bill.
	 *
	 * @param rn the specific bill to cancel or null, if it is just a general
	 *           question
	 * @return true if this outputter wants to be informed if this (or any in case
	 *         of Rn==null) bill is cancelled. If an outputter does need to react on
	 *         storno messages, it should return false.
	 */
	public boolean canStorno(Rechnung rn);

	/**
	 * check whether a case could be billed, i.e. all billing data are present. This
	 * should only check for required absolutely mandatory data. At output time, the
	 * outputter still can reject.
	 *
	 * @param fall the case to check
	 * @return true if we can send a bill from this case
	 */
	public boolean canBill(Fall fall);

	/**
	 * Create a Control to perform necessary settings for his outputter.
	 *
	 * @param parent <code>null</code> or castable to
	 *               org.eclipse.swt.widgets.Composite
	 * @return <code>null</code> or an object castable to
	 *         org.eclipse.swt.widgets.Control
	 * @since 3.0.0 direct dependencies to Composite and Control removed
	 */
	public Object createSettingsControl(Object parent);

	/**
	 * Methode is called before dialog is closed. Used to save the widget contents
	 * before the composite is disposed;
	 */
	public void saveComposite();

	/**
	 * Implementations can customize the RnOutputDialog.
	 *
	 * @param rnOutputDialog
	 * @return
	 */
	public default void customizeDialog(Object rnOutputDialog) {
		// do no customization
	}

	public default void openOutput(IInvoice invoice, LocalDateTime timestamp, InvoiceState invoiceState) {
		// outputter specific lookup of the output to open
	}
}
