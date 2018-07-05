package ch.elexis.core.ui.dbcheck.ui;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.InvocationTargetException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.dialogs.TrayDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StyleRange;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.statushandlers.StatusManager;
import org.eclipse.wb.swt.SWTResourceManager;

import ch.elexis.core.ui.dbcheck.Activator;
import ch.elexis.core.ui.dbcheck.RefIntegrityCheckExec;
import ch.elexis.core.ui.dbcheck.SemanticCheckExec;
import ch.elexis.core.ui.dbcheck.SyntacticCheckExec;
import ch.elexis.core.ui.dbcheck.external.ExecExternalContribution;
import ch.elexis.core.ui.dbcheck.external.ExtContributionsLabelProvider;
import ch.elexis.core.ui.dbcheck.external.ExternalContributions;
import ch.elexis.core.ui.dbcheck.external.ExternalMaintenance;
import ch.elexis.core.ui.dbcheck.model.DBModel;
import ch.elexis.data.PersistentObject;

public class DBTestDialog extends TrayDialog {
	
	protected Object result;
	private StyledText checkStatus;
	private Button btnSyntax;
	private Button btnSemantic;
	private Button btnReferentialIntegrity;
	private Composite composite;
	private Composite container;
	private Text logFile;
	private FileDialog fileChooser;
	private ComboViewer extContributions;
	private Button exec;
	private Group grpAusgabeInDatei;
	private DBCheckJob dbCheckJob;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parent
	 * @param style
	 */
	public DBTestDialog(Shell parent){
		super(parent);
		setShellStyle(SWT.RESIZE | SWT.TITLE);
	}
	
