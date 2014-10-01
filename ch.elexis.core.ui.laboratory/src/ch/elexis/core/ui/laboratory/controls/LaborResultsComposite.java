package ch.elexis.core.ui.laboratory.controls;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.viewers.ColumnLabelProvider;
import org.eclipse.jface.viewers.ColumnViewerToolTipSupport;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.FocusCellOwnerDrawHighlighter;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.jface.viewers.TreeViewerColumn;
import org.eclipse.jface.viewers.TreeViewerFocusCellManager;
import org.eclipse.jface.viewers.ViewerCell;
import org.eclipse.jface.window.ToolTip;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.DisplayLabDokumenteDialog;
import ch.elexis.core.ui.dialogs.DisplayTextDialog;
import ch.elexis.core.ui.laboratory.actions.LaborResultDeleteAction;
import ch.elexis.core.ui.laboratory.actions.LaborResultEditDetailAction;
import ch.elexis.core.ui.laboratory.actions.TogglePathologicAction;
import ch.elexis.core.ui.util.Log;
import ch.elexis.data.LabItem;
import ch.elexis.data.LabItem.typ;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.elexis.data.Person;
import ch.rgw.tools.TimeTool;

public class LaborResultsComposite extends Composite {
	private static Log log = Log.get("LaborView"); //$NON-NLS-1$
	
	private final FormToolkit tk = UiDesk.getToolkit();
	private Form form;
	
	private Patient actPatient;
	
	private TreeViewer viewer;
	private TreeViewerFocusCellManager focusCell;
	
	private final static String COLUMN_DATE_KEY = "labresult.date"; //$NON-NLS-1$
	private List<TreeViewerColumn> resultColumns = new ArrayList<TreeViewerColumn>();
	
	private LaborResultsContentProvider contentProvider = new LaborResultsContentProvider();
	
	private int columnOffset = 0;
	private static final int COLUMNS_PER_PAGE = 7;
	
	public LaborResultsComposite(Composite parent, int style){
		super(parent, style);
		
		createContent();
	}
	
	public int getColumnOffset(){
		return columnOffset;
	}
	
	public void setColumnOffset(int newOffset){
		List<TimeTool> dates = contentProvider.getDates();
		if (dates.size() <= COLUMNS_PER_PAGE) {
			columnOffset = 0;
		} else {
			if ((newOffset + COLUMNS_PER_PAGE <= dates.size()) && (newOffset >= 0)) {
				columnOffset = newOffset;
			}
		}
	}
	
	private void setInitialColumnOffset(){
		List<TimeTool> dates = contentProvider.getDates();
		int offset = dates.size() - COLUMNS_PER_PAGE;
		if (offset > 0) {
			setColumnOffset(offset);
		} else {
			setColumnOffset(0);
		}
	}
	
