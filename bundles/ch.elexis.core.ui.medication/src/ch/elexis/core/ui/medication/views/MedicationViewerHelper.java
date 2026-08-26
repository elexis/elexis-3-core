package ch.elexis.core.ui.medication.views;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;
import java.util.Arrays;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class MedicationViewerHelper {

	/** Width of the fixed icon columns (interaction, type, article marking). */
	private static final int ICON_COLUMN_WIDTH = 20;

	/** Width of the article marking column when visible. */
	public static final int ARTICLE_MARKING_COLUMN_WIDTH = ICON_COLUMN_WIDTH;

	/** Key under which the column order config key is stored in the table data. */
	private static final String COLUMN_ORDER_KEY_DATA = "medication.columnOrder.configKey"; //$NON-NLS-1$

	/** Re-entrancy guard flag stored in the table data while adjusting the filler. */
	private static final String FILLER_ADJUSTING_DATA = "medication.fillerColumn.adjusting"; //$NON-NLS-1$

	/** Minimum width the filler column keeps when columns overflow the view. */
	private static final int FILLER_MIN_WIDTH = 40;

	public static TableViewerColumn createInteractionColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnStateDisposition = ret.getColumn();
		tblclmnStateDisposition.setToolTipText("Interaktion");
		tblclmnStateDisposition.setWidth(ICON_COLUMN_WIDTH);
		tblclmnStateDisposition.setResizable(false);
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public String getToolTipText(Object element) {
				return ((MedicationTableViewerItem) element).getInteractionText();
			}

			@Override
			public Image getImage(Object element) {
				return ((MedicationTableViewerItem) element).getInteractionImage();
			}
		});
		return ret;
	}

	public static TableViewerColumn createTypeColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnStateDisposition = ret.getColumn();
		tblclmnStateDisposition.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.Core_Type);
		tblclmnStateDisposition.setWidth(ICON_COLUMN_WIDTH);
		tblclmnStateDisposition.setResizable(false);
		tblclmnStateDisposition.addSelectionListener(getSelectionAdapter(viewer, tblclmnStateDisposition, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public Image getImage(Object element) {
				return ((MedicationTableViewerItem) element).getImage();
			}
		});
		return ret;
	}

	public static TableViewerColumn createArticleColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn tblclmnArticle = ret.getColumn();
		tblclmnArticle.setWidth(250);
		tblclmnArticle.setText(Messages.Core_Article);
		tblclmnArticle.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.Core_Article);
		tblclmnArticle.addSelectionListener(getSelectionAdapter(viewer, tblclmnArticle, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {

			@Override
			public String getText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				return pres.getArtikelLabel();
			}

			@Override
			public String getToolTipText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				String label = StringUtils.EMPTY;

				if (pres.isActiveMedication()) {
					String date = pres.getBeginDate();
					if (date != null && !date.isEmpty()) {
						label = MessageFormat.format(Messages.MedicationComposite_startedAt, date);
					}
					// check if stop date is set
					String endDate = pres.getEndDate();
					if (endDate != null && !endDate.isEmpty()) {
						String reason = pres.getStopReason() == null ? "?" : pres.getStopReason(); //$NON-NLS-1$
						label += (StringUtils.LF + MessageFormat.format(Messages.MedicationComposite_stopDateAndReason,
								endDate, reason));
					}
				} else {
					IRecipe recipe = pres.getRecipe();
					IBilled billed = pres.getBilled();
					if (recipe != null || billed != null) {
						if (recipe != null) {
							label = MessageFormat.format(Messages.MedicationComposite_lastReceivedAt,
									DateTimeFormatter.ofPattern("dd.MM.yyyy").format(recipe.getDate())); //$NON-NLS-1$
						} else if (billed != null) {
							if (billed.getEncounter() != null) {
								label = MessageFormat.format(Messages.MedicationComposite_lastReceivedAt,
										DateTimeFormatter.ofPattern("dd.MM.yyyy") //$NON-NLS-1$
												.format(billed.getEncounter().getDate()));
							}
						}
					} else {
						String date = pres.getEndDate();
						String reason = pres.getStopReason() == null ? "?" : pres.getStopReason(); //$NON-NLS-1$
						label = MessageFormat.format(Messages.MedicationComposite_stopDateAndReason, date, reason);
					}
				}
				return label;
			}
		});
		return ret;
	}

	public static TableViewerColumn createArticleMarkingColumn(TableViewer viewer) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnMarking = ret.getColumn();
		tblclmnMarking.setToolTipText(Messages.Core_Article);
		tblclmnMarking.setWidth(ARTICLE_MARKING_COLUMN_WIDTH);
		tblclmnMarking.setResizable(false);
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element) {
				return StringUtils.EMPTY;
			}

			@Override
			public Image getImage(Object element) {
				return ((MedicationTableViewerItem) element).getArticleImage();
			}
		});
		return ret;
	}

	public static TableViewerColumn createDosageColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		ret.setLabelProvider(new MedicationCellLabelProvider() {

			@Override
			public String getText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				String dosis = pres.getDosis();
				return (dosis.equals(StringConstants.ZERO) ? Messages.MedicationComposite_stopped : dosis);
			}
		});
		TableColumn tblclmnDosage = ret.getColumn();
		tblclmnDosage.setToolTipText(Messages.TherapieplanComposite_tblclmnDosage_toolTipText);
		tblclmnDosage.addSelectionListener(getSelectionAdapter(viewer, tblclmnDosage, columnIndex));
		tblclmnDosage.setWidth(60);
		ret.getColumn().setText(Messages.TherapieplanComposite_tblclmnDosage_text);
		return ret;
	}

	public static TableViewerColumn createBeginColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn tblclmnEnacted = ret.getColumn();
		tblclmnEnacted.setWidth(60);
		tblclmnEnacted
				.setImage(Images.resize(Images.IMG_NEXT_WO_SHADOW.getImage(), ImageSize._12x12_TableColumnIconSize));
		tblclmnEnacted.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.Core_Date_Startdate);
		tblclmnEnacted.addSelectionListener(getSelectionAdapter(viewer, tblclmnEnacted, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {

			@Override
			public String getText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				return pres.getBeginDate();
			}
		});
		return ret;
	}

	public static TableViewerColumn createIntakeCommentColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnComment = ret.getColumn();
		tblclmnComment.setWidth(200);
		tblclmnComment.setText(Messages.Prescription_Instruction);
		tblclmnComment.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.Prescription_Instruction);
		tblclmnComment.addSelectionListener(getSelectionAdapter(viewer, tblclmnComment, columnIndex));
		tblclmnComment.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.Prescription_Instruction);
		ret.setLabelProvider(new MedicationCellLabelProvider() {

			@Override
			public String getText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				return pres.getRemark();
			}
		});
		return ret;
	}

	public static TableViewerColumn createDisposalCommentColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnComment = ret.getColumn();
		tblclmnComment.setWidth(200);
		tblclmnComment.setText(Messages.Prescription_Reason);
		tblclmnComment.setToolTipText(
				Messages.MedicationComposite_column_sortBy + StringUtils.SPACE + Messages.Prescription_Reason);
		tblclmnComment.addSelectionListener(getSelectionAdapter(viewer, tblclmnComment, columnIndex));
		tblclmnComment.setToolTipText(
				Messages.MedicationComposite_column_sortBy + StringUtils.SPACE + Messages.Prescription_Reason);
		ret.setLabelProvider(new MedicationCellLabelProvider() {

			@Override
			public String getText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				return pres.getDisposalComment();
			}
		});
		return ret;
	}

	public static TableViewerColumn createStopColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn tblclmnStop = ret.getColumn();
		tblclmnStop.setWidth(60);
		tblclmnStop.setImage(
				Images.resize(Images.IMG_ARROWSTOP_WO_SHADOW.getImage(), ImageSize._12x12_TableColumnIconSize));
		tblclmnStop.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.MedicationComposite_column_endDate);
		tblclmnStop.addSelectionListener(getSelectionAdapter(viewer, tblclmnStop, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				if (pres.getEntryType() != EntryType.RECIPE && pres.getEntryType() != EntryType.SELF_DISPENSED) {
					return pres.getEndDate();
				}
				return StringUtils.EMPTY;
			}
		});
		return ret;
	}

	public static TableViewerColumn createStopReasonColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn tblclmnReason = ret.getColumn();
		tblclmnReason.setWidth(150);
		tblclmnReason.setText(Messages.MedicationComposite_stopReason);
		tblclmnReason.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.MedicationComposite_stopReason);
		tblclmnReason.addSelectionListener(getSelectionAdapter(viewer, tblclmnReason, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element) {
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				if (pres.getEntryType() != EntryType.RECIPE && pres.getEntryType() != EntryType.SELF_DISPENSED) {
					String stopReason = pres.getStopReason();
					if (stopReason != null && !stopReason.isEmpty()) {
						return stopReason;
					}
				}
				return StringUtils.EMPTY;
			}
		});
		return ret;
	}

	public static TableViewerColumn createMandantColumn(TableViewer viewer, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn tblclmnMandant = ret.getColumn();
		tblclmnMandant.setWidth(150);
		tblclmnMandant.setText(Messages.Core_User);
		tblclmnMandant.setToolTipText(Messages.Core_User);
		tblclmnMandant.addSelectionListener(getSelectionAdapter(viewer, tblclmnMandant, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element) {
				return ((MedicationTableViewerItem) element).getPrescriptorLabel();
			}
		});
		return ret;
	}

	public static void addContextMenu(TableViewer viewer, MedicationComposite medicationComposite,
			IWorkbenchPartSite site) {
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(viewer.getTable());

		viewer.getTable().setMenu(menu);
		if (site != null) {
			site.registerContextMenu("ch.elexis.core.ui.medication.tables", menuManager, viewer); //$NON-NLS-1$
		}
	}

	public static void setColumnVisible(TableViewerColumn column, boolean visible) {
		if (column == null || column.getColumn().isDisposed()) {
			return;
		}
		column.getColumn().setWidth(visible ? ARTICLE_MARKING_COLUMN_WIDTH : 0);
	}

	public static void enablePersistentColumns(TableViewer viewer, String configKey) {
		Table table = viewer.getTable();
		table.setData(COLUMN_ORDER_KEY_DATA, configKey);
		String widthKey = configKey + ".width"; //$NON-NLS-1$
		for (TableColumn col : table.getColumns()) {
			col.setMoveable(true);
			col.addListener(SWT.Move, e -> {
				adjustFillerColumn(table);
				Display.getDefault().timerExec(300, () -> {
					if (!table.isDisposed()) {
						int[] order = table.getColumnOrder();
						String orderString = Arrays.stream(order).mapToObj(String::valueOf)
								.collect(Collectors.joining(",")); //$NON-NLS-1$
						ConfigServiceHolder.setUser(configKey, orderString);
					}
				});
			});
			col.addListener(SWT.Resize, e -> {
				adjustFillerColumn(table);
				if (col.getResizable()) {
					Display.getDefault().timerExec(300, () -> saveColumnWidths(table, widthKey));
				}
			});
		}
		table.addListener(SWT.Resize, e -> adjustFillerColumn(table));
		restoreColumnOrder(viewer, configKey);
		restoreColumnWidths(viewer, widthKey);
		Display.getDefault().asyncExec(() -> adjustFillerColumn(table));
	}

	private static void adjustFillerColumn(Table table) {
		if (table == null || table.isDisposed()) {
			return;
		}
		if (Boolean.TRUE.equals(table.getData(FILLER_ADJUSTING_DATA))) {
			return;
		}
		int[] order = table.getColumnOrder();
		if (order.length == 0) {
			return;
		}
		int clientWidth = table.getClientArea().width;
		if (clientWidth <= 0) {
			return;
		}
		TableColumn filler = table.getColumn(order[order.length - 1]);
		int othersWidth = 0;
		for (TableColumn c : table.getColumns()) {
			if (c != filler) {
				othersWidth += c.getWidth();
			}
		}
		int target = Math.max(FILLER_MIN_WIDTH, clientWidth - othersWidth);
		if (filler.getWidth() != target) {
			table.setData(FILLER_ADJUSTING_DATA, Boolean.TRUE);
			try {
				filler.setWidth(target);
			} finally {
				table.setData(FILLER_ADJUSTING_DATA, Boolean.FALSE);
			}
		}
	}

	private static void saveColumnWidths(Table table, String widthKey) {
		if (table.isDisposed()) {
			return;
		}
		String widths = Arrays.stream(table.getColumns()).map(c -> String.valueOf(c.getWidth()))
				.collect(Collectors.joining(",")); //$NON-NLS-1$
		ConfigServiceHolder.setUser(widthKey, widths);
	}

	private static void restoreColumnWidths(TableViewer viewer, String widthKey) {
		Display.getDefault().asyncExec(() -> {
			Table tbl = viewer.getTable();
			if (tbl == null || tbl.isDisposed()) {
				return;
			}
			String widthString = ConfigServiceHolder.getUser(widthKey, null);
			if (widthString != null && !widthString.isEmpty()) {
				try {
					int[] widths = Arrays.stream(widthString.split(",")) //$NON-NLS-1$
							.mapToInt(Integer::parseInt).toArray();
					TableColumn[] columns = tbl.getColumns();
					if (widths.length == columns.length) {
						for (int i = 0; i < columns.length; i++) {
							if (columns[i].getResizable()) {
								columns[i].setWidth(widths[i]);
							}
						}
					}
				} catch (RuntimeException ex) {
					LoggerFactory.getLogger(MedicationViewerHelper.class)
							.warn("Invalid column widths for " + widthKey, ex); //$NON-NLS-1$
				}
			}
		});
	}

	public static void resetColumnOrder(TableViewer viewer) {
		if (viewer == null || viewer.getTable().isDisposed()) {
			return;
		}
		Object configKey = viewer.getTable().getData(COLUMN_ORDER_KEY_DATA);
		if (configKey instanceof String) {
			resetColumnOrder(viewer, (String) configKey);
		}
	}

	public static void resetColumnOrder(TableViewer viewer, String configKey) {
		Table table = viewer.getTable();
		if (table == null || table.isDisposed()) {
			return;
		}
		ConfigServiceHolder.setUser(configKey, null);
		int[] defaultOrder = IntStream.range(0, table.getColumnCount()).toArray();
		table.setColumnOrder(defaultOrder);
	}

	private static void restoreColumnOrder(TableViewer viewer, String configKey) {
		Display.getDefault().asyncExec(() -> {
			Table tbl = viewer.getTable();
			if (tbl == null || tbl.isDisposed()) {
				return;
			}
			String orderString = ConfigServiceHolder.getUser(configKey, null);
			if (orderString != null && !orderString.isEmpty()) {
				try {
					int[] order = Arrays.stream(orderString.split(",")) //$NON-NLS-1$
							.mapToInt(Integer::parseInt).toArray();
					if (order.length == tbl.getColumnCount()) {
						tbl.setColumnOrder(order);
					}
				} catch (RuntimeException ex) {
					LoggerFactory.getLogger(MedicationViewerHelper.class)
							.warn("Invalid column order for " + configKey, ex); //$NON-NLS-1$
				}
			}
		});
	}

	private static SelectionAdapter getSelectionAdapter(final TableViewer viewer, final TableColumn column,
			final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				ViewerSortOrder comparator = MedicationViewHelper.getSelectedComparator();
				if (ViewerSortOrder.DEFAULT.equals(comparator)) {
					comparator.setColumn(index);
					int dir = comparator.getDirection();
					viewer.getTable().setSortColumn(column);
					viewer.getTable().setSortDirection(dir);
					viewer.refresh(true);
				}
			}
		};
		return selectionAdapter;
	}
}
