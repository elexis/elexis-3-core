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
package ch.elexis.core.ui.text;

import java.util.Hashtable;
import java.util.List;

import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.graphics.Point;

import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.model.Identifiable;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.GenericObjectDropTarget.IReceiver;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;

public class ETFDropReceiver implements IReceiver {
	EnhancedTextField etf;

	Hashtable<Class<?>, IKonsExtension> targets;

	ETFDropReceiver(final EnhancedTextField et) {
		etf = et;
		targets = new Hashtable<Class<?>, IKonsExtension>();
	}

	public void addReceiver(final Class<?> clazz, final IKonsExtension rec) {
		targets.put(clazz, rec);
	}

	public void removeReceiver(final Class<?> clazz, final IKonsExtension rec) {
		targets.remove(clazz);
	}

	private IKonsExtension getTargetForObject(Object o) {
		IKonsExtension ret = targets.get(o.getClass());
		if (ret == null) {
			// check the interfaces
			Class<?>[] interfaces = o.getClass().getInterfaces();
			if (interfaces != null && interfaces.length > 0) {
				for (Class<?> inter : interfaces) {
					ret = targets.get(inter);
					if (ret != null) {
						break;
					}
				}
			}
		}
		return ret;
	}

	@Override
	public void dropped(List<Object> list, DropTargetEvent e) {
		if (list != null && !list.isEmpty()) {
			Point point = UiDesk.getDisplay().getCursorLocation();
			Point mapped = UiDesk.getDisplay().map(null, etf.text, point);
			Point maxOffset = etf.text.getLocationAtOffset(etf.text.getCharCount());
			int pos = etf.text.getCharCount();
			if (mapped.y < maxOffset.y) {
				pos = etf.text.getOffsetAtPoint(new Point(0, mapped.y));
			}
			Object object = list.get(0);
			IKonsExtension rec = getTargetForObject(object);

			if (rec != null) {
				rec.insert(object, pos);
			} else {
				Konsultation actKons = (Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
				if (actKons != null) {
					if (LocalLockServiceHolder.get().acquireLock(actKons).isOk()) {
						etf.text.insert(getLabel(object));
						actKons.updateEintrag(etf.getContentsAsXML(), false);
						LocalLockServiceHolder.get().releaseLock(actKons);
					}
				}
			}
		}
	}

	private String getLabel(Object object) {
		if (object instanceof PersistentObject) {
			return ((PersistentObject) object).getLabel();
		} else if (object instanceof Identifiable) {
			return ((Identifiable) object).getLabel();
		}
		return object.toString();
	}

	@Override
	public boolean accept(List<Object> list) {
		return true;
	}
}
