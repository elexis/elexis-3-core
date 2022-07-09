package ch.elexis.core.ui.startup;

import java.util.Collection;
import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.workbench.UIEvents;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

/**
 * Class for code executed on {@link IWorkbench} startup.
 *
 * @author thomas
 *
 */
@Component(property = EventConstants.EVENT_TOPIC + "=" + UIEvents.UILifeCycle.APP_STARTUP_COMPLETE)
public class StartupHandler implements EventHandler {

	private static IEclipseContext applicationContext;

	@Override
	public void handleEvent(Event event) {
		LoggerFactory.getLogger(getClass()).info("APPLICATION STARTUP COMPLETE"); //$NON-NLS-1$
		Object property = event.getProperty("org.eclipse.e4.data"); //$NON-NLS-1$
		if (property instanceof MApplication) {
			MApplication application = (MApplication) property;
			StartupHandler.applicationContext = application.getContext();
		}

		PlatformUI.getWorkbench().addWorkbenchListener(new IWorkbenchListener() {
			@Override
			public boolean preShutdown(IWorkbench workbench, boolean forced) {
				if (LocalDocumentServiceHolder.getService() != null
						&& LocalDocumentServiceHolder.getService().isPresent()
						&& !LocalDocumentServiceHolder.getService().get().getAll().isEmpty()) {
					ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
							.getService(ICommandService.class);
					Command command = commandService.getCommand("ch.elexis.core.ui.command.openLocalDocuments"); //$NON-NLS-1$

					ExecutionEvent event = new ExecutionEvent(command, Collections.EMPTY_MAP, this, null);
					try {
						command.executeWithChecks(event);
						return LocalDocumentServiceHolder.getService().get().getAll().isEmpty();
					} catch (ExecutionException | NotDefinedException | NotEnabledException | NotHandledException e) {
						MessageDialog.openError(PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell(),
								Messages.UiStartup_errortitle, Messages.UiStartup_errormessage);
					}
				}
				// reset dirty of closed compatibility parts that would open save resources
				// dialog (redmine #20724)
				if (StartupHandler.applicationContext != null
						&& StartupHandler.applicationContext.get(EPartService.class) != null) {
					EPartService partService = StartupHandler.applicationContext.get(EPartService.class);
					try {
						Collection<MPart> dirtyParts = partService.getDirtyParts();
						if (!dirtyParts.isEmpty()) {
							for (MPart mPart : dirtyParts) {
								if (mPart.getObject() == null && mPart.getContributionURI()
										.endsWith("internal.e4.compatibility.CompatibilityView")) { //$NON-NLS-1$
									mPart.setDirty(false);
								}
							}
						}
					} catch (IllegalStateException e) {
						LoggerFactory.getLogger(getClass()).warn("Exception resetting dirty state", e); //$NON-NLS-1$
					}
				}
				return true;
			}

			@Override
			public void postShutdown(IWorkbench workbench) {
				// nothing to do here
			}
		});
	}
}
