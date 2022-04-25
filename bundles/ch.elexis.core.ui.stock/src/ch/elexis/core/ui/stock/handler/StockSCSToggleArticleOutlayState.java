package ch.elexis.core.ui.stock.handler;

import org.eclipse.core.commands.State;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;

public class StockSCSToggleArticleOutlayState extends State {

	@Override
	public Object getValue() {
		return ConfigServiceHolder.get().getLocal(Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY,
				Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY_DEFAULT);
	}

	@Override
	public void setValue(Object value) {
		ConfigServiceHolder.get().setLocal(Preferences.INVENTORY_MACHINE_SUSPEND_OUTLAY, (Boolean) value);
	}
}
