package ch.elexis.core.ui.startup;

import java.io.File;
import java.io.IOException;
import java.net.URISyntaxException;
import java.util.Collections;

import org.apache.commons.io.FileUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.runtime.URIUtil;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.e4.ui.internal.workbench.E4Workbench;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.osgi.service.datalocation.Location;
import org.eclipse.ui.IStartup;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchListener;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.services.LocalDocumentServiceHolder;

/**
 * Class for code executed on {@link IWorkbench} startup.
 * 
 * @author thomas
 *
 */
public class UiStartup implements IStartup {
	
	@Override
	public void earlyStartup(){
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
				
				IEclipseContext context = workbench.getService(IEclipseContext.class);
				if (context != null) {
					Location instanceLocation =
						(Location) context.get(E4Workbench.INSTANCE_LOCATION);
					if (instanceLocation != null) {
						File baseLocation;
						try {
							baseLocation = new File(URIUtil.toURI(instanceLocation.getURL()));
						} catch (URISyntaxException e) {
							throw new RuntimeException(e);
						}
						baseLocation = new File(baseLocation, ".metadata"); //$NON-NLS-1$
						baseLocation = new File(baseLocation, ".plugins"); //$NON-NLS-1$
						baseLocation = new File(baseLocation, "org.eclipse.e4.workbench"); //$NON-NLS-1$
						File workbenchXmiLocation = new File(baseLocation, "workbench.xmi"); //$NON-NLS-1$
						if (workbenchXmiLocation.exists()) {
							try {
								File copied =
									new File(CoreHub.getWritableUserDir(), "37_workbench.xmi");
								FileUtils.copyFile(workbenchXmiLocation, copied);
							} catch (IOException e) {
								LoggerFactory.getLogger(getClass())
									.error("Exception on workbench xmi copy", e);
							}
						}
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
