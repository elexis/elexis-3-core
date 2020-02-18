package ch.elexis.core.ui.tasks.internal.detailcontributors;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.Map;

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

import ch.elexis.core.model.tasks.IIdentifiedRunnable;
import ch.elexis.core.model.tasks.IIdentifiedRunnable.ReturnParameter;
import ch.elexis.core.services.IVirtualFilesystemService;
import ch.elexis.core.services.IVirtualFilesystemService.IVirtualFilesystemHandle;
import ch.elexis.core.tasks.model.ITask;

public class HL7ImporterTaskResultDetailComposite {
	
	private ECommandService commandService;
	private EHandlerService handlerService;
	
	public HL7ImporterTaskResultDetailComposite(Composite parent, ITask task,
		Map<String, Object> e4Services, IVirtualFilesystemService vfsService){
		
		commandService = (ECommandService) e4Services.get(ECommandService.class.getName());
		handlerService = (EHandlerService) e4Services.get(EHandlerService.class.getName());
		
		Composite container = new Composite(parent, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		Label lblStatus = new Label(container, SWT.NONE);
		lblStatus.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		final String fileUrl =
			(String) task.getRunContext().get(IIdentifiedRunnable.RunContextParameter.STRING_URL);
		String fileName = "???";
		
		try {
			IVirtualFilesystemHandle importFileHandle = vfsService.of(fileUrl);
			fileName = importFileHandle.getName();
		} catch (IOException e) {
			LoggerFactory.getLogger(getClass()).warn("Error parsing url", e);
		}
		
		StringBuilder text = new StringBuilder();
		if (task.isSucceeded()) {
			text.append("Die Datei " + fileName + " wurde erfolgreich importiert.");
		} else {
			text.append("Die Datei " + fileName + " konnte nicht automatisch importiert werden.");
			text.append("\n\n");
			text.append("Grund: " + task.getResultEntryTyped(
				ReturnParameter.FAILED_TASK_EXCEPTION_MESSAGE, String.class) + "\n");
		}
		lblStatus.setText(text.toString());
		
		Button btnManualImport = new Button(container, SWT.NONE);
		btnManualImport.setText("Datei manuell importieren");
		btnManualImport.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				Map<String, Serializable> params = Collections
					.singletonMap("ch.elexis.laborimport.hl7.allg.importFile.fileUrl", fileUrl);
				ParameterizedCommand command = commandService
					.createCommand("ch.elexis.laborimport.hl7.allg.importFile", params);
				Object executeHandler = handlerService.executeHandler(command);
				MessageBox dialog = new MessageBox(parent.getShell(), SWT.OK);
				dialog.setText("Result");
				dialog.setMessage((executeHandler != null) ? executeHandler.toString() : "");
				dialog.open();
				// TODO mark task as manually fixed?
			}
		});
		btnManualImport.setEnabled(!task.isSucceeded());
		
		Button btnArchive = new Button(container, SWT.NONE);
		btnArchive.setText("Datei archivieren");
		btnArchive.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				MessageBox dialog = new MessageBox(parent.getShell(), SWT.OK);
				dialog.setText("Not yet implemented");
				dialog.open();
				super.widgetSelected(e);
			}
		});
		
	}
	
}
