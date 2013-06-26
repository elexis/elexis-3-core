/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.util.List;
import java.util.Vector;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuCreator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.util.Log;

public class LaborOrderPulldownMenuCreator implements IMenuCreator {
	private final String LAB_ORDER_SELECTED_ACTION_ID = "ch.elexis.LaborOrder.selectedId";
	private static Log log = Log.get("LaborOrderPulldownMenuCreator"); //$NON-NLS-1$
	
	List<IAction> actions = new Vector<IAction>();
	Menu menu = null;
	IAction selectedAction = null;
	
	public LaborOrderPulldownMenuCreator(final Shell shell){
		super();
		init(shell);
	}
	
	@SuppressWarnings("unchecked")
	private void init(final Shell shell){
		List<IAction> orderActions =
			Extensions.getClasses(
				Extensions.getExtensions("ch.elexis.LaborOrder"), "ToolbarAction", //$NON-NLS-1$ //$NON-NLS-2$
				false);
		for (IAction action : orderActions) {
			if (action.getId() != null && action.getImageDescriptor() != null
				&& action.getText() != null) {
				this.actions.add(action);
			} else {
				log.log(MessageFormat.format(
					"Missing #id, #imagedescriptor or #text for LaborOrder action: {0}",
					action.getText()), Log.WARNINGS);
			}
		}
		if (this.actions != null && this.actions.size() > 0) {
			String selectedId = CoreHub.localCfg.get(LAB_ORDER_SELECTED_ACTION_ID, null);
			if (selectedId != null) {
				for (IAction action : this.actions) {
					if (selectedId.equals(action.getId())) {
						this.selectedAction = action;
					}
				}
			}
			if (this.selectedAction == null) {
				this.selectedAction = this.actions.get(0);
			}
		}
	}
	
	public IAction getSelected(){
		return this.selectedAction;
	}
	
	@Override
	public void dispose(){
		if (this.menu != null) {
			this.menu.dispose();
		}
	}
	
	@Override
	public Menu getMenu(Menu parent){
		return null;
	}
	
	/**
	 * Pulldown menu wird anhand selection angepasst
	 * 
	 * @param parent
	 * @param action
	 * @param image
	 */
	private void select(final Control parent, final IAction action, final Image image){
		if (parent instanceof ToolBar) {
			ToolBar toolBar = (ToolBar) parent;
			if (toolBar.getItemCount() > 0) {
				ToolItem toolItem = toolBar.getItem(0);
				toolItem.setImage(image);
				toolItem.setHotImage(image);
				toolItem.setToolTipText(action.getToolTipText());
				
				this.selectedAction = action;
				CoreHub.localCfg.set(LAB_ORDER_SELECTED_ACTION_ID, this.selectedAction.getId());
			}
		}
	}
	
	@Override
	public Menu getMenu(final Control parent){
		if (this.menu == null) {
			this.menu = new Menu(parent);
			for (final IAction action : this.actions) {
				final MenuItem menuItem = new MenuItem(this.menu, SWT.PUSH);
				final Image image = action.getImageDescriptor().createImage();
				menuItem.setImage(image);
				menuItem.setText(action.getText());
				
				// Add listeners
				menuItem.addSelectionListener(new SelectionAdapter() {
					@Override
					public void widgetSelected(SelectionEvent e){
						select(parent, action, image);
						action.run();
					}
				});
			}
			
		}
		
		return this.menu;
	}
	
	/**
	 * Returns action to the pulldown button
	 * 
	 * @return
	 */
	public IAction getAction(){
		int buttonStyle = IAction.AS_DROP_DOWN_MENU;
		if (actions.size() == 1) {
			buttonStyle = IAction.AS_PUSH_BUTTON;
		}
		IAction dropDownAction = new Action("Dropdown", buttonStyle) {
			@Override
			public void run(){
				getSelected().run();
			}
		};
		dropDownAction.setMenuCreator(this);
		return dropDownAction;
	}
	
}
