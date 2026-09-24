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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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

import ch.elexis.core.data.service.StoreToStringServiceHolder;
import ch.elexis.core.ui.actions.ICodeSelectorTarget;
import ch.elexis.data.PersistentObject;

/**
 * Universal {@link DropTarget}
 *
 */
public class GenericObjectDropTarget implements DropTargetListener, ICodeSelectorTarget {
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
	public GenericObjectDropTarget(String name, Control target, IReceiver r, boolean colorizeControl) {
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

	public GenericObjectDropTarget(Control target, IReceiver r) {
		this(StringUtils.EMPTY, target, r, true);
	}

	public GenericObjectDropTarget(String name, Control target, IReceiver r) {
		this(name, target, r, true);
	}

	public void dragEnter(DropTargetEvent event) {
		boolean bOk = false;
		if (rc.accept(getDataObjects(event))) {
			bOk = true;
		}
		if (bOk) {
			event.detail = DND.DROP_COPY;
		} else {
			event.detail = DND.DROP_NONE;
		}
	}

	private List<Object> getDataObjects(DropTargetEvent event) {
		if (event != null && event.data != null) {
			List<Object> ret = new ArrayList<>();
			String droppedString = (String) event.data;
			String[] parts = droppedString.split(","); //$NON-NLS-1$
			for (String part : parts) {
				StoreToStringServiceHolder.get().loadFromString(part.trim()).ifPresent(ret::add);
			}
			return ret;
		}
		return Collections.emptyList();
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

	public void dropAccept(DropTargetEvent event) {
		if (!rc.accept(getDataObjects(event))) {
			event.detail = DND.DROP_NONE;
		}
	}

	@Override
	public void codeSelected(Object obj) {
		if (rc.accept(Collections.singletonList(obj))) {
			rc.dropped(Collections.singletonList(obj), null);
		}
	}

	public String getName() {
		return name;
	}

	public void registered(boolean bIsRegistered) {
		if (normalColor != null) {
			highlight(bIsRegistered);
		}
	}

	private void highlight(boolean bOn) {
		Control highlightControl = getHighLightControl();
		if (!mine.isDisposed()) {
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
		public void dropped(List<Object> list, DropTargetEvent e);

		public boolean accept(List<Object> list);
	}

	@Override
	public void drop(DropTargetEvent event) {
		rc.dropped(getDataObjects(event), event);
	}
}
