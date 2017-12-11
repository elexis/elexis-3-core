package ch.elexis.core.findings.templates.ui.dlg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

import org.apache.commons.lang3.ObjectUtils;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.emf.common.util.ECollections;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.IElementComparer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSourceAdapter;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DropTargetAdapter;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.TableItem;

import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.ui.util.FindingsTemplateUtil;

public class FindingsSelectionDialog extends TitleAreaDialog {
	private final FindingsTemplates model;
	private List<FindingsTemplate> selections;
	private TableViewer viewer;
	private boolean multiSelection;
	private FindingsTemplate current;
	private boolean dndReordering;
	
	public FindingsSelectionDialog(Shell parentShell, FindingsTemplates model,
		List<FindingsTemplate> selections, boolean multiSelection, FindingsTemplate current,
		boolean dndReordering){
		super(parentShell);
		setShellStyle(SWT.RESIZE);
		this.model = model;
		this.current = current;
		this.selections = selections;
		this.multiSelection = multiSelection;
		this.dndReordering = dndReordering;
	}
	
	/**
	 * Create contents of the dialog.
	 * 
	 * @param parent
	 */
	@Override
	protected Control createDialogArea(Composite parent){
		StringBuilder message = new StringBuilder();
		if (multiSelection) {
			message.append("Wählen Sie Vorlagen aus");
		} else {
			message.append("Wählen Sie eine Vorlage aus");
		}
		if (dndReordering) {
			message.append("\nDie Reihenfolge des Auswahls kann per Drag & Drop geändert werden.");
		}
		setMessage(message.toString());
		setTitle("Befund Vorlage");
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		EList<FindingsTemplate> eTemplates = ECollections.newBasicEList();
		
		viewer = new TableViewer(composite,
			SWT.FULL_SELECTION | SWT.BORDER | (multiSelection ? SWT.MULTI : SWT.SINGLE));
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				if (element instanceof FindingsTemplate) {
					return ((FindingsTemplate) element).getTitle();
				}
				return "";
			}
			
