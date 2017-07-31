package ch.elexis.core.findings.templates.ui.views;

import java.util.Optional;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.TabFolder;
import org.eclipse.swt.widgets.TabItem;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

import ch.elexis.core.findings.codes.ICodingService;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.service.FindingsTemplateService;
import ch.elexis.core.findings.templates.ui.composite.CodesSystemsComposite;
import ch.elexis.core.findings.templates.ui.composite.FindingsComposite;
import ch.elexis.core.findings.templates.ui.composite.FindingsDetailComposite;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;

@Component(service = {})
public class FindingsTemplateView extends ViewPart implements IActivationListener {
	
	public static FindingsTemplateService findingsTemplateService;
	public static ICodingService codingService;
	private TabFolder tabFolder;
	private TabItem tabTemplates;
	private Composite compositeTemplates;
	
	private FindingsComposite findingsComposite;
	
	public FindingsTemplateView(){
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
		tabFolder = new TabFolder(parent, SWT.NONE);
		tabFolder.setLayout(new GridLayout(1, true));
		tabFolder.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		compositeTemplates = new Composite(tabFolder, SWT.NONE);
		compositeTemplates.setLayout(new GridLayout(1, true));
		compositeTemplates.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		tabTemplates = new TabItem(tabFolder, SWT.NONE, 0);
		tabTemplates.setText("Vorlagen");
		TabItem tabCodeSystems = new TabItem(tabFolder, SWT.NONE, 1);
		tabCodeSystems.setText("Codesysteme");
		
		FindingsTemplates model = findingsTemplateService.getFindingsTemplates();
		findingsComposite = new FindingsComposite(compositeTemplates, model);
		FindingsDetailComposite findingsDetailComposite =
			new FindingsDetailComposite(compositeTemplates, model);
		findingsComposite.setFindingsDetailComposite(findingsDetailComposite);
		
		findingsComposite.createContents();
		findingsDetailComposite.createContents();
		getSite().setSelectionProvider(findingsComposite.getViewer());
		

		CodesSystemsComposite codesSystemsComposite =
			new CodesSystemsComposite(tabFolder);
		codesSystemsComposite.createContens();
		tabTemplates.setControl(compositeTemplates);
		tabCodeSystems.setControl(codesSystemsComposite);
		
		GlobalEventDispatcher.addActivationListener(this, this);
	}

	@Override
	public void setFocus(){
		resetTab();
		FindingsTemplates model = findingsTemplateService.getFindingsTemplates();
		findingsComposite.setModel(model);
	}
	

	@Override
	public void activation(boolean mode){
	}
	
	@Override
	public void visible(boolean mode){
		if (!mode) {
			Optional<FindingsTemplates> model = findingsComposite.getModel();
			findingsTemplateService
				.saveFindingsTemplates(model);
			model.ifPresent(item -> findingsComposite.setModel(item));
		}
	}

	private void resetTab(){
		tabTemplates.setControl(compositeTemplates);
		tabFolder.setSelection(0);
	};
	
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		super.dispose();
	}
	
}
