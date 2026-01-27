package ch.elexis.core.ui.e4.fhir.parts;

import java.util.List;
import java.util.Map;
import java.util.function.Supplier;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.Focus;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.modeling.EPartService.PartState;
import org.eclipse.e4.ui.workbench.modeling.ESelectionService;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;

import ch.elexis.core.fhir.model.IFhirModelService;
import ch.elexis.core.fhir.model.interfaces.IFhirBased;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.IContextService;
import ch.elexis.core.services.ICoverageService;
import ch.elexis.core.ui.e4.fhir.parts.supplier.CoverageSupplierFactory;
import ch.elexis.core.ui.e4.fhir.providers.FhirBasedLabelProvider;
import ch.elexis.core.ui.e4.parts.IRefreshablePart;
import ch.elexis.core.ui.icons.Images;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class CoverageListPart implements IDoubleClickListener, IRefreshablePart {

	@Inject
	ESelectionService selectionService;

	@Inject
	EPartService partService;

	@Inject
	IContextService contextService;

	@Inject
	ICoverageService coverageService;

	private TableViewer tableViewer;

	@Inject
	public CoverageListPart(MPart part, @Optional IFhirModelService fhirModelService) {
		part.setLabel("Fälle");
		if (fhirModelService != null) {
			part.setIconURI("icon://IMG_FHIR");
		}
	}

	@Inject
	void activePatient(@Optional IPatient patient) {
		refresh();
	}

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		parent.setLayout(new GridLayout(1, false));

		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		TableColumnLayout tcl_composite = new TableColumnLayout();
		composite.setLayout(tcl_composite);

		tableViewer = new TableViewer(composite, SWT.FULL_SELECTION);
		Table table = tableViewer.getTable();
		table.setHeaderVisible(false);
		table.setLinesVisible(false);
		tableViewer.setContentProvider(ArrayContentProvider.getInstance());
		tableViewer.setUseHashlookup(true);

		TableViewerColumn tableViewerColumn = new TableViewerColumn(tableViewer, SWT.NONE);
		TableColumn tblclmn = tableViewerColumn.getColumn();
		tcl_composite.setColumnData(tblclmn, new ColumnWeightData(100, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmn.setResizable(false);
		tableViewer.addDoubleClickListener(this);
		tableViewerColumn.setLabelProvider(createColumnLabelProvider());

		tableViewer.addSelectionChangedListener(event -> {
			IStructuredSelection selection = tableViewer.getStructuredSelection();
			selectionService.setSelection(selection.toList());
			// TODO enforce load latest if set to context?
			contextService.setActiveCoverage((ICoverage) selection.getFirstElement());
		});

		tableViewer.addDoubleClickListener(this);

		menuService.registerContextMenu(tableViewer,
				"ch.elexis.core.ui.e4.fhir.parts.coveragelist.popupmenu.tableresults"); //$NON-NLS-1$

		refresh();
	}

	@Override
	public void refresh(Map<Object, Object> filterParameters) {
		if (tableViewer == null) {
			return;
		}
		tableViewer.setInput(null);
		IPatient patient = contextService.getActivePatient().orElse(null);
		if (patient != null) {
			Supplier<List<ICoverage>> supplier = CoverageSupplierFactory.get(patient, 100);
			Object[] array = supplier.get().toArray();
			if (array.length > 0) {
				tableViewer.setInput(array);
			}
		}
	}

	@Focus
	public void setFocus() {
		tableViewer.getTable().setFocus();
	}

	private ColumnLabelProvider createColumnLabelProvider() {
		return new ColumnLabelProvider() {
			@Override
			public Image getImage(Object element) {
				if (element instanceof IFhirBased fb) {
					return FhirBasedLabelProvider.getImage(fb);
				}
				if (element instanceof ICoverage coverage) {
					if (coverage.isOpen()) {
						// show red/green dot is case invalid/valid
						if (coverageService.isValid(coverage)) {
							return Images.IMG_OK.getImage();
						} else {
							return Images.IMG_FEHLER.getImage();
						}
					} else {
						return Images.IMG_LOCK_CLOSED.getImage();
					}
				}
				return super.getImage(element);
			}

			@Override
			public String getText(Object element) {
				if (element instanceof IFhirBased fb) {
					return FhirBasedLabelProvider.getText(fb);
				}
				return ((ICoverage) element).getLabel();
			}
		};
	}

	@Override
	public void doubleClick(DoubleClickEvent event) {
		partService.showPart("ch.elexis.FallDetailView", PartState.VISIBLE);
	}

}