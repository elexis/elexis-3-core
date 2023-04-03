package ch.elexis.core.ui.text;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.IViewPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.data.Brief;

public class EditLocalDocumentUtil {

	/**
	 * If {@link Preferences#P_TEXT_EDIT_LOCAL} is set, the
	 * <code> ch.elexis.core.ui.command.startEditLocalDocument </code> command is
	 * called with the provided {@link Brief}, and the provided {@link IViewPart} is
	 * hidden.
	 *
	 * @param view
	 * @param brief
	 * @return returns true if edit local is started and view is hidden
	 */
	public static boolean startEditLocalDocument(IViewPart view, Brief brief) {
		if (CoreHub.localCfg.get(Preferences.P_TEXT_EDIT_LOCAL, false) && brief != null) {
			// open for editing
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			Command command = commandService.getCommand("ch.elexis.core.ui.command.startEditLocalDocument"); //$NON-NLS-1$

			PlatformUI.getWorkbench().getService(IEclipseContext.class).set(command.getId().concat(".selection"), //$NON-NLS-1$
					new StructuredSelection(brief));
			try {
				command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, view, null));
			} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
				MessageDialog.openError(view.getSite().getShell(), Messages.Core_Error,
						Messages.Core_Document_Not_Opened_Locally);
			}
			view.getSite().getPage().hideView(view);
			return true;
		}
		return false;
	}

	/**
	 * If {@link Preferences#P_TEXT_EDIT_LOCAL} is set, the
	 * <code> ch.elexis.core.ui.command.startEditLocalDocument </code> command is
	 * called with the provided {@link IDocumentLetter}, and the provided
	 * {@link IViewPart} is hidden.
	 *
	 * @param view
	 * @param document
	 * @return returns true if edit local is started and view is hidden
	 */
	public static boolean startEditLocalDocument(IViewPart view, IDocumentLetter document) {
		if (CoreHub.localCfg.get(Preferences.P_TEXT_EDIT_LOCAL, false) && document != null) {
			// open for editing
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);
			Command command = commandService.getCommand("ch.elexis.core.ui.command.startEditLocalDocument"); //$NON-NLS-1$

			PlatformUI.getWorkbench().getService(IEclipseContext.class).set(command.getId().concat(".selection"), //$NON-NLS-1$
					new StructuredSelection(document));
			try {
				command.executeWithChecks(new ExecutionEvent(command, Collections.EMPTY_MAP, view, null));
			} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
				MessageDialog.openError(view.getSite().getShell(), Messages.Core_Error,
						Messages.Core_Document_Not_Opened_Locally);
			}
			view.getSite().getPage().hideView(view);
			return true;
		}
		return false;
	}
}
