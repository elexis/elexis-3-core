package ch.elexis.core.ui.compatibility;

import java.util.List;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.MUIElement;
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

import ch.elexis.core.ui.UiDesk;

/**
 * since e4 doesnt support fastviews thats we need our own fastview implementation
 * 
 * @author med1
 *
 */
public class ElexisFastViewUtil {
	
	private static final String ELEXIS_FASTVIEW_STACK = "fastview.elexis";
	
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
			Object obj = eModelService.find(perspectiveId, mApplication);
			if (obj instanceof MPerspective) {
				MPerspective mPerspective = (MPerspective) obj;
				// check if fastview stack exists
				MPartStack stack =
					(MPartStack) eModelService.find(ELEXIS_FASTVIEW_STACK, mPerspective);
				
				if (stack == null) {
					stack = createFastViewStack(window, mPerspective, eModelService);
				}
				if (stack != null) {
					// check if toolcontrol exists
					MToolControl toolControl = (MToolControl) eModelService
						.find(getToolControlId(window, mPerspective), mApplication);
					
					if (toolControl == null) {
						MTrimBar mTrimBar = eModelService.getTrim(window, SideValue.BOTTOM);
						if (toolControl == null) {
							toolControl = createFastViewToolControl(window, mPerspective,
								eModelService, mTrimBar);
						}
					}
					if (toolControl != null
						&& !ElexisFastViewUtil.isViewInsideFastview(stack, viewId)
						&& mApplication.getContext().getActiveChild() != null) {
						EPartService partService = getService(EPartService.class);
						MPlaceholder placeholder = partService.createSharedPart(viewId);
						placeholder.setToBeRendered(true);
						placeholder.setElementId(viewId);
						placeholder.setCloseable(true);
						placeholder.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
						((MPart) placeholder.getRef()).setToBeRendered(true);
						stack.getChildren().add(placeholder); // Add part to stack
					}
					
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
			UiDesk.asyncExec(new Runnable() {
				public void run(){
					for (String viewId : viewIds) {
						ElexisFastViewUtil.addToFastView(perspectiveId, viewId);
					}
				}
			});
		}
	}
	
