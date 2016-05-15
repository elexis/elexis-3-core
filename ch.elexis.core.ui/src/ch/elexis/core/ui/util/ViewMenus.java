/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - Declarative access to the contextMenu
 *    
 *******************************************************************************/

package ch.elexis.core.ui.util;

import org.eclipse.jface.action.GroupMarker;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IContributionItem;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.IToolBarManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.IWorkbenchActionConstants;

import ch.elexis.core.ui.actions.RestrictedAction;

/**
 * This class simplifies the handling of menus and toolbars.
 * 
 * @author gerry
 * 
 */
public class ViewMenus {
	IViewSite site;
	MenuManager contextMenu = null;
	
	// IAction[] actions;
	public ViewMenus(IViewSite s){
		site = s;
		
	}
	
	/**
	 * Create a menu containing the specified actions.
	 * 
	 * @param actions
	 *            a collection of actions and null-values (that represent separators)
	 */
	public void createMenu(IAction... actions){
		IMenuManager mgr = site.getActionBars().getMenuManager();
		mgr.addMenuListener(new IMenuListener() {
			@Override
			public void menuAboutToShow(IMenuManager manager){
				for (IAction iAction : actions) {
					if(iAction instanceof RestrictedAction) {
						((RestrictedAction) iAction).reflectRight();
					}
				}
				// update the UI
				IContributionItem[] items = manager.getItems();
				for (IContributionItem iContributionItem : items) {
					iContributionItem.update();
				}
			}
		});
		for (IAction ac : actions) {
			if (ac == null) {
				mgr.add(new Separator());
			} else {
				mgr.add(ac);
			}
		}
	}
	
	/**
	 * Create a toolbar containing the specified actions
	 * 
	 * @param actions
	 *            a collection of actions and null-values (that represent separators)
	 */
	public void createToolbar(IAction... actions){
		IToolBarManager tmg = site.getActionBars().getToolBarManager();
		for (IAction ac : actions) {
			if (ac == null) {
				tmg.add(new Separator());
			} else {
				tmg.add(ac);
			}
		}
	}
	
	/**
	 * Attach a context menu to a org.eclipse.jface.StructuredViewer
	 * 
	 * @param viewer
	 *            the viewer
	 * @param actions
	 *            the actions to use
	 */
	public void createViewerContextMenu(StructuredViewer viewer, final IAction... actions){
		MenuManager menuMgr = new MenuManager();
		menuMgr.setRemoveAllWhenShown(true);
		menuMgr.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				fillContextMenu(manager, actions);
			}
		});
		Menu menu = menuMgr.createContextMenu(viewer.getControl());
		viewer.getControl().setMenu(menu);
		
		site.registerContextMenu(menuMgr, viewer);
	}
	
	/**
	 * Creates a menu for the given control containing the given actions
	 * 
	 * @param control
	 *            the Control to add the menu to
	 * @param actions
	 *            the actions to be shown in the menu
	 */
	public void createControlContextMenu(Control control, final IAction... actions){
		contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				for (IAction iAction : actions) {
					if(iAction instanceof RestrictedAction) {
						((RestrictedAction) iAction).reflectRight();
					}
				}
				fillContextMenu(manager, actions);
			}
		});
		Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);
	}
	
	/**
	 * Return the context menu for registration with the selectionProvider
	 * 
	 * @return MenuManager for the contextMenu
	 */
	public MenuManager getContextMenu(){
		return contextMenu;
	}
	
	/**
	 * Creates a menu for the given Control that will be populated by the provided populator This
	 * can be used to construct dynamic menus that change contents depending of state.
	 */
	public void createControlContextMenu(Control control, final IMenuPopulator populator){
		contextMenu = new MenuManager();
		contextMenu.setRemoveAllWhenShown(true);
		contextMenu.addMenuListener(new IMenuListener() {
			public void menuAboutToShow(IMenuManager manager){
				for (IAction ac : populator.fillMenu()) {
					if (ac == null) {
						contextMenu.add(new Separator());
					} else {
						contextMenu.add(ac);
					}
				}
			}
		});
		Menu menu = contextMenu.createContextMenu(control);
		control.setMenu(menu);
	}
	
	protected void fillContextMenu(IMenuManager manager, IAction... actions){
		manager.add(new GroupMarker(IWorkbenchActionConstants.MB_ADDITIONS));
		for (IAction ac : actions) {
			if (ac == null) {
				manager.add(new Separator());
			} else {
				if (ac instanceof RestrictedAction) {
					((RestrictedAction) ac).reflectRight();
				}
				manager.add(ac);
			}
		}
	}
	
	public static interface IMenuPopulator {
		public IAction[] fillMenu();
	};
}