			@Override
			public Image getImage(Object object){
				Image img = FindingsTemplateUtil.getImage(object);
				return img != null ? img : super.getImage(object);
			}
		});
		
		viewer.setComparer(new IElementComparer() {
			
			@Override
			public int hashCode(Object element){
				return element.hashCode();
			}
			
			@Override
			public boolean equals(Object a, Object b){
				if (a.equals(b)) {
					return true;
				}
				else if (a instanceof FindingsTemplate && b instanceof FindingsTemplate) {
					FindingsTemplate findingsTemplate1 = (FindingsTemplate) a;
					FindingsTemplate findingsTemplate2 = (FindingsTemplate) b;
					return StringUtils.equals(findingsTemplate1.getTitle(),
						findingsTemplate2.getTitle());
				}
				return false;
			}
		});
		if (dndReordering) {
			final Transfer[] dragTransferTypes = new Transfer[] {
				TextTransfer.getInstance()
			};
			viewer.addDragSupport(DND.DROP_MOVE, dragTransferTypes, new DragSourceAdapter() {
				@Override
				public void dragSetData(DragSourceEvent event){
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					if (selection != null && !selection.isEmpty()) {
						Object item = selection.getFirstElement();
						if (item instanceof FindingsTemplate) {
							FindingsTemplate findingsTemplate =
								(FindingsTemplate) selection.getFirstElement();
							event.data = findingsTemplate.getTitle();
						}
					}
				}
			});
			viewer.addDropSupport(DND.DROP_MOVE, dragTransferTypes, new DropTargetAdapter() {
				
				@Override
				public void dragEnter(DropTargetEvent event){
					event.detail = DND.DROP_MOVE;
				}
				
				@Override
				public void drop(DropTargetEvent event){
					IStructuredSelection selection = (IStructuredSelection) viewer.getSelection();
					TableItem tableItem = (TableItem) event.item;
					if (tableItem != null) {
						int idx = viewer.getTable().indexOf(tableItem);
						if (selection != null && !selection.isEmpty()) {
							FindingsTemplate findingsTemplate =
								(FindingsTemplate) selection.getFirstElement();
							if (findingsTemplate != null) {
								if (eTemplates.remove(findingsTemplate)) {
									eTemplates.add(idx, findingsTemplate);
									viewer.refresh();
								}
							}
						}
					}
				}
			});
		}
		
		
		List<FindingsTemplate> templatesTemp = null;
		if (current != null) {
			if (current.getInputData() instanceof InputDataGroupComponent)
			{
				// remove component selections
				templatesTemp = model.getFindingsTemplates().stream()
					.filter(item -> !(item.getInputData() instanceof InputDataGroup
						|| item.getInputData() instanceof InputDataGroupComponent))
					.collect(Collectors.toList());
				
				for (FindingsTemplate findingsTemplate : ((InputDataGroupComponent) current
					.getInputData()).getFindingsTemplates()) {
					if (!templatesTemp.contains(findingsTemplate)) {
						templatesTemp.add(findingsTemplate);
					}
				}
			}
			else {
				// remove self selection
				templatesTemp = model.getFindingsTemplates().stream()
					.filter(item -> !item.equals(current))
					.collect(Collectors.toList());
			}
			
		}
		else {
			templatesTemp = model.getFindingsTemplates();
		}
		
		if (templatesTemp != null) {
			
			eTemplates.addAll(templatesTemp);
			// sort
			ECollections.sort(eTemplates, new Comparator<FindingsTemplate>() {
				
				@Override
				public int compare(FindingsTemplate o1, FindingsTemplate o2){
					if (o1 == null || o2 == null) {
						return o1 != null ? 1 : -1;
					}
					else if (o1.getInputData() instanceof InputDataGroup) {
						return -1;
					} else if (o2.getInputData() instanceof InputDataGroup) {
						return 1;
					}
					else if (o1.getInputData() instanceof InputDataGroupComponent) {
						return -1;
					}
					else if (o2.getInputData() instanceof InputDataGroupComponent) {
						return 1;
					}
					return ObjectUtils.compare(o1.getTitle(), o2.getTitle());
				}
			});
		}
		viewer.setInput(eTemplates);
		viewer.setSelection(new StructuredSelection(selections));
		
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(DoubleClickEvent event){
				StructuredSelection selection = (StructuredSelection) viewer.getSelection();
				if (!selection.isEmpty()) {
					Object selectedObj = selection.getFirstElement();
					if (selectedObj instanceof FindingsTemplate) {
						selections = new ArrayList<>();
						selections.add((FindingsTemplate) selectedObj);
						close();
					}
				}
			}
		});
		
		return composite;


	}
	
	/**
	 * Create contents of the button bar.
	 * 
	 * @param parent
	 */
	@Override
	protected void createButtonsForButtonBar(Composite parent){
		createButton(parent, IDialogConstants.OK_ID, IDialogConstants.OK_LABEL, true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}
	
	@Override
	protected void buttonPressed(int buttonId){
		super.buttonPressed(buttonId);
	}
	
	@Override
	protected void okPressed(){
		selections = new ArrayList<>();
		StructuredSelection structuredSelection = (StructuredSelection) viewer.getSelection();
		for (Object o : structuredSelection.toArray()) {
			if (o instanceof FindingsTemplate) {
				selections.add((FindingsTemplate) o);
			}
		}
		
		super.okPressed();
	}
	
	public List<FindingsTemplate> getSelection(){
		if (selections == null) {
			selections = Collections.emptyList();
		}
		return selections;
	}
	
	public FindingsTemplate getSingleSelection(){
		List<FindingsTemplate> findingsTemplates = getSelection();
		if (findingsTemplates.size() > 0) {
			return findingsTemplates.get(0);
		}
		return null;
	}
}
