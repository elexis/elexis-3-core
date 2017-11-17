package ch.elexis.core.ui.perspective.handler;

import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.compatibility.ElexisFastViewUtil;
import ch.elexis.core.ui.perspective.service.IPerspectiveImportService;

@Component
public class PerspektiveImportHandler extends AbstractHandler {
	
	static IPerspectiveImportService perspectiveImportService;
	
	@Reference(unbind = "-")
	public static void bind(IPerspectiveImportService service){
		perspectiveImportService = service;
	}
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		try {
			EModelService modelService = getService(EModelService.class);
			FileDialog dialog = new FileDialog(UiDesk.getTopShell(), SWT.OPEN);
			dialog.setFilterNames(new String[] {
				"XMI", "XML (Legacy)"
			});
			dialog.setFilterExtensions(new String[] {
				"*.xmi", "*.xml"
			});
			
			dialog.setFilterPath(CoreHub.getWritableUserDir().getAbsolutePath()); // Windows path
			
			String path = dialog.open();
			if (path != null) {
				IPerspectiveDescriptor createdPd = null;
				if (path.toLowerCase().endsWith("xml")) {
					// legacy
					
					MPerspective mPerspective = modelService.createModelElement(MPerspective.class);
					// the workbench window must be on top - otherwise the exception 'Application does not have an active window' occurs
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().open();
					List<String> fastViewIds =
						perspectiveImportService.createPerspectiveFromLegacy(path, mPerspective);
					createdPd = savePerspectiveToRegistryLegacy(mPerspective);
					perspectiveImportService.openPerspective(createdPd);
					switchToPerspectiveLegacy(mPerspective, fastViewIds);
					IWorkbenchPage wp = (IWorkbenchPage) PlatformUI.getWorkbench()
						.getActiveWorkbenchWindow().getActivePage();
					wp.savePerspectiveAs(createdPd);
					
				} else {
					createdPd = perspectiveImportService.importPerspective(path, null, true);
					
				}
				
				if (createdPd != null) {
					MessageDialog.openInformation(UiDesk.getDisplay().getActiveShell(), "Import",
						"Die Perspektive '" + createdPd.getLabel()
							+ "' wurde erfolgreich importiert.");
				}
			}
		} catch (Exception e) {
			MessageDialog.openError(UiDesk.getDisplay().getActiveShell(), "Import",
				"Diese Perspektive kann nicht importiert werden.");
			LoggerFactory.getLogger(PerspektiveExportHandler.class).error("import error", e);
		}
		
		return null;
	}
	
	@SuppressWarnings("restriction")
	private IPerspectiveDescriptor savePerspectiveToRegistryLegacy(MPerspective perspective){
		IPerspectiveRegistry perspectiveRegistry =
			(PerspectiveRegistry) PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor pd =
			perspectiveRegistry.findPerspectiveWithId(perspective.getElementId());
		if (pd == null) {
			((PerspectiveRegistry) perspectiveRegistry).addPerspective(perspective);
			pd = perspectiveRegistry.findPerspectiveWithId(perspective.getElementId());
		} else {
			LoggerFactory.getLogger(PerspektiveImportHandler.class)
				.error("perspective descriptor already exists for perspective id: "
				+ perspective.getElementId());
		}
		
		return pd;
	}
	
	private void switchToPerspectiveLegacy(MPerspective loadedPerspective,
		List<String> preLoadedFastViewIds){
		
		EModelService modelService = getService(EModelService.class);
		EPartService partService = getService(EPartService.class);
		MApplication mApplication = getService(MApplication.class);
		MTrimmedWindow mWindow = (MTrimmedWindow) modelService.find("IDEWindow", mApplication);
		if (mWindow == null) {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				mWindow = (MTrimmedWindow) windows.get(0);
			}
		}
		MPerspective activePerspective = modelService.getActivePerspective(mWindow);
		MElementContainer<MUIElement> perspectiveParent = activePerspective.getParent();
		List<String> fastViewIds = preLoadedFastViewIds;
		
		// add the loaded perspective and switch to it
		String id = loadedPerspective.getElementId();
		Iterator<MUIElement> it = perspectiveParent.getChildren().iterator();
		while (it.hasNext()) {
			MUIElement element = it.next();
			if (id.equals(element.getElementId()) || element.getElementId().startsWith(id + ".<")) {
				it.remove();
			}
		}
		perspectiveParent.getChildren().add(loadedPerspective);
		
		// add fast view
		for (String fastViewId : fastViewIds) {
			ElexisFastViewUtil.addToFastView(loadedPerspective.getElementId(), fastViewId);
		}
		// the workbench window must be on top - otherwise the exception 'Application does not have an active window' occurs
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getShell().open();
		
		partService.switchPerspective(loadedPerspective);
	}
	
}