	@Override
	protected void configureShell(Shell newShell){
		super.configureShell(newShell);
		newShell.setText("Datenbank Wartungs Tool");
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		setHelpAvailable(true);
		setDialogHelpAvailable(true);
		PlatformUI.getWorkbench().getHelpSystem()
			.setHelp(parent, Activator.PLUGIN_ID + ".DBCheckTrayDialog");
		
		container = (Composite) super.createDialogArea(parent);
		container.setLayout(new GridLayout(2, false));
		
		Button btnNewButton = new Button(container, SWT.NONE);
		btnNewButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				ProgressMonitorDialog pmd = new ProgressMonitorDialog(null);
				
				IRunnableWithProgress dbcheck = new IRunnableWithProgress() {
					@Override
					public void run(IProgressMonitor monitor) throws InvocationTargetException,
						InterruptedException{
						
						try {
							boolean logOK = false;
							OutputStreamWriter out = null;
							FileOutputStream fos = null;
							
							File log = new File(logFile.getText().trim());
							if (logFile.getText().length() > 0)
								log.createNewFile();
							if (log.exists() && log.canWrite())
								logOK = true;
							
							monitor.beginTask("Datenbank-Test läuft...", 5);
							StringBuilder sb = new StringBuilder();
							// TODO Call DB Check accordingly
							SyntacticCheckExec.setJDBCLink(PersistentObject.getConnection());
							sb.append(SyntacticCheckExec.getDBInformation() + "\n");
							StyleRange styleDBInfo = new StyleRange();
							styleDBInfo.start = 0;
							styleDBInfo.length = sb.length();
							styleDBInfo.fontStyle = SWT.BOLD;
							sb.length();
							
							String DBcontainedVersion = SyntacticCheckExec.getDBVersion();
							String[] isValid = DBModel.getTableModel(DBcontainedVersion);
							if (isValid.length <= 1) {
								sb.append("Nicht unterstützte DB Version: " + DBcontainedVersion);
								checkStatus.setText(sb.toString());
								monitor.done();
								return;
							}
							
							sb.append("--- Database Version Consistence ---\n");
							monitor.subTask("Prüfe Datenbank Konsistenz");
							sb.append(SyntacticCheckExec.checkDBVersionConsistence() + "\n");
							monitor.worked(1);
							
							monitor.subTask("Checking Syntax");
							if (btnSyntax.getSelection()) {
								sb.append("--- Syntactic Check---\n");
								sb.append(SyntacticCheckExec.doSyntacticCheckOffCore(monitor));
							}
							monitor.worked(2);
							monitor.subTask("Checking Semantics");
							if (btnSemantic.getSelection()) {
								sb.append("--- Semantic Check ---\n");
								SemanticCheckExec.setJDBCLink(PersistentObject.getConnection());
								sb.append(SemanticCheckExec.doSemanticCheckOffCore(monitor));
							}
							monitor.worked(3);
							monitor.subTask("Checking referential integrity");
							if (btnReferentialIntegrity.getSelection()) {
								sb.append("--- Referential Integrity ---\n");
								RefIntegrityCheckExec.setJDBCLink(PersistentObject.getConnection());
								sb.append(RefIntegrityCheckExec
									.doReferentialIntegrityCheckOffCore(monitor));
							}
							sb.append("--- DONE ---\n");
							monitor.worked(4);
							checkStatus.setText(sb.toString());
							checkStatus.setStyleRange(styleDBInfo);
							monitor.done();
							
							if (logOK) {
								fos = new FileOutputStream(new File(logFile.getText().trim()));
								out = new OutputStreamWriter(fos, "UTF-8");
								DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
								Date date = new Date();
								out.append("=== " + dateFormat.format(date) + " ===\n");
								out.append(sb.toString());
								out.append("--- OUTPUT LOG Syntactic Check ---\n");
								out.append(SyntacticCheckExec.getOutputLog() + "\n");
								out.append("--- OUTPUT LOG Semantic Check ---\n");
								out.append(SemanticCheckExec.getOutputLog() + "\n");
								out.append("--- OUTPUT LOG Referential Integrity Check ---\n");
								out.append(RefIntegrityCheckExec.getOutputLog() + "\n");
								out.close();
								fos.close();
							}
							
						} catch (FileNotFoundException e) {
							Status status =
								new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
									.getLocalizedMessage(), e);
							StatusManager.getManager().handle(status, StatusManager.SHOW);
						} catch (UnsupportedEncodingException e) {
							Status status =
								new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
									.getLocalizedMessage(), e);
							StatusManager.getManager().handle(status, StatusManager.SHOW);
						} catch (IOException e) {
							Status status =
								new Status(IStatus.ERROR, Activator.PLUGIN_ID, e
									.getLocalizedMessage(), e);
							StatusManager.getManager().handle(status, StatusManager.SHOW);
						}
						
					}
				};
				try {
					pmd.run(false, false, dbcheck);
				} catch (InvocationTargetException | InterruptedException e1) {
					
					StringBuilder sb = new StringBuilder();
					sb.append(e1.getMessage());
					
					Throwable cause = e1.getCause();
					if (cause != null) {
						sb.append(": " + cause.getMessage());
					}
					Status status =
						new Status(IStatus.ERROR, Activator.PLUGIN_ID, sb.toString(), e1);
					e1.printStackTrace();
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		btnNewButton.setText("DB Prüfen");
		
		Group grpPrfoptionen = new Group(container, SWT.NONE);
		grpPrfoptionen.setLayout(new GridLayout(2, true));
		grpPrfoptionen.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		grpPrfoptionen.setText("Optionen");
		
		btnSyntax = new Button(grpPrfoptionen, SWT.CHECK);
		btnSyntax.setSelection(true);
		btnSyntax.setText("Syntaxtest");
		
		btnSemantic = new Button(grpPrfoptionen, SWT.CHECK);
		btnSemantic.setText("Semantictest");
		
		btnReferentialIntegrity = new Button(grpPrfoptionen, SWT.CHECK);
		btnReferentialIntegrity.setForeground(SWTResourceManager.getColor(SWT.COLOR_RED));
		btnReferentialIntegrity.setToolTipText("ACHTUNG: Kann sehr lange dauern!");
		btnReferentialIntegrity.setText("Referential Integrity Test");
		new Label(grpPrfoptionen, SWT.NONE);
		
		checkStatus =
			new StyledText(container, SWT.BORDER | SWT.READ_ONLY | SWT.WRAP | SWT.V_SCROLL
				| SWT.MULTI | SWT.H_SCROLL);
		checkStatus.setEditable(false);
		GridData gd_checkStatus = new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1);
		gd_checkStatus.minimumWidth = 400;
		gd_checkStatus.minimumHeight = 250;
		checkStatus.setLayoutData(gd_checkStatus);
		
		grpAusgabeInDatei = new Group(container, SWT.NONE);
		grpAusgabeInDatei.setText("Ausgabe in Datei speichern (optional)");
		grpAusgabeInDatei.setLayoutData(new GridData(SWT.FILL, SWT.FILL, false, false, 2, 1));
		grpAusgabeInDatei.setLayout(new GridLayout(2, false));
		
		composite = new Composite(grpAusgabeInDatei, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, true, 2, 1));
		composite.setLayout(new GridLayout(2, false));
		
		logFile = new Text(composite, SWT.BORDER);
		logFile.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		logFile.setBounds(0, 0, 64, 19);
		
		Button btnNewButton_1 = new Button(composite, SWT.NONE);
		btnNewButton_1.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				fileChooser =
					new FileDialog(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
						SWT.OPEN | SWT.SAVE);
				fileChooser.setText("Ausgabedatei für Datenbank-Test wählen...");
				String file = fileChooser.open();
				if (file != null)
					logFile.setText(file);
			}
		});
		btnNewButton_1.setBounds(0, 0, 94, 30);
		btnNewButton_1.setText("Wählen...");
		
		// Contributions
		Group external = new Group(container, SWT.None);
		GridData gridData = new GridData(SWT.FILL, SWT.CENTER, true, false);
		gridData.horizontalSpan = 2;
		external.setLayoutData(gridData);
		external.setLayout(new GridLayout(2, false));
		external.setText("External Maintenance Tools");
		extContributions = new ComboViewer(external, SWT.None);
		extContributions.getCombo().setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		extContributions.setLabelProvider(new ExtContributionsLabelProvider());
		extContributions.setContentProvider(new ArrayContentProvider());
		extContributions.setInput(ExternalContributions.getExt());
		extContributions.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (sel.getFirstElement() instanceof ExternalMaintenance)
					exec.setEnabled(true);
			}
		});
		
		exec = new Button(external, SWT.PUSH);
		exec.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false));
		exec.setText("Execute");
		exec.setEnabled(false);
		
		exec.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				final ExternalMaintenance m =
					(ExternalMaintenance) ((IStructuredSelection) extContributions.getSelection())
						.getFirstElement();
				Display.getDefault().asyncExec(new Runnable() {
					@Override
					public void run(){
						dbCheckJob = new DBCheckJob(m);
						dbCheckJob.setUser(true);
						dbCheckJob.schedule();
					}
				});
			}
			
		});
		
