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

import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ContextServiceHolder;

public class MedicationHistoryTableComposite extends Composite {

	private TableViewer viewer;
	private TableColumnLayout layout;

	private MedicationComposite medicationComposite;
	private List<IPrescription> pendingInput;

	public MedicationHistoryTableComposite(Composite parent, int style) {
		super(parent, style);

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		layout = new TableColumnLayout();
		setLayout(layout);

		viewer = new TableViewer(this, SWT.BORDER | SWT.FULL_SELECTION | SWT.MULTI);
		viewer.getTable().setHeaderVisible(true);
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);

		viewer.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent e) {
				IStructuredSelection is = (IStructuredSelection) viewer.getSelection();
				MedicationTableViewerItem presc = (MedicationTableViewerItem) is.getFirstElement();

				// set last disposition information
				Identifiable identifiable = (presc != null) ? presc.getLastDisposed() : null;
				medicationComposite.setLastDisposal(identifiable);

				// set writable databinding value
				medicationComposite.setSelectedMedication(presc);
				if (presc != null) {
					IPrescription selectedObj = presc.getPrescription();
					ContextServiceHolder.get().getRootContext().setTyped(selectedObj);
				} else {
					ContextServiceHolder.get().getRootContext().removeTyped(IPrescription.class);
				}
			}
		});
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event) {
				StructuredSelection ss = (StructuredSelection) event.getSelection();
				if (ss != null && !ss.isEmpty()) {
					try {
						IHandlerService handlerService = (IHandlerService) PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getService(IHandlerService.class);
						handlerService.executeCommand("ch.elexis.core.ui.medication.OpenArticelDetailDialog", null);
					} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
						MessageDialog.openError(getShell(), "Fehler", "Eigenschaften konnten nicht geöffnet werden.");
					}
				}
			}

		});

		MedicationViewerHelper.createTypeColumn(viewer, layout, 0);
		MedicationViewerHelper.createArticleColumn(viewer, layout, 1);
		MedicationViewerHelper.createDosageColumn(viewer, layout, 2);
		MedicationViewerHelper.createBeginColumn(viewer, layout, 3);
		MedicationViewerHelper.createIntakeCommentColumn(viewer, layout, 4);

		MedicationViewerHelper.createStopColumn(viewer, layout, 5);
		MedicationViewerHelper.createStopReasonColumn(viewer, layout, 6);
		MedicationViewerHelper.createMandantColumn(viewer, layout, 7);

		viewer.setContentProvider(new MedicationTableViewerContentProvider(viewer));
	}

	public void setMedicationComposite(MedicationComposite medicationComposite) {
		this.medicationComposite = medicationComposite;
	}

	public TableViewer getTableViewer() {
		return viewer;
	}

	public void setInput(List<IPrescription> medicationHistoryInput) {
		if (isVisible()) {
			viewer.setInput(medicationHistoryInput);
		} else {
			pendingInput = medicationHistoryInput;
		}
	}

	public void setPendingInput() {
		if (pendingInput != null) {
			viewer.setInput(pendingInput);
			pendingInput = null;
		}
	}
}
