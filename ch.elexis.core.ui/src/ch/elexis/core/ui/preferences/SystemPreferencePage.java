package ch.elexis.core.ui.preferences;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.layout.GridData;

public class SystemPreferencePage extends PreferencePage implements IWorkbenchPreferencePage {
	
	/**
	 * Create the preference page.
	 */
	public SystemPreferencePage(){}
	
	/**
	 * Create contents of the preference page.
	 * @param parent
	 */
	@Override
	public Control createContents(Composite parent){
		Composite container = new Composite(parent, SWT.NULL);
		container.setLayout(new GridLayout(1, false));
		
		Label lblDieseKategorieBeinhaltet = new Label(container, SWT.NONE);
		lblDieseKategorieBeinhaltet.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblDieseKategorieBeinhaltet.setText("Diese Kategorie beinhaltet system-bezogene Einstellungen.");
		
		return container;
	}
	
	/**
	 * Initialize the preference page.
	 */
	public void init(IWorkbench workbench){
		// Initialize the preference page
	}
	
}
