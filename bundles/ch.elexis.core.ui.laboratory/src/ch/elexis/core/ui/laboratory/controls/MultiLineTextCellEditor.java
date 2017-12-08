package ch.elexis.core.ui.laboratory.controls;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.StatusDialog;
import org.eclipse.jface.viewers.DialogCellEditor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

public class MultiLineTextCellEditor extends DialogCellEditor {
	
	public class MultiLineTextDialog extends StatusDialog {
		private Text text;
		private String initialValue;
		private String value;
		
		protected MultiLineTextDialog(Shell parentShell){
			super(parentShell);
		}
		
		public String getText(){
			return value;
		}
		
		@Override
		protected Control createDialogArea(Composite parent){
			Composite composite = new Composite(parent, SWT.NONE);
			GridData data = new GridData(GridData.FILL, GridData.FILL, true, true);
			data.minimumWidth = 500;
			data.minimumHeight = 200;
			composite.setLayoutData(data);
			composite.setLayout(new FillLayout());
			text = new Text(composite, SWT.MULTI | SWT.V_SCROLL | SWT.BORDER);
			text.setText(initialValue);
			return composite;
		}
		
		public int open(String value){
			initialValue = value;
			return super.open();
		}
		
		@Override
		protected void okPressed(){
			value = text.getText();
			super.okPressed();
		}
	}
	
	public MultiLineTextCellEditor(Composite parent){
		super(parent);
	}
	
	@Override
	protected Object openDialogBox(Control cellEditorWindow){
		MultiLineTextDialog dialog = new MultiLineTextDialog(cellEditorWindow.getShell());
		dialog.setTitle(Messages.MultiLineTextCellEditor_title);
		
		int result = -1;
		if (getValue() != null) {
			result = dialog.open(getValue().toString());
		} else {
			result = dialog.open(""); //$NON-NLS-1$
		}
		
		if (result == Dialog.OK) {
			return dialog.getText();
		} else {
			return null;
		}
	}
}
