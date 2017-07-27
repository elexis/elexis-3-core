package ch.elexis.core.findings.templates.ui.views;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;
import ch.elexis.core.findings.templates.ui.composite.FindingsComposite;
import ch.elexis.core.findings.templates.ui.composite.FindingsDetailComposite;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;

@Component(service = {})
public class FindingsTemplateView extends ViewPart implements IActivationListener {
	
	private static FindingsTemplateService findingsTemplateService;
	private FindingsComposite findingsComposite;
	
	public FindingsTemplateView(){
	}
	
	@Reference(unbind = "-")
	public synchronized void setService(FindingsTemplateService service){
		findingsTemplateService = service;
	}
	
	@Override
	public void createPartControl(Composite parent){
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, true));
		composite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		FindingsTemplates model = findingsTemplateService.getFindingsTemplates();
		findingsComposite = new FindingsComposite(composite, model);
		FindingsDetailComposite findingsDetailComposite =
			new FindingsDetailComposite(composite);
		findingsComposite.setFindingsDetailComposite(findingsDetailComposite);
		
		findingsComposite.createContents();
		findingsDetailComposite.createContents();
		getSite().setSelectionProvider(findingsComposite.getViewer());
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	@Override
	public void setFocus(){}
	
	

	@Override
	public void activation(boolean mode){
	}
	
	@Override
	public void visible(boolean mode){
		if (mode) {
			FindingsTemplates model = findingsTemplateService.getFindingsTemplates();
			findingsComposite.setModel(model);
		} else {
			Optional<FindingsTemplates> model = findingsComposite.getModel();
			findingsTemplateService
				.saveFindingsTemplates(model);
			model.ifPresent(item -> findingsComposite.setModel(item));
		}
	};
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
}
