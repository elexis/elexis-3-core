/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/
package ch.elexis.core.ui.util.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.Identifiable;

public class IdentifiableDropTarget implements DropTargetListener {
	IReceiver receiver;
	String name = "";
	
	public IdentifiableDropTarget(Control target, IReceiver receiver){
		this.receiver = receiver;
		DropTarget dtarget = new DropTarget(target, DND.DROP_COPY);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] {
			textTransfer
		};
		dtarget.setTransfer(types);
		dtarget.addDropListener(this);
	}
	
	public void dragEnter(DropTargetEvent event){
		if (receiver.accept(getIdentifiables(event))) {
			event.detail = DND.DROP_COPY;
		} else {
			event.detail = DND.DROP_NONE;
		}		
	}
	
	public void drop(DropTargetEvent event){
		receiver.dropped(getIdentifiables(event));
	}
	
	private List<Identifiable> getIdentifiables(DropTargetEvent event){
		List<Identifiable> ret = new ArrayList<>(); 
		if (event.data instanceof String) {
			String[] parts = ((String) event.data).split(","); //$NON-NLS-1$
			for (String part : parts) {
				Optional<Identifiable> loaded = StoreToStringServiceHolder.get().loadFromString(part);
				loaded.ifPresent(i -> ret.add(i));
			}
		}
		return ret;
	}
	
	public void dropAccept(DropTargetEvent event){
		if (!receiver.accept(getIdentifiables(event))) {
			event.detail = DND.DROP_NONE;
		}
	}
	
	public String getName(){
		return name;
	}
	
	public interface IReceiver {
		public void dropped(List<Identifiable> identifiables);
		
		public boolean accept(List<Identifiable> identifiables);
	}
	
	@Override
	public void dragLeave(DropTargetEvent event){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dragOperationChanged(DropTargetEvent event){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dragOver(DropTargetEvent event){
		// TODO Auto-generated method stub
		
	}
}
