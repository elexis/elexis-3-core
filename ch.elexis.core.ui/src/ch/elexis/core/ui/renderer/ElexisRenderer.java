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

public class ElexisRenderer extends StackRenderer {
	public static final String ELEXIS_FASTVIEW_STACK = "my.minimized.parts";
	
	public static <T> T getService(final Class<T> clazz){
		return PlatformUI.getWorkbench().getService(clazz);
	}
	
	private MStackElement getStackElementById(String placeholderId){
		EModelService modelService = getService(EModelService.class);
		MApplication mApplication = getService(MApplication.class);
		MPartStack stack = (MPartStack) modelService.find(ELEXIS_FASTVIEW_STACK, mApplication);
		
		for (MStackElement stackElement : stack.getChildren()) {
			if (stackElement.getElementId().equals(placeholderId)) {
				return stackElement;
			}
		}
		return null;
	}
	
	protected void populateTabMenu(final Menu menu, MPart part){
		super.populateTabMenu(menu, part);
		

		// e4 doesnt support fastviews thats we need our own fastview menu
		MenuItem menuItemClose = new MenuItem(menu, SWT.NONE);
		menuItemClose.setText("Fast View");
		
		menuItemClose.setEnabled(getStackElementById("ph_" + part.getElementId()) == null);
		menuItemClose.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				MPart part = (MPart) menu.getData("stack_selected_part");
				EPartService partService = getContextForParent(part).get(EPartService.class);
				
				if (partService.savePart(part, true)) {
					String id = part.getElementId();
					partService.hidePart(part, true);
					
					EModelService modelService = getService(EModelService.class);
					MApplication mApplication = getService(MApplication.class);
					EPartService ePartService = getService(EPartService.class);
					MPartStack stack =
						(MPartStack) modelService.find(ELEXIS_FASTVIEW_STACK, mApplication);
					
					if (getStackElementById("ph_" + part.getElementId()) == null) {
						MPlaceholder placeholder =
							modelService.createModelElement(MPlaceholder.class);
						placeholder.setElementId("ph_" + id);
						placeholder.setCloseable(true);
						placeholder.getTags().add(EPartService.REMOVE_ON_HIDE_TAG);
						placeholder.setRef(part);
						

						stack.getChildren().add(placeholder); // Add part to stack
						//ePartService.activate(currentPart);
						//ePartService.savePart(currentPart, false);
					}
				}
				
			}
		});
		
	}
}
