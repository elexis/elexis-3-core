package ch.elexis.core.ui.views.controls;

import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.Labor;
import ch.elexis.core.ui.dialogs.KontaktSelektor;

public class LaborSelectionComposite extends KontaktSelectionComposite {
	
	public LaborSelectionComposite(Composite parent, int style){
		super(parent, style);
	}
	
	@Override
	protected KontaktSelektor getKontaktSelector(){
		return new KontaktSelektor(getShell(), Labor.class, Messages.LaborSelectionComposite_title,
			Messages.LaborSelectionComposite_message, Kontakt.DEFAULT_SORT);
	}
}
