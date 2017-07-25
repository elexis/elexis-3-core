package ch.elexis.core.ui.renderer;

import org.eclipse.e4.ui.model.application.MApplication;
import org.eclipse.e4.ui.model.application.ui.advanced.MPlaceholder;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.basic.MPartStack;
import org.eclipse.e4.ui.model.application.ui.basic.MStackElement;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.e4.ui.workbench.modeling.EPartService;
import org.eclipse.e4.ui.workbench.renderers.swt.StackRenderer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.PlatformUI;

@SuppressWarnings("restriction")
public class ElexisRenderer extends StackRenderer {
	public static final String ELEXIS_FASTVIEW_STACK = "my.minimized.parts";
	
	@Override
	protected void populateTabMenu(final Menu menu, MPart part){
		super.populateTabMenu(menu, part);
		createFastViewMenuItem(menu, part);
	}
	
	private MStackElement getStackChildElementById(String placeholderId){
		EModelService eModelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		
		MPartStack stack = (MPartStack) eModelService.find(ELEXIS_FASTVIEW_STACK, mApplication);
		if (stack != null) {
			for (MStackElement stackElement : stack.getChildren()) {
				if (stackElement.getElementId().equals(placeholderId)) {
					return stackElement;
				}
			}
		}
		return null;
	}
	
	private static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}

	private void createFastViewMenuItem(final Menu menu, MPart part){
		EModelService eModelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		EPartService ePartService = getService(EPartService.class);
		
		MPartStack stack = (MPartStack) eModelService.find(ELEXIS_FASTVIEW_STACK, mApplication);
		if (stack != null) {
			// e4 doesnt support fastviews thats we need our own fastview menu
			MenuItem menuItemClose = new MenuItem(menu, SWT.NONE);
			menuItemClose.setText("Fast View");
			
			menuItemClose.setEnabled(getStackChildElementById("ph_" + part.getElementId()) == null);
			menuItemClose.addSelectionListener(new SelectionAdapter() {
				@Override
				public void widgetSelected(SelectionEvent e){
					MPart part = (MPart) menu.getData("stack_selected_part");
					
					// save and close the current view
					if (ePartService.savePart(part, true)) {
						String id = part.getElementId();
						ePartService.hidePart(part, true);
						
						MPartStack stack =
							(MPartStack) eModelService.find(ELEXIS_FASTVIEW_STACK, mApplication);
						
						if (getStackChildElementById("ph_" + part.getElementId()) == null) {
							MPlaceholder placeholder =
								eModelService.createModelElement(MPlaceholder.class);
							placeholder.setElementId("ph_" + id);
							placeholder.setCloseable(true);
							placeholder.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
							placeholder.setRef(part);


							stack.getChildren().add(placeholder); // Add part to stack
						}
					}
					
				}
			});
		}
	}
}
