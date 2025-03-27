 
package ch.elexis.core.ui.reminder.part;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.ParameterizedCommand;
import org.eclipse.e4.core.commands.ECommandService;
import org.eclipse.e4.core.commands.EHandlerService;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.di.UIEventTopic;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.jface.window.DefaultToolTip;
import org.eclipse.nebula.widgets.nattable.NatTable;
import org.eclipse.nebula.widgets.nattable.config.AbstractUiBindingConfiguration;
import org.eclipse.nebula.widgets.nattable.config.CellConfigAttributes;
import org.eclipse.nebula.widgets.nattable.config.IConfigRegistry;
import org.eclipse.nebula.widgets.nattable.grid.GridRegion;
import org.eclipse.nebula.widgets.nattable.layer.DataLayer;
import org.eclipse.nebula.widgets.nattable.layer.ILayerListener;
import org.eclipse.nebula.widgets.nattable.layer.SpanningDataLayer;
import org.eclipse.nebula.widgets.nattable.layer.cell.ILayerCell;
import org.eclipse.nebula.widgets.nattable.layer.event.ILayerEvent;
import org.eclipse.nebula.widgets.nattable.painter.layer.NatGridLayerPainter;
import org.eclipse.nebula.widgets.nattable.resize.MaxCellBoundsHelper;
import org.eclipse.nebula.widgets.nattable.resize.command.MultiRowResizeCommand;
import org.eclipse.nebula.widgets.nattable.selection.SelectionLayer;
import org.eclipse.nebula.widgets.nattable.selection.action.SelectCellAction;
import org.eclipse.nebula.widgets.nattable.selection.event.CellSelectionEvent;
import org.eclipse.nebula.widgets.nattable.style.CellStyleAttributes;
import org.eclipse.nebula.widgets.nattable.style.DisplayMode;
import org.eclipse.nebula.widgets.nattable.style.Style;
import org.eclipse.nebula.widgets.nattable.ui.action.IMouseAction;
import org.eclipse.nebula.widgets.nattable.ui.binding.UiBindingRegistry;
import org.eclipse.nebula.widgets.nattable.ui.matcher.MouseEventMatcher;
import org.eclipse.nebula.widgets.nattable.util.GCFactory;
import org.eclipse.nebula.widgets.nattable.util.ObjectUtils;
import org.eclipse.nebula.widgets.nattable.viewport.ViewportLayer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.MouseEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Text;
import org.slf4j.LoggerFactory;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.IReminder;
import ch.elexis.core.model.IUser;
import ch.elexis.core.model.issue.ProcessStatus;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.reminder.part.nattable.ReminderBodyDataProvider;
import ch.elexis.core.ui.reminder.part.nattable.ReminderColumn;
import ch.elexis.core.ui.views.IRefreshable;
import jakarta.annotation.PostConstruct;
import jakarta.inject.Inject;

public class ReminderTablesPart implements IRefreshable {

	private NatTable natTable;
	private ReminderBodyDataProvider dataProvider;
	private DataLayer dataLayer;

	@Inject
	private ECommandService commandService;

	@Inject
	private EHandlerService handlerService;
	private SelectionLayer selectionLayer;
	private ViewportLayer viewportLayer;

	private Text searchText;
	private SearchRunnable currentSearchRunnable;

	@Inject
	public ReminderTablesPart() {
		dataProvider = new ReminderBodyDataProvider();
	}
	
	@Optional
	@Inject
	void activePatient(IPatient patient) {
		refresh(false);
	}

	@Optional
	@Inject
	void activeUser(IUser user) {
		refresh();
	}

	@Optional
	@Inject
	void updateReminder(@UIEventTopic(ElexisEventTopics.EVENT_UPDATE) IReminder reminder) {
		refresh(false);
	}

	@Optional
	@Inject
	void createReminder(@UIEventTopic(ElexisEventTopics.EVENT_CREATE) IReminder reminder) {
		refresh(false);
	}

	@Optional
	@Inject
	void deleteReminder(@UIEventTopic(ElexisEventTopics.EVENT_DELETE) IReminder reminder) {
		refresh(false);
	}

