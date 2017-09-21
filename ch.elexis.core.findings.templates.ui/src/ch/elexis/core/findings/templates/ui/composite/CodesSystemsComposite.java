package ch.elexis.core.findings.templates.ui.composite;

import java.util.List;

import org.apache.commons.lang3.ObjectUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.ui.dlg.CodeDialog;
import ch.elexis.core.findings.templates.ui.views.FindingsTemplateView;

public class CodesSystemsComposite extends Composite {
	
	private TableViewer tableViewer;
	
	public CodesSystemsComposite(Composite parent){
		super(parent, SWT.NONE);
		this.setLayout(new GridLayout(4, false));
		this.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}
	
	public void createContens(){
		Label lblCodeSystem = new Label(this, SWT.NONE);
		lblCodeSystem.setText("Code System:");
		Label lblCodeSystemText = new Label(this, SWT.NONE);
		lblCodeSystemText.setText(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());

		
		Button btnAdd = new Button(this, SWT.PUSH);
		btnAdd.setText("Code erstellen..");
		btnAdd.setLayoutData(new GridData(SWT.RIGHT, SWT.FILL, false, false, 2, 1));
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				CodeDialog codeDialog = new CodeDialog(getShell());
				if (codeDialog.open() == MessageDialog.OK) {
					loadTable();
				}
			}
		});
		
		tableViewer = new TableViewer(this);
		tableViewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 4, 1));
		tableViewer.setContentProvider(new ArrayContentProvider());
		tableViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				ICoding iCoding = (ICoding) element;
				return iCoding != null ? iCoding.getDisplay() : "";
			}
		});
		
		loadTable();
		createContextMenu(tableViewer);
	}
	
	private void createContextMenu(Viewer viewer){
		MenuManager contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager mgr){
				IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
				if (selection.getFirstElement() instanceof ICoding) {
					fillContextMenu(mgr, selection.toArray());
				}
			}
		});
		
		Menu menu = contextMenu.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
	}
	
	public void loadTable(){
		List<ICoding> codings = FindingsTemplateView.codingService
			.getAvailableCodes(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
		codings.sort((a, b) -> ObjectUtils.compare(a.getDisplay(), b.getDisplay()));
		tableViewer.setInput(codings);
	}
	
	private void fillContextMenu(IMenuManager contextMenu, Object[] objects){
		contextMenu.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		contextMenu.add(new Action("entfernen") {
			@Override
			public void run(){
				if (objects != null) {
					for (Object o : objects) {
						if (o instanceof ICoding) {
							FindingsTemplateView.codingService.removeLocalCoding((ICoding) o);
						}
					}
					loadTable();
				}
			}
		});
	}
}
