package ch.elexis.core.ui.medication.views;

import java.util.List;

import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.IHandlerService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.data.Prescription;

public class MedicationTableComposite extends Composite {
	
	private static Logger log = LoggerFactory.getLogger(MedicationTableComposite.class);
	private TableViewer viewer;
	private TableColumnLayout layout;
	
	private MedicationComposite medicationComposite;
	private List<Prescription> pendingInput;
	
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
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				if (ss != null && !ss.isEmpty()) {
					try {
					IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getService(IHandlerService.class);
						handlerService.executeCommand(
							"ch.elexis.core.ui.medication.OpenArticelDetailDialog", null);
					} catch (ExecutionException | NotDefinedException | NotEnabledException
							| NotHandledException e) {
						MessageDialog.openError(getShell(), "Fehler",
							"Eigenschaften konnten nicht geöffnet werden.");
						log.error("cannot open article detail dialog", e);
					}
				}
			}
			
		});
		
		MedicationViewerHelper.createTypeColumn(viewer, layout, 0);
		MedicationViewerHelper.createArticleColumn(viewer, layout, 1);
		MedicationViewerHelper.createDosageColumn(viewer, layout, 2);
		MedicationViewerHelper.createBeginColumn(viewer, layout, 3);
		MedicationViewerHelper.createIntakeCommentColumn(viewer, layout, 4);
		MedicationViewerHelper.createMandantColumn(viewer, layout, 5);
		
		viewer.setContentProvider(new MedicationTableViewerContentProvider(viewer));
	}
	
	public void setMedicationComposite(MedicationComposite medicationComposite){
		this.medicationComposite = medicationComposite;
	}
	
	public TableViewer getTableViewer(){
		return viewer;
	}
	
	public void setInput(List<Prescription> medicationInput){
		if (isVisible()) {
			viewer.setInput(medicationInput);
		} else {
			pendingInput = medicationInput;
		}
	}
	
	public void setPendingInput(){
		if (pendingInput != null) {
			viewer.setInput(pendingInput);
			pendingInput = null;
		}
	}
}
