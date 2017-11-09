package ch.elexis.core.ui.perspective.handler;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileReader;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MElementContainer;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.FileDialog;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IPerspectiveRegistry;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PerspectiveExtensionReader;
import org.eclipse.ui.internal.PerspectiveTagger;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.compatibility.ElexisFastViewUtil;

public class PerspektiveImportHandler extends AbstractHandler {
	
	private EPartService partService;
	private EModelService modelService;
	private MApplication mApplication;
	private MWindow mWindow;
	
	public PerspektiveImportHandler(){
		partService = getService(EPartService.class);
		modelService = getService(EModelService.class);
		mApplication = getService(MApplication.class);
		mWindow = (MTrimmedWindow) modelService.find("IDEWindow", mApplication);
		if (mWindow == null) {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				mWindow = (MTrimmedWindow) windows.get(0);
			}
		}
	}
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		try {
			
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
				if (path.toLowerCase().endsWith("xml")) {
					// legacy
					try (FileReader reader = new FileReader(new File(path))) {
						XMLMemento memento = XMLMemento.createReadRoot(reader);
						MPerspective mPerspective =
							modelService.createModelElement(MPerspective.class);
						// special handling for fastviews for legacy imports
						List<String> fastViewIds = createPerspective(memento, mPerspective);
						IPerspectiveDescriptor pd = savePerspectiveToRegistry(mPerspective);
						switchToPerspective(mPerspective, fastViewIds);
						WorkbenchPage wp = (WorkbenchPage) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow().getActivePage();
						wp.savePerspectiveAs(pd);
					}
				} else {
					Resource resource = new E4XMIResourceFactory().createResource(null);
					try (InputStream inputStream = new FileInputStream(path)) {
						resource.load(inputStream, null);
						
						if (!resource.getContents().isEmpty()) {
							MPerspective loadedPerspective =
								(MPerspective) resource.getContents().get(0);
							
							// e4 models dont need a special handling for fastviews
							importPerspective(loadedPerspective, mApplication);
							IPerspectiveDescriptor pd =
								savePerspectiveToRegistry(loadedPerspective);
							switchToPerspective(loadedPerspective, new ArrayList<>());
							WorkbenchPage wp = (WorkbenchPage) PlatformUI.getWorkbench()
								.getActiveWorkbenchWindow().getActivePage();
							wp.savePerspectiveAs(pd);
						}
					}
					
				}
			}
		} catch (Exception e) {
			e.printStackTrace();
		}
		
		return null;
	}
	
	private IPerspectiveDescriptor savePerspectiveToRegistry(MPerspective perspective){
		IPerspectiveRegistry perspectiveRegistry =
			(PerspectiveRegistry) PlatformUI.getWorkbench().getPerspectiveRegistry();
		IPerspectiveDescriptor pd =
			perspectiveRegistry.findPerspectiveWithId(perspective.getElementId());
		if (pd == null) {
			((PerspectiveRegistry) perspectiveRegistry).addPerspective(perspective);
			pd = perspectiveRegistry.findPerspectiveWithId(perspective.getElementId());
		} else {
			System.out.println("perspective descriptor already exists for perspective id: "
				+ perspective.getElementId());
		}
		
		return pd;
	}
	
	private void switchToPerspective(MPerspective loadedPerspective,
		List<String> preLoadedFastViewIds){
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
		System.out.println(loadedPerspective.getElementId());
		partService.switchPerspective(loadedPerspective);
		System.out.println(loadedPerspective.getElementId());
	}
	
	private List<String> createPerspective(IMemento memento, MPerspective mPerspective){
		List<String> fastViewIds = new ArrayList<>();
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
		WorkbenchPage workbenchPage =
			(WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();
		
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
				ratio1 != null ? ratio1 : 1.0f, refId != null ? refId : IPageLayout.ID_EDITOR_AREA);
			
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
					
					String stateVal = tab.getString(IWorkbenchConstants.TAG_LABEL);
				}
			}
		}
		
		PerspectiveTagger.tagPerspective(mPerspective, modelService);
		PerspectiveExtensionReader reader = new PerspectiveExtensionReader();
		reader.extendLayout(workbenchPage.getExtensionTracker(), id, modelLayout);
		
		return fastViewIds;
		
	}
	
	public List<String> importPerspective(MPerspective perspective, MApplication application){
		
		List<String> fastViewIds = new ArrayList<>();
		List<MPlaceholder> placeholderClones = modelService.findElements(perspective, null,
			MPlaceholder.class, null, EModelService.IN_ANY_PERSPECTIVE);
		for (MPlaceholder placeholder : placeholderClones) {
			String id = placeholder.getElementId();
			List<MPart> findElements =
				modelService.findElements(application, id, MPart.class, null);
			
			if (findElements.size() == 0) {
				
				List<MUIElement> tmps =
					modelService.findElements(application, id, MUIElement.class, null);
				
				if (tmps.isEmpty()) {
					// the application does not know this view try to open it
					try {
						PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
							.showView(id);
						tmps = modelService.findElements(application, id, MUIElement.class, null);
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
				
				for (MUIElement muiElement : tmps) {
					if (!(muiElement instanceof MPlaceholder)) {
						placeholder.setRef(muiElement);
						break;
					}
				}
				
			} else if (findElements.size() != 1) {
				throw new IllegalStateException(placeholder.getElementId() + " id is not unique");
			}
			
			else {
				placeholder.setRef(findElements.get(0));
			}
		}
		
		return fastViewIds;
	}
}
