package ch.elexis.core.ui.perspective.service.internal;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindowElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PerspectiveExtensionReader;
import org.eclipse.ui.internal.PerspectiveTagger;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.perspective.service.IPerspectiveImportService;
import ch.elexis.core.ui.perspective.service.IStateCallback;
import ch.elexis.core.ui.perspective.service.IStateCallback.State;

@Component
public class PerspectiveImportService implements IPerspectiveImportService {

	@SuppressWarnings("restriction")
	@Override
	public void importPerspective(String uri,
		IStateCallback iStateHandle, boolean openPerspectiveIfAdded){
		
		IPerspectiveRegistry iPerspectiveRegistry =
			PlatformUI.getWorkbench().getPerspectiveRegistry();
		
		if (uri != null && !uri.toLowerCase().startsWith("http")) {
			if (uri.toLowerCase().endsWith("xmi")) {
				File f = new File(uri);
				
				MPerspective mPerspective = loadPerspectiveFromFile(f);
				if (mPerspective != null) {
					String id = mPerspective.getElementId();
					IPerspectiveDescriptor existingPerspectiveDescriptor =
						iPerspectiveRegistry.findPerspectiveWithId(id);
					
					String activePerspectiveId = getActivePerspectiveId();
					
					if (existingPerspectiveDescriptor == null
						|| iStateHandle == null || iStateHandle.state(State.OVERRIDE)) {
						
						IPerspectiveDescriptor activePerspectiveDescriptor =
							iPerspectiveRegistry.findPerspectiveWithId(activePerspectiveId);
						
						int idx = deletePerspective(id);
						
						((PerspectiveRegistry) iPerspectiveRegistry).addPerspective(mPerspective);
						IPerspectiveDescriptor pd = iPerspectiveRegistry.findPerspectiveWithId(id);
						if (pd != null) {
							((PerspectiveDescriptor) pd).setHasCustomDefinition(false); //not sure
							
						}
						if (idx > -1 || openPerspectiveIfAdded) {
							openPerspective(pd);
							
							// there was already an opened perspective switch back to it
							openPerspective(activePerspectiveDescriptor);
						}
					}
				}
				
			}
		}
	}
	
	@Override
	public void openPerspective(IPerspectiveDescriptor iPerspectiveDescriptor){
		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
			.setPerspective(iPerspectiveDescriptor);
	}
	
	/**
	 * Deletes a perspective
	 * 
	 * @param existingPerspectiveDescriptor
	 * @return the prespective idx inside stack
	 */
	private int deletePerspective(String perspectiveId){
		IPerspectiveRegistry iPerspectiveRegistry =
			PlatformUI.getWorkbench().getPerspectiveRegistry();
		MApplication mApplication = getService(MApplication.class);
		IPerspectiveDescriptor existingPerspectiveDescriptor =
			iPerspectiveRegistry.findPerspectiveWithId(perspectiveId);
		List<MPerspective> perspectivesInStack;
		IWorkbenchPage iWorkbenchPage;
		int idx = -1;
		
		if (existingPerspectiveDescriptor != null) {
			
			MPerspectiveStack mPerspectiveStack = getPerspectiveStack();
			perspectivesInStack = mPerspectiveStack.getChildren();
			
			for (MPerspective perspectiveInStack : perspectivesInStack) {
				if (existingPerspectiveDescriptor.getId()
					.equals(perspectiveInStack.getElementId())) {
					idx++;
					break;
				}
			}
			
			if (idx > -1) {
				iWorkbenchPage =
					PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
				iWorkbenchPage.closePerspective(existingPerspectiveDescriptor, true, false);
			}
			//NOT WORKING IF PERSPECTIVE IS PREDEFINED - workaround with generics
			iPerspectiveRegistry.deletePerspective(existingPerspectiveDescriptor);
			PerspectiveImportService.genericInvokMethod(iPerspectiveRegistry, "removeSnippet",
				MSnippetContainer.class, String.class, mApplication,
				existingPerspectiveDescriptor.getId());
			
		}
		return idx;
	}
	
	
	@SuppressWarnings("restriction")
	@Override
	public MPerspective loadPerspectiveFromFile(File f){
		if (f != null && f.exists()) {
			String path = f.getAbsolutePath();
			if (path.toLowerCase().endsWith("xmi")) {
				Resource resource = new E4XMIResourceFactory().createResource(null);
				try (InputStream inputStream = new FileInputStream(f)) {
					resource.load(inputStream, null);
					if (!resource.getContents().isEmpty()) {
						MPerspective loadedPerspective =
							(MPerspective) resource.getContents().get(0);
						return loadedPerspective;
					}
				} catch (IOException e) {
					LoggerFactory.getLogger(PerspectiveImportService.class)
						.error("cannot load perspective [{}]", f.getAbsolutePath(), e);
				}
			}
		}
		return null;
	}
	
	
	private MPerspective getActivePerspective(){
		EModelService modelService = getService(EModelService.class);
		MTrimmedWindow mWindow = getActiveWindow();
		if (mWindow != null) {
			return modelService.getActivePerspective(mWindow);
		}
		return null;
	}
	
