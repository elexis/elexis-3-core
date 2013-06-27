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
package ch.elexis.core.ui.views.artikel;

import java.text.MessageFormat;
import java.util.ArrayList;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.Separator;
import org.eclipse.jface.dialogs.InputDialog;
import org.eclipse.jface.dialogs.MessageDialog;

import ch.elexis.core.data.Artikel;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.ui.commands.EditEigenartikelUi;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.CommonViewer;

public class ArtikelContextMenu {
	private IAction deleteAction, createAction, editAction;
	CommonViewer cv;
	ArtikelDetailDisplay add;
	ArtikelMenuListener menuListener = new ArtikelMenuListener();
	MenuManager menu;
	ArrayList<IAction> actions = new ArrayList<IAction>();
	
	public ArtikelContextMenu(final Artikel template, final CommonViewer cv){
		this.cv = cv;
		makeActions(template);
		actions.add(deleteAction);
		actions.add(createAction);
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
	
	public ArtikelContextMenu(final Artikel template, final CommonViewer cv,
		final ArtikelDetailDisplay add){
		this(template, cv);
		this.add = add;
	}
	
	private void makeActions(final Artikel art){
		deleteAction = new Action(Messages.ArtikelContextMenu_deleteAction) {
			{
				setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
				setToolTipText(art.getClass().getName()
					+ Messages.ArtikelContextMenu_deleteActionToolTip);
			}
			
			@Override
			public void run(){
				Artikel act = (Artikel) ElexisEventDispatcher.getSelected(art.getClass());
				if (MessageDialog.openConfirm(
					cv.getViewerWidget().getControl().getShell(),
					Messages.ArtikelContextMenu_deleteActionConfirmCaption,
					MessageFormat.format(Messages.ArtikelContextMenu_deleteConfirmBody,
						act.getName()))) {
					act.delete();
					cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
					cv.notify(CommonViewer.Message.update);
				}
				
			}
		};
		createAction = new Action(Messages.ArtikelContextMenu_newAction) {
			{
				setImageDescriptor(Images.IMG_NEW.getImageDescriptor());
				setToolTipText(Messages.ArtikelContextMenu_newActionTooltip);
			}
			
			@Override
			public void run(){
				InputDialog inp =
					new InputDialog(cv.getViewerWidget().getControl().getShell(), art.getClass()
						.getName() + Messages.ArtikelContextMenu_create,
						Messages.ArtikelContextMenu_pleaseEnterNameForArticle, "", null); //$NON-NLS-1$
				if (inp.open() == InputDialog.OK) {
					String name = inp.getValue();
					Artikel n = new Artikel(name, art.getCodeSystemName(), ""); //$NON-NLS-1$
					if (add == null) {
						EditEigenartikelUi.executeWithParams(n);
					} else {
						add.show(n);
					}
				}
				
			}
		};
		editAction = new Action(Messages.ArtikelContextMenu_propertiesAction) {
			{
				setImageDescriptor(Images.IMG_EDIT.getImageDescriptor());
				setToolTipText(Messages.ArtikelContextMenu_propertiesTooltip);
			}
			
			@Override
			public void run(){
				Artikel n = (Artikel) ElexisEventDispatcher.getSelected(art.getClass());
				if (add == null) {
					EditEigenartikelUi.executeWithParams(n);
				} else {
					add.show(n);
				}
			}
		};
	}
	
	public interface ArtikelDetailDisplay {
		public boolean show(Artikel art);
	}
	
	class ArtikelMenuListener implements IMenuListener {
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
