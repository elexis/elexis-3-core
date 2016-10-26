package ch.elexis.core.findings.ui.composites;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
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
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.NatTableFactory;
import ch.elexis.core.ui.util.NatTableWrapper;

public class CodingComposite extends Composite {
	
	private NatTableWrapper natTableWrapper;
	private ToolBarManager toolbarManager;
	
	private Label title;
	
	private EventList<ICoding> dataList = new BasicEventList<>();
	private CodingAdapter adapter;
	
	public static interface CodingAdapter {
		public List<ICoding> getCoding();
		
		public void setCoding(List<ICoding> coding);
	}
	
	public CodingComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		title = new Label(this, SWT.NONE);
		title.setText("Codes:");
		title.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, false, false));
		title.setVisible(false);
		
		toolbarManager = new ToolBarManager();
		toolbarManager.add(new AddCodingAction());
		toolbarManager.add(new RemoveCodingAction());
		ToolBar toolbar = toolbarManager.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		toolbar.setBackground(parent.getBackground());
		
		natTableWrapper = NatTableFactory.createSingleColumnTable(this,
			new GlazedListsDataProvider<ICoding>(dataList, new IColumnAccessor<ICoding>() {

				@Override
				public int getColumnCount(){
					return 1;
				}

				@Override
				public Object getDataValue(ICoding coding, int arg1){
					StringBuilder text = new StringBuilder();
					
					text.append("<strong>");
					text.append("[").append(coding.getCode()).append("]");
					text.append("</strong>");
					text.append(" ").append(coding.getDisplay());
					
					return text.toString();
				}

				@Override
				public void setDataValue(ICoding coding, int arg1, Object arg2){
					// setting data values is not enabled here.
				}
			}), null);
		GridData tableGd = new GridData(GridData.FILL_BOTH);
		tableGd.horizontalSpan = 2;
		natTableWrapper.getNatTable().setLayoutData(tableGd);
	}
	
	private class AddCodingAction extends Action {
		
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
			List<ICoding> existingCoding = adapter.getCoding();
			existingCoding.add(new ICoding() {
				
				@Override
				public String getSystem(){
					return "system";
				}
				
				@Override
				public String getDisplay(){
					return "der display text";
				}
				
				@Override
				public String getCode(){
					return "code";
				}
			});
		}
	}
	
	private class RemoveCodingAction extends Action {
		
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
				
			}
		}
	}
	
	public void showTitle(boolean value){
		title.setVisible(value);
		layout();
	}
	
	public void setInput(CodingAdapter adapter){
		this.adapter = adapter;
		dataList.clear();
		dataList.addAll(adapter.getCoding());
		natTableWrapper.getNatTable().refresh();
	}
}
