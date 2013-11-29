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

package ch.elexis.core.ui.util;

import java.util.List;

import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.widgets.Control;

import ch.elexis.data.PersistentObject;

public class PersistentObjectDragSource implements DragSourceListener {
	
	ISelectionRenderer renderer;
	Control dragSource;
	List<PersistentObject> selection;
	protected static PersistentObject draggedObject;
	
	Transfer myTransfer = TextTransfer.getInstance();
	
	public PersistentObjectDragSource(final StructuredViewer v){
		dragSource = v.getControl();
		renderer = new ISelectionRenderer() {
			
			public List<PersistentObject> getSelection(){
				IStructuredSelection sel = (IStructuredSelection) v.getSelection();
				return sel.toList();
			}
			
		};
		setup();
	}
	
	public PersistentObjectDragSource(final Control source, final ISelectionRenderer renderer){
		this.renderer = renderer;
		dragSource = source;
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
	
	public void dragSetData(final DragSourceEvent event){
		
		StringBuilder sb = new StringBuilder();
		for (PersistentObject s : selection) {
			sb.append(s.storeToString()).append(","); //$NON-NLS-1$
		}
		
		event.data = sb.toString().replace(",$", ""); //$NON-NLS-1$ //$NON-NLS-2$
	}
	
	public void dragStart(final DragSourceEvent event){
		
		selection = renderer.getSelection();
		// IStructuredSelection
		// select=(IStructuredSelection)viewer.getSelection();
		// Object[] sel=select.toArray();
		if ((selection == null) || (selection.isEmpty())) {
			event.doit = false;
		} else {
			event.doit = selection.get(0).isDragOK();
		}
		if (event.doit) {
			PersistentObjectDragSource.draggedObject = selection.get(0);
		}
	}
	
	public static PersistentObject getDraggedObject(){
		return draggedObject;
	}
	
	public interface ISelectionRenderer {
		public List<PersistentObject> getSelection();
	}
}
