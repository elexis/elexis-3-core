package ch.elexis.core.ui.tasks.internal.detailcontributors;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.MessageBox;
import org.slf4j.LoggerFactory;

import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.tasks.model.ITask;
import ch.rgw.tools.Result;

public class HL7ImporterTaskResultDetailComposite {

	private ECommandService commandService;
	private EHandlerService handlerService;
	private IVirtualFilesystemService vfsService;

	private Button btnManualImport;
	private Button btnArchive;
	private Label lblStatus;
	private String fileUrl;

	public HL7ImporterTaskResultDetailComposite(Composite parent, ITask task, Map<String, Object> e4Services,
			IVirtualFilesystemService vfsService) {

		this.vfsService = vfsService;
		commandService = (ECommandService) e4Services.get(ECommandService.class.getName());
		handlerService = (EHandlerService) e4Services.get(EHandlerService.class.getName());

		fileUrl = task.getResultEntryTyped(ReturnParameter.STRING_URL, String.class);

		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(2, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));

		lblStatus = new Label(container, SWT.WRAP);
		GridData gd_lblStatus = new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1);
		gd_lblStatus.heightHint = 60;
		lblStatus.setLayoutData(gd_lblStatus);

		btnManualImport = new Button(container, SWT.NONE);
		btnManualImport.setText("Datei manuell importieren");
		btnManualImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, Serializable> params = Collections
						.singletonMap("ch.elexis.laborimport.hl7.allg.importFile.fileUrl", fileUrl); //$NON-NLS-1$
				ParameterizedCommand command = commandService.createCommand("ch.elexis.laborimport.hl7.allg.importFile", //$NON-NLS-1$
						params);
				@SuppressWarnings("rawtypes")
				Result result = (Result) handlerService.executeHandler(command);
				String message;
				if (result.isOK()) {
					message = "Import erfolgreich.";
				} else {
					message = (String) result.getMessages().get(0);
					task.setStateCompletedManual("manual import");
				}

				MessageBox dialog = new MessageBox(parent.getShell(), result.isOK() ? SWT.OK : SWT.ERROR);
				dialog.setText("Info");
				dialog.setMessage(message);
				dialog.open();
				setTask(task);
			}
		});

		btnArchive = new Button(container, SWT.NONE);
		btnArchive.setText("Datei archivieren");
		btnArchive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Map<String, Serializable> params = Collections
						.singletonMap("ch.elexis.laborimport.hl7.allg.archiveFile.fileUrl", fileUrl); //$NON-NLS-1$
				ParameterizedCommand command = commandService
						.createCommand("ch.elexis.laborimport.hl7.allg.archiveFile", params); //$NON-NLS-1$
				@SuppressWarnings("rawtypes")
				Result result = (Result) handlerService.executeHandler(command);
				String message;
				if (result.isOK()) {
					message = "Archivierung erfolgreich.";
				} else {
					message = (String) result.getMessages().get(0);
					task.setStateCompletedManual("manual archive");
				}

				MessageBox dialog = new MessageBox(parent.getShell(), result.isOK() ? SWT.OK : SWT.ERROR);
				dialog.setText("Info");
				dialog.setMessage(message);
				dialog.open();
				setTask(task);
			}
		});

		setTask(task);
	}

	private void setTask(ITask task) {
		fileUrl = task.getResultEntryTyped(ReturnParameter.STRING_URL, String.class);
		IVirtualFilesystemHandle importFileHandle = null;
		String fileName;
		try {
			importFileHandle = vfsService.of(fileUrl);
			fileName = importFileHandle.getName();
		} catch (IOException e) {
			fileName = e.getMessage();
			LoggerFactory.getLogger(getClass()).warn("Error parsing url", e); //$NON-NLS-1$
		}

		StringBuilder text = new StringBuilder();
		if (task.isSucceeded()) {
			text.append("Die Datei [" + fileName + "] wurde erfolgreich importiert.");
		} else {
			text.append("Die Datei [" + fileName + "] konnte nicht automatisch importiert werden.");
			text.append("\n\n");
			text.append(
					"Grund: " + task.getResultEntryTyped(ReturnParameter.RESULT_DATA, String.class) + StringUtils.LF);
		}
		lblStatus.setText(text.toString());

		btnManualImport.setEnabled(!task.isSucceeded() && (importFileHandle != null));
		btnArchive.setEnabled(!task.isSucceeded() && (importFileHandle != null));
	}

}
