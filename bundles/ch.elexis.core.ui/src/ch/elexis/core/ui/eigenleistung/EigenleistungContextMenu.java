/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/
package ch.elexis.core.ui.eigenleistung;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.commands.EditEigenleistungUi;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.data.Eigenleistung;

public class EigenleistungContextMenu {
	private IAction deleteAction, editAction;
	CommonViewer cv;
	EigenleistungDetailDisplay add;
	EigenleistungMenuListener menuListener = new EigenleistungMenuListener();
	MenuManager menu;
	ArrayList<IAction> actions = new ArrayList<IAction>();
	
	public EigenleistungContextMenu(final Eigenleistung template, final CommonViewer cv){
		this.cv = cv;
		makeActions(template);
		actions.add(deleteAction);
		actions.add(editAction);
		menu = new MenuManager();
		menu.addMenuListener(menuListener);
		cv.setContextMenu(menu);
	}
	
	public void addAction(final IAction ac){
		actions.add(ac);
	}
	
	public void removeAction(final IAction ac){
		actions.remove(ac);
	}
	
	public EigenleistungContextMenu(final Eigenleistung template, final CommonViewer cv,
		final EigenleistungDetailDisplay add){
		this(template, cv);
		this.add = add;
	}
	
	private void makeActions(final Eigenleistung art){
		deleteAction = new Action(Messages.EigenleistungContextMenu_deleteAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(art.getClass().getName()
					+ Messages.EigenleistungContextMenu_deleteActionToolTip);
			}
			
			@Override
			public void run(){
				Eigenleistung act =
					(Eigenleistung) ElexisEventDispatcher.getSelected(art.getClass());
				if (MessageDialog.openConfirm(cv.getViewerWidget().getControl().getShell(),
					Messages.EigenleistungContextMenu_deleteActionConfirmCaption,
					MessageFormat.format(Messages.EigenleistungContextMenu_deleteConfirmBody,
						act.getText()))) {
					act.delete();
					cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
					cv.notify(CommonViewer.Message.update);
				}
				
			}
		};
		editAction = new Action(Messages.EigenleistungContextMenu_propertiesAction) {
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.EigenleistungContextMenu_propertiesTooltip);
			}
			
			@Override
			public void run(){
				Eigenleistung lstg =
					(Eigenleistung) ElexisEventDispatcher.getSelected(art.getClass());
				EditEigenleistungUi.executeWithParams(lstg);
			}
		};
	}
	
	class EigenleistungMenuListener implements IMenuListener {
		public void menuAboutToShow(final IMenuManager manager){
			menu.removeAll();
			for (IAction ac : actions) {
				if (ac == null) {
					menu.add(new Separator());
				} else {
					menu.add(ac);
				}
			}
		}
	}
}
