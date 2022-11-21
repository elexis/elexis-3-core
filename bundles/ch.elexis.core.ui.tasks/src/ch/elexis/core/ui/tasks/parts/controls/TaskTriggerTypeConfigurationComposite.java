package ch.elexis.core.ui.tasks.parts.controls;

import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.text.ParseException;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.quartz.CronExpression;

import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.services.holder.VirtualFilesystemServiceHolder;
import ch.elexis.core.tasks.model.ITaskDescriptor;
import ch.elexis.core.tasks.model.TaskTriggerType;
import ch.elexis.core.tasks.model.TaskTriggerTypeParameter;
import ch.elexis.core.ui.e4.dialog.VirtualFilesystemUriEditorDialog;
import net.redhogs.cronparser.CronExpressionDescriptor;

public class TaskTriggerTypeConfigurationComposite extends AbstractTaskDescriptorConfigurationComposite {

	private ComboViewer comboViewer;
	private Composite compositeParameters;

	/**
	 * Create the composite.
	 *
	 * @param parent
	 * @param style
	 */
	public TaskTriggerTypeConfigurationComposite(Composite parent, int style) {
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
			public String getText(Object element) {
				TaskTriggerType type = (TaskTriggerType) element;
				return type.getName();
			}
		});
		comboViewer.addSelectionChangedListener(sel -> {
			TaskTriggerType taskTriggerType = (TaskTriggerType) sel.getStructuredSelection().getFirstElement();
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

	private void refreshParameterComposite(TaskTriggerType taskTriggerType) {
		Control[] children = compositeParameters.getChildren();
		for (Control control : children) {
			control.dispose();
		}

		if (taskTriggerType != null) {
			switch (taskTriggerType) {
			case CRON:
				createCompositeParameters_CRON();
				break;
			case FILESYSTEM_CHANGE:
				createCompositeParameters_FILESYSTEM_CHANGE();
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

	private void createCompositeParameters_FALLBACK() {
		Label label = new Label(compositeParameters, SWT.None);
		label.setText("Please select a trigger type");
		label.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
	}

	private void createCompositeParameters_FILESYSTEM_CHANGE() {
		String url = (taskDescriptor != null)
				? taskDescriptor.getTriggerParameters().get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.URL)
				: StringUtils.EMPTY;
		String fileExtension = (taskDescriptor != null)
				? taskDescriptor.getTriggerParameters()
						.get(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER)
				: StringUtils.EMPTY;

		Label label = new Label(compositeParameters, SWT.None);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		label.setText(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.URL);

		Composite vfsComposite = new Composite(compositeParameters, SWT.None);
		GridLayout gl_compositeParameters = new GridLayout(2, false);
		gl_compositeParameters.marginHeight = 0;
		gl_compositeParameters.marginWidth = 0;
		vfsComposite.setLayout(gl_compositeParameters);
		vfsComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));

		Text urlText = new Text(vfsComposite, SWT.BORDER);
		urlText.setText((url != null) ? url : StringUtils.EMPTY);
		urlText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));
		urlText.setEnabled(false);

		Button searchButton = new Button(vfsComposite, SWT.None);
		searchButton.setText(JFaceResources.getString("openBrowse"));
		searchButton.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		searchButton.addListener(SWT.Selection, (sel) -> {
			IVirtualFilesystemService virtualFilesystemService = VirtualFilesystemServiceHolder.get();
			URI inputUri = null;
			try {
				String _urlText = urlText.getText();
				if (StringUtils.isNotBlank(_urlText)) {
					IVirtualFilesystemHandle fileHandle = virtualFilesystemService.of(_urlText);
					inputUri = fileHandle.toURL().toURI();
				}
			} catch (URISyntaxException | IOException e) {
			}
			VirtualFilesystemUriEditorDialog dialog = new VirtualFilesystemUriEditorDialog(getShell(),
					virtualFilesystemService, inputUri);
			int open = dialog.open();
			if (IDialogConstants.OK_ID == open) {
				String _url = dialog.getValue().toString();
				taskDescriptor.setTriggerParameter(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.URL, _url);
				urlText.setText(_url);
			}
		});

		Label labelFef = new Label(compositeParameters, SWT.None);
		labelFef.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		labelFef.setText(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER);

		Text text = new Text(compositeParameters, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		text.setText((fileExtension != null) ? fileExtension : StringUtils.EMPTY);
		text.addModifyListener(event -> {
			String value = ((Text) event.widget).getText();
			taskDescriptor.setTriggerParameter(TaskTriggerTypeParameter.FILESYSTEM_CHANGE.FILE_EXTENSION_FILTER, value);
			saveTaskDescriptor();
		});
		text.setMessage("Only file ending, like 'txt' or 'pdf'");

	}

	private void createCompositeParameters_CRON() {
		String cron = (taskDescriptor != null) ? taskDescriptor.getTriggerParameters().get("cron") : StringUtils.EMPTY; //$NON-NLS-1$

		Label label = new Label(compositeParameters, SWT.None);
		label.setLayoutData(new GridData(SWT.LEFT, SWT.FILL, false, false, 1, 1));
		label.setText("cron"); //$NON-NLS-1$
		Text text = new Text(compositeParameters, SWT.BORDER);
		text.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1));
		text.setText((cron != null) ? cron : StringUtils.EMPTY);
		Label valid = new Label(compositeParameters, SWT.NONE | SWT.WRAP);
		valid.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, false, 2, 1));

		text.addModifyListener(event -> {
			String value = ((Text) event.widget).getText();

			boolean isValidCron = validateAndupdateCronExpressionDescriptor(value, valid);
			if (isValidCron) {
				taskDescriptor.setTriggerParameter("cron", value); //$NON-NLS-1$
				saveTaskDescriptor();
			}
		});

		Button btnEveryMinute = new Button(compositeParameters, getStyle());
		btnEveryMinute.setText("set every minute");
		btnEveryMinute.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				text.setText("0 * * * * ?"); //$NON-NLS-1$
			}
		});

		validateAndupdateCronExpressionDescriptor(cron, valid);
	}

	private boolean validateAndupdateCronExpressionDescriptor(String cron, Label label) {
		cron = (cron == null) ? StringUtils.EMPTY : cron;
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
	public void setSelection(ITaskDescriptor taskDescriptor) {
		this.taskDescriptor = taskDescriptor;
		if (taskDescriptor != null) {
			comboViewer.setSelection(new StructuredSelection(taskDescriptor.getTriggerType()));
		} else {
			comboViewer.setSelection(null);
		}

	}

}