	@PostConstruct
	public void postConstruct(Composite parent, EMenuService menuService) {
		parent.setLayout(new GridLayout());
		searchText = new Text(parent, SWT.SEARCH | SWT.BORDER);
		searchText.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false));
		searchText.setMessage("Suche nach Titel");
		searchText.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (StringUtils.isNotBlank(searchText.getText()) && searchText.getText().length() > 2) {
					if (currentSearchRunnable != null) {
						currentSearchRunnable.cancel();
					}
					currentSearchRunnable = new SearchRunnable(searchText.getText());
					CompletableFuture.delayedExecutor(1000, TimeUnit.MILLISECONDS).execute(currentSearchRunnable);
				} else {
					resetSearch();
				}
			}

			private void resetSearch() {
				if (currentSearchRunnable != null) {
					currentSearchRunnable.cancel();
				}
				if (dataProvider.getColumnCount() > 0 && dataProvider.getColumns().get(0).hasSearch()) {
					dataProvider.getColumns().forEach(c -> c.setSearch(null));
					refresh(true);
				}
			}
		});

		// To make the default edit and selection configurations work correctly,
		// the region label GridRegion.BODY is necessary, which is directly set to the
		// ViewportLayer instance here.
		dataLayer = new SpanningDataLayer(dataProvider);
		selectionLayer = new SelectionLayer(dataLayer);
		viewportLayer = new ViewportLayer(selectionLayer);
		viewportLayer.setRegionName(GridRegion.BODY);

		viewportLayer.setConfigLabelAccumulator(new ReminderTablesConfigLabelsAccumulator(dataProvider, viewportLayer));
		dataProvider.setColumns(loadColumnsPreference());

		natTable = new NatTable(parent, NatTable.DEFAULT_STYLE_OPTIONS | SWT.BORDER, viewportLayer, false);
		natTable.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		natTable.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WHITE));
		natTable.setLayerPainter(new NatGridLayerPainter(natTable, DataLayer.DEFAULT_ROW_HEIGHT));

		natTable.addConfiguration(new ReminderTablesStyleConfiguration());

		addSelection();
		addTooltip();

		natTable.configure();

		updateColumns();

		// add DnD support
		DragAndDropSupport dndSupport = new DragAndDropSupport(natTable, selectionLayer, dataProvider);
		Transfer[] transfer = { TextTransfer.getInstance() };
		natTable.addDragSupport(DND.DROP_COPY, transfer, dndSupport);
		natTable.addDropSupport(DND.DROP_COPY, transfer, dndSupport);

		// register context menu for natTable
		menuService.registerContextMenu(natTable, "ch.elexis.core.ui.reminder.popupmenu.remindertable"); //$NON-NLS-1$

		dataProvider.reload();
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(natTable)) {
				natTable.refresh();
			}
		});
	}

	public List<ReminderColumn> getColumns() {
		return new ArrayList<>(dataProvider.getColumns());

	}

	public void setColumns(List<ReminderColumn> columns) {
		dataProvider.setColumns(columns);
		updateColumns();

		dataProvider.reload();
		Display.getDefault().asyncExec(() -> {
			if (CoreUiUtil.isActiveControl(natTable)) {
				natTable.refresh();
			}
		});
		saveColumnsPreference();
	}

	private void saveColumnsPreference() {
		String columns = getColumns().stream().map(c -> c.getName()).collect(Collectors.joining("|"));
		ConfigServiceHolder.get().setActiveUserContact("ch.elexis.core.ui.reminder.part/columns", columns);
	}

	private List<ReminderColumn> loadColumnsPreference() {
		String names = ConfigServiceHolder.get().getActiveUserContact("ch.elexis.core.ui.reminder.part/columns", "Meine|Patient|Alle");
		List<String> nameParts = Arrays.asList(names.split("\\|")); 
		return ReminderColumn.getAllAvailable().stream().filter(c -> nameParts.contains(c.getName())).toList();
	}

	private void addTooltip() {
		DefaultToolTip toolTip = new ReminderNatTableToolTip(natTable, dataProvider);
//		toolTip.setBackgroundColor(natTable.getDisplay().getSystemColor(SWT.COLOR_RED));
		toolTip.setPopupDelay(500);
		toolTip.activate();
		toolTip.setShift(new Point(10, 10));
	}

	private void addSelection() {
		natTable.addLayerListener(new ILayerListener() {

			// Default selection behavior selects cells by default.
			@Override
			public void handleLayerEvent(ILayerEvent event) {
				if (event instanceof CellSelectionEvent) {
					CellSelectionEvent cellEvent = (CellSelectionEvent) event;
					Integer columnPosition = natTable.getColumnIndexByPosition(cellEvent.getColumnPosition());
					Integer rowPosition = natTable.getRowIndexByPosition(cellEvent.getRowPosition());
					if (columnPosition >= 0 && rowPosition >= 0) {
						Object data = dataProvider.getData(columnPosition, rowPosition);
						if (data instanceof IReminder) {
//							System.out.println("Selected cell: [" + cellEvent.getRowPosition() + ", "
//									+ cellEvent.getColumnPosition() + "], "
//									+ ((IReminder) data).getSubject());
							ContextServiceHolder.get().setTyped(data);
						} else {
							ContextServiceHolder.get().removeTyped(IReminder.class);
						}
					} else {
						ContextServiceHolder.get().removeTyped(IReminder.class);
					}
				}
			}
		});

		natTable.addConfiguration(new AbstractUiBindingConfiguration() {

			@Override
			public void configureUiBindings(UiBindingRegistry uiBindingRegistry) {
				uiBindingRegistry.registerDoubleClickBinding(
						new MouseEventMatcher(SWT.NONE, GridRegion.BODY, MouseEventMatcher.LEFT_BUTTON),
						new IMouseAction() {

							@Override
							public void run(NatTable natTable, MouseEvent event) {
								Object data = SelectionUtil.getData(natTable, dataProvider, event.x, event.y);
								if (data instanceof IReminder) {
									ILayerCell cell = SelectionUtil.getCell(natTable, dataProvider, event.x,
											event.y);
									if (cell != null) {
										if (SelectionUtil.isHoverCell(natTable, dataProvider, cell, event.x,
												event.y)) {
											if (SelectionUtil.isHoverCheck(natTable, dataProvider, cell, event.x,
													event.y)) {
												IReminder reminder = (IReminder) data;
												reminder.setStatus(ProcessStatus.CLOSED);
												CoreModelServiceHolder.get().save(reminder);
												ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE,
														reminder);
												return;
											}
											ParameterizedCommand command = commandService.createCommand(
													"ch.elexis.core.ui.reminder.command.editReminder", null);//$NON-NLS-1$
											if (command != null) {
												handlerService.executeHandler(command);
											} else {
												LoggerFactory.getLogger(getClass()).error("Command not found"); //$NON-NLS-1$
											}
										}
									}
								}
							}
						});

				uiBindingRegistry.registerMouseDownBinding(
						new MouseEventMatcher(SWT.NONE, null, MouseEventMatcher.RIGHT_BUTTON), new SelectCellAction());
			}
		});
	}

	private void updateColumns() {
		for (ReminderColumn reminderColumn : dataProvider.getColumns()) {
			IConfigRegistry configRegistry = natTable.getConfigRegistry();
			Style columnStyle = new Style();
			columnStyle.setAttributeValue(CellStyleAttributes.BACKGROUND_COLOR,
					CoreUiUtil.getColorForString(reminderColumn.getColor()));
			configRegistry.registerConfigAttribute(CellConfigAttributes.CELL_STYLE, columnStyle,
					DisplayMode.NORMAL, "BG_" + reminderColumn.getName());
		}

		resetColumns();
		dataLayer.setDefaultMinColumnWidth(50);
		dataLayer.setColumnPercentageSizing(true);
	}

	private void resetColumns() {
		// disable column width percentage sizing
		dataLayer.setColumnPercentageSizing(false);

		// reset the size configuration to show the default sizes again
		dataLayer.resetColumnWidthConfiguration(true);
		dataLayer.resetRowHeightConfiguration(true);
	}

	private void refresh(boolean updateColumns) {
		if (dataProvider != null) {
			dataProvider.reload();
			Display.getDefault().asyncExec(() -> {
				if (CoreUiUtil.isActiveControl(natTable)) {
					if (updateColumns) {
						updateColumns();
					} else {
						updateRowHeights();
					}
					natTable.refresh();
				}
			});
		}
	}

	private void updateRowHeights() {
		final List<Integer> positions = new ArrayList<>();
		final List<Integer> heights = new ArrayList<>();
		int rowCount = this.viewportLayer.getRowCount();
		if (rowCount > 0) {
			for (int i = 0; i < rowCount; i++) {
				int[] rowPos = new int[1];
				int[] rowHeights = new int[1];
				rowPos[0] = this.viewportLayer.getRowIndexByPosition(i);
				rowHeights[0] = this.viewportLayer.getRowHeightByPosition(i);

				if (dataProvider.getData(0, rowPos[0]) instanceof String && rowPos[0] > 0) {
					int[] calculatedRowHeights = MaxCellBoundsHelper.getPreferredRowHeights(
							this.natTable.getConfigRegistry(), new GCFactory(this.natTable), dataLayer, rowPos);
					if (calculatedRowHeights != null && calculatedRowHeights.length > 0) {
						if (calculatedRowHeights[0] >= 0) {
							// on scaling there could be a difference of 1
							// pixel because of rounding issues.
							// in that case we do not trigger a resize to
							// avoid endless useless resizing
							int diff = rowHeights[0] - calculatedRowHeights[0];
							if (diff < -1 || diff > 1) {
								positions.add(rowPos[0]);
								heights.add(calculatedRowHeights[0]);
							}
						}
					}
				}
			}
		}
		if (!positions.isEmpty()) {
			dataLayer.doCommand(new MultiRowResizeCommand(dataLayer, ObjectUtils.asIntArray(positions),
					ObjectUtils.asIntArray(heights), true));
		}
	}

	@Override
	public void refresh() {
		refresh(true);
	}

	private class SearchRunnable implements Runnable {

		private String search;
		private boolean cancel;

		public SearchRunnable(String search) {
			this.search = search;
		}

		public void cancel() {
			this.cancel = true;
		}

		@Override
		public void run() {
			if (!cancel) {
				currentSearchRunnable = null;
				dataProvider.getColumns().forEach(c -> c.setSearch(search));
				refresh(true);
			}
		}
	}
}