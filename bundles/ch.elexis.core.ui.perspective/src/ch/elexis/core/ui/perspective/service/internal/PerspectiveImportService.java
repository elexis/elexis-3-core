package ch.elexis.core.ui.perspective.service.internal;

import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.Method;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.eclipse.e4.ui.internal.workbench.E4XMIResourceFactory;
import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MSnippetContainer;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspectiveStack;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
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
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.WorkbenchException;
import org.eclipse.ui.XMLMemento;
import org.eclipse.ui.internal.IWorkbenchConstants;
import org.eclipse.ui.internal.PerspectiveExtensionReader;
import org.eclipse.ui.internal.PerspectiveTagger;
import org.eclipse.ui.internal.WorkbenchPage;
import org.eclipse.ui.internal.e4.compatibility.ModeledPageLayout;
import org.eclipse.ui.internal.menus.MenuHelper;
import org.eclipse.ui.internal.registry.PerspectiveDescriptor;
import org.eclipse.ui.internal.registry.PerspectiveRegistry;
import org.osgi.service.component.annotations.Component;
import org.slf4j.LoggerFactory;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.perspective.service.IPerspectiveImportService;
import ch.elexis.core.ui.perspective.service.IStateCallback;
import ch.elexis.core.ui.perspective.service.IStateCallback.State;

@Component
public class PerspectiveImportService implements IPerspectiveImportService {

	@Override
	public IPerspectiveDescriptor importPerspective(String uri, IStateCallback iStateHandle,
			boolean openPerspectiveIfAdded) {

		try {
			if (uri != null) {
				if (uri.toLowerCase().startsWith("http")) { //$NON-NLS-1$
					// import from web
					InputStream in = new URL(uri).openStream();
					return importPerspectiveFromStream(in, iStateHandle, openPerspectiveIfAdded);
				} else if (uri.toLowerCase().endsWith("xmi")) { //$NON-NLS-1$
					// import from file
					File f = new File(uri);
					if (f != null && f.exists()) {
						InputStream in = FileUtils.openInputStream(f);
						return importPerspectiveFromStream(in, iStateHandle, openPerspectiveIfAdded);
					}
				}

			}
		} catch (IOException e) {
			LoggerFactory.getLogger(PerspectiveImportService.class).error("cannot import perspective at: " + uri, e); //$NON-NLS-1$
		}
		return null;
	}

	private void updateCloseablePlaceholder(MPerspective mPerspective) {
		EModelService modelService = getService(EModelService.class);
		List<MPlaceholder> foundMPlaceholders = modelService.findElements(mPerspective, null, MPlaceholder.class,
				null);
		for (MPlaceholder mPlaceholder : foundMPlaceholders) {
			// set closeable true
			if (!mPlaceholder.isCloseable()) {
				mPlaceholder.setCloseable(true);
			}
		}
	}

	@SuppressWarnings("restriction")
	private IPerspectiveDescriptor importPerspectiveFromStream(InputStream in, IStateCallback iStateHandle,
			boolean openPerspectiveIfAdded) throws IOException {
		MPerspective mPerspective = loadPerspectiveFromStream(in);
		if (mPerspective != null) {
			updateCloseablePlaceholder(mPerspective);
			IPerspectiveRegistry iPerspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();

			// the perspective id to import
			String id = mPerspective.getElementId();
			IPerspectiveDescriptor existingPerspectiveDescriptor = iPerspectiveRegistry.findPerspectiveWithId(id);

			// the active perspective id
			String activePerspectiveId = getActivePerspectiveId();

			// check if the import should be done
			if (existingPerspectiveDescriptor == null || iStateHandle == null || iStateHandle.state(State.OVERRIDE)) {

				IPerspectiveDescriptor activePd = iPerspectiveRegistry.findPerspectiveWithId(activePerspectiveId);

				// delete if a perspective with the id already exists
				int idx = deletePerspective(id);

				// add the new perspective to the registry
				((PerspectiveRegistry) iPerspectiveRegistry).addPerspective(mPerspective);
				IPerspectiveDescriptor createdPd = iPerspectiveRegistry.findPerspectiveWithId(id);
				if (createdPd != null) {
					((PerspectiveDescriptor) createdPd).setHasCustomDefinition(false); // no original descriptor should
																						// exists
				}
				// check if the new perspective should be opened
				if (idx > -1 || openPerspectiveIfAdded) {
					openPerspective(createdPd);
					// there was already an opened active perspective switch back to it
					openPerspective(activePd);
				}
				return createdPd;
			}

		}
		return null;
	}