	@Override
	public MTrimmedWindow getActiveWindow(){
		EModelService modelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		
		MTrimmedWindow mWindow = (MTrimmedWindow) modelService.find("IDEWindow", mApplication);
		if (mWindow == null) {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				mWindow = (MTrimmedWindow) windows.get(0);
			}
		}
		return mWindow;
	}
	
	private String getActivePerspectiveId(){
		MPerspective mPerspective = getActivePerspective();
		if (mPerspective != null) {
			return mPerspective.getElementId();
		}
		return null;
	}
	
	private MPerspectiveStack getPerspectiveStack(){
		EModelService modelService = getService(EModelService.class);
		MWindow window = getActiveWindow();
		List<MPerspectiveStack> theStack =
			modelService.findElements(window, null, MPerspectiveStack.class, null);
		if (theStack.size() > 0) {
			return theStack.get(0);
		}
		
		for (MWindowElement child : window.getChildren()) {
			if (child instanceof MPerspectiveStack) {
				return (MPerspectiveStack) child;
			}
		}
		return null;
	}
	
	@SuppressWarnings("restriction")
	@Override
	public List<String> createLegacyPerspective(String path, MPerspective mPerspective)
		throws IOException{
		List<String> fastViewIds = new ArrayList<>();
		
		try (FileReader reader = new FileReader(new File(path))) {
			XMLMemento memento = XMLMemento.createReadRoot(reader);
			
			String label = memento.getChild("descriptor").getString("label");
			String id = memento.getChild("descriptor").getString("id");
			PerspectiveDescriptor pd = new PerspectiveDescriptor(id, label, null);
			
			mPerspective.setLabel(label);
			mPerspective.setElementId(id);
			
			IMemento layout = memento.getChild(IWorkbenchConstants.TAG_LAYOUT);
			
			EPartService partService = getService(EPartService.class);
			EModelService modelService = getService(EModelService.class);
			MApplication mApplication = getService(MApplication.class);
			MWindow window = (MWindow) modelService.find("IDEWindow", mApplication);
			if (window == null) {
				List<MWindow> windows = mApplication.getChildren();
				if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
					window = (MWindow) windows.get(0);
				}
			}
			
			// instantiate the perspective
			WorkbenchPage workbenchPage = (WorkbenchPage) PlatformUI.getWorkbench()
				.getActiveWorkbenchWindow().getActivePage();
			
			ModeledPageLayout modelLayout = new ModeledPageLayout(window, modelService, partService,
				mPerspective, pd, workbenchPage, true);
			
			IPageLayout pLayout = modelLayout;
			pLayout.setEditorAreaVisible(false);
			pLayout.setFixed(false);
			
			// fastviews
			IMemento fastViews = memento.getChild(IWorkbenchConstants.TAG_FAST_VIEWS);
			if (fastViews != null) {
				
				for (IMemento fastViewTags : fastViews.getChildren(IWorkbenchConstants.TAG_VIEW)) {
					fastViewIds.add(fastViewTags.getString(IWorkbenchConstants.TAG_ID));
				}
			}
			
			IMemento mainWindow = layout.getChild(IWorkbenchConstants.TAG_MAIN_WINDOW);
			
			// Read the info elements.
			IMemento[] children = mainWindow.getChildren(IWorkbenchConstants.TAG_INFO);
			
			for (int i = 0; i < children.length; i++) {
				// Get the info details.
				IMemento childMem = children[i];
				
				String partID = childMem.getString(IWorkbenchConstants.TAG_PART);
				String refId = childMem.getString(IWorkbenchConstants.TAG_RELATIVE);
				Float ratio1 = childMem.getFloat(IWorkbenchConstants.TAG_RATIO);
				Integer relationShip = childMem.getInteger(IWorkbenchConstants.TAG_RELATIONSHIP);
				IFolderLayout folder1 = pLayout.createFolder(partID,
					relationShip != null ? relationShip : IPageLayout.LEFT,
					ratio1 != null ? ratio1 : 1.0f,
					refId != null ? refId : IPageLayout.ID_EDITOR_AREA);
				
				// Populate the stack
				IMemento folderData = childMem.getChild(IWorkbenchConstants.TAG_FOLDER);
				if (folderData != null) {
					IMemento[] tabs = folderData.getChildren(IWorkbenchConstants.TAG_PAGE);
					for (int j = 0; j < tabs.length; j++) {
						IMemento tab = tabs[j];
						String tagId = tab.getString(IWorkbenchConstants.TAG_CONTENT);
						
						if (!fastViewIds.contains(tagId)) {
							folder1.addView(tagId);
						}
						
						tab.getString(IWorkbenchConstants.TAG_LABEL);
					}
				}
			}
			
			PerspectiveTagger.tagPerspective(mPerspective, modelService);
			PerspectiveExtensionReader extReader = new PerspectiveExtensionReader();
			extReader.extendLayout(workbenchPage.getExtensionTracker(), id, modelLayout);
			
		}
		catch (WorkbenchException e) {
			throw new IOException(e);
		}
		return fastViewIds;
		
	}
	
	private static Object genericInvokMethod(Object obj, String methodName, Class<?> clazz1,
		Class<?> clazz2, Object param1, Object param2){
		Method method;
		Object requiredObj = null;
		Object[] parameters = new Object[] {
			param1, param2
		};
		Class<?>[] classArray = new Class<?>[] {
			clazz1, clazz2
		};
		
		try {
			method = obj.getClass().getDeclaredMethod(methodName, classArray);
			method.setAccessible(true);
			requiredObj = method.invoke(obj, parameters);
		} catch (Exception e) {
			LoggerFactory.getLogger(PerspectiveImportService.class).error("generic error", e);
		}
		
		return requiredObj;
	}
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
}
