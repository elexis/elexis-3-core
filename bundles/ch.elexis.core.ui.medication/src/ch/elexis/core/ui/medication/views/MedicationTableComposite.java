package ch.elexis.core.ui.medication.views;

import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_CLASS;
import static ch.elexis.core.ui.constants.ExtensionPointConstantsUi.VIEWCONTRIBUTION_VIEWID;

import java.lang.reflect.Method;
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

import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.views.contribution.IViewContribution;
import ch.elexis.core.ui.views.contribution.ViewContributionHelper;

public class MedicationTableComposite extends Composite {

	private static Logger log = LoggerFactory.getLogger(MedicationTableComposite.class);
	private TableViewer viewer;
	private TableColumnLayout layout;

	private MedicationComposite medicationComposite;
	private List<IPrescription> pendingInput;

	@SuppressWarnings("unchecked")
	private final List<IViewContribution> tableViewerColumnContributions = Extensions.getClasses(VIEWCONTRIBUTION,
			VIEWCONTRIBUTION_CLASS, VIEWCONTRIBUTION_VIEWID, "ch.medelexis.MedicationCompositeContribution");


	public MedicationTableComposite(Composite parent, int style) {
		super(parent, style);

		setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		layout = new TableColumnLayout();
		setLayout(layout);

		viewer = new TableViewer(this, SWT.FULL_SELECTION | SWT.MULTI);
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
						handlerService.executeCommand("ch.elexis.core.ui.medication.OpenArticelDetailDialog", null); //$NON-NLS-1$
					} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
						MessageDialog.openError(getShell(), "Fehler", "Eigenschaften konnten nicht geöffnet werden.");
						log.error("cannot open article detail dialog", e); //$NON-NLS-1$
					}
				}
			}

		});

		List<IViewContribution> _tableViewerColumnContributions = ViewContributionHelper
				.getFilteredAndPositionSortedContributions(tableViewerColumnContributions, 0);
		for (IViewContribution ivc : _tableViewerColumnContributions) {
			Composite ret = ivc.initComposite(this);
			ret.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
			try {
				Method method = ivc.getClass().getMethod("addNewColumn", TableViewer.class);
				method.invoke(ivc, viewer);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}

		MedicationViewerHelper.createTypeColumn(viewer, layout, 0);
		MedicationViewerHelper.createArticleColumn(viewer, layout, 1);
		MedicationViewerHelper.createDosageColumn(viewer, layout, 2);
		MedicationViewerHelper.createBeginColumn(viewer, layout, 3);
		MedicationViewerHelper.createIntakeCommentColumn(viewer, layout, 4);
		MedicationViewerHelper.createDisposalCommentColumn(viewer, layout, 8);
		MedicationViewerHelper.createMandantColumn(viewer, layout, 7);

		viewer.setContentProvider(new MedicationTableViewerContentProvider(viewer));
	}

	public void setMedicationComposite(MedicationComposite medicationComposite) {
		this.medicationComposite = medicationComposite;
	}

	public TableViewer getTableViewer() {
		return viewer;
	}

	public void setInput(List<IPrescription> medicationInput) {
		if (isVisible()) {
			viewer.setInput(medicationInput);
		} else {
			pendingInput = medicationInput;
		}
	}

	public void setPendingInput() {
		if (pendingInput != null) {
			viewer.setInput(pendingInput);
			pendingInput = null;
		}
	}
}
