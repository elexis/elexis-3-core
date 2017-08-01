package ch.elexis.core.findings.templates.ui.dlg;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.templates.ui.views.FindingsView;

public class FindingsEditDialog extends TitleAreaDialog {
	
	private final IFinding iFinding;
	private ICompositeSaveable iCompositeSaveable;
	
	public FindingsEditDialog(Shell parentShell, IFinding iFinding){
		super(parentShell);
		this.iFinding = iFinding;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		String title = FindingsView.findingsTemplateService.getTypeAsText(iFinding);
		setMessage(title);
		setTitle(title + " editieren");
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		String content = iFinding.getText().orElse("");
		
		iCompositeSaveable =
			new CompositeTextUnit(composite, "", "", iFinding);
		return composite;


	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		super.buttonPressed(buttonId);
	}
	
	@Override
	protected void okPressed(){
		if (iCompositeSaveable != null) {
			iCompositeSaveable.saveContents();
		}
		super.okPressed();
	}
	
	class CompositeTextUnit extends Composite implements ICompositeSaveable {
		private Text fieldText;
		private IFinding iFinding;
		
		public CompositeTextUnit(Composite parent, String label, String unit, IFinding iFinding){
			super(parent, SWT.NONE);
			
			setLayout(new GridLayout(3, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			Label lbl = new Label(this, SWT.NONE);
			lbl.setText(label);
			

			if (label.isEmpty() && unit.isEmpty()) {
				fieldText = new Text(this, SWT.BORDER | SWT.MULTI | SWT.V_SCROLL);
				fieldText.setText(iFinding.getText().get());
				fieldText.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			}
			else {
				fieldText = new Text(this, SWT.BORDER);
				fieldText.setText(iFinding.getText().get());
			}
			
			Label lblUnit = new Label(this, SWT.NONE);
			lblUnit.setText(unit);
			
			this.iFinding = iFinding;
		}
		
		@Override
		public void saveContents(){
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
			}
			iFinding.setText(fieldText.getText());
		}
	}
	
	class CompositeGroup extends Composite implements ICompositeSaveable {
		private IFinding iFinding;
		
		private List<ICompositeSaveable> children = Collections.emptyList();
		
		public CompositeGroup(Composite parent, IFinding iFinding, String label){
			super(parent, SWT.NONE);
			this.iFinding = iFinding;
			
			setLayout(new GridLayout(1, false));
			setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
			Label lbl = new Label(this, SWT.NONE);
			lbl.setText(label);
		}
		
		@Override
		public void saveContents(){
			if (iFinding.getId() == null) {
				iFinding = FindingsView.findingsTemplateService.create(iFinding.getClass());
			}
			
			for (ICompositeSaveable iCompositeSaveable : children) {
				iCompositeSaveable.saveContents();
			}
		}
	}
	
	interface ICompositeSaveable {
		public void saveContents();
	}
}
