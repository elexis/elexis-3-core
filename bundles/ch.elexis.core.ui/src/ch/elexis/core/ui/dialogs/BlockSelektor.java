package ch.elexis.core.ui.dialogs;

import java.util.Comparator;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.dialogs.FilteredItemsSelectionDialog;
import org.eclipse.ui.internal.WorkbenchMessages;

import ch.elexis.data.Leistungsblock;
import ch.elexis.data.Query;

public class BlockSelektor extends FilteredItemsSelectionDialog {
	
	private boolean ignoreErrors;
	
	public BlockSelektor(Shell shell){
		super(shell);
		setTitle("Block Selektion");
		
		setListLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element == null) {
					return "";
				}
				return ((Leistungsblock) element).getLabel();
			}
		});
	}
	
	public BlockSelektor(Shell shell, Object data){
		this(shell);
		if (data instanceof String && data.equals("ignoreErrors")) {
			ignoreErrors = true;
		}
		
	}
	
	@Override
	protected void updateButtonsEnableState(IStatus status){
		if (!ignoreErrors) {
			super.updateButtonsEnableState(status);
		}
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		String oldListLabel = WorkbenchMessages.FilteredItemsSelectionDialog_listLabel;
		
		setMessage("");
		WorkbenchMessages.FilteredItemsSelectionDialog_listLabel = ""; //$NON-NLS-1$
		Control ret = super.createDialogArea(parent);
		
		WorkbenchMessages.FilteredItemsSelectionDialog_listLabel = oldListLabel;
		return ret;
	}
	
	@Override
	protected IDialogSettings getDialogSettings(){
		return new DialogSettings("blockselector"); //$NON-NLS-1$
	}
	
	@Override
	protected IStatus validateItem(Object item){
		return Status.OK_STATUS;
	}
	
	@Override
	protected void okPressed(){
		if (ignoreErrors) {
			updateStatus(Status.OK_STATUS);
		}
		super.okPressed();
	}
	
	@Override
	protected ItemsFilter createFilter(){
		return new ItemsFilter() {
			@Override
			public boolean isConsistentItem(Object item){
				return true;
			}
			
			@Override
			public boolean matchItem(Object item){
				Leistungsblock block = (Leistungsblock) item;
				
				return matches(block.getLabel());
			}
		};
	}
	
	@Override
	protected Comparator<Leistungsblock> getItemsComparator(){
		return new Comparator<Leistungsblock>() {
			
			public int compare(Leistungsblock o1, Leistungsblock o2){
				return o1.getLabel().compareTo(o2.getLabel());
			}
		};
	}
	
	@Override
	protected void fillContentProvider(AbstractContentProvider contentProvider,
		ItemsFilter itemsFilter, IProgressMonitor progressMonitor) throws CoreException{
		
		List<Leistungsblock> allBlocks = new Query<Leistungsblock>(Leistungsblock.class).execute();
		
		for (Leistungsblock block : allBlocks) {
			if (progressMonitor.isCanceled()) {
				return;
			}
			contentProvider.add(block, itemsFilter);
		}
	}
	
	@Override
	public String getElementName(Object item){
		Leistungsblock block = (Leistungsblock) item;
		return block.getLabel();
	}
	
	@Override
	protected Control createExtendedContentArea(Composite parent){
		// TODO Auto-generated method stub
		return null;
	}
}
