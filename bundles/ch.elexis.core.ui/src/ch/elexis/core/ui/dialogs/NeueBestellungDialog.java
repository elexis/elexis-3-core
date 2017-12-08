package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.util.LimitedText;

public class NeueBestellungDialog extends TitleAreaDialog {
	private String title, message;
	private LimitedText ltTitle;
	
	public NeueBestellungDialog(Shell parentShell, String title, String message){
		super(parentShell);
		this.title = title;
		this.message = message;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle(title);
		setMessage(message);
		
		Composite area = new Composite(parent, SWT.NONE);
		area.setLayoutData(new GridData(GridData.FILL_BOTH));
		area.setLayout(new GridLayout(2, false));
		
		ltTitle = new LimitedText(area, SWT.BORDER, calcMaxAllowedLength());
		ltTitle.setText(Messages.NeueBestellungDialog_Automatic);
		
		return area;
	}
	
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		super.createButtonsForButtonBar(parent);
		ltTitle.setDisableControl(getButton(IDialogConstants.OK_ID));
	}
	
	private int calcMaxAllowedLength(){
		int timestampAndSeparators = 16;
		int reserved = CoreHub.actUser.getId().length() + timestampAndSeparators;
		return 80 - reserved;
	}
	
	@Override
	protected void okPressed(){
		title = ltTitle.getText();
		super.okPressed();
	}
	
	public String getTitle(){
		return title;
	}
}
