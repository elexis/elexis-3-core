package ch.elexis.core.spotlight.ui.controls;

import java.util.Objects;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailComposite;
import ch.elexis.core.spotlight.ui.internal.ISpotlightResultEntryDetailCompositeService;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class SpotlightResultDetailComposite extends Composite {
	
	private ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService;
	
	private ISpotlightResultEntryDetailComposite activeDetailComposite;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SpotlightResultDetailComposite(Composite parent, int style,
		ISpotlightResultEntryDetailCompositeService resultEntryDetailCompositeService){
		super(parent, style);
		this.resultEntryDetailCompositeService = resultEntryDetailCompositeService;
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
		gridLayout.marginTop = 0;
		setLayout(gridLayout);
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}
	
	@Override
	public boolean setFocus(){
		if (activeDetailComposite != null) {
			return activeDetailComposite.setFocus();
		}
		return false;
	}
	
	protected void setSelection(ISpotlightResultEntry selectedEntry){
		// TODO make category not selectable
		if (selectedEntry != null) {
			assertComposite(selectedEntry.getCategory());
		}
		activeDetailComposite.setSpotlightEntry(selectedEntry);
		this.layout();
	}
	
	private void assertComposite(Category category){
		if (activeDetailComposite != null) {
			if (Objects.equals(category, activeDetailComposite.appliedForCategory())) {
				return;
			}
			activeDetailComposite.dispose();
		}
		
		activeDetailComposite =
			resultEntryDetailCompositeService.instantiate(category, this, SWT.None);
		CoreUiUtil.injectServicesWithContext(activeDetailComposite);
		activeDetailComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
	}

	public boolean handleAltKeyPressed(int keyCode){
		return activeDetailComposite.handleAltKeyPressed(keyCode);
	}
	
}
