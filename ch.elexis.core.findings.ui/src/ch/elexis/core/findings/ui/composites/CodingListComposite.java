package ch.elexis.core.findings.ui.composites;

import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ToolBar;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.ui.services.CodingServiceComponent;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.NatTableFactory;
import ch.elexis.core.ui.util.NatTableWrapper;

public class CodingListComposite extends Composite {
	
	private NatTableWrapper natTableWrapper;
	private ToolBarManager toolbarManager;
	
	private CodingSelectionComposite selectionComposite;
	
	private EventList<ICoding> dataList = new BasicEventList<>();
	private CodingAdapter adapter;
	
	public static interface CodingAdapter {
		public List<ICoding> getCoding();
		
		public void setCoding(List<ICoding> coding);
	}
	
	public CodingListComposite(Composite parent, int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		
		selectionComposite = new CodingSelectionComposite(this, SWT.NONE);
		selectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		selectionComposite.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				ISelection selection = event.getSelection();
				if (selection instanceof StructuredSelection && !selection.isEmpty()) {
					ICoding iCoding = (ICoding) ((StructuredSelection) selection).getFirstElement();
					List<ICoding> existingCoding = adapter.getCoding();
					existingCoding.add(iCoding);
					adapter.setCoding(existingCoding);
					dataList.add(iCoding);
					natTableWrapper.getNatTable().refresh();
				}
			}
		});
		selectionComposite.setCodeSystem(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
		
		toolbarManager = new ToolBarManager();
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
					String display = coding.getDisplay();
					if (display == null || display.isEmpty()) {
						List<ICoding> codes = CodingServiceComponent.getService()
							.getAvailableCodes(coding.getSystem());
						if (codes != null && !codes.isEmpty()) {
							ICoding code = searchForCode(coding.getCode(), codes);
							if (code != null) {
								display = code.getDisplay();
							} else {
								display = "?";
							}
						}
					}
					text.append("<strong>");
					text.append("[").append(coding.getCode()).append("]");
					text.append("</strong>");
					text.append(" ").append(display);
					
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
	
	private ICoding searchForCode(String code, List<ICoding> codes){
		for (ICoding iCoding : codes) {
			if (iCoding.getCode().equals(code)) {
				return iCoding;
			}
		}
		return null;
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
				@SuppressWarnings("unchecked")
				List<ICoding> list = ((StructuredSelection) selection).toList();
				list.stream().forEach(c -> {
					List<ICoding> existing = adapter.getCoding();
					existing.remove(c);
					adapter.setCoding(existing);
					dataList.remove(c);
					natTableWrapper.getNatTable().refresh();
				});
			}
		}
	}
	
	public void setInput(CodingAdapter adapter){
		this.adapter = adapter;
		dataList.clear();
		dataList.addAll(adapter.getCoding());
		natTableWrapper.getNatTable().refresh();
	}
}
