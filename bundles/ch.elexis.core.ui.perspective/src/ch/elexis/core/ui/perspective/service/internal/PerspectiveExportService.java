package ch.elexis.core.ui.perspective.service.internal;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.emf.ecore.EObject;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.ui.PlatformUI;
import org.osgi.service.component.annotations.Component;

import ch.elexis.core.ui.compatibility.ElexisFastViewUtil;
import ch.elexis.core.ui.perspective.service.IPerspectiveExportService;

@Component
public class PerspectiveExportService implements IPerspectiveExportService {
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
	
	/**
	 * Workaround for https://bugs.eclipse.org/bugs/show_bug.cgi?id=481996 Set the references of the
	 * placeholder to the corresponding parts again in the copy.
	 */
	private static MPerspective clonePerspectiveWithWorkaround(EModelService modelService,
		MPerspective original){
		MPerspective clone = (MPerspective) modelService.cloneElement(original, null);
		clone.setElementId(original.getElementId());
		
		List<MPlaceholder> placeholderClones = modelService.findElements(clone, null,
			MPlaceholder.class, null, EModelService.IN_ANY_PERSPECTIVE);
		// For each placeholder in the new perspective, set the reference value to the one from the old perspective
		for (MPlaceholder placeholderClone : placeholderClones) {
			// Search for the corresponding placeholder in the "old" perspective
			List<MPlaceholder> placeholderOriginal =
				modelService.findElements(original, placeholderClone.getElementId(),
					MPlaceholder.class, null, EModelService.IN_ANY_PERSPECTIVE);
			if (placeholderOriginal.size() == 1) {
				// We found only one corresponding placeholder element. Set reference of old element to the new element
				placeholderClone.setRef((placeholderOriginal.get(0).getRef()));
			} else if (placeholderOriginal.isEmpty()) {
				System.out.println("NO PLACEHOLDER");
			} else {
				System.out.println("MORE THEN ONE PLACEHOLDER" + " " //$NON-NLS-1$
					+ placeholderOriginal.toString());
				placeholderClone.setRef((placeholderOriginal.get(0).getRef()));
			}
		}
		
		return clone;
	}
	
	@SuppressWarnings("restriction")
	@Override
	public void exportPerspective(String pathToExport, String newCode, String newLabel)
		throws IOException{
		
		try (OutputStream outputStream = new FileOutputStream(pathToExport)) {
			EModelService modelService = getService(EModelService.class);
			
			MApplication mApplication = getService(MApplication.class);
			MTrimmedWindow window = (MTrimmedWindow) modelService.find("IDEWindow", mApplication);
			if (window == null) {
				List<MWindow> windows = mApplication.getChildren();
				if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
					window = (MTrimmedWindow) windows.get(0);
				}
			}
			
			// store model of the active perspective
			MPerspective activePerspective = modelService.getActivePerspective(window);
			
			// create a resource, which is able to store e4 model elements
			E4XMIResourceFactory e4xmiResourceFactory = new E4XMIResourceFactory();
			Resource resource = e4xmiResourceFactory.createResource(null);
			
			//clone the perspective and replace the placeholder ref with element ids of their content
			MPerspective clone = clonePerspectiveWithWorkaround(modelService, activePerspective);
			
			if (newLabel != null) {
				clone.setLabel(newLabel);
			}
			
			if (newCode != null) {
				clone.setElementId(newCode);
			}
			
			List<MPlaceholder> placeholderClones = modelService.findElements(clone, null,
				MPlaceholder.class, null, EModelService.IN_ANY_PERSPECTIVE);
			for (MPlaceholder placeholder : placeholderClones) {
				/* MUIElement ref = placeholder.getRef();
				if placeholder elementid is not the view id then use tags
				if (ref != null && !placeholder.getTags().contains(ref.getElementId())) {
					placeholder.getTags().add(0, ref.getElementId());
				}*/
				placeholder.setRef(null);
			}
			
			ElexisFastViewUtil.transferFastViewPersistedState(window, clone);
			
			// add the cloned model element to the resource so that it may be stored
			resource.getContents().add((EObject) clone);
			
			resource.save(outputStream, null);
		}
	}
	
}
