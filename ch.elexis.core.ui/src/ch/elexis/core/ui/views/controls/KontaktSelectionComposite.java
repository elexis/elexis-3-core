package ch.elexis.core.ui.views.controls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.data.Kontakt;

public class KontaktSelectionComposite extends Composite {
	
	protected Button selectButton;
	protected Label selectLabel;
	
	protected Kontakt kontakt;
	
	public KontaktSelectionComposite(Composite parent, int style){
		super(parent, style);
		
		createContent();
	}
	
	private void createContent(){
		setLayout(new GridLayout(2, false));
		
		selectLabel = new Label(this, SWT.NONE);
		selectLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		
		selectButton = new Button(this, SWT.NONE);
		selectButton.setText("..."); //$NON-NLS-1$
		selectButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));
		
		selectButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				KontaktSelektor ksl = getKontaktSelector();
				if (ksl.open() == Dialog.OK) {
					kontakt = (Kontakt) ksl.getSelection();
					setKontakt(kontakt);
				}
			}
		});
	}
	
	protected KontaktSelektor getKontaktSelector(){
		return new KontaktSelektor(getShell(), Kontakt.class,
			Messages.KontaktSelectionComposite_title, Messages.KontaktSelectionComposite_message,
			Kontakt.DEFAULT_SORT);
	}
	
	public void setKontakt(Kontakt kontakt){
		this.kontakt = kontakt;
		if (kontakt != null) {
			selectLabel.setText(kontakt.getLabel());
		} else {
			selectLabel.setText(""); //$NON-NLS-1$
		}
		this.layout();
	}
	
	public Kontakt getKontakt(){
		return kontakt;
	}
}
