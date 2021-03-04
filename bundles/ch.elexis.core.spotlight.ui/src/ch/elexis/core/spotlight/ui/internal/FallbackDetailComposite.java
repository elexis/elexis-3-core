package ch.elexis.core.spotlight.ui.internal;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.spotlight.ISpotlightResultEntry;
import ch.elexis.core.spotlight.ISpotlightResultEntry.Category;
import ch.elexis.core.spotlight.ui.ISpotlightResultEntryDetailComposite;

public class FallbackDetailComposite extends Composite implements ISpotlightResultEntryDetailComposite {
	
	/**
	 * Create the composite.
	 * @param parent
	 * @param style
	 */
	public FallbackDetailComposite(Composite parent, int style){
		super(parent, style);
		Label label = new Label(parent, style);
		label.setText("No specialized composite found");
	}
	
	@Override
	protected void checkSubclass(){
		// Disable the check that prevents subclassing of SWT components
	}

	@Override
	public void setSpotlightEntry(ISpotlightResultEntry resultEntry){
		// TODO Auto-generated method stub
		
	}

	@Override
	public Category appliedForCategory(){
		return null;
	}

	@Override
	public boolean handleAltKeyPressed(int keyCode){
		return true;
	}
	
}
