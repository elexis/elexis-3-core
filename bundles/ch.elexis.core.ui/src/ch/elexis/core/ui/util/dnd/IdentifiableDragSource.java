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

package ch.elexis.core.ui.util.dnd;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.StringJoiner;

import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DragSource;
import org.eclipse.swt.dnd.DragSourceEvent;
import org.eclipse.swt.dnd.DragSourceListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.model.Identifiable;

/**
 * Basic {@link DragSourceListener} implementation for {@link StructuredViewer} of
 * {@link Identifiable} objects.
 * 
 * @author thomas
 *
 */
public class IdentifiableDragSource implements DragSourceListener {
	
	private StructuredViewer viewer;
	private List<Identifiable> selection;
	protected static Identifiable draggedObject;
	
	Transfer myTransfer = TextTransfer.getInstance();
	
	public IdentifiableDragSource(final StructuredViewer viewer){
		this.viewer = viewer;
		setup();
	}
	
	private void setup(){
		DragSource mine = new DragSource(viewer.getControl(), DND.DROP_COPY);
		mine.setTransfer(new Transfer[] {
			myTransfer
		});
		mine.addDragListener(this);
	}
	
	@Override
	public void dragFinished(final DragSourceEvent event){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void dragSetData(final DragSourceEvent event){
		StringJoiner sj = new StringJoiner(",");
		for (Identifiable identifiable : selection) {
			Optional<String> storeToString =
				StoreToStringServiceHolder.get().storeToString(identifiable);
			storeToString.ifPresent(s -> sj.add(s));
		}
		
		event.data = sj.toString().replace(",$", StringConstants.EMPTY); //$NON-NLS-1$
	}
	
	@Override
	public void dragStart(final DragSourceEvent event){
		selection = new ArrayList<>();
		if (viewer != null) {
			ISelection viewerSelection = viewer.getSelection();
			if (viewerSelection instanceof IStructuredSelection) {
				for (Object object : ((IStructuredSelection) viewerSelection).toList()) {
					if (object instanceof Identifiable) {
						selection.add((Identifiable) object);
					}
				}
			}
		}
		
		if ((selection == null) || (selection.isEmpty())) {
			event.doit = false;
		}
	}
}
