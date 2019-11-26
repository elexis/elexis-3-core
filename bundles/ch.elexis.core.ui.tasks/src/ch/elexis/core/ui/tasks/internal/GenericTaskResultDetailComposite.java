package ch.elexis.core.ui.tasks.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.tasks.model.ITask;
import org.eclipse.swt.widgets.Label;

public class GenericTaskResultDetailComposite {
	
	public GenericTaskResultDetailComposite(Composite parent, ITask task){
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblNewLabel = new Label(container, SWT.NONE);
		lblNewLabel.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblNewLabel.setText("generic result view: not yet implemented");
		
	}
	
}
