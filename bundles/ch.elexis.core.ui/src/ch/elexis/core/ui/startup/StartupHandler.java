package ch.elexis.core.ui.startup;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

/**
 * Class for code executed on {@link IWorkbench} startup.
 * 
 * @author thomas
 *
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class StartupHandler implements EventHandler {
	
	@Override
	public void handleEvent(Event event){
		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced){
				if (LocalDocumentServiceHolder.getService() != null
					&& LocalDocumentServiceHolder.getService().isPresent()
					&& !LocalDocumentServiceHolder.getService().get().getAll().isEmpty()) {
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
						.getService(ICommandService.class);
					Command command =
						commandService.getCommand("ch.elexis.core.ui.command.openLocalDocuments"); //$NON-NLS-1$
					
					ExecutionEvent event =
						new ExecutionEvent(command, Collections.EMPTY_MAP, this, null);
					try {
						command.executeWithChecks(event);
						return LocalDocumentServiceHolder.getService().get().getAll().isEmpty();
					} catch (ExecutionException | NotDefinedException | NotEnabledException
							| NotHandledException e) {
						MessageDialog.openError(
							PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
							Messages.UiStartup_errortitle,
							Messages.UiStartup_errormessage);
					}
				}
				return true;
			}
			
			@Override
			public void postShutdown(IWorkbench workbench){
				// nothing to do here
			}
		});
	}
}
