/*******************************************************************************
 * Copyright (c) 2007-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 	  M. Descher - several changes
 *******************************************************************************/

package ch.elexis.core.ui.util;

import java.util.Collections;
import java.util.List;

import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.service.StoreToStringServiceHolder;

public class GenericObjectDragSource implements DragSourceListener {
	
	ISelectionRenderer renderer;
	Control dragSource;
	Transfer myTransfer = TextTransfer.getInstance();
	
	public GenericObjectDragSource(final StructuredViewer viewer){
		dragSource = viewer.getControl();
		renderer = new ISelectionRenderer() {
			@SuppressWarnings("unchecked")
			public List<Object> getSelection(){
				if (viewer != null && viewer.getStructuredSelection() != null) {
					return viewer.getStructuredSelection().toList();
				}
				return Collections.emptyList();
			}
		};
		setup();
	}
	
	private void setup(){
		DragSource mine = new DragSource(dragSource, DND.DROP_COPY);
		mine.setTransfer(new Transfer[] {
			myTransfer
		});
		mine.addDragListener(this);
	}
	
	public void dragFinished(final DragSourceEvent event){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dragSetData(final DragSourceEvent event){
		StringBuilder sb = new StringBuilder();
		for (Object selected : renderer.getSelection()) {
			String string = StoreToStringServiceHolder.getStoreToString(selected);
			sb.append(string).append(StringConstants.COMMA);
		}
		event.data = sb.toString().replace(",$", StringConstants.EMPTY); //$NON-NLS-1$
	}
	
	public void dragStart(final DragSourceEvent event){
		List<Object> selection = renderer.getSelection();
		if ((selection == null) || (selection.isEmpty())) {
			event.doit = false;
		} else {
			event.doit = isDrag(selection);
		}
	}
	
	/**
	 * Override this method to decide if drag if the selection is valid.
	 * 
	 * @param selection
	 * @return
	 */
	protected boolean isDrag(List<Object> selection){
		return true;
	}
	
	public interface ISelectionRenderer {
		public List<Object> getSelection();
	}
}
