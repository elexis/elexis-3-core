package ch.elexis.core.ui.stock.handler;

import org.eclipse.core.commands.State;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;

public class StockSCSToggleArticleOutlayState extends State {
	
	@Override
	public Object getValue(){
		return CoreHub.localCfg.get(Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY,
			Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY_DEFAULT);
	}
	
	@Override
	public void setValue(Object value){
		CoreHub.localCfg.set(Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY,
			(Boolean) value);
	}
}
