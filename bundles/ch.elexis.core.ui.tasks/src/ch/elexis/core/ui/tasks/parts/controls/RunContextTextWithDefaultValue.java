package ch.elexis.core.ui.tasks.parts.controls;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

public class RunContextTextWithDefaultValue extends Text {
	
	public RunContextTextWithDefaultValue(Composite compAssisted,
		AbstractTaskDescriptorConfigurationComposite atdcc, String key, String defaultValue,
		String configuredValue){
		super(compAssisted, SWT.BORDER);
		
		if (configuredValue != null) {
			setText(configuredValue);
		} else {
			if (defaultValue != null) {
				setText(defaultValue);
			}
		}
		
		addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				if (defaultValue != null && getText().trim().equals(defaultValue)) {
					// remove key from map as default equals config
					atdcc.taskDescriptor.setRunContextParameter(key, null);
				} else {
					atdcc.taskDescriptor.setRunContextParameter(key, getText().trim());
				}
				atdcc.saveTaskDescriptor();
			}
			
		});
		
	}
	
	@Override
	protected void checkSubclass(){}
	
}
