package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;

import ch.rgw.tools.StringTool;
public class TextTemplateImportConflictDialog extends TitleAreaDialog {
	private String name;
	private Button btnReplace, btnChangeName, btnSkip;
	private Text txtNewName;
	
	private String newFilename;
	private boolean replaceTemplate, changeTemplateName, skipTemplate;
	
	public TextTemplateImportConflictDialog(Shell parentShell, String name){
		super(parentShell);
		this.name = name;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayout(new GridLayout(1, false));
		area.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		setMessage("Eine Vorlage mit dem Namen '" + name
			+ "' besteht bereits. Wie soll vorgegangen werden?");
		setTitle("Vorlage existiert bereits");
		
		btnReplace = new Button(area, SWT.RADIO);
		btnReplace.setText("Ersetzen (Bestehende Vorlage wird überschrieben)");
		btnReplace.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				clearAndDisableTextField();
			}
		});
		
		btnChangeName = new Button(area, SWT.RADIO);
		btnChangeName.setText("Name der Vorlage ändern zu");
		btnChangeName.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				txtNewName.setEnabled(true);
			}
		});
		
		txtNewName = new Text(area, SWT.BORDER);
		txtNewName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		txtNewName.setEnabled(false);
		
		btnSkip = new Button(area, SWT.RADIO);
		btnSkip.setText("Vorlage nicht importieren");
		btnSkip.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				clearAndDisableTextField();
			}
		});
		
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, "OK", true);
	}
	
	private void clearAndDisableTextField(){
		txtNewName.setText(StringTool.leer);
		txtNewName.setEnabled(false);
	}
	
	@Override
	protected void okPressed(){
		if (btnChangeName.getSelection()) {
			if (txtNewName.getText().isEmpty()) {
				SWTHelper.alert("Fehlende Bezeichnung",
					"Neuer Name der Vorlage muss definiert werden!");
				return;
			}
			this.newFilename = txtNewName.getText();
		}
		replaceTemplate = btnReplace.getSelection();
		changeTemplateName = btnChangeName.getSelection();
		skipTemplate = btnSkip.getSelection();
		
		super.okPressed();
	}
	
	@Override
	protected boolean canHandleShellCloseEvent(){
		return false;
	}
	
	public boolean doReplaceTemplate(){
		return replaceTemplate;
	}
	
	public boolean doChangeTemplateName(){
		return changeTemplateName;
	}
	
	public boolean doSkipTemplate(){
		return skipTemplate;
	}
	
	public String getNewName(){
		return newFilename;
	}
}
