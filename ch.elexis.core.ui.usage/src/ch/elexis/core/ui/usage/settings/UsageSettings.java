package ch.elexis.core.ui.usage.settings;

import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

public class UsageSettings extends PreferencePage implements IWorkbenchPreferencePage {
	
	@Override
	public void init(IWorkbench workbench){
	}
	
	@Override
	protected Control createContents(Composite parent){
		
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(new GridLayout(2, false));
		
		
		return null;
	}
	
}
