package ch.elexis.core.ui.dialogs;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.ui.util.SWTHelper;

public class FreeTextDiagnoseDialog extends TitleAreaDialog {
	
	private Text text;
	
	private String result;
	
	public FreeTextDiagnoseDialog(Shell activeShell){
		super(activeShell);
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		setTitle("Neue Freitext Diagnose");
		setMessage("Geben Sie den Text der Diagnose ein");
		
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		text = new Text(ret, SWT.MULTI | SWT.BORDER | SWT.WRAP | SWT.V_SCROLL);
		text.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		result = text.getText();
		super.okPressed();
	}
	
	public String getText(){
		return result;
	}
}
