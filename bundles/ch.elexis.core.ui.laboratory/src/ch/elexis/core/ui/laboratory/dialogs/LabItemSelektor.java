package ch.elexis.core.ui.laboratory.dialogs;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite.Group;
import ch.elexis.core.ui.laboratory.controls.LabItemTreeSelectionComposite.GroupItem;
import ch.elexis.core.ui.laboratory.dialogs.LabItemLabelProvider.ItemLabelFields;
import ch.elexis.core.ui.laboratory.preferences.Messages;
import ch.elexis.data.LabItem;

public class LabItemSelektor extends TitleAreaDialog {
	
	private LabItemTreeSelectionComposite treeSelectionComposite;
	private ILabelProvider labelProvider;
	
	private List<LabItem> selection;
	
	public LabItemSelektor(Shell parentShell){
		super(parentShell);
		selection = Collections.emptyList();
		
		labelProvider = new LabelProvider() {
			private ILabelProvider itemLabelProvider =
				new LabItemLabelProvider(Arrays.asList(ItemLabelFields.KUERZEL,
					ItemLabelFields.NAME, ItemLabelFields.REFERENCES, ItemLabelFields.UNIT), false);
					
			@Override
			public String getText(Object element){
				if (element instanceof Group) {
					return ((Group) element).toString();
				} else if (element instanceof GroupItem) {
					return itemLabelProvider.getText(((GroupItem) element).getLabItem());
				}
				return "?";
			}
		};
	}
	
	@Override
	protected Control createContents(Composite parent){
		Control contents = super.createContents(parent);
		
		setMessage(Messages.LabGroupPrefs_pleaseSelectLabItems);
		setTitle(Messages.LabGroupPrefs_selectLabItems);
		
		return contents;
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite composite = (Composite) super.createDialogArea(parent);
		composite.setLayout(new GridLayout(1, false));
		
		treeSelectionComposite =
			new LabItemTreeSelectionComposite(composite, labelProvider, false, SWT.NONE);
		treeSelectionComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		
		return composite;
	}
	
	protected void okPressed(){
		List<GroupItem> groupItems = treeSelectionComposite.getSelectedItems();
		selection = new ArrayList<LabItem>();
		for (GroupItem groupItem : groupItems) {
			selection.add(groupItem.getLabItem());
		}
		super.okPressed();
	}
	
	public List<LabItem> getSelection(){
		return selection;
	}
}
