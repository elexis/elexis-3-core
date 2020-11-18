package ch.elexis.core.spotlight.ui.controls;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.spotlight.ISpotlightService;
import ch.elexis.core.spotlight.ui.SpotlightUiUtil;

import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.SWT;

public class SpotlightResultComposite extends Composite {
	
	private SpotlightResultListComposite resultListComposite;
	private SpotlightResultDetailComposite resultDetailComposite;
	
	public SpotlightResultComposite(Composite parent, int style,
		ISpotlightService spotlightService, SpotlightUiUtil uiUtil){
		super(parent, style);
		GridLayout gridLayout = new GridLayout(2, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		setLayout(gridLayout);
		
		Label lblSeparator = new Label(this, SWT.SEPARATOR | SWT.HORIZONTAL);
		lblSeparator.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
		
		resultListComposite = new SpotlightResultListComposite(this, SWT.NONE, spotlightService, uiUtil);
		GridData gd_list = new GridData(SWT.LEFT, SWT.FILL, false, true, 1, 1);
		gd_list.widthHint = 300;
		resultListComposite.setLayoutData(gd_list);
		
		resultDetailComposite = new SpotlightResultDetailComposite(this, style);
		GridData gd_resultDetailComposite = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
		gd_resultDetailComposite.widthHint = 400;
		resultDetailComposite.setLayoutData(gd_resultDetailComposite);
		
		resultListComposite.setDetailComposite(resultDetailComposite);
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public boolean setFocus(){
		return resultListComposite.setFocus();
	}
	
}
