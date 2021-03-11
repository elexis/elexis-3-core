package ch.elexis.core.ui.tasks.parts.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

public class RunContextCheckButtonWithDefaultValue extends Button {
	
	public RunContextCheckButtonWithDefaultValue(Composite compAssisted,
		AbstractTaskDescriptorConfigurationComposite atdcc, String key, Boolean defaultValue,
		Boolean configuredValue){
		super(compAssisted, SWT.CHECK);
		
		if (configuredValue != null) {
			setSelection(configuredValue);
		} else {
			if (defaultValue != null) {
				setSelection(defaultValue);
			}
		}
		
		addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				if (defaultValue != null && getSelection() == defaultValue) {
					// remove key from map as default equals config
					atdcc.taskDescriptor.setRunContextParameter(key, null);
				} else {
					atdcc.taskDescriptor.setRunContextParameter(key,
						Boolean.valueOf(getSelection()));
				}
				atdcc.saveTaskDescriptor();
			}
			
		});
	}
	
	@Override
	protected void checkSubclass(){}
	
}
