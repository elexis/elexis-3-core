package ch.elexis.core.ui.locks;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.lock.ILocalLockService.Status;
import ch.elexis.core.lock.types.LockInfo;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Table;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Label;

public class LockStatusDialog extends TitleAreaDialog {
	private Table table;
	
	/**
	 * Create the dialog.
	 * 
	 * @param parentShell
	 */
	public LockStatusDialog(Shell parentShell){
		super(parentShell);
		
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		setMessage("Overview on the locks currently held by this Elexis instance");
		setTitle("Lock Status");
		Composite area = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(area, SWT.NONE);
		container.setLayout(new GridLayout(1, false));
		container.setLayoutData(new GridData(GridData.FILL_BOTH));
		
		CheckboxTableViewer checkboxTableViewer =
			CheckboxTableViewer.newCheckList(container, SWT.BORDER | SWT.FULL_SELECTION);
		table = checkboxTableViewer.getTable();
		table.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		checkboxTableViewer.setContentProvider(ArrayContentProvider.getInstance());
		checkboxTableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				LockInfo li = (LockInfo) element;
				return li.getUser() + "@" + li.getElementStoreToString();
			}
		});
		checkboxTableViewer.setInput(CoreHub.getLocalLockService().getCopyOfAllHeldLocks());
		
		Label lblSystemUuid = new Label(container, SWT.NONE);
		lblSystemUuid.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		lblSystemUuid.setText("System UUID: " + CoreHub.getLocalLockService().getSystemUuid());
		
		Label lblLockStatus = new Label(container, SWT.NONE);
		Status status = CoreHub.getLocalLockService().getStatus();
		StringBuilder statusString = new StringBuilder();
		statusString.append("Lock-Service: " + status.name());
		if (status != Status.STANDALONE) {
			statusString.append(" @ " + System
				.getProperty(ElexisSystemPropertyConstants.ELEXIS_SERVER_REST_INTERFACE_URL));
		}
		lblLockStatus.setText(statusString.toString());
		
		return area;
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
	
	/**
	 * Return the initial size of the dialog.
	 */
	@Override
	protected Point getInitialSize(){
		return new Point(450, 300);
	}
}