	@Override
	public void openPerspective(IPerspectiveDescriptor iPerspectiveDescriptor) {
		try {
			EPartService partService = PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getService(EPartService.class);
			GlobalActions.addModelOfParts(partService);

			IWorkbenchWindow win = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			PlatformUI.getWorkbench().showPerspective(iPerspectiveDescriptor.getId(), win);

			// update fixedLayout
			boolean value = ConfigServiceHolder.getUser(Preferences.USR_FIX_LAYOUT, false);
			ContextServiceHolder.get().getRootContext().setNamed(Preferences.USR_FIX_LAYOUT, !value);
			ContextServiceHolder.get().getRootContext().setNamed(Preferences.USR_FIX_LAYOUT, value);
		} catch (WorkbenchException e) {
			LoggerFactory.getLogger(PerspectiveImportService.class).error("cannot open perspective [{}]", //$NON-NLS-1$
					iPerspectiveDescriptor.getId(), e);
		}
	}

	public int closePerspective(IPerspectiveDescriptor iPerspectiveDescriptor) {
		int idx = isPerspectiveInsideStack(iPerspectiveDescriptor);
		if (idx > -1) {
			PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
					.closePerspective(iPerspectiveDescriptor, true, false);
		}
		return idx;
	}

	@Override
	public int deletePerspective(String perspectiveId) {
		IPerspectiveRegistry iPerspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		MApplication mApplication = getService(MApplication.class);
		IPerspectiveDescriptor existingPerspectiveDescriptor = iPerspectiveRegistry
				.findPerspectiveWithId(perspectiveId);

		int idx = -1;

		if (existingPerspectiveDescriptor != null) {

			idx = closePerspective(existingPerspectiveDescriptor);
			// NOT WORKING IF PERSPECTIVE IS PREDEFINED - workaround with generics
			iPerspectiveRegistry.deletePerspective(existingPerspectiveDescriptor);
			PerspectiveImportService.genericInvokMethod(iPerspectiveRegistry, "removeSnippet", MSnippetContainer.class, //$NON-NLS-1$
					String.class, mApplication, existingPerspectiveDescriptor.getId());

		}
		return idx;
	}

	public int isPerspectiveInStack(String perspectiveId) {
		IPerspectiveRegistry iPerspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();
		return isPerspectiveInsideStack(iPerspectiveRegistry.findPerspectiveWithId(perspectiveId));
	}

	private int isPerspectiveInsideStack(IPerspectiveDescriptor pd) {
		int idx = -1;
		if (pd != null) {
			List<MPerspective> perspectivesInStack;
			MPerspectiveStack mPerspectiveStack = getPerspectiveStack();
			perspectivesInStack = mPerspectiveStack.getChildren();

			for (MPerspective perspectiveInStack : perspectivesInStack) {
				if (pd.getId().equals(perspectiveInStack.getElementId())) {
					idx++;
					break;
				}
			}
		}
		return idx;
	}

	@SuppressWarnings("restriction")
	@Override
	public MPerspective loadPerspectiveFromStream(InputStream in) throws IOException {
		try {
			Resource resource = new E4XMIResourceFactory().createResource(null);
			resource.load(in, null);
			if (!resource.getContents().isEmpty()) {
				MPerspective loadedPerspective = (MPerspective) resource.getContents().get(0);
				return loadedPerspective;
			}
			return null;
		} finally {
			IOUtils.closeQuietly(in);
		}
	}

	private MPerspective getActivePerspective() {
		EModelService modelService = getService(EModelService.class);
		MTrimmedWindow mWindow = getActiveWindow();
		if (mWindow != null) {
			return modelService.getActivePerspective(mWindow);
		}
		return null;
	}

	@Override
	public MTrimmedWindow getActiveWindow() {
		EModelService modelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);

