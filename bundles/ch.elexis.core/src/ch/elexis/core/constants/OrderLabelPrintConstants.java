package ch.elexis.core.constants;

/**
 * Contract between the order management (core) and a label printing
 * implementation (e.g. Medi-Print) for printing labels of delivered order
 * entries. The core only knows the command id, the implementation registers a
 * handler for it.
 */
public class OrderLabelPrintConstants {

	/**
	 * Command printing labels for delivered patient stock order entries.
	 */
	public static final String COMMAND_ID = "ch.itmed.fop.printing.command.OrderEntryLabelPrint"; //$NON-NLS-1$

	/**
	 * Command parameter containing the order entries and label amounts, formatted
	 * as <code>&lt;orderEntryId&gt;:&lt;amount&gt;;&lt;orderEntryId&gt;:&lt;amount&gt;</code>.
	 */
	public static final String PARAM_ENTRIES = "orderentry_label_entries"; //$NON-NLS-1$

	public static final String ENTRY_SEPARATOR = ";"; //$NON-NLS-1$
	public static final String AMOUNT_SEPARATOR = ":"; //$NON-NLS-1$

	/**
	 * Key of the result map (<code>Map&lt;String, Integer&gt;</code>) returned by
	 * the command handler, number of labels sent to a printer.
	 */
	public static final String RESULT_PRINTED = "printed"; //$NON-NLS-1$

	/**
	 * Key of the result map (<code>Map&lt;String, Integer&gt;</code>) returned by
	 * the command handler, number of labels not printed because no printer is
	 * configured for the label type.
	 */
	public static final String RESULT_SKIPPED_NO_PRINTER = "skippedNoPrinter"; //$NON-NLS-1$
}
