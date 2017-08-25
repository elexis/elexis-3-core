package ch.elexis.core.ui.fastview;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.SideValue;
import org.eclipse.e4.ui.model.application.ui.advanced.MPerspective;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimBar;
import org.eclipse.e4.ui.model.application.ui.basic.MTrimmedWindow;
import org.eclipse.e4.ui.model.application.ui.basic.MWindow;
import org.eclipse.e4.ui.model.application.ui.menu.MToolControl;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.IPerspectiveDescriptor;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PerspectiveAdapter;
import org.eclipse.ui.PlatformUI;

/**
 * since e4 doesnt support fastviews thats we need our own fastview implementation
 * 
 * @author med1
 *
 */
public class ElexisFastViewUtil {
	
	private static final String ELEXIS_FASTVIEW_STACK = "fastview.elexis";
	private static final String ELEXIS_PLACEHOLDER_PREFIX = "placeh_";
	
	/**
	 * Adds a view to the fastview, if {@link MPartStack} not exists it will be created and the view
	 * will be added within a {@link MPlaceholder} element.
	 * 
	 * @param perspectiveId
	 * @param viewId
	 */
	public static void addToFastView(String perspectiveId, String viewId){
		EModelService eModelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		
		MTrimmedWindow window = getCurrentWindow(eModelService, mApplication);
		
		if (window != null) {
			MPerspective mPerspective =
				(MPerspective) eModelService.find(perspectiveId, mApplication);
			if (mPerspective != null) {
				// check if fastview stack exists
				MPartStack stack =
					(MPartStack) eModelService.find(ELEXIS_FASTVIEW_STACK, mPerspective);
				
				if (stack == null) {
					stack = createFastViewStack(window, mPerspective, eModelService, mApplication);
				}
				if (stack != null
					&& !ElexisFastViewUtil.isViewInsideFastview(stack, viewId)) {
					EPartService partService = getService(EPartService.class);
					MPlaceholder placeholder = partService.createSharedPart(viewId);
					placeholder.setToBeRendered(true);
					placeholder.setElementId(ELEXIS_PLACEHOLDER_PREFIX + viewId);
					placeholder.setCloseable(true);
					placeholder.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
					((MPart) placeholder.getRef()).setToBeRendered(true);
					stack.getChildren().add(placeholder); // Add part to stack
				}
			}
		}
	}

	private static MTrimmedWindow getCurrentWindow(EModelService eModelService,
		MApplication mApplication){
		MTrimmedWindow window = (MTrimmedWindow) eModelService.find("IDEWindow", mApplication);
		if (window == null) {
			List<MWindow> windows = mApplication.getChildren();
			if (!windows.isEmpty() && windows.get(0) instanceof MTrimmedWindow) {
				window = (MTrimmedWindow) windows.get(0);
			}
		}
		return window;
	}
	
	/**
	 * Creates a fastview menu item
	 * 
	 * @param menu
	 * @param part
	 */
	public static void createFastViewMenuItem(final Menu menu, MPart part){
		EModelService eModelService = ElexisFastViewUtil.getService(EModelService.class);
		MApplication mApplication = ElexisFastViewUtil.getService(MApplication.class);
		EPartService ePartService = ElexisFastViewUtil.getService(EPartService.class);
		
		MPerspective mPerspective = eModelService.getPerspectiveFor(part);
		if (mPerspective != null) {
			MPartStack stack = (MPartStack) eModelService
				.find(ElexisFastViewUtil.ELEXIS_FASTVIEW_STACK, mPerspective);
			
			MenuItem menuItemClose = new MenuItem(menu, SWT.NONE);
			menuItemClose.setText("Fast View");
			
			menuItemClose
				.setEnabled(!ElexisFastViewUtil.isViewInsideFastview(stack, part.getElementId()));
			menuItemClose.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					MPart part = (MPart) menu.getData("stack_selected_part");
					// save and close the current view
					if (ePartService.savePart(part, true)) {
						String id = part.getElementId();
						MPerspective mPerspective = ElexisFastViewUtil
							.getService(EModelService.class).getActivePerspective(ElexisFastViewUtil
								.getService(EModelService.class).getTopLevelWindowFor(part));
						ePartService.hidePart(part, true);
						ElexisFastViewUtil.addToFastView(mPerspective.getElementId(), id);
					}
				}
			});
		}
	}
	
	/**
	 * After the perspective is opened the views will be added to the fastview
	 * 
	 * @param perspectiveId
	 * @param viewIds
	 */
	public static void addToFastViewAfterPerspectiveOpened(String perspectiveId, String... viewIds){
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow != null) {
			workbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
				
				@Override
				public void perspectiveOpened(IWorkbenchPage page,
					IPerspectiveDescriptor perspective){
					super.perspectiveOpened(page, perspective);
					if (perspectiveId.equals(perspective.getId())) {
						
						for (String viewId : viewIds) {
							ElexisFastViewUtil.addToFastView(perspectiveId, viewId);
						}
						// the work is done so remove this listener
						PlatformUI.getWorkbench().getActiveWorkbenchWindow()
							.removePerspectiveListener(this);
					}
				}
			});
		}
	}
	
	private static MPartStack createFastViewStack(MTrimmedWindow window, MPerspective mPerspective,
		EModelService eModelService, MApplication mApplication){
		
		if (window != null && mPerspective != null) {
			MPartStack mPartStack = eModelService.createModelElement(MPartStack.class);
			mPartStack.setElementId(ELEXIS_FASTVIEW_STACK);
			mPartStack.setToBeRendered(true);
			mPartStack.getTags().add("Minimized");
			mPartStack.setOnTop(false);
			mPartStack.setVisible(false);
			mPartStack.getTags().add("NoAutoCollapse");
			mPartStack.getTags().add("active");
			mPerspective.getChildren().add(0, mPartStack);
			
			MToolControl mToolControl = eModelService.createModelElement(MToolControl.class);
			mToolControl.setElementId(getToolControlId(window, mPerspective));
			mToolControl.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack");
			mToolControl.setToBeRendered(true);
			mToolControl.setVisible(true);
			mToolControl.getTags().add("TrimStack");
			
			MTrimBar mTrimBar = eModelService.getTrim(window, SideValue.BOTTOM);
			mTrimBar.getChildren().add(mToolControl);
			mTrimBar.setVisible(true);
			mTrimBar.setToBeRendered(true);
			
			return mPartStack;
		}
		return null;
	}
	
	private static String getToolControlId(MTrimmedWindow window, MPerspective mPerspective){
		return ELEXIS_FASTVIEW_STACK + "(" + window.getElementId() + ").("
			+ mPerspective.getElementId() + ")";
	}
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
	
	private static boolean isViewInsideFastview(MPartStack stack, String placeholderId){
		if (stack != null) {
			for (MStackElement stackElement : stack.getChildren()) {
				if (stackElement.getElementId().equals(ELEXIS_PLACEHOLDER_PREFIX + placeholderId)) {
					return true;
				}
			}
		}
		return false;
	}
}
