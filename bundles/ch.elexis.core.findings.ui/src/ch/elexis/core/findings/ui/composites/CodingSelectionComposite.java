package ch.elexis.core.findings.ui.composites;

import java.util.Optional;
import java.util.stream.Collectors;

import org.eclipse.core.runtime.ListenerList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.fieldassist.ContentProposalAdapter;
import org.eclipse.jface.fieldassist.IContentProposal;
import org.eclipse.jface.fieldassist.IContentProposalListener;
import org.eclipse.jface.fieldassist.TextContentAdapter;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.ISelectionProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Text;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.ui.dialogs.CodingEditDialog;
import ch.elexis.core.findings.ui.services.CodingServiceComponent;
import ch.elexis.core.findings.util.model.TransientCoding;

public class CodingSelectionComposite extends Composite implements ISelectionProvider {
	
	private ListenerList selectionListeners = new ListenerList();
	
	private Text selectionTxt;
	
	private ComboViewer systemCombo;
	
	private CodingContentProposalProvider proposalProvider;
	private Optional<ICoding> selectedCode = Optional.empty();
	
	public CodingSelectionComposite(Composite parent, int style){
		super(parent, style);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		systemCombo = new ComboViewer(this);
		systemCombo.setContentProvider(new ArrayContentProvider());
		systemCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				return super.getText(element);
			}
		});
		systemCombo.setInput(CodingServiceComponent.getService().getAvailableCodeSystems().stream()
			.filter(system -> !system.equals(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem()))
			.collect(Collectors.toList()));
		systemCombo.addSelectionChangedListener(new ISelectionChangedListener() {
			@Override
			public void selectionChanged(SelectionChangedEvent event){
				if (proposalProvider != null) {
					ISelection selection = event.getSelection();
					if (selection instanceof StructuredSelection && !selection.isEmpty()) {
						proposalProvider
							.setSelectedSystem(Optional
								.of((String) ((StructuredSelection) selection).getFirstElement()));
					} else {
						proposalProvider.setSelectedSystem(Optional.empty());
					}
				}
			}
		});
		
		selectionTxt = new Text(this, SWT.BORDER);
		selectionTxt.setMessage("Coding selektieren");
		selectionTxt.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		proposalProvider = new CodingContentProposalProvider();
		ContentProposalAdapter toAddressProposalAdapter = new ContentProposalAdapter(selectionTxt,
			new TextContentAdapter(), proposalProvider, null, null);
		toAddressProposalAdapter.addContentProposalListener(new IContentProposalListener() {
			@Override
			public void proposalAccepted(IContentProposal proposal){
				selectionTxt.setText(proposal.getContent());
				proposalProvider.getCodingForProposal(proposal)
					.ifPresent(iCoding -> selectedCode = Optional.of(iCoding));
				selectionTxt.setSelection(selectionTxt.getText().length());
				Object[] listeners = selectionListeners.getListeners();
				for (Object object : listeners) {
					SelectionChangedEvent selectionEvent =
						new SelectionChangedEvent(CodingSelectionComposite.this, getSelection());
					((ISelectionChangedListener) object).selectionChanged(selectionEvent);
				}
			}
		});
		MenuManager menuManager = new MenuManager();
		menuManager.add(new Action("Lokalen Code anlegen") {
			@Override
			public void run(){
				TransientCoding transientCoding = new TransientCoding(
					CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem(),
					selectionTxt.getSelectionText(),
					"");
				CodingEditDialog editDialog = new CodingEditDialog(transientCoding, getShell());
				if (editDialog.open() == CodingEditDialog.OK) {
					CodingServiceComponent.getService().addLocalCoding(transientCoding);
					// trigger reload of code system
					setCodeSystem(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
				}
			}
			
			@Override
			public boolean isEnabled(){
				ISelection systemSelection = systemCombo.getSelection();
				if (systemSelection instanceof StructuredSelection) {
					Object codeSystem = ((StructuredSelection) systemSelection).getFirstElement();
					if(codeSystem instanceof String && codeSystem.equals(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem()) ) {
						String text = selectionTxt.getSelectionText();
						return text != null && !text.isEmpty();
					}
				}
				return false;
			}
		});
		menuManager.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				IContributionItem[] items = manager.getItems();
				for (IContributionItem iContributionItem : items) {
					iContributionItem.update();
				}
			}
		});
		selectionTxt.setMenu(menuManager.createContextMenu(selectionTxt));
	}
	
	public void setCodeSystem(String codeSystem){
		if (systemCombo != null) {
			systemCombo.setSelection(new StructuredSelection(codeSystem));
		}
	}
	
	public Optional<ICoding> getSelectedCoding(){
		return selectedCode;
	}
	
	@Override
	public void addSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.add(listener);
	}
	
	@Override
	public ISelection getSelection(){
		StructuredSelection ret = new StructuredSelection();
		if (selectedCode.isPresent()) {
			ret = new StructuredSelection(selectedCode.get());
		}
		return ret;
	}
	
	@Override
	public void removeSelectionChangedListener(ISelectionChangedListener listener){
		selectionListeners.remove(listener);
	}
	
	@Override
	public void setSelection(ISelection selection){
		if (selection instanceof IStructuredSelection && !selection.isEmpty()) {
			ICoding iCoding = (ICoding) ((IStructuredSelection) selection).getFirstElement();
			String label = proposalProvider.toLabel(iCoding);
			if (label != null) {
				selectionTxt.setText(label);
				selectionTxt.setSelection(selectionTxt.getText().length());
				selectedCode = Optional.of(iCoding);
			}
		} else {
			selectionTxt.setText("");
			selectedCode = Optional.empty();
		}
	}
}
