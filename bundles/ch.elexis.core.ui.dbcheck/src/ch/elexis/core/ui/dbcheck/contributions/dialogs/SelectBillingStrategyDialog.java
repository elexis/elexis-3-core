package ch.elexis.core.ui.dbcheck.contributions.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.dbcheck.contributions.BillAllOpenCons;
import ch.elexis.core.ui.dbcheck.contributions.BillAllOpenCons.IBillStrategy;

public class SelectBillingStrategyDialog extends TitleAreaDialog {
	
	private IBillStrategy strategy;
	private Button standardBtn;
	private Button vitodataBtn;
	
	public SelectBillingStrategyDialog(Shell parentShell){
		super(parentShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle("Strategie für das Abrechnen auswählen.");
		setMessage(
			"Es sind verschiedene Strategien für das Abrechnen verfügbar, bitte wählen Sie eine aus.");
		
		Composite container = (Composite) super.createDialogArea(parent);
		Composite area = new Composite(container, SWT.NONE);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		area.setLayout(new GridLayout(1, false));
		
		standardBtn = new Button(area, SWT.CHECK);
		standardBtn.setText("Standard Strategie");
		standardBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				strategy = BillAllOpenCons.BillStrategies.DEFAULT;
				vitodataBtn.setSelection(false);
			}
		});
		Label lblDesc = new Label(area, SWT.NONE);
		lblDesc.setText(
			"Standard Strategie - geht alle Fälle durch.\n"
				+ "Wenn der Fall noch nicht geschlossen ist, werden alle Konsultationen des Falls abgerechnet, und dann der Fall geschlossen.");
		
		vitodataBtn = new Button(area, SWT.CHECK);
		vitodataBtn.setText("Vitodata Strategie");
		vitodataBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				strategy = BillAllOpenCons.BillStrategies.VITODATA;
				standardBtn.setSelection(false);
			}
		});
		lblDesc = new Label(area, SWT.NONE);
		lblDesc.setText(
			"Vitodata Strategie - geht alle Fälle durch.\n"
				+ "Wenn der Fall nur Vitodata Konsultationen enthält, und diese noch nicht abgerechnet wurden. Werden alle Konsultationen des Falls abgerechnet, und der Fall geschlossen.\n");
		
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, "OK", false);
	}
	
	public IBillStrategy getStrategy(){
		return strategy;
	}
	
	@Override
	protected void okPressed(){
		if (strategy == null) {
			return;
		}
		super.okPressed();
	}
}
