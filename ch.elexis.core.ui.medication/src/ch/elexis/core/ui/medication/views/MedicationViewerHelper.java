package ch.elexis.core.ui.medication.views;

import java.text.MessageFormat;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.layout.TableColumnLayout;
import org.eclipse.jface.viewers.ColumnPixelData;
import org.eclipse.jface.viewers.ColumnWeightData;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.TableViewerColumn;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.IWorkbenchActionConstants;
import org.eclipse.ui.IWorkbenchPartSite;
import org.eclipse.ui.PlatformUI;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.model.prescription.EntryType;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.ArticleDefaultSignatureTitleAreaDialog;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.medication.action.MovePrescriptionPositionInTableDownAction;
import ch.elexis.core.ui.medication.action.MovePrescriptionPositionInTableUpAction;
import ch.elexis.data.Prescription;
import ch.elexis.data.Rezept;
import ch.elexis.data.Verrechnet;

public class MedicationViewerHelper {
	
	public static TableViewerColumn createTypeColumn(TableViewer viewer, TableColumnLayout layout,
		int columnIndex){
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnStateDisposition = ret.getColumn();
		layout.setColumnData(tblclmnStateDisposition, new ColumnPixelData(20, false, false));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element){
				return "";
			}
			
			@Override
			public Image getImage(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				EntryType et = pres.getEntryType();
				switch (et) {
				case FIXED_MEDICATION:
					return Images.IMG_FIX_MEDI.getImage();
				case RESERVE_MEDICATION:
					return Images.IMG_RESERVE_MEDI.getImage();
				case SYMPTOMATIC_MEDICATION:
					return Images.IMG_SYMPTOM_MEDI.getImage();
				case SELF_DISPENSED:
					return Images.IMG_VIEW_CONSULTATION_DETAIL.getImage();
				case RECIPE:
					return Images.IMG_VIEW_RECIPES.getImage();
				default:
					return Images.IMG_EMPTY_TRANSPARENT.getImage();
				}
			}
		});
		return ret;
	}
	
	public static TableViewerColumn createArticleColumn(TableViewer viewer,
		TableColumnLayout layout, int columnIndex){
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		final TableColumn tblclmnArticle = ret.getColumn();
		layout.setColumnData(tblclmnArticle, new ColumnPixelData(250, true, true));
		tblclmnArticle.setText(Messages.TherapieplanComposite_tblclmnArticle_text);
		tblclmnArticle
			.addSelectionListener(getSelectionAdapter(viewer, tblclmnArticle, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			
			@Override
			public String getText(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				return pres.getArtikelLabel();
			}
			
			@Override
			public String getToolTipText(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				String label = "";
				
				if (pres.isFixedMediation()) {
					String date = pres.getBeginDate();
					if (date != null && !date.isEmpty()) {
						label = MessageFormat.format(Messages.MedicationComposite_startedAt, date);
					}
				} else {
					IPersistentObject po = pres.getLastDisposed();
					if (po != null) {
						if (po instanceof Rezept) {
							Rezept rp = (Rezept) po;
							label = MessageFormat
								.format(Messages.MedicationComposite_lastReceivedAt, rp.getDate());
						} else if (po instanceof Verrechnet) {
							Verrechnet v = (Verrechnet) po;
							if (v.getKons() != null) {
								label = MessageFormat.format(
									Messages.MedicationComposite_lastReceivedAt,
									v.getKons().getDatum());
							}
						}
					} else {
						String date = pres.getEndDate();
						String reason = pres.getStopReason() == null ? "?" : pres.getStopReason();
						label = MessageFormat.format(Messages.MedicationComposite_stopDateAndReason,
							date, reason);
					}
				}
				return label;
			}
		});
		return ret;
	}
	
	public static TableViewerColumn createDosageColumn(TableViewer viewer,
		TableColumnLayout layout, int columnIndex){
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			
			@Override
			public String getText(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				String dosis = pres.getDosis();
				return (dosis.equals(StringConstants.ZERO) ? Messages.MedicationComposite_stopped
						: dosis);
			}
		});
		TableColumn tblclmnDosage = ret.getColumn();
		tblclmnDosage.setToolTipText(Messages.TherapieplanComposite_tblclmnDosage_toolTipText);
		tblclmnDosage.addSelectionListener(getSelectionAdapter(viewer, tblclmnDosage, columnIndex));
		layout.setColumnData(tblclmnDosage, new ColumnPixelData(60, true, true));
		ret.getColumn().setText(Messages.TherapieplanComposite_tblclmnDosage_text);
		return ret;
	}
	
	public static TableViewerColumn createBeginColumn(TableViewer viewer,
		TableColumnLayout layout, int columnIndex){
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn tblclmnEnacted = ret.getColumn();
		layout.setColumnData(tblclmnEnacted, new ColumnPixelData(60, true, true));
		tblclmnEnacted.setImage(Images.resize(Images.IMG_NEXT_WO_SHADOW.getImage(),
			ImageSize._12x12_TableColumnIconSize));
		tblclmnEnacted
			.addSelectionListener(getSelectionAdapter(viewer, tblclmnEnacted, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			
			@Override
			public String getText(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				return pres.getBeginDate();
			}
		});
		return ret;
	}
	
	public static TableViewerColumn createIntakeCommentColumn(TableViewer viewer,
		TableColumnLayout layout, int columnIndex){
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.NONE);
		TableColumn tblclmnComment = ret.getColumn();
		layout.setColumnData(tblclmnComment,
			new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true));
		tblclmnComment.setText(Messages.TherapieplanComposite_tblclmnComment_text);
		tblclmnComment
			.addSelectionListener(getSelectionAdapter(viewer, tblclmnComment, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			
			@Override
			public String getText(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				return pres.getBemerkung();
			}
		});
		return ret;
	}
	
	public static TableViewerColumn createStopColumn(TableViewer viewer, TableColumnLayout layout,
		int columnIndex){
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.CENTER);
		TableColumn tblclmnStop = ret.getColumn();
		ColumnPixelData stopColumnPixelData = new ColumnPixelData(60, true, true);
		layout.setColumnData(tblclmnStop, stopColumnPixelData);
		tblclmnStop.setImage(Images.resize(Images.IMG_ARROWSTOP_WO_SHADOW.getImage(),
			ImageSize._12x12_TableColumnIconSize));
		tblclmnStop.addSelectionListener(getSelectionAdapter(viewer, tblclmnStop, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				if (pres.getEntryType() != EntryType.RECIPE
					&& pres.getEntryType() != EntryType.SELF_DISPENSED) {
					return pres.getEndDate();
				}
				return "";
			}
		});
		return ret;
	}
	
	public static TableViewerColumn createStopReasonColumn(TableViewer viewer,
		TableColumnLayout layout,
		int columnIndex){
		TableViewerColumn ret = new TableViewerColumn(viewer, SWT.LEFT);
		TableColumn tblclmnReason = ret.getColumn();
		ColumnWeightData reasonColumnWeightData =
			new ColumnWeightData(1, ColumnWeightData.MINIMUM_WIDTH, true);
		layout.setColumnData(tblclmnReason, reasonColumnWeightData);
		tblclmnReason.setText(Messages.MedicationComposite_stopReason);
		tblclmnReason.addSelectionListener(getSelectionAdapter(viewer, tblclmnReason, columnIndex));
		ret.setLabelProvider(new MedicationCellLabelProvider() {
			@Override
			public String getText(Object element){
				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
				if (pres.getEntryType() != EntryType.RECIPE
					&& pres.getEntryType() != EntryType.SELF_DISPENSED) {
					String stopReason = pres.getStopReason();
					if (stopReason != null && !stopReason.isEmpty()) {
						return stopReason;
					}
				}
				return "";
			}
		});
		return ret;
	}
	
	//	public static TableViewerColumn createSuppliedUntilColumn(TableViewer viewer, TableColumnLayout layout, int columnIndex) {
	// supplied until
	//		TableViewerColumn tableViewerColumnSuppliedUntil =
	//			new TableViewerColumn(medicationTableViewer, SWT.CENTER);
	//		TableColumn tblclmnSufficient = tableViewerColumnSuppliedUntil.getColumn();
	//		tcl_compositeMedicationTable.setColumnData(tblclmnSufficient,
	//			new ColumnPixelData(45, true, true));
	//		tblclmnSufficient.setText(Messages.TherapieplanComposite_tblclmnSupplied_text);
	//		tblclmnSufficient.addSelectionListener(getSelectionAdapter(tblclmnSufficient, columnIndex));
	//		tableViewerColumnSuppliedUntil.setLabelProvider(new MedicationCellLabelProvider() {
	//			
	//			@Override
	//			public String getText(Object element){
	//				// SLLOW
	//				MedicationTableViewerItem pres = (MedicationTableViewerItem) element;
	//				if (!pres.isFixedMediation() || pres.isReserveMedication())
	//					return "";
	//					
	//				TimeTool tt = pres.getSuppliedUntilDate();
	//				if (tt != null && tt.isAfterOrEqual(new TimeTool())) {
	//					return "OK";
	//				}
	//				
	//				return "?";
	//			}
	//		});
	//	}
	
	public static void addContextMenu(TableViewer viewer, MedicationComposite medicationComposite){
		// register context menu for table viewer
		MenuManager menuManager = new MenuManager();
		menuManager.add(new MovePrescriptionPositionInTableUpAction(viewer, medicationComposite));
		menuManager.add(new MovePrescriptionPositionInTableDownAction(viewer, medicationComposite));
		menuManager.add(new Separator());
		menuManager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
		menuManager.add(new Separator());
		menuManager.add(new Action() {
			{
				setImageDescriptor(Images.IMG_BOOKMARK_PENCIL.getImageDescriptor());
				setText(Messages.FixMediDisplay_AddDefaultSignature);
			}
			
			@Override
			public void run(){
				StructuredSelection ss = (StructuredSelection) viewer.getSelection();
				MedicationTableViewerItem viewerItem =
					(MedicationTableViewerItem) ss.getFirstElement();
				if (viewerItem != null) {
					Prescription pr = viewerItem.getPrescription();
					if (pr != null) {
						ArticleDefaultSignatureTitleAreaDialog adtad =
							new ArticleDefaultSignatureTitleAreaDialog(UiDesk.getTopShell(), pr);
						adtad.open();
					}
				}
			}
		});
		Menu menu = menuManager.createContextMenu(viewer.getTable());
		
		viewer.getTable().setMenu(menu);
		IWorkbenchPartSite site = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
			.getActivePage().getActivePart().getSite();
		if (site != null) {
			site.registerContextMenu("ch.elexis.core.ui.medication.tables", menuManager, viewer);
		}
	}
	
	public static void addKeyMoveUpDown(TableViewer viewer,
		MedicationComposite medicationComposite){
		viewer.getTable().addKeyListener(new UpDownKeyAdapter(viewer, medicationComposite));
	}
	
	private static class UpDownKeyAdapter extends KeyAdapter {
		private MovePrescriptionPositionInTableUpAction upAction;
		private MovePrescriptionPositionInTableDownAction downAction;
		
		public UpDownKeyAdapter(TableViewer viewer, MedicationComposite medicationComposite){
			upAction = new MovePrescriptionPositionInTableUpAction(viewer, medicationComposite);
			downAction = new MovePrescriptionPositionInTableDownAction(viewer, medicationComposite);
		}
		
		@Override
		public void keyPressed(KeyEvent e){
			if ((e.stateMask == SWT.COMMAND || e.stateMask == SWT.CTRL)
				&& (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN)) {
				if (e.keyCode == SWT.ARROW_UP) {
					upAction.run();
				} else if (e.keyCode == SWT.ARROW_DOWN) {
					downAction.run();
				}
				e.doit = false;
			} else {
				super.keyPressed(e);
			}
		}
		
		@Override
		public void keyReleased(KeyEvent e){
			if (e.stateMask == SWT.COMMAND
				&& (e.keyCode == SWT.ARROW_UP || e.keyCode == SWT.ARROW_DOWN))
				return;
			super.keyReleased(e);
		}
	}
	
	private static SelectionAdapter getSelectionAdapter(final TableViewer viewer,
		final TableColumn column, final int index){
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
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
