package ch.elexis.core.ui.e4.controls;

import java.util.function.Consumer;

import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ListViewer;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.List;

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.e4.providers.IdentifiableLabelProvider;
import ch.elexis.core.ui.icons.Images;

public class IIdentifiableModifiableListComposite<T extends Identifiable> extends Composite {
	
	private ListViewer identifiableListViewer;
	
	private Runnable addElementHandler;
	private Consumer<T> removeElementHandler;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public IIdentifiableModifiableListComposite(Composite parent, int style){
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.verticalSpacing = 0;
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		Composite toolbar = new Composite(this, SWT.NONE);
		GridLayout gl_toolbar = new GridLayout(2, false);
		gl_toolbar.marginHeight = 0;
		toolbar.setLayout(gl_toolbar);
		toolbar.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		
		Button btnAdd = new Button(toolbar, SWT.NONE);
		btnAdd.setImage(Images.IMG_ADDITEM.getImage());
		btnAdd.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				addElementHandler.run();
				identifiableListViewer.refresh();
			}
		});
		
		Button btnRemove = new Button(toolbar, SWT.NONE);
		btnRemove.setImage(Images.IMG_REMOVEITEM.getImage());
		btnRemove.setEnabled(false);
		btnRemove.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e){
				IStructuredSelection structuredSelection =
					identifiableListViewer.getStructuredSelection();
				if (!structuredSelection.isEmpty()) {
					structuredSelection.forEach(removeElementHandler);
					identifiableListViewer.refresh();
				}
			}
		});
		
		identifiableListViewer = new ListViewer(this, SWT.BORDER | SWT.V_SCROLL);
		identifiableListViewer.setLabelProvider(new IdentifiableLabelProvider());
		identifiableListViewer.setContentProvider(ArrayContentProvider.getInstance());
		
		List identifiableList = identifiableListViewer.getList();
		GridData gd_identifiableList = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_identifiableList.minimumWidth = 150;
		identifiableList.setLayoutData(gd_identifiableList);
		
		identifiableListViewer.addSelectionChangedListener(sel -> {
			btnRemove.setEnabled(sel.getStructuredSelection().getFirstElement() != null);
		});
		
	}
	
	@Override
	public boolean setFocus(){
		return identifiableListViewer.getList().setFocus();
	}
	
	public StructuredViewer getStructuredViewer(){
		return identifiableListViewer;
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	/**
	 * Set a {@link Runnable} to be executed when the user selects the <i>Add Element</i>
	 * 
	 * @param runnable
	 */
	public void setAddElementHandler(Runnable runnable){
		this.addElementHandler = runnable;
	}
	
	/**
	 * Set a {@link Consumer} to be executed when the user selects <i>Delete Element</i>, will be
	 * called separately for each selected element
	 * 
	 * @param consumer
	 */
	public void setRemoveElementHandler(Consumer<T> consumer){
		this.removeElementHandler = consumer;
		
	}
	
}