	private static MPartStack createFastViewStack(MTrimmedWindow window, MPerspective mPerspective,
		EModelService eModelService){
		
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
			return mPartStack;
		}
		return null;
	}
	
	private static MToolControl createFastViewToolControl(MTrimmedWindow window,
		MPerspective mPerspective, EModelService eModelService, MTrimBar mTrimBar){
		if (mTrimBar != null) {
			MToolControl mToolControl = eModelService.createModelElement(MToolControl.class);
			mToolControl.setElementId(getToolControlId(window, mPerspective));
			mToolControl.setContributionURI(
				"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack");
			mToolControl.setToBeRendered(true);
			mToolControl.setVisible(true);
			mToolControl.getTags().add("TrimStack");
			mToolControl.getPersistedState().put("YSize", "600");
			mTrimBar.getChildren().add(0, mToolControl);
			mTrimBar.setVisible(true);
			mTrimBar.setToBeRendered(true);
			
			return mToolControl;
		}
		return null;
	}
	
	private static String getToolControlId(MTrimmedWindow window, MPerspective mPerspective){
		return getToolControlId(window, mPerspective.getElementId());
	}
	
	private static String getToolControlId(MUIElement window, String perspectiveId){
		return ELEXIS_FASTVIEW_STACK + "("
			+ perspectiveId + ")";
	}
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
	
	private static boolean isViewInsideFastview(MPartStack stack, String placeholderId){
		if (stack != null) {
			for (MStackElement stackElement : stack.getChildren()) {
				if (stackElement.getElementId().equals(placeholderId)) {
					return true;
				}
			}
		}
		return false;
	}

	/**
	 * Registers a perspective listener
	 */
	public static void registerPerspectiveListener(){
		IWorkbenchWindow workbenchWindow = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		if (workbenchWindow != null) {
			workbenchWindow.addPerspectiveListener(new PerspectiveAdapter() {
				
				@Override
				public void perspectiveChanged(IWorkbenchPage page,
					IPerspectiveDescriptor perspective, String changeId){
					super.perspectiveChanged(page, perspective, changeId);
					// if perspective reset is complete
					if (IWorkbenchPage.CHANGE_RESET_COMPLETE.equals(changeId)) {
						UiDesk.asyncExec(new Runnable() {
							@Override
							public void run(){
								changeFastViewBarFromLeftToBottom();
							}
						});
					}
				}
				
				@Override
				public void perspectiveActivated(IWorkbenchPage page,
					IPerspectiveDescriptor perspective){
					UiDesk.asyncExec(new Runnable() {
						
						@Override
						public void run(){
							changeFastViewBarFromLeftToBottom();
						}
					});
					
				}
			});
		}
	}
	
	private static MToolControl findFastViewToolControl(EModelService eModelService,
		MTrimmedWindow workbenchWindow, String perspectiveId, SideValue sideValue){
		if (workbenchWindow != null) {
			MTrimBar trimbar = findTrimBar(eModelService, workbenchWindow, sideValue);
			if (trimbar != null) {
				MToolControl toolControl = (MToolControl) eModelService
					.find(getToolControlId(workbenchWindow, perspectiveId), trimbar);
				if (toolControl == null && workbenchWindow.getElementId() != null) {
					// it also can be that the main view id is also a part of the stack
					toolControl = (MToolControl) eModelService.find(ELEXIS_FASTVIEW_STACK + "("
						+ workbenchWindow.getElementId() + ").(" + perspectiveId + ")", trimbar);
					if (toolControl != null) {
						toolControl.setElementId(getToolControlId(workbenchWindow, perspectiveId));
					}
				}
				return toolControl;
			}
		}
		return null;
	}
	
	private static MTrimBar findTrimBar(EModelService eModelService, MTrimmedWindow workbenchWindow,
		SideValue sideValue){
		if (workbenchWindow != null) {
			MTrimBar trimbar = eModelService.getTrim(workbenchWindow, sideValue);
			return trimbar;
		}
		return null;
	}
	
	/**
	 * Changes the fastviews position from left trimbar to bottom trimbar
	 */
	public static void changeFastViewBarFromLeftToBottom(){
		EModelService eModelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		MTrimmedWindow mWindow = getCurrentWindow(eModelService, mApplication);
		if (mWindow != null) {
			MPerspective mPerspective = eModelService.getActivePerspective(mWindow);
			
			if (mPerspective != null) {
				String perspectiveId = mPerspective.getElementId();
				
				MToolControl mToolControl =
					findFastViewToolControl(eModelService, mWindow, perspectiveId,
						SideValue.BOTTOM);
				if (mToolControl == null) {
					mToolControl = findFastViewToolControl(eModelService, mWindow, perspectiveId,
						SideValue.LEFT);
					if (mToolControl != null) {
						MTrimBar trimBarBottom =
							findTrimBar(eModelService, mWindow, SideValue.BOTTOM);
						if (trimBarBottom != null) {
							MToolControl copyToolcontrol =
								eModelService.createModelElement(MToolControl.class);
							copyToolcontrol.setElementId(mToolControl.getElementId());
							copyToolcontrol.setContributionURI(
								"bundleclass://org.eclipse.e4.ui.workbench.addons.swt/org.eclipse.e4.ui.workbench.addons.minmax.TrimStack");
							copyToolcontrol.setToBeRendered(true);
							copyToolcontrol.setVisible(true);
							copyToolcontrol.getTags().add("TrimStack");
							
							copyToolcontrol.getPersistedState().put("YSize", "600");
							trimBarBottom.getChildren().add(0, copyToolcontrol);
							
							mToolControl.setToBeRendered(false);
							mToolControl.setVisible(false);
							mToolControl.getParent().getChildren().remove(mToolControl);
							mToolControl.setParent(null);
							trimBarBottom.setVisible(true);
							trimBarBottom.setToBeRendered(true);
						}
						
					}
				}
				else {
					mToolControl.setToBeRendered(true);
				}
			}
		}
	}
}
