package ch.elexis.core.ui.tasks.parts.controls;

import java.text.ParseException;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.quartz.CronExpression;

import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskTriggerType;
import net.redhogs.cronparser.CronExpressionDescriptor;

public class TaskTriggerTypeConfigurationComposite
		extends AbstractTaskDescriptorConfigurationComposite {
	
	private ComboViewer comboViewer;
	private Composite compositeParameters;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public TaskTriggerTypeConfigurationComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		Label lblType = new Label(this, SWT.NONE);
		lblType.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblType.setText("type");
		
		comboViewer = new ComboViewer(this, SWT.NONE);
		Combo combo = comboViewer.getCombo();
		combo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setInput(TaskTriggerType.VALUES);
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				TaskTriggerType type = (TaskTriggerType) element;
				return type.getName();
			}
		});
		comboViewer.addSelectionChangedListener(sel -> {
			TaskTriggerType taskTriggerType =
				(TaskTriggerType) sel.getStructuredSelection().getFirstElement();
			if (taskDescriptor != null && taskDescriptor.getTriggerType() != taskTriggerType) {
				taskDescriptor.setTriggerType(taskTriggerType);
				saveTaskDescriptor();
			}
			refreshParameterComposite(taskTriggerType);
		});
		
		compositeParameters = new Composite(this, SWT.NONE);
		GridLayout gl_compositeParameters = new GridLayout(2, false);
		gl_compositeParameters.marginHeight = 0;
		gl_compositeParameters.marginWidth = 0;
		compositeParameters.setLayout(gl_compositeParameters);
		compositeParameters.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		
		refreshParameterComposite(null);
	}
	
	private void refreshParameterComposite(TaskTriggerType taskTriggerType){
		Control[] children = compositeParameters.getChildren();
		for (Control control : children) {
			control.dispose();
		}
		
		if (taskTriggerType != null) {
			switch (taskTriggerType) {
			case CRON:
				createCompositeParameters_CRON();
				break;
			
			default:
				createCompositeParameters_FALLBACK();
				break;
			}
		} else {
			createCompositeParameters_FALLBACK();
		}
		compositeParameters.layout(true);
	}
	
	private void createCompositeParameters_FALLBACK(){
		Label label = new Label(compositeParameters, SWT.None);
		label.setText("Please select a trigger type");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}
	
	private void createCompositeParameters_CRON(){
		String cron =
			(taskDescriptor != null) ? taskDescriptor.getTriggerParameters().get("cron") : null;
		
		Label label = new Label(compositeParameters, SWT.None);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		label.setText("cron");
		Text text = new Text(compositeParameters, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		text.setText((cron != null) ? cron : "");
		Label valid = new Label(compositeParameters, SWT.NONE | SWT.WRAP);
		valid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));
		
		text.addModifyListener(event -> {
			String value = ((Text) event.widget).getText();
			
			boolean isValidCron = validateAndupdateCronExpressionDescriptor(value, valid);
			if (isValidCron) {
				taskDescriptor.getTriggerParameters().put("cron", value);
				saveTaskDescriptor();
			}
		});
		validateAndupdateCronExpressionDescriptor(cron, valid);
	}
	
	private boolean validateAndupdateCronExpressionDescriptor(String cron, Label label){
		boolean validExpression = CronExpression.isValidExpression(cron);
		if (validExpression) {
			try {
				String description = CronExpressionDescriptor.getDescription(cron);
				label.setText(description);
			} catch (ParseException | IllegalArgumentException e) {
				label.setText(e.getMessage());
			}
		} else {
			label.setText("Invalid expression");
		}
		return validExpression;
	}
	
	@Override
	public void setSelection(ITaskDescriptor taskDescriptor){
		this.taskDescriptor = taskDescriptor;
		if (taskDescriptor != null) {
			comboViewer.setSelection(new StructuredSelection(taskDescriptor.getTriggerType()));
		} else {
			comboViewer.setSelection(null);
		}
		
	}
	
}
