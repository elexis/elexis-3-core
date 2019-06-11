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

import org.eclipse.jface.action.IAction;

import ch.elexis.core.model.ICodeElement;
import ch.elexis.core.ui.actions.AddVerrechenbarToLeistungsblockAction;
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
		addToBlockAction = new AddVerrechenbarToLeistungsblockAction(el.getClass());
	}
}
