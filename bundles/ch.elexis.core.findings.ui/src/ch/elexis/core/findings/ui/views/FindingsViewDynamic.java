package ch.elexis.core.findings.ui.views;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.DefaultNatTableStyleConfiguration;
import org.eclipse.nebula.widgets.nattable.data.IDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.data.DefaultCornerDataProvider;
import org.eclipse.nebula.widgets.nattable.grid.layer.ColumnHeaderLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.CornerLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.GridLayer;
import org.eclipse.nebula.widgets.nattable.grid.layer.RowHeaderLayer;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayer;
import org.eclipse.nebula.widgets.nattable.painter.layer.CellLayerPainter;
import org.eclipse.nebula.widgets.nattable.painter.layer.ILayerPainter;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.style.theme.ModernNatTableThemeConfiguration;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.ILocalCoding;
import ch.elexis.core.findings.ui.preferences.FindingsSettings;
import ch.elexis.core.findings.ui.util.FindingsUiUtil;
import ch.elexis.core.findings.ui.views.nattable.DragAndDropSupport;
import ch.elexis.core.findings.ui.views.nattable.DynamicCodingHeaderDataProvider;
import ch.elexis.core.findings.ui.views.nattable.DynamicCodingRowDataProvider;
import ch.elexis.core.findings.ui.views.nattable.DynamicDataProvider;
import ch.elexis.core.findings.ui.views.nattable.DynamicDateHeaderDataProvider;
import ch.elexis.core.findings.ui.views.nattable.DynamicDateRowDataProvider;
import ch.elexis.core.findings.ui.views.nattable.FindingsNatTableTooltip;
import ch.elexis.core.findings.ui.views.nattable.LabelDataProvider;
import ch.elexis.core.findings.ui.views.nattable.NatTableWrapper;
import ch.elexis.core.findings.ui.views.nattable.NatTableWrapper.IDoubleClickListener;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.events.RefreshingPartListener;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.IRefreshable;
import jakarta.inject.Inject;

public class FindingsViewDynamic extends ViewPart implements IRefreshable {

	private RefreshingPartListener udpateOnVisible = new RefreshingPartListener(this);

	private CodesSelectionComposite codeSelectionComposite;

	private NatTable natTable;
	private DynamicDataProvider dataProvider;
	private IDataProvider headerDataProvider;
	private IDataProvider rowDataProvider;

	@Optional
	@Inject
	void crudFinding(@UIEventTopic(ElexisEventTopics.BASE_MODEL + "*") IFinding finding) {
		CoreUiUtil.runAsyncIfActive(() -> {
			refresh();
		}, natTable);
	}

	@Optional
	@Inject
	void reloadFindings(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Object clazz) {
		if (IFinding.class.equals(clazz)) {
			CoreUiUtil.runAsyncIfActive(() -> {
				refresh();
			}, natTable);
		}
	}

	@Optional
	@Inject
	void reloadCodings(@UIEventTopic(ElexisEventTopics.EVENT_RELOAD) Object clazz) {
		if (ICoding.class.equals(clazz)) {
			CoreUiUtil.runAsyncIfActive(() -> {
				codeRefresh();
			}, natTable);
		}
	}

	@Optional
	@Inject
	void crudCoding(@UIEventTopic(ElexisEventTopics.BASE_MODEL + "*") ICoding coding) {
		codeRefresh();
	}

	@Inject
	void activePatient(@Optional IPatient patient) {
		CoreUiUtil.runAsyncIfActive(() -> {
			refresh();
		}, natTable);
	}

	private NatTableWrapper wrapper;

