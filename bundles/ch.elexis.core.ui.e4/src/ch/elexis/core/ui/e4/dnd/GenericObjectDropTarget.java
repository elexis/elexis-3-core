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
package ch.elexis.core.ui.e4.dnd;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

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

import ch.elexis.core.model.Identifiable;
import ch.elexis.core.services.IStoreToStringService;

public class GenericObjectDropTarget implements DropTargetListener {

	public interface IReceiver {
		void dropped(List<Object> list, DropTargetEvent e);
	}

	private final IStoreToStringService storeToStringService;

	IReceiver rc;
	String name = StringUtils.EMPTY;

	private final Color normalColor;
	private final Color highlightColor;
	private final Control mine;

	/**
	 * Register the provided target as {@link DropTarget}
	 *
	 * @param target
	 * @param r
	 * @param colorizeControl whether the target control should change color during
	 *                        selection
	 * @since 3.12.0
	 */
	public GenericObjectDropTarget(IStoreToStringService storeToStringService, String name, Control target,
			IReceiver rc, boolean colorizeControl) {

		this.storeToStringService = storeToStringService;
		mine = target;
		this.name = name;
		this.rc = rc;

		if (colorizeControl) {
			normalColor = target.getBackground();
			highlightColor = target.getDisplay().getSystemColor(SWT.COLOR_RED);
		} else {
			normalColor = null;
			highlightColor = null;
		}

		DropTarget dtarget = new DropTarget(target, DND.DROP_COPY);
		final TextTransfer textTransfer = TextTransfer.getInstance();
		Transfer[] types = new Transfer[] { textTransfer };
		dtarget.setTransfer(types);
		dtarget.addDropListener(this);
	}

	public GenericObjectDropTarget(IStoreToStringService storeToStringService, Control target, IReceiver r) {
		this(storeToStringService, StringUtils.EMPTY, target, r, true);
	}

	public GenericObjectDropTarget(IStoreToStringService storeToStringService, String name, Control target,
			IReceiver r) {
		this(storeToStringService, name, target, r, true);
	}

	@Override
	public void dragEnter(DropTargetEvent event) {
		event.detail = DND.DROP_COPY;
	}

	private List<Object> getDataObjects(DropTargetEvent event) {
		if (event != null && event.data != null) {
			List<Object> ret = new ArrayList<>();
			String droppedString = (String) event.data;
			String[] parts = droppedString.split(","); //$NON-NLS-1$
			for (String part : parts) {
				Optional<Identifiable> loaded = storeToStringService.loadFromString(part);
				loaded.ifPresent(ret::add);
			}
			return ret;
		}
		return Collections.emptyList();
	}

	@Override
	public void dragLeave(DropTargetEvent event) {
	}

	@Override
	public void dragOperationChanged(DropTargetEvent event) {
	}

	@Override
	public void dragOver(DropTargetEvent event) {
	}

	@Override
	public void dropAccept(DropTargetEvent event) {
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

	@Override
	public void drop(DropTargetEvent event) {
		rc.dropped(getDataObjects(event), event);
	}
}
