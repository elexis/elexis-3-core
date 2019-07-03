package ch.elexis.core.ui.actions;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.UiVerrechenbarAdapter;
import ch.elexis.core.ui.dialogs.AddElementToBlockDialog;
import ch.elexis.data.Leistungsblock;

/**
 * @since 3.7 extracted from {@link UiVerrechenbarAdapter}
 */
public class AddVerrechenbarToLeistungsblockAction extends Action {

	private final Class<?> clazz;

	public AddVerrechenbarToLeistungsblockAction(Class<?> clazz) {
		super("Zu Leistungsblock...");
		this.clazz = clazz;
	}

	@Override
	public void run() {
		AddElementToBlockDialog adb = new AddElementToBlockDialog(UiDesk.getTopShell());
		if (adb.open() == Dialog.OK) {
			ICodeElement ice = (ICodeElement) ElexisEventDispatcher.getSelected(clazz);
			Leistungsblock lb = adb.getResult();
			lb.addElement(ice);
			ElexisEventDispatcher.reload(Leistungsblock.class);
		}
	}

}
