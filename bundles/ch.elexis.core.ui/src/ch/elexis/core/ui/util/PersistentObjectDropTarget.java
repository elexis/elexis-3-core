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

import org.apache.commons.lang3.StringUtils;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DND;
import org.eclipse.swt.dnd.DropTarget;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.dnd.DropTargetListener;
import org.eclipse.swt.dnd.TextTransfer;
import org.eclipse.swt.dnd.Transfer;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Table;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.data.PersistentObject;

/**
 * Universal {@link DropTarget} for {@link PersistentObject}
 *
 * @author gerry
 * @deprecated use {@link GenericObjectDropTarget} instead
 *
 */
public class PersistentObjectDropTarget implements DropTargetListener, ICodeSelectorTarget {
	IReceiver rc;
	String name = StringUtils.EMPTY;
	private final Color normalColor;
	private final Color highlightColor;
	private final Control mine;

	/**
	 * Register the provided target as {@link DropTarget} for a
	 * {@link PersistentObject}
	 *
	 * @param target
	 * @param r
	 * @param colorizeControl whether the target control should change color during
	 *                        selection
	 * @since 3.1.0
	 */
	public PersistentObjectDropTarget(String name, Control target, IReceiver r, boolean colorizeControl) {
		if (colorizeControl) {
			normalColor = target.getBackground();
			highlightColor = target.getDisplay().getSystemColor(SWT.COLOR_RED);
		} else {
			normalColor = null;
			highlightColor = null;
		}

		this.name = name;
		mine = target;
		rc = r;
		DropTarget dtarget = new DropTarget(target, DND.DROP_COPY);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] { textTransfer };
		dtarget.setTransfer(types);
		dtarget.addDropListener(this);
	}

	public PersistentObjectDropTarget(Control target, IReceiver r) {
		this(StringUtils.EMPTY, target, r, true);
	}

	public PersistentObjectDropTarget(String name, Control target, IReceiver r) {
		this(name, target, r, true);
	}

	public void dragEnter(DropTargetEvent event) {

		boolean bOk = false;
		PersistentObject dropped = PersistentObjectDragSource.getDraggedObject();

		if (rc.accept(dropped)) {
			bOk = true;
		}

		if (bOk) {
			event.detail = DND.DROP_COPY;
		} else {
			event.detail = DND.DROP_NONE;
		}
	}

	public void dragLeave(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	public void dragOperationChanged(DropTargetEvent event) {
		// TODO Auto-generated method stub

	}

	public void dragOver(DropTargetEvent event) {
		// TODO Auto-generated method stub
	}

	public void drop(DropTargetEvent event) {
		String drp = (String) event.data;
		String[] dl = drp.split(","); //$NON-NLS-1$
		for (String obj : dl) {
			PersistentObject dropped = CoreHub.poFactory.createFromString(obj);
			if (dropped != null) {
				rc.dropped(dropped, event);
			}
		}
	}

	public void dropAccept(DropTargetEvent event) {
		if (!rc.accept(PersistentObjectDragSource.getDraggedObject())) {
			event.detail = DND.DROP_NONE;
		}
	}

	public void codeSelected(PersistentObject obj) {
		rc.dropped(obj, null);
	}

	@Override
	public void codeSelected(Object obj) {
		throw new UnsupportedOperationException();
	}

	public String getName() {
		return name;
	}

	public void registered(boolean bIsRegistered) {
		if (normalColor != null)
			highlight(bIsRegistered);

	}

	private void highlight(boolean bOn) {
		Control highlightControl = getHighLightControl();
		if (!highlightControl.isDisposed()) {
			if (bOn) {
				highlightControl.setBackground(highlightColor);
			} else {
				highlightControl.setBackground(normalColor);
			}
		}
	}

	/**
	 * Override if mine is a {@link Table} that shows column background. On Win
	 * Platform setting background of Table disables all column background.
	 *
	 * @return
	 */
	protected Control getHighLightControl() {
		return mine;
	}

	public interface IReceiver {
		public void dropped(PersistentObject o, DropTargetEvent e);

		public boolean accept(PersistentObject o);
	}
}
