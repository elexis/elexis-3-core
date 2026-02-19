package ch.elexis.core.ui.medication.views;

import java.text.MessageFormat;
import java.time.format.DateTimeFormatter;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchPartSite;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.IBilled;
import ch.elexis.core.model.IRecipe;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;

public class MedicationViewerHelper {

	public static TableViewerColumn createInteractionColumn(TableViewer viewer, TableColumnLayout layout,
			int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnStateDisposition = ret.getColumn();
		tblclmnStateDisposition.setToolTipText("Interaktion");
		layout.setColumnData(tblclmnStateDisposition, new ColumnPixelData(20, false, false));
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

	public static TableViewerColumn createTypeColumn(TableViewer viewer, TableColumnLayout layout, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnStateDisposition = ret.getColumn();
		tblclmnStateDisposition.setToolTipText(Messages.MedicationComposite_column_sortBy + StringUtils.SPACE
				+ Messages.Core_Type);
		layout.setColumnData(tblclmnStateDisposition, new ColumnPixelData(20, false, false));
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

	public static TableViewerColumn createArticleColumn(TableViewer viewer, TableColumnLayout layout, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn tblclmnArticle = ret.getColumn();
		layout.setColumnData(tblclmnArticle, new ColumnPixelData(250, true, true));
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

	public static TableViewerColumn createDosageColumn(TableViewer viewer, TableColumnLayout layout, int columnIndex) {
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
		layout.setColumnData(tblclmnDosage, new ColumnPixelData(60, true, true));
		ret.getColumn().setText(Messages.TherapieplanComposite_tblclmnDosage_text);
		return ret;
	}

	public static TableViewerColumn createBeginColumn(TableViewer viewer, TableColumnLayout layout, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn tblclmnEnacted = ret.getColumn();
		layout.setColumnData(tblclmnEnacted, new ColumnPixelData(60, true, true));
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

	public static TableViewerColumn createIntakeCommentColumn(TableViewer viewer, TableColumnLayout layout,
			int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnComment = ret.getColumn();
		layout.setColumnData(tblclmnComment, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
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

	public static TableViewerColumn createDisposalCommentColumn(TableViewer viewer, TableColumnLayout layout,
			int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnComment = ret.getColumn();
		layout.setColumnData(tblclmnComment, new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
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

	public static TableViewerColumn createStopColumn(TableViewer viewer, TableColumnLayout layout, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn tblclmnStop = ret.getColumn();
		ColumnPixelData stopColumnPixelData = new ColumnPixelData(60, true, true);
		layout.setColumnData(tblclmnStop, stopColumnPixelData);
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

	public static TableViewerColumn createStopReasonColumn(TableViewer viewer, TableColumnLayout layout,
			int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn tblclmnReason = ret.getColumn();
		ColumnWeightData reasonColumnWeightData = new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true);
		layout.setColumnData(tblclmnReason, reasonColumnWeightData);
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

	public static TableViewerColumn createMandantColumn(TableViewer viewer, TableColumnLayout layout, int columnIndex) {
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn tblclmnMandant = ret.getColumn();
		ColumnWeightData mandantColumnWeightData = new ColumnWeightData(0, 50, true);
		layout.setColumnData(tblclmnMandant, mandantColumnWeightData);
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

	// public static TableViewerColumn createSuppliedUntilColumn(TableViewer viewer,
	// TableColumnLayout layout, int columnIndex) {
	// supplied until
	// TableViewerColumn tableViewerColumnSuppliedUntil =
	// new TableViewerColumn(medicationTableViewer, SWT.CENTER);
	// TableColumn tblclmnSufficient = tableViewerColumnSuppliedUntil.getColumn();
	// tcl_compositeMedicationTable.setColumnData(tblclmnSufficient,
	// new ColumnPixelData(45, true, true));
	// tblclmnSufficient.setText(Messages.TherapieplanComposite_tblclmnSupplied_text);
	// tblclmnSufficient.addSelectionListener(getSelectionAdapter(tblclmnSufficient,
	// columnIndex));
	// tableViewerColumnSuppliedUntil.setLabelProvider(new
	// MedicationCellLabelProvider() {
	//
	// @Override
	// public String getText(Object element){
	// // SLLOW
	// MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
	// if (!pres.isFixedMediation() || pres.isReserveMedication())
	// return StringUtils.EMPTY;
	//
	// TimeTool tt = pres.getSuppliedUntilDate();
	// if (tt != null && tt.isAfterOrEqual(new TimeTool())) {
	// return "OK";
	// }
	//
	// return "?";
	// }
	// });
	// }

	public static void addContextMenu(TableViewer viewer, MedicationComposite medicationComposite,
			IWorkbenchPartSite site) {
		// register context menu for table viewer
		MenuManager menuManager = new MenuManager();
		Menu menu = menuManager.createContextMenu(viewer.getTable());

		viewer.getTable().setMenu(menu);
		if (site != null) {
			site.registerContextMenu("ch.elexis.core.ui.medication.tables", menuManager, viewer); //$NON-NLS-1$
		}
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
