package ch.elexis.core.findings.ui.composites;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.ToolBar;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.NatTableFactory;
import ch.elexis.core.ui.util.NatTableWrapper;

public class NotesComposite extends Composite {
	
	private NatTableWrapper natTableWrapper;
	private ToolBarManager toolbarManager;
	
	private Label title;
	
	private EventList<String> dataList = new BasicEventList<>();
	private NotesAdapter adapter;
	
	public static interface NotesAdapter {
		public List<String> getNotes();
		
		public void addNote(String note);
		
		public void removeNote(String note);
	}
	
	public NotesComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		title = new Label(this, SWT.NONE);
		title.setText("Notizen:");
		title.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		title.setVisible(false);
		
		toolbarManager = new ToolBarManager();
		toolbarManager.add(new AddNoteAction());
		toolbarManager.add(new RemoveNoteAction());
		ToolBar toolbar = toolbarManager.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		toolbar.setBackground(parent.getBackground());
		
		natTableWrapper = NatTableFactory.createSingleColumnTable(this,
			new GlazedListsDataProvider<String>(dataList, new IColumnAccessor<String>() {

				@Override
				public int getColumnCount(){
					return 1;
				}

				@Override
				public Object getDataValue(String note, int columnIndex){
					return note;
				}

				@Override
				public void setDataValue(String note, int arg1, Object arg2){
					// setting data values is not enabled here.
				}
			}), null);
		GridData tableGd = new GridData(GridData.FILL_BOTH);
		tableGd.horizontalSpan = 2;
		natTableWrapper.getNatTable().setLayoutData(tableGd);
	}
	
	public void setInput(NotesAdapter adapter){
		this.adapter = adapter;
		dataList.clear();
		dataList.addAll(adapter.getNotes());
		natTableWrapper.getNatTable().refresh();
	}
	
	private class AddNoteAction extends Action {
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_NEW.getImageDescriptor();
		}
		
		@Override
		public String getText(){
			return "hinzufügen";
		}
		
		@Override
		public void run(){
			InputDialog input = new InputDialog(getShell(), "Notiz", "Notiz erfassen", "", null);
			if (input.open() == InputDialog.OK) {
				if (input.getValue() != null && !input.getValue().isEmpty()) {
					adapter.addNote(input.getValue());
					dataList.add(input.getValue());
					natTableWrapper.getNatTable().refresh();
				}
			}
		}
	}
	
	private class RemoveNoteAction extends Action {
		
		@Override
		public ImageDescriptor getImageDescriptor(){
			return Images.IMG_DELETE.getImageDescriptor();
		}
		
		@Override
		public String getText(){
			return "entfernen";
		}
		
		@Override
		public void run(){
			ISelection selection = natTableWrapper.getSelection();
			if (selection instanceof StructuredSelection && !selection.isEmpty()) {
				@SuppressWarnings("unchecked")
				List<String> list = ((StructuredSelection) selection).toList();
				list.stream().forEach(note -> {
					adapter.removeNote(note);
					dataList.remove(note);
				});
				natTableWrapper.getNatTable().refresh();
			}
		}
	}
	
	public void showTitle(boolean value){
		title.setVisible(value);
		layout();
	}
}
