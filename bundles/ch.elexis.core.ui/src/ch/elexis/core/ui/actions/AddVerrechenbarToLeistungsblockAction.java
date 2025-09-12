package ch.elexis.core.ui.actions;

import java.util.Optional;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;

import ch.elexis.core.common.ElexisEventTopics;
import ch.elexis.core.model.ICodeElementBlock;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.AddElementToBlockDialog;

/**
 *  @since 3.7 extracted from {@link UiVerrechenbarAdapter}
 */
public class AddVerrechenbarToLeistungsblockAction extends Action {

	private String named;

	public AddVerrechenbarToLeistungsblockAction(String named) {
		super("Zu Leistungsblock...");
		this.named = named;
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
			if (named != null && block != null) {
				selected = ContextServiceHolder.get().getNamed(named);
				block.addElement((ch.elexis.core.model.ICodeElement) selected.get());
				CoreModelServiceHolder.get().save(block);
				ContextServiceHolder.get().postEvent(ElexisEventTopics.EVENT_UPDATE, block);
			}
		}
	}
}
