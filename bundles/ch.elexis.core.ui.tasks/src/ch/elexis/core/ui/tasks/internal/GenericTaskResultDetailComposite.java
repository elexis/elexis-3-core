package ch.elexis.core.ui.tasks.internal;

import java.time.format.DateTimeFormatter;
import java.util.Objects;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.tasks.model.ITask;

public class GenericTaskResultDetailComposite {

	private final DateTimeFormatter dtf;

	private Text txtRunContext;
	private Text txtResult;

	private static Font boldFont;

	public GenericTaskResultDetailComposite(Composite parent, ITask task) {
		dtf = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm.ss"); //$NON-NLS-1$

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label taskHeader = new Label(container, SWT.NONE);
		taskHeader.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		if (boldFont == null) {
			FontDescriptor boldDescriptor = FontDescriptor.createFrom(taskHeader.getFont()).setStyle(SWT.BOLD);
			boldFont = boldDescriptor.createFont(taskHeader.getDisplay());
		}
		taskHeader.setFont(boldFont);
		taskHeader.setText(task.getState().getName() + ": " + task.getId() + " (" //$NON-NLS-1$ //$NON-NLS-2$
				+ task.getTaskDescriptor().getReferenceId() + ")"); //$NON-NLS-1$

		Label lblCreated = new Label(container, SWT.NONE);
		lblCreated.setText("created ");

		Label valCreated = new Label(container, SWT.NONE);
		valCreated.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		valCreated.setText(dtf.format(task.getCreatedAt()));

		Label lblLblrun = new Label(container, SWT.NONE);
		lblLblrun.setText("run");

		Label valRun = new Label(container, SWT.NONE);
		valRun.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (task.getRunAt() != null) {
			valRun.setText(dtf.format(task.getRunAt()) + " on " + task.getRunner());
		}

		Label lblFinished = new Label(container, SWT.NONE);
		lblFinished.setText("finished");

		Label valFinished = new Label(container, SWT.NONE);
		valFinished.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		if (task.getFinishedAt() != null) {
			valFinished.setText(dtf.format(task.getFinishedAt()));
		}

		Label lblTrigger = new Label(container, SWT.NONE);
		lblTrigger.setText("trigger");

		Label valTrigger = new Label(container, SWT.NONE);
		valTrigger.setText(task.getTriggerEvent().getName());

		Label lblResult = new Label(container, SWT.NONE);
		lblResult.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblResult.setText("result");

		txtResult = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtResult.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		StringBuilder sbResult = new StringBuilder();
		task.getResult().forEach((k, v) -> {
			if (v instanceof String) {
				String value = (String) v;
				if (IIdentifiedRunnable.ReturnParameter.STRING_URL.equals(k)) {
					value = IVirtualFilesystemService.hidePasswordInUrlString(value);
				}
				sbResult.append("- " + k + ": " + value + StringUtils.LF); //$NON-NLS-1$ //$NON-NLS-2$
			} else {
				sbResult.append("- " + k + ": " + v + StringUtils.LF); //$NON-NLS-1$ //$NON-NLS-2$
			}
		});
		txtResult.setText(sbResult.toString());

		Label lblRunContext = new Label(container, SWT.NONE);
		lblRunContext.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false, 1, 1));
		lblRunContext.setText("runcontext");

		txtRunContext = new Text(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.MULTI);
		txtRunContext.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		StringBuilder sbRunContext = new StringBuilder();
		task.getRunContext().forEach((k, v) -> {
			String value = Objects.toString(v);
			if (IIdentifiedRunnable.RunContextParameter.STRING_URL.equals(k)) {
				value = IVirtualFilesystemService.hidePasswordInUrlString((String) v);
			}
			sbRunContext.append("- " + k + ": " + value + StringUtils.LF); //$NON-NLS-1$ //$NON-NLS-2$
		});
		txtRunContext.setText(sbRunContext.toString());

	}

}
