package ch.elexis.core.ui.views.rechnung.invoice;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IContact;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.MoneyInput;
import ch.elexis.core.ui.util.NumberInput;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.Money;

public class InvoiceListBottomComposite extends Composite {

	private static final String REMINDER_3 = Messages.Core_Invoice_Reminder_3; // $NON-NLS-1$
	private static final String REMINDER_2 = Messages.Core_Invoice_Reminder_2; // $NON-NLS-1$
	private static final String REMINDER_1 = Messages.Core_Invoice_Reminder_1; // $NON-NLS-1$
	private Text totalPatientsInListText;
	private Text tSumInvoiceInList;
	private Text tSum;
	private Text tOpen;
	private NumberInput niDaysTo1st;
	private NumberInput niDaysTo2nd;
	private NumberInput niDaysTo3rd;
	private MoneyInput mi1st;
	private MoneyInput mi2nd;
	private MoneyInput mi3rd;
	private SelectionAdapter mahnWizardListener;
	private FormToolkit tk = UiDesk.getToolkit();

	/**
	 * Create the composite.
	 *
	 * @wbp.eval.method.parameter rnStellerSettings new ch.rgw.io.InMemorySettings()
	 * @param parent
	 * @param style
	 */
	public InvoiceListBottomComposite(Composite parent, int style) {
		super(parent, style);
		mahnWizardListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(final SelectionEvent e) {
				ConfigServiceHolder.get().set(getCurrentBiller(), Preferences.RNN_DAYSUNTIL1ST, niDaysTo1st.getValue());
				ConfigServiceHolder.get().set(getCurrentBiller(), Preferences.RNN_DAYSUNTIL2ND, niDaysTo2nd.getValue());
				ConfigServiceHolder.get().set(getCurrentBiller(), Preferences.RNN_DAYSUNTIL3RD, niDaysTo3rd.getValue());
				ConfigServiceHolder.get().set(getCurrentBiller(), Preferences.RNN_AMOUNT1ST,
						mi1st.getMoney(false).getAmountAsString());
				ConfigServiceHolder.get().set(getCurrentBiller(), Preferences.RNN_AMOUNT2ND,
						mi2nd.getMoney(false).getAmountAsString());
				ConfigServiceHolder.get().set(getCurrentBiller(), Preferences.RNN_AMOUNT3RD,
						mi3rd.getMoney(false).getAmountAsString());
			}
		};

		RowLayout rowLayout = new RowLayout();
		rowLayout.wrap = false;
		rowLayout.pack = true;
		rowLayout.justify = true;
		rowLayout.fill = true;
		rowLayout.type = SWT.HORIZONTAL;
		rowLayout.marginLeft = 0;
		rowLayout.marginTop = 0;
		rowLayout.marginRight = 0;
		rowLayout.marginBottom = 0;
		rowLayout.spacing = 5;

		setLayout(rowLayout);
		Form fSum = tk.createForm(this);
		Form fWizard = tk.createForm(this);
		fSum.setText(Messages.RechnungsListeView_sum); // $NON-NLS-1$
		fWizard.setText(Messages.Invoice_reminder_automatism); // $NON-NLS-1$
		Composite cSum = fSum.getBody();
		cSum.setLayout(new GridLayout(2, false));
		tk.createLabel(cSum, Messages.RechnungsListeView_patInList); // $NON-NLS-1$
		totalPatientsInListText = tk.createText(cSum, StringUtils.EMPTY, SWT.BORDER | SWT.READ_ONLY);
		totalPatientsInListText.setLayoutData(new GridData(100, SWT.DEFAULT));
		tk.createLabel(cSum, Messages.RechnungsListeView_accountsInList); // $NON-NLS-1$
		tSumInvoiceInList = tk.createText(cSum, StringUtils.EMPTY, SWT.BORDER | SWT.READ_ONLY);
		tSumInvoiceInList.setLayoutData(new GridData(100, SWT.DEFAULT));
		tk.createLabel(cSum, Messages.Invoice_amount_due); // $NON-NLS-1$
		tSum = SWTHelper.createText(tk, cSum, 1, SWT.BORDER | SWT.READ_ONLY);
		tSum.setLayoutData(new GridData(100, SWT.DEFAULT));
		tk.createLabel(cSum, Messages.RechnungsListeView_paidInList); // $NON-NLS-1$
		tOpen = SWTHelper.createText(tk, cSum, 1, SWT.BORDER | SWT.READ_ONLY);
		tOpen.setLayoutData(new GridData(100, SWT.DEFAULT));
		Composite cW = fWizard.getBody();
		cW.setLayout(new GridLayout(4, true));

		tk.createLabel(cW, Messages.RechnungsListeView_delayInDays); // $NON-NLS-1$

		niDaysTo1st = new NumberInput(cW, REMINDER_1);
		niDaysTo1st.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		niDaysTo1st.getControl().addSelectionListener(mahnWizardListener);
		niDaysTo1st.setValue(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_DAYSUNTIL1ST, 30));
		niDaysTo2nd = new NumberInput(cW, REMINDER_2);
		niDaysTo2nd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		niDaysTo2nd.getControl().addSelectionListener(mahnWizardListener);
		niDaysTo2nd.setValue(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_DAYSUNTIL2ND, 10));
		niDaysTo3rd = new NumberInput(cW, REMINDER_3);
		niDaysTo3rd.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		niDaysTo3rd.getControl().addSelectionListener(mahnWizardListener);
		niDaysTo3rd.setValue(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_DAYSUNTIL3RD, 5));
		tk.createLabel(cW, Messages.RechnungsListeView_fine); // $NON-NLS-1$
		mi1st = new MoneyInput(cW, REMINDER_1);
		mi1st.addSelectionListener(mahnWizardListener);
		mi1st.setMoney(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_AMOUNT1ST,
				new Money().getAmountAsString()));
		mi2nd = new MoneyInput(cW, REMINDER_2);
		mi2nd.addSelectionListener(mahnWizardListener);
		mi2nd.setMoney(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_AMOUNT2ND,
				new Money().getAmountAsString()));
		mi3rd = new MoneyInput(cW, REMINDER_3);
		mi3rd.addSelectionListener(mahnWizardListener);
		mi3rd.setMoney(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_AMOUNT3RD,
				new Money().getAmountAsString()));
	}

	private IContact getCurrentBiller() {
		return ContextServiceHolder.get().getActiveMandator().get().getBiller();
	}

	@Override
	protected void checkSubclass() {
		// Disable the check that prevents subclassing of SWT components
	}

	public void update(String totalPatientsInListValue, String tRnVal, String tSumVal, String tOpenVal) {
		totalPatientsInListText.setText(totalPatientsInListValue);
		tSumInvoiceInList.setText(tRnVal);
		tSum.setText(tSumVal);
		tOpen.setText(tOpenVal);
	}

	public void updateMahnAutomatic() {
		niDaysTo1st.setValue(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_DAYSUNTIL1ST, 30));
		niDaysTo2nd.setValue(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_DAYSUNTIL2ND, 10));
		niDaysTo3rd.setValue(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_DAYSUNTIL3RD, 5));
		mi1st.setMoney(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_AMOUNT1ST,
				new Money().getAmountAsString()));
		mi2nd.setMoney(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_AMOUNT2ND,
				new Money().getAmountAsString()));
		mi3rd.setMoney(ConfigServiceHolder.get().get(getCurrentBiller(), Preferences.RNN_AMOUNT3RD,
				new Money().getAmountAsString()));
	}

}
