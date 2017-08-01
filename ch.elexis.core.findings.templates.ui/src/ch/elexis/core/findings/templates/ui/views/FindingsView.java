package ch.elexis.core.findings.templates.ui.views;

import java.util.Optional;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.DoubleClickEvent;
import org.eclipse.jface.viewers.IDoubleClickListener;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;
import ch.elexis.core.findings.templates.ui.dlg.FindingsEditDialog;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.data.Patient;

@Component(service = {})
public class FindingsView extends ViewPart implements IActivationListener {
	
	public static FindingsTemplateService findingsTemplateService;
	public static ICodingService codingService;
	
	private TableViewer viewer;
	
	private final ElexisUiEventListenerImpl eeli_find =
		new ElexisUiEventListenerImpl(IFinding.class, ElexisEvent.EVENT_CREATE) {
			
			@Override
			public void runInUi(ElexisEvent ev){
				setInput();
			}
			
		};
	
	private ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		public void runInUi(ElexisEvent ev){
			setInput();
		}
	};
	
	public FindingsView(){
	}
	
	@Reference(unbind = "-")
	public synchronized void setFindingsTemplateService(FindingsTemplateService service){
		findingsTemplateService = service;
	}
	
	@Reference(unbind = "-")
	public synchronized void setCodingService(ICodingService service){
		codingService = service;
	}
	
	@Override
	public void createPartControl(Composite parent){
		Composite c = new Composite(parent, SWT.NONE);
		c.setLayout(new GridLayout(1, false));
		c.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		viewer = new TableViewer(c, SWT.FULL_SELECTION | SWT.BORDER | SWT.SINGLE);
		viewer.getTable().setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1));
		
		viewer.setContentProvider(new ArrayContentProvider());
		viewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element){
				// TODO Auto-generated method stub
				if (element instanceof IFinding) {
					Optional<String> t = ((IFinding) element).getText();
					if (t.isPresent()) {
						return findingsTemplateService.getTypeAsText((IFinding) element) + ": " + t.get();
					}
				}
				return "";
			}
		});
		setInput();
		
		viewer.addDoubleClickListener(new IDoubleClickListener() {
			
			@Override
			public void doubleClick(DoubleClickEvent event){
				StructuredSelection structuredSelection =
					(StructuredSelection) event.getSelection();
				if (!structuredSelection.isEmpty()) {
					Object o = structuredSelection.getFirstElement();
					if (o instanceof IFinding) {
						IFinding selection = (IFinding) o;
						FindingsEditDialog findingsEditDialog = new FindingsEditDialog(
							Display.getDefault().getActiveShell(), selection);
						if (findingsEditDialog.open() == MessageDialog.OK) {
							setInput();
						}
					}
				}
				
			}
		});
		ElexisEventDispatcher.getInstance().addListeners(eeli_pat, eeli_find);
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	private void setInput(){
		viewer.setInput(
			findingsTemplateService.getFindings(ElexisEventDispatcher.getSelectedPatient()));
	}

	@Override
	public void setFocus(){
		
	}
	

	@Override
	public void activation(boolean mode){
	}
	
	@Override
	public void visible(boolean mode){
		if (!mode) {
			
		}
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_pat, eeli_find);
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
}
