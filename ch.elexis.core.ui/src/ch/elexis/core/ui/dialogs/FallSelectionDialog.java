package ch.elexis.core.ui.dialogs;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.ToolBar;

import ch.elexis.core.data.interfaces.IFall;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.dto.FallDTO;

public class FallSelectionDialog extends TitleAreaDialog {
	private final String message;
	private final FallDTO currentFall;
	
	private TableViewer tableViewer;
	private Composite ret;
	
	private Optional<IFall> selectedFall = Optional.empty();
	
	public FallSelectionDialog(Shell parentShell, String message, FallDTO currentFall){
		super(parentShell);
		setShellStyle(getShellStyle() | SWT.RESIZE);
		this.message = message;
		this.currentFall = currentFall;
	}
	
	@Override
	protected Control createDialogArea(final Composite parent){
		selectedFall = Optional.empty();
		ret = new Composite(parent, SWT.NONE);
		GridLayout gd = new GridLayout(1, false);
		gd.marginTop = 10;
		ret.setLayout(gd);
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		
		Patient patient = currentFall.getPatient();
		
		if (patient == null || !patient.exists()) {
			Label lblPatient = new Label(ret, SWT.NONE);
			lblPatient.setText("Kein Patient ausgewählt!");
		} else {
			Text txtPatient = new Text(ret, SWT.READ_ONLY);
			txtPatient.setText("Patient: " + currentFall.getPatient().getLabel());

			ToolBarManager tbManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.WRAP);
			tbManager.add(new Action("Neuer Fall") {
				
				@Override
				public ImageDescriptor getImageDescriptor(){
					return Images.IMG_NEW.getImageDescriptor();
				}
				
				@Override
				public String getToolTipText(){
					return "Neuer Fall";
				}
				
				@Override
				public void run(){
					NeuerFallDialog neuerFallDialog =
						new NeuerFallDialog(getShell(), currentFall.getPatient(), true);
					neuerFallDialog.open();
					refresh();
				}
			});
			ToolBar toolbar = tbManager.createControl(ret);
			// align toolbar right
			GridDataFactory.fillDefaults().align(SWT.END, SWT.CENTER).grab(true, false)
				.applyTo(toolbar);
			Composite tableComposite = new Composite(ret, SWT.NONE);
			GridData gdTable = SWTHelper.getFillGridData(1, true, 1, true);
			gdTable.heightHint = 200;
			tableComposite.setLayoutData(gdTable);
			tableViewer =
				new TableViewer(tableComposite,
					SWT.BORDER | SWT.FULL_SELECTION);
			tableViewer.setContentProvider(new ArrayContentProvider());
			tableViewer.getTable().setHeaderVisible(true);
			tableViewer.getTable().setLinesVisible(true);
			tableViewer.setLabelProvider(new LabelProvider() {
				@Override
				public String getText(Object element){
					Fall f = (Fall) element;
					return f.getLabel();
				}
			});
			tableViewer.addDoubleClickListener(new IDoubleClickListener() {
				
				@Override
				public void doubleClick(DoubleClickEvent event){
					IStructuredSelection selection = (IStructuredSelection) event.getSelection();
					Object firstElement = selection.getFirstElement();
					if (firstElement instanceof Fall) {
						FallEditDialog neuerFallDialog =
							new FallEditDialog(getShell(), (Fall) firstElement);
						neuerFallDialog.open();
						refresh();
					}
				}
			});
			

			TableColumn singleColumn = new TableColumn(tableViewer.getTable(), SWT.NONE);
			singleColumn.setText("Fälle");
			TableColumnLayout tableColumnLayout = new TableColumnLayout();
			tableColumnLayout.setColumnData(singleColumn, new ColumnWeightData(100));
			tableComposite.setLayout(tableColumnLayout);
			
			refresh();
			ret.pack();
		}

		return ret;
	}
	
	private void refresh(){
		if (tableViewer != null && ret != null) {
			Patient patient = currentFall.getPatient();
			if (patient != null && patient.exists()) {
				List<Fall> faelle = new ArrayList<>();
				for (Fall f : patient.getFaelle()) {
					if (!f.getId().equals(currentFall.getId())) {
						faelle.add(f);
					}
				}
				tableViewer.setInput(faelle);
				tableViewer.refresh();
				ret.layout(true, true);
			}
		}
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Fallauswahl");
		setMessage(message);
	}
	
	@Override
	protected void okPressed(){
		IStructuredSelection iSelection = (IStructuredSelection) tableViewer.getSelection();
		if (iSelection != null) {
			Object o = iSelection.getFirstElement();
			if (o instanceof IFall) {
				selectedFall = Optional.of((IFall) o);
			}
		}
		super.okPressed();
	}
	
	public Optional<IFall> getSelectedFall(){
		return selectedFall;
	}
	
}
