package ch.elexis.core.ui.e4.parts;

import java.util.List;

import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectMenuItem;
import org.eclipse.e4.ui.model.application.ui.menu.MDirectToolItem;
import org.eclipse.e4.ui.model.application.ui.menu.MMenu;
import org.eclipse.e4.ui.model.application.ui.menu.MToolBar;
import org.eclipse.e4.ui.services.EMenuService;
import org.eclipse.e4.ui.workbench.modeling.EModelService;
import org.eclipse.jface.action.ActionContributionItem;
import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IWorkbenchActionConstants;

import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.util.ViewMenus;

public class PartMenus {
	
	public MenuManager createViewerContextMenu(EMenuService menuService, String iD, Control control){
		MenuManager menuManager = new MenuManager();
		Menu contextMenu = menuManager.createContextMenu(control);
		control.setMenu(contextMenu);
		menuService.registerContextMenu(control, iD + ".control");
		return menuManager;
	}
	
	public void createViewerContextMenu(EMenuService menuService, String iD, Control control,
		IAction... actions){
		List<IContributionItem> contributionItems =
			ViewMenus.convertActionsToContributionItems(actions);
		
		MenuManager menuManager = createViewerContextMenu(menuService, iD, control);
		menuManager.addMenuListener(new IMenuListener() {
			
			@Override
			public void menuAboutToShow(IMenuManager manager){
				fillContextMenu(manager, contributionItems);
			}
		});
	}
	
	private void fillContextMenu(IMenuManager manager, List<IContributionItem> contributionItems){
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		for (IContributionItem contributionItem : contributionItems) {
			if (contributionItem == null) {
				manager.add(new Separator());
				continue;
			} else if (contributionItem instanceof ActionContributionItem) {
				ActionContributionItem ac = (ActionContributionItem) contributionItem;
				if (ac.getAction() instanceof RestrictedAction) {
					((RestrictedAction) ac.getAction()).reflectRight();
				}
			}
			contributionItem.update();
			manager.add(contributionItem);
		}
	}
	
	public MToolBar createToolbar(EModelService modelService, MPart mPart){
		MToolBar mBar = modelService.createModelElement(MToolBar.class);
		mBar.setElementId(mPart.getElementId() + ".toolbar");
		mPart.setToolbar(mBar);
		return mBar;
	}
	
	public void createToolbar(EModelService modelService, MPart mPart, IAction... actions){
		MToolBar mToolBar = createToolbar(modelService, mPart);
		List<IContributionItem> contributionItems =
			ViewMenus.convertActionsToContributionItems(actions);
		for (IContributionItem iContributionItem : contributionItems) {
			MDirectToolItem element = modelService.createModelElement(MDirectToolItem.class);
			// see org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil
			// might break in the future
			element.getTags().add("Opaque");
			element.getTransientData().put("OpaqueItem", iContributionItem);
			mToolBar.getChildren().add(element);
		}
	}
	
	public MMenu createViewMenu(EModelService modelService, MPart mPart){
		MMenu mMenu = modelService.createModelElement(MMenu.class);
		mMenu.setElementId(mPart.getElementId() + ".menu");
		mMenu.getTags().add("ViewMenu");
		mPart.getMenus().add(mMenu);
		return mMenu;
	}
	
	public void createViewMenu(EModelService modelService, MPart mPart, IAction... actions){
		MMenu viewMenu = createViewMenu(modelService, mPart);
		List<IContributionItem> contributionItems =
			ViewMenus.convertActionsToContributionItems(actions);
		for (IContributionItem iContributionItem : contributionItems) {
			MDirectMenuItem element = modelService.createModelElement(MDirectMenuItem.class);
			// see org.eclipse.e4.ui.internal.workbench.OpaqueElementUtil
			// might break in the future
			element.getTags().add("Opaque");
			element.getTransientData().put("OpaqueItem", iContributionItem);
			viewMenu.getChildren().add(element);
		}
	}
	
}