	private void createContent(){
		setLayout(new GridLayout());
		form = tk.createForm(this);
		form.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		Composite body = form.getBody();
		body.setLayout(new GridLayout());
		
		viewer = new TreeViewer(body, SWT.FULL_SELECTION | SWT.LEFT | SWT.V_SCROLL | SWT.H_SCROLL);
		viewer.getTree().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewer.getTree().setHeaderVisible(true);
		viewer.getTree().setLinesVisible(true);
		
		viewer.setContentProvider(contentProvider);
		
		ColumnViewerToolTipSupport.enableFor(viewer, ToolTip.NO_RECREATE);
		
		focusCell =
			new TreeViewerFocusCellManager(viewer, new FocusCellOwnerDrawHighlighter(viewer));
		viewer.addDoubleClickListener(new DisplayDoubleClickListener(this));
		
		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				List<LabResult> results = getSelectedResults();
				if (results != null) {
					mgr.add(new TogglePathologicAction(results, viewer));
				}
				if (results != null) {
					mgr.add(new LaborResultEditDetailAction(results, viewer));
				}
				if (results != null) {
					mgr.add(new LaborResultDeleteAction(results, viewer));
				}
			}
		});
		viewer.getControl().setMenu(mgr.createContextMenu(viewer.getControl()));
		
		TreeViewerColumn column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(200);
		column.getColumn().setText(Messages.LaborResultsComposite_columnParameter);
		column.setLabelProvider(new ColumnLabelProvider() {
			private StringBuilder sb = new StringBuilder();
			
			@Override
			public String getText(Object element){
				if (element instanceof String) {
					String groupName = (String) element;
					if ((groupName.length() > 2) && groupName.charAt(1) == ' ') {
						groupName = groupName.substring(2);
					}
					return groupName;
				} else if (element instanceof LaborItemResults) {
					sb.setLength(0);
					LabItem item = ((LaborItemResults) element).getFirstResult().getItem();
					sb.append(item.getKuerzel()).append(" - ").append(item.getName()).append(" [") //$NON-NLS-1$ //$NON-NLS-2$
						.append(item.getEinheit()).append("]"); //$NON-NLS-1$
					return sb.toString();
				}
				return ""; //$NON-NLS-1$
			}
		});
		
		column = new TreeViewerColumn(viewer, SWT.NONE);
		column.getColumn().setWidth(100);
		column.getColumn().setText(Messages.LaborResultsComposite_columnReference);
		column.setLabelProvider(new ColumnLabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof LaborItemResults) {
					if (actPatient.getGeschlecht().equals(Person.MALE)) {
						return ((LaborItemResults) element).getFirstResult().getItem().getRefM();
					} else {
						return ((LaborItemResults) element).getFirstResult().getItem().getRefW();
					}
				}
				return ""; //$NON-NLS-1$
			}
		});
		
		for (int i = 0; i < COLUMNS_PER_PAGE; i++) {
			column = new TreeViewerColumn(viewer, SWT.NONE);
			column.getColumn().setWidth(75);
			column.getColumn().setText(""); //$NON-NLS-1$
			column.setLabelProvider(new LaborResultsLabelProvider(column));
			resultColumns.add(column);
		}
	}
	
	public List<LabResult> getSelectedResults(){
		ViewerCell cell = focusCell.getFocusCell();
		return getSelectedResults(cell);
	}
	
	private List<LabResult> getSelectedResults(ViewerCell cell){
		if (cell != null && cell.getColumnIndex() > 1) {
			TreeViewerColumn column = resultColumns.get(cell.getColumnIndex() - 2);
			TimeTool time = (TimeTool) column.getColumn().getData(COLUMN_DATE_KEY);
			if ((time != null) && (cell.getElement() instanceof LaborItemResults)) {
				LaborItemResults results = (LaborItemResults) cell.getElement();
				return results.getResult(time.toString(TimeTool.DATE_COMPACT));
			}
		}
		return null;
	}
	
	private static class LaborResultsLabelProvider extends ColumnLabelProvider {
		
		private TreeViewerColumn column;
		
		public LaborResultsLabelProvider(TreeViewerColumn column){
			this.column = column;
		}
		
		@Override
		public String getText(Object element){
			if (element instanceof LaborItemResults) {
				TimeTool date = (TimeTool) column.getColumn().getData(COLUMN_DATE_KEY);
				if (date != null) {
					List<LabResult> results =
						((LaborItemResults) element)
							.getResult(date.toString(TimeTool.DATE_COMPACT));
					if (results != null) {
						StringBuilder sb = new StringBuilder();
						for (LabResult labResult : results) {
							if (sb.length() == 0) {
								sb.append(getResultString(labResult));
							} else {
								sb.append(" / "); //$NON-NLS-1$
								sb.append(getResultString(labResult));
							}
						}
						return sb.toString();
					}
				}
			}
			return ""; //$NON-NLS-1$
		}
		
		private String getResultString(LabResult labResult){
			if (labResult.getItem().getTyp() == typ.DOCUMENT) {
				return Messages.LaborResultsComposite_Open;
			} else if (labResult.getItem().getTyp() == typ.TEXT) {
				return getNonEmptyResultString(labResult);
			} else {
				int digits = labResult.getItem().getDigits();
				String result = getNonEmptyResultString(labResult);
				if (digits == 0) {
					return result;
				} else {
					try {
						Float resultNumeric = Float.parseFloat(result);
						return String.format("%." + digits + "f", resultNumeric); //$NON-NLS-1$ //$NON-NLS-2$
					} catch (NumberFormatException e) {
						return result;
					}
				}
			}
		}
		
		private String getNonEmptyResultString(LabResult labResult){
			String result = labResult.getResult();
			if (result != null && result.isEmpty()) {
				result = "?"; //$NON-NLS-1$
			}
			if (labResult.getItem().getTyp() == typ.TEXT) {
				if (labResult.isLongText()) {
					result = labResult.getComment();
					if (result.length() > 20) {
						result = result.substring(0, 20);
					}
				}
			}
			return result;
		}
		
		private String getUnitAndReferenceString(LabResult labResult){
			StringBuilder sb = new StringBuilder();
			sb.append("[").append(labResult.getUnit()).append("]");
			if (labResult.getPatient().getGeschlecht().equals(Patient.MALE)) {
				sb.append("[").append(labResult.getRefMale()).append("]");
			} else {
				sb.append("[").append(labResult.getRefFemale()).append("]");
			}
			return sb.toString();
		}
		
		private String getCommentString(LabResult labResult){
			StringBuilder sb = new StringBuilder();
			String comment = labResult.getComment();
			if (!comment.isEmpty()) {
				sb.append("\n").append(comment);
			}
			return sb.toString();
		}
		
		@Override
		public String getToolTipText(Object element){
			if (element instanceof LaborItemResults) {
				TimeTool date = (TimeTool) column.getColumn().getData(COLUMN_DATE_KEY);
				if (date != null) {
					List<LabResult> results =
						((LaborItemResults) element)
							.getResult(date.toString(TimeTool.DATE_COMPACT));
					if (results != null) {
						StringBuilder sb = new StringBuilder();
						for (LabResult labResult : results) {
							TimeTool time = labResult.getObservationTime();
							if (time == null) {
								time = labResult.getDateTime();
							}
							if (sb.length() == 0) {
								sb.append(time.toString(TimeTool.TIME_FULL));
								sb.append(" - "); //$NON-NLS-1$
								sb.append(getResultString(labResult));
								sb.append(getUnitAndReferenceString(labResult));
								sb.append(getCommentString(labResult));
							} else {
								sb.append(",\n"); //$NON-NLS-1$
								sb.append(time.toString(TimeTool.TIME_FULL));
								sb.append(" - "); //$NON-NLS-1$
								sb.append(getResultString(labResult));
								sb.append(getUnitAndReferenceString(labResult));
								sb.append(getCommentString(labResult));
							}
						}
						return sb.toString();
					}
				}
			}
			return null;
		}
		
		@Override
		public Color getForeground(Object element){
			if (element instanceof LaborItemResults) {
				TimeTool date = (TimeTool) column.getColumn().getData(COLUMN_DATE_KEY);
				if (date != null) {
					List<LabResult> results =
						((LaborItemResults) element)
							.getResult(date.toString(TimeTool.DATE_COMPACT));
					if (results != null) {
						boolean pathologic = false;
						for (LabResult labResult : results) {
							if (labResult.isFlag(LabResult.PATHOLOGIC)) {
								pathologic = true;
								break;
							}
						}
						if (pathologic) {
							return Display.getCurrent().getSystemColor(SWT.COLOR_RED);
						}
					}
				}
			}
			return Display.getCurrent().getSystemColor(SWT.COLOR_BLACK);
		}
	}
	
	static class LaborItemResults implements Comparable<LaborItemResults> {
		
		private HashMap<String, List<LabResult>> results;
		private String item;
		
		public LaborItemResults(String item, HashMap<String, List<LabResult>> results){
			this.results = results;
			this.item = item;
		}
		
		public String getItem(){
			return item;
		}
		
		public boolean isVisible(){
			return results.values().iterator().next().get(0).getItem().isVisible();
		}
		
		public LabResult getFirstResult(){
			return results.values().iterator().next().get(0);
		}
		
		public List<LabResult> getResult(String date){
			return results.get(date);
		}
		
		public List<String> getDays(){
			return new ArrayList<String>(results.keySet());
		}
		
		@Override
		public int compareTo(LaborItemResults o){
			return item.compareTo(o.getItem());
		}
	}
	
	private static class DisplayDoubleClickListener implements IDoubleClickListener {
		private static Font dialogFont = null;
		private LaborResultsComposite composite;
		
		public DisplayDoubleClickListener(LaborResultsComposite composite){
			this.composite = composite;
		}
		
		@Override
		public void doubleClick(DoubleClickEvent event){
			List<LabResult> results = composite.getSelectedResults();
			if (results != null) {
				for (LabResult labResult : results) {
					openDisplayDialog(labResult);
				}
			}
		}
		
		private void openDisplayDialog(LabResult labResult){
			LabItem labItem = labResult.getItem();
			if (labItem.getTyp().equals(LabItem.typ.TEXT) || (labResult.getComment().length() > 0)) {
				DisplayTextDialog dlg =
					new DisplayTextDialog(composite.getShell(),
						Messages.LaborResultsComposite_textResultTitle, labItem.getName(),
						labResult.getComment());
				// HL7 Befunde enthalten oft mit Leerzeichen formatierte Bemerkungen,
				// die nur mit nicht-proportionalen Fonts dargestellt werden k��nnen
				// Wir versuchen also, die Anzeige mit Courier New, ohne zu wissen ob die
				// auf Mac und Linux auch drauf sind.
				// Falls der Font nicht geladen werden kann, wird der System-Default Font
				// verwendet
				// Hier die Fonts, welche getestet worden sind:
				// Windows: Courier New (getestet=
				// Mac: nicht getestet
				// Linux: nicht getestet
				try {
					if (dialogFont == null) {
						dialogFont = new Font(null, "Courier New", 9, SWT.NORMAL); //$NON-NLS-1$
					}
				} catch (Exception ex) {
					// Do nothing -> Use System Default font
				} finally {
					dlg.setFont(dialogFont);
				}
				dlg.setWhitespaceNormalized(false);
				dlg.open();
			} else if (labItem.getTyp().equals(LabItem.typ.DOCUMENT)) {
				Patient patient = ElexisEventDispatcher.getSelectedPatient();
				if (patient != null) {
					new DisplayLabDokumenteDialog(composite.getShell(),
						Messages.LaborResultsComposite_Documents,
						Collections.singletonList(labResult)).open();//$NON-NLS-1$
				}
			}
		}
	}
	
	public String[] getPrintHeaders(){
		ArrayList<String> ret = new ArrayList<String>();
		
		TreeColumn[] columns = viewer.getTree().getColumns();
		for (TreeColumn treeColumn : columns) {
			ret.add(treeColumn.getText());
		}
		return ret.toArray(new String[ret.size()]);
	}
	
	public TreeItem[] getPrintRows(){
		ArrayList<TreeItem> ret = new ArrayList<TreeItem>();
		getAllItems(viewer.getTree(), ret);
		return ret.toArray(new TreeItem[ret.size()]);
	}
	
	private void getAllItems(Tree tree, List<TreeItem> allItems){
		for (TreeItem item : tree.getItems()) {
			getAllItems(item, allItems);
		}
	}
	
	private void getAllItems(TreeItem currentItem, List<TreeItem> allItems){
		TreeItem[] children = currentItem.getItems();
		for (int i = 0; i < children.length; i++) {
			allItems.add(children[i]);
			getAllItems(children[i], allItems);
		}
	}
	
	/**
	 * Reload all content and update the Viewer
	 */
	public void reload(){
		setRedraw(false);
		viewer.setInput(LabResult.getGrouped(actPatient));
		
		for (int i = 0; i < resultColumns.size(); i++) {
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, null);
			resultColumns.get(i).getColumn().setText(""); //$NON-NLS-1$
		}
		
		List<TimeTool> dates = contentProvider.getDates();
		for (int i = 0; i < dates.size() && i < resultColumns.size()
			&& ((i + columnOffset) < dates.size()); i++) {
			resultColumns.get(i).getColumn()
				.setText(dates.get(i + columnOffset).toString(TimeTool.DATE_GER));
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, dates.get(i + columnOffset));
		}
		
		viewer.expandAll();
		setRedraw(true);
	}
	
	/**
	 * Reload all content and update the Viewer with the data of the Patient.
	 */
	public void selectPatient(Patient patient){
		setRedraw(false);
		actPatient = patient;
		viewer.setInput(LabResult.getGrouped(actPatient));
		
		setInitialColumnOffset();
		
		for (int i = 0; i < resultColumns.size(); i++) {
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, null);
			resultColumns.get(i).getColumn().setText(""); //$NON-NLS-1$
		}
		
		List<TimeTool> dates = contentProvider.getDates();
		for (int i = 0; i < dates.size() && i < resultColumns.size(); i++) {
			resultColumns.get(i).getColumn()
				.setText(dates.get(i + columnOffset).toString(TimeTool.DATE_GER));
			resultColumns.get(i).getColumn().setData(COLUMN_DATE_KEY, dates.get(i + columnOffset));
		}
		
		viewer.expandAll();
		setRedraw(true);
	}
	
	public Patient getPatient(){
		return actPatient;
	}
	
	public void expandAll(){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.expandAll();
		}
	}
	
	public void collapseAll(){
		if (viewer != null && !viewer.getControl().isDisposed()) {
			viewer.collapseAll();
		}
	}
}
