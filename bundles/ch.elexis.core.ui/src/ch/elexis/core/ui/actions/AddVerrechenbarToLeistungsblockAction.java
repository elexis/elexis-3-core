package ch.elexis.core.ui.actions;

import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.interfaces.ICodeElement;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.AddElementToBlockDialog;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.PersistentObject;

/**
 *  @since 3.7 extracted from {@link UiVerrechenbarAdapter}
 */
public class AddVerrechenbarToLeistungsblockAction extends Action {

	private final Class<?> clazz;
	private String named;

	public AddVerrechenbarToLeistungsblockAction(String named) {
		super("Zu Leistungsblock...");
		this.named = named;
		this.clazz = null;
	}

	@Override
	public void run() {
		AddElementToBlockDialog adb = new AddElementToBlockDialog(UiDesk.getTopShell());
		if (adb.open() == Dialog.OK) {
			if (adb.getResult() == null) {
				return;
			}
			ICodeElementBlock block = CoreModelServiceHolder.get()
					.load(adb.getResult().getId(), ICodeElementBlock.class).orElse(null);
			Optional<?> selected = Optional.empty();
			if (clazz != null) {
				if (PersistentObject.class.isAssignableFrom(clazz)) {
					ICodeElement ice = (ICodeElement) ElexisEventDispatcher.getSelected(clazz);
					Leistungsblock lb = adb.getResult();
					lb.addElement(ice);
					ElexisEventDispatcher.reload(Leistungsblock.class);
				} else if (Identifiable.class.isAssignableFrom(clazz)) {
					selected = ContextServiceHolder.get().getTyped(clazz);
				}
			} else if (named != null) {
				selected = ContextServiceHolder.get().getNamed(named);
			}
			if (selected.isPresent() && selected.get() instanceof ch.elexis.core.model.ICodeElement) {
				block.addElement((ch.elexis.core.model.ICodeElement) selected.get());
			}
		}
	}

}
