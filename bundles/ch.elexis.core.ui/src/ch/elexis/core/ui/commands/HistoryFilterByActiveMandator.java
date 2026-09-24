package ch.elexis.core.ui.commands;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.ui.views.KonsListe;

public class HistoryFilterByActiveMandator extends AbstractHandler {

	public static final String CMD_ID = "ch.elexis.core.ui.command.filterHistoryByMandator";
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState";

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean state = !HandlerUtil.toggleCommandState(event.getCommand());

		IWorkbenchPart part = HandlerUtil.getActivePart(event);
		if (part instanceof KonsListe) {
			((KonsListe) part).setHistoryFilterByActiveMandatorState(state);
		}
		return null;
	}

}