// ExternalContributions.getExternalContributions(e, checkStatus);
		
		return container;
	}
	
	protected void setExecOutput(final String execOutput){
		Display.getDefault().syncExec(new Runnable() {
			@Override
			public void run(){
				checkStatus.setText(execOutput);
			}
		});
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, "Beenden", true);
	}
	
	@Override
	protected void okPressed(){
		if (dbCheckJob == null || dbCheckJob.getState() == Job.NONE) {
			super.okPressed();
		} else {
			MessageDialog.openInformation(getShell(), "Laufende Datenbank Wartung",
				"Zuletzt gestartete Datenbanküberprüfung noch nicht abgeschlossen...\n(siehe rechts unten)");
		}
	}
	
	class DBCheckJob extends Job {
		private ExternalMaintenance extMaintenance;
		
		public DBCheckJob(ExternalMaintenance extMaintenance){
			super("Datenbank Wartung");
			this.extMaintenance = extMaintenance;
		}
		
		@Override
		protected IStatus run(IProgressMonitor monitor){
			try {
				ExecExternalContribution eec = new ExecExternalContribution(extMaintenance);
				eec.run(monitor);
				setExecOutput(eec.getOutput());
			} catch (InvocationTargetException e1) {
				e1.printStackTrace();
				Status status = new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getMessage());
				StatusManager.getManager().handle(status, StatusManager.SHOW);
				return status;
			} catch (InterruptedException e1) {
				Status status =
					new Status(IStatus.ERROR, Activator.PLUGIN_ID, e1.getLocalizedMessage());
				StatusManager.getManager().handle(status, StatusManager.SHOW);
				return status;
			}
			return Status.OK_STATUS;
		}
	}
}
