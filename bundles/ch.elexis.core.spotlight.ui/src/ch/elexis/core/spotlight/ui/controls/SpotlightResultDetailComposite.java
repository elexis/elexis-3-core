package ch.elexis.core.spotlight.ui.controls;

import java.lang.reflect.InvocationTargetException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.controls.detail.PatientDetailComposite;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class SpotlightResultDetailComposite extends Composite {
	
	private AbstractSpotlightResultEntryDetailComposite activeDetailComposite;
	
	/**
	 * Create the composite.
	 * 
	 * @param parent
	 * @param style
	 */
	public SpotlightResultDetailComposite(Composite parent, int style){
		super(parent, style);
		GridLayout gridLayout = new GridLayout(1, false);
		gridLayout.marginHeight = 0;
		gridLayout.marginWidth = 0;
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
		if(selectedEntry != null) {
			assertComposite(selectedEntry.getCategory());
		}
		activeDetailComposite.setSpotlightEntry(selectedEntry);
		this.layout();
		
	}
	
	private void assertComposite(Category category){
		
		Class<? extends AbstractSpotlightResultEntryDetailComposite> categoryComposite =
			getCategoryComposite(category);
		
		if (activeDetailComposite != null) {
			if (activeDetailComposite.getClass().equals(categoryComposite)) {
				return;
			}
			activeDetailComposite.dispose();
		}
		
		try {
			Class<?>[] cArg = new Class[2];
			cArg[0] = Composite.class;
			cArg[1] = int.class;
			AbstractSpotlightResultEntryDetailComposite newInstance =
				categoryComposite.getDeclaredConstructor(cArg).newInstance(this, SWT.None);
			CoreUiUtil.injectServicesWithContext(newInstance);
			activeDetailComposite = newInstance;
			activeDetailComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
			
		} catch (InstantiationException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException | NoSuchMethodException | SecurityException e) {
			e.printStackTrace();
		}
		
	}
	
	private Class<? extends AbstractSpotlightResultEntryDetailComposite> getCategoryComposite(
		Category category){
		// TODO registry?
		switch (category) {
		case PATIENT:
			return PatientDetailComposite.class;
		default:
			break;
		}
		return null;
	}
	
}
