package ch.elexis.core.findings.ui.dialogs;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Table;

import ch.elexis.core.findings.ICoding;

public class VisibleCodingsSelectionDialog extends TitleAreaDialog {
	
	private List<ICoding> available;
	
	private CheckboxTableViewer viewer;
	
	private List<ICoding> selected;
	
	public VisibleCodingsSelectionDialog(Shell parentShell, List<ICoding> available){
		super(parentShell);
		this.available = available;
		
	}
	
	@Override
	public void create(){
		super.create();
		setTitle("Sichtbare Befunde Codes auswählen.");
	}
	
	@Override
	protected Control createDialogArea(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayoutData(new GridData(GridData.FILL_BOTH));
		ret.setLayout(new FillLayout());
		
		Table table = new Table(ret, SWT.MULTI | SWT.CHECK);
		viewer = new CheckboxTableViewer(table);
		viewer.setContentProvider(ArrayContentProvider.getInstance());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				ICoding iCoding = (ICoding) element;
				return iCoding != null ? iCoding.getDisplay() + " (" + iCoding.getCode() + ")" : "";
			}
		});
		
		viewer.setInput(available);
		for (ICoding iCoding : selected) {
			viewer.setChecked(iCoding, true);
		}
		
		return ret;
	}
	
	@Override
	protected void okPressed(){
		this.selected = Arrays.asList(viewer.getCheckedElements()).stream().map(c -> (ICoding) c)
			.collect(Collectors.toList());
		super.okPressed();
	}
	
	public void setSelected(List<ICoding> selected){
		this.selected = selected;
	}
	
	public List<ICoding> getSelected(){
		return selected;
	}
}
