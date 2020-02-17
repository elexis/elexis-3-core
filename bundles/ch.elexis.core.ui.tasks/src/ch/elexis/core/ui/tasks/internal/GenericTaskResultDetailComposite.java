package ch.elexis.core.ui.tasks.internal;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.tasks.model.ITask;

public class GenericTaskResultDetailComposite {
	
	private Text txt;
	
	public GenericTaskResultDetailComposite(Composite parent, ITask task){
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		txt = new Text(container, SWT.BORDER);
		txt.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		StringBuilder sb = new StringBuilder();
		sb.append(task.getId()+"\n");
		sb.append("Created at: "+task.getCreatedAt()+"\n");
		sb.append("Run at: "+task.getRunAt()+"\n");
		
		sb.append(task.getResult());
		
		txt.setText(sb.toString());
		
	}
	
}
