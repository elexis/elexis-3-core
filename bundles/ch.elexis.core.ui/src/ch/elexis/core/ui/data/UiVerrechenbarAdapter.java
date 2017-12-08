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

package ch.elexis.core.ui.data;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.Dialog;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.AddElementToBlockDialog;
import ch.elexis.data.Leistungsblock;
import ch.elexis.data.VerrechenbarAdapter;

/**
 * @since 3.0.0
 */
public abstract class UiVerrechenbarAdapter extends VerrechenbarAdapter {
	
	protected IAction addToBlockAction;
	
	protected UiVerrechenbarAdapter(final String id){
		super(id);
	}
	
	protected UiVerrechenbarAdapter(){
		makeActions(this);
	}
	
	@Override
	public List<Object> getActions(Object kontext){
		List<Object> actions = new ArrayList<Object>(1);
		if (addToBlockAction == null) {
			makeActions(this);
		}
		actions.add(addToBlockAction);
		return actions;
	}
	
	private void makeActions(final ICodeElement el){
		addToBlockAction = new Action("Zu Leistungsblock...") {
			@Override
			public void run(){
				AddElementToBlockDialog adb = new AddElementToBlockDialog(UiDesk.getTopShell());
				if (adb.open() == Dialog.OK) {
					ICodeElement ice =
						(ICodeElement) ElexisEventDispatcher.getSelected(el.getClass());
					Leistungsblock lb = adb.getResult();
					lb.addElement(ice);
					ElexisEventDispatcher.reload(Leistungsblock.class);
				}
			}
		};
	}
}