	public FindingsViewDynamic() {
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite main = new Composite(parent, SWT.NONE);
		main.setLayout(SWTHelper.createGridLayout(true, 1));

		codeSelectionComposite = new CodesSelectionComposite(main, SWT.NONE);
		codeSelectionComposite.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));

		// NatTable setup
		dataProvider = new DynamicDataProvider();
		if (ConfigServiceHolder.getGlobal(FindingsSettings.ROWSAREDATES, false)) {
			dataProvider.setRowsAreDates(true);
			headerDataProvider = new DynamicCodingHeaderDataProvider(dataProvider);
			rowDataProvider = new DynamicDateRowDataProvider(dataProvider);
		} else {
			dataProvider.setRowsAreDates(false);
			headerDataProvider = new DynamicDateHeaderDataProvider(dataProvider);
			rowDataProvider = new DynamicCodingRowDataProvider(dataProvider);
		}

		// wrap the data provider with a label data provider
		DataLayer bodyDataLayer = new DataLayer(new LabelDataProvider(dataProvider, new ObservationLabelProvider()));
		bodyDataLayer.setColumnPercentageSizing(true);
		// disable drawing cells lines
		SelectionLayer selectionLayer = new SelectionLayer(bodyDataLayer) {
			private CellLayerPainter painter = new CellLayerPainter();

			@Override
			public ILayerPainter getLayerPainter() {
				return painter;
			}
		};
		ViewportLayer viewportLayer = new ViewportLayer(selectionLayer);

		// build the column header layer stack
		DataLayer columnHeaderDataLayer = new DataLayer(headerDataProvider);
		ILayer columnHeaderLayer = new ColumnHeaderLayer(columnHeaderDataLayer, viewportLayer, selectionLayer);

		// build the row header layer
		DataLayer rowHeaderDataLayer = new DataLayer(rowDataProvider, 150, 20);
		ILayer rowHeaderLayer = new RowHeaderLayer(rowHeaderDataLayer, viewportLayer, selectionLayer);

		// build the corner layer
		IDataProvider cornerDataProvider = new DefaultCornerDataProvider(headerDataProvider, rowDataProvider);
		DataLayer cornerDataLayer = new DataLayer(cornerDataProvider);
		ILayer cornerLayer = new CornerLayer(cornerDataLayer, rowHeaderLayer, columnHeaderLayer);

		// build the grid layer
		GridLayer gridLayer = new GridLayer(viewportLayer, columnHeaderLayer, rowHeaderLayer, cornerLayer);

		natTable = new NatTable(main, NatTable.DEFAULT_STYLE_OPTIONS | SWT.BORDER, gridLayer, false);
		natTable.setBackground(natTable.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		natTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		natTable.addConfiguration(new DefaultNatTableStyleConfiguration());

		wrapper = new NatTableWrapper(natTable, dataProvider, selectionLayer);
		wrapper.addContextMenu("ch.elexis.core.findings.ui.views.FindingsView", getSite());
		getSite().setSelectionProvider(wrapper);
		wrapper.configure();

		natTable.setTheme(new ModernNatTableThemeConfiguration());

		codeSelectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {

			@Override
			public void selectionChanged(SelectionChangedEvent event) {
				StructuredSelection selection = (StructuredSelection) event.getSelection();
				updateCodingsSelection(selection);
			}
		});
		updateCodingsSelection((StructuredSelection) codeSelectionComposite.getSelection());

		wrapper.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(NatTableWrapper source, ISelection selection) {
				StructuredSelection structuredSelection = (StructuredSelection) selection;
				if (!structuredSelection.isEmpty()) {
					for (Object o : structuredSelection.toList()) {
						if (o instanceof IFinding) {
							FindingsUiUtil.executeCommand("ch.elexis.core.findings.ui.commandEdit", (IFinding) o);
						}
					}
				}
			}
		});

		// add DnD support
		DragAndDropSupport dndSupport = new DragAndDropSupport(wrapper);
		Transfer[] transfer = { TextTransfer.getInstance() };
		natTable.addDragSupport(DND.DROP_COPY, transfer, dndSupport);

		atachTooltip();

		getSite().getPage().addPartListener(udpateOnVisible);

		refresh();
	}

	public void setRowsAreDates(boolean value) {
		if (dataProvider.isRowsAreDates() != value) {
			dataProvider.setRowsAreDates(value);
			if (value) {
				headerDataProvider = new DynamicCodingHeaderDataProvider(dataProvider);
				rowDataProvider = new DynamicDateRowDataProvider(dataProvider);
			} else {
				headerDataProvider = new DynamicDateHeaderDataProvider(dataProvider);
				rowDataProvider = new DynamicCodingRowDataProvider(dataProvider);
			}
		}
	}

	private void atachTooltip() {
		DefaultToolTip toolTip = new FindingsNatTableTooltip(natTable, dataProvider);
		toolTip.setPopupDelay(250);
		toolTip.activate();
		toolTip.setShift(new Point(10, 10));
	}

	private void updateCodingsSelection(StructuredSelection selection) {
		@SuppressWarnings("unchecked")
		List<ICoding> shownCodings = (List<ICoding>) selection.toList();
		// convert groups to contents
		shownCodings = expandGroups(shownCodings);

		shownCodings.sort(new Comparator<ICoding>() {

			@Override
			public int compare(ICoding arg0, ICoding arg1) {
				if (arg0 instanceof ILocalCoding && arg1 instanceof ILocalCoding) {
					ILocalCoding left = (ILocalCoding) arg0;
					ILocalCoding right = (ILocalCoding) arg1;
					return Integer.valueOf(left.getPrio()).compareTo(Integer.valueOf(right.getPrio()));
				}
				return 0;
			}
		});
		dataProvider.setShownCodings(shownCodings);
		natTable.refresh(true);
	}

	private List<ICoding> expandGroups(List<ICoding> codings) {
		List<ICoding> ret = new ArrayList<>();
		for (ICoding iCoding : codings) {
			if (FindingsUiUtil.isCodingForGroup(iCoding)) {
				List<ICoding> codesOfGroup = FindingsUiUtil.getCodesOfGroup(iCoding);
				for (ICoding codeOfGroup : codesOfGroup) {
					if (!ret.contains(codeOfGroup)) {
						ret.add(codeOfGroup);
					}
				}
			} else {
				ret.add(iCoding);
			}
		}
		return ret;
	}

	@Override
	public void refresh() {
		dataProvider.reload(ContextServiceHolder.get().getActivePatient().orElse(null));
		natTable.refresh();
	}

	public void codeRefresh() {
		codeSelectionComposite.refresh();
		updateCodingsSelection((StructuredSelection) codeSelectionComposite.getSelection());
	}

	@Override
	public void dispose() {
		getSite().getPage().removePartListener(udpateOnVisible);
		super.dispose();
	}

	@Override
	public void setFocus() {
		natTable.setFocus();
	}
}
