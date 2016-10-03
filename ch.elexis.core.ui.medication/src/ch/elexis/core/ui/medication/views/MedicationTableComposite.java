package ch.elexis.core.ui.medication.views;

import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IPersistentObject;

public class MedicationTableComposite extends Composite {
	
	private TableViewer viewer;
	private TableColumnLayout layout;
	
	private MedicationComposite medicationComposite;
	
	public MedicationTableComposite(Composite parent, int style){
		super(parent, style);
		
		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		layout = new TableColumnLayout();
		setLayout(layout);
		
		viewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		viewer.getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		
		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e){
				IStructuredSelection is =
					(IStructuredSelection) viewer.getSelection();
				MedicationTableViewerItem presc = (MedicationTableViewerItem) is.getFirstElement();
				
				// set last disposition information
				IPersistentObject po = (presc != null) ? presc.getLastDisposed() : null;
				medicationComposite.setLastDisposalPO(po);
				
				// set writable databinding value
				medicationComposite.setSelectedMedication(presc);
				ElexisEventDispatcher
					.fireSelectionEvent((presc != null) ? presc.getPrescription() : null);
			}
		});
		
		MedicationViewerHelper.createTypeColumn(viewer, layout, 0);
		MedicationViewerHelper.createArticleColumn(viewer, layout, 1);
		MedicationViewerHelper.createDosageColumn(viewer, layout, 2);
		MedicationViewerHelper.createBeginColumn(viewer, layout, 3);
		MedicationViewerHelper.createIntakeCommentColumn(viewer, layout, 4);
		MedicationViewerHelper.createMandantColumn(viewer, layout, 5);
		
		viewer.setContentProvider(ArrayContentProvider.getInstance());
	}
	
	public void setMedicationComposite(MedicationComposite medicationComposite){
		this.medicationComposite = medicationComposite;
	}
	
	public TableViewer getTableViewer(){
		return viewer;
	}
}