		MTrimmedWindow mWindow = (MTrimmedWindow) modelService.find("IDEWindow", mApplication); //$NON-NLS-1$
		if (mWindow == null) {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				mWindow = (MTrimmedWindow) windows.get(0);
			}
		}
		return mWindow;
	}

	private String getActivePerspectiveId() {
		MPerspective mPerspective = getActivePerspective();
		if (mPerspective != null) {
			return mPerspective.getElementId();
		}
		return null;
	}

	private MPerspectiveStack getPerspectiveStack() {
		EModelService modelService = getService(EModelService.class);
		MWindow window = getActiveWindow();
		List<MPerspectiveStack> theStack = modelService.findElements(window, null, MPerspectiveStack.class, null);
		if (!theStack.isEmpty()) {
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
	public List<String> createPerspectiveFromLegacy(String path, MPerspective mPerspective) throws IOException {
		List<String> fastViewIds = new ArrayList<>();
		IPerspectiveRegistry iPerspectiveRegistry = PlatformUI.getWorkbench().getPerspectiveRegistry();

		try (FileReader reader = new FileReader(new File(path))) {
			XMLMemento memento = XMLMemento.createReadRoot(reader);

			String label = memento.getChild("descriptor").getString("label"); //$NON-NLS-1$ //$NON-NLS-2$
			String id = memento.getChild("descriptor").getString("id"); //$NON-NLS-1$ //$NON-NLS-2$
			String descriptorId = memento.getChild("descriptor").getString("descriptor"); //$NON-NLS-1$ //$NON-NLS-2$
			PerspectiveDescriptor pd = new PerspectiveDescriptor(id, label, null);

			// sets the icon of the org descriptor to the new descriptor
			if (descriptorId != null) {
				IPerspectiveDescriptor orgPd = iPerspectiveRegistry.findPerspectiveWithId(descriptorId);
				if (orgPd != null) {
					pd.setImageDescriptor(orgPd.getImageDescriptor());
					mPerspective.setIconURI(MenuHelper.getImageUrl(orgPd.getImageDescriptor()));
				}
			}
			mPerspective.setLabel(label);
			mPerspective.setElementId(id);

			IMemento layout = memento.getChild(IWorkbenchConstants.TAG_LAYOUT);

			EPartService partService = getService(EPartService.class);
			EModelService modelService = getService(EModelService.class);
			MApplication mApplication = getService(MApplication.class);
			MWindow window = (MWindow) modelService.find("IDEWindow", mApplication); //$NON-NLS-1$
			if (window == null) {
				List<MWindow> windows = mApplication.getChildren();
				if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
					window = (MWindow) windows.get(0);
				}
			}

			// instantiate the perspective
			WorkbenchPage workbenchPage = (WorkbenchPage) PlatformUI.getWorkbench().getActiveWorkbenchWindow()
					.getActivePage();

			ModeledPageLayout modelLayout = new ModeledPageLayout(window, modelService, partService, mPerspective, pd,
					workbenchPage, true);

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
						relationShip != null ? relationShip : IPageLayout.LEFT, ratio1 != null ? ratio1 : 1.0f,
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

		} catch (WorkbenchException e) {
			throw new IOException(e);
		}
		return fastViewIds;

	}

	private static Object genericInvokMethod(Object obj, String methodName, Class<?> clazz1, Class<?> clazz2,
			Object param1, Object param2) {
		Method method;
		Object requiredObj = null;
		Object[] parameters = new Object[] { param1, param2 };
		Class<?>[] classArray = new Class<?>[] { clazz1, clazz2 };

		try {
			method = obj.getClass().getDeclaredMethod(methodName, classArray);
			method.setAccessible(true);
			requiredObj = method.invoke(obj, parameters);
		} catch (Exception e) {
			LoggerFactory.getLogger(PerspectiveImportService.class).error("generic error", e); //$NON-NLS-1$
		}

		return requiredObj;
	}

	private static <T> T getService(final Class<T> clazz) {
		if (PlatformUI.getWorkbench().getActiveWorkbenchWindow() != null) {
			return PlatformUI.getWorkbench().getActiveWorkbenchWindow().getService(clazz);
		}
		return PlatformUI.getWorkbench().getService(clazz);
	}

	@SuppressWarnings("restriction")
	@Override
	public void savePerspectiveAs(String perspectiveId, String newName) {
		EModelService modelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		PerspectiveRegistry perspectiveRegistry = (PerspectiveRegistry) PlatformUI.getWorkbench()
				.getPerspectiveRegistry();
		PerspectiveDescriptor existingPerspectiveDescriptor = (PerspectiveDescriptor) perspectiveRegistry
				.findPerspectiveWithId(perspectiveId);
		if (existingPerspectiveDescriptor != null) {

			int idx = isPerspectiveInsideStack(existingPerspectiveDescriptor);

			// loads the mapplication from the orginal descriptor
			openPerspective(existingPerspectiveDescriptor);

			// the model must be loaded
			List<MPerspective> modelPerspective = modelService.findElements(mApplication,
					existingPerspectiveDescriptor.getId(), MPerspective.class, null);

			// check if the model is loaded
			if (!modelPerspective.isEmpty()) {
				// create a new pd
				PerspectiveDescriptor newPd = perspectiveRegistry.createPerspective(newName,
						existingPerspectiveDescriptor);

				// saves an opens the new perspective
				PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().savePerspectiveAs(newPd);

				// close the new created one
				closePerspective(newPd);

				if (idx > -1) {
					// opens the original descriptor if it was already opened
					openPerspective(existingPerspectiveDescriptor);
				}
			}

		}
	}
}
