/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views.rechnung;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Rechnung;
import ch.elexis.core.data.RnStatus;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.rgw.tools.Tree;

public class RnMenuListener implements IMenuListener {
	
	RechnungsListeView view;
	
	RnMenuListener(RechnungsListeView view){
		this.view = view;
	}
	
	public void menuAboutToShow(IMenuManager manager){
		Object[] o = view.cv.getSelection();
		if (o != null && o.length > 0) {
			if (o.length == 1) {
				Tree t = (Tree) o[0];
				if (t.contents instanceof Rechnung) {
					Rechnung rn = (Rechnung) t.contents;
					if (rn.getStatus() == RnStatus.FEHLERHAFT) {
						manager.add(view.actions.delRnAction);
						manager.add(view.actions.reactivateRnAction);
					} else {
						((RestrictedAction) view.actions.changeStatusAction).reflectRight();
						manager.add(view.actions.rnExportAction);
						manager.add(view.actions.addPaymentAction);
						manager.add(view.actions.addExpenseAction);
						manager.add(view.actions.increaseLevelAction);
						manager.add(new Separator());
						manager.add(view.actions.changeStatusAction);
						manager.add(view.actions.stornoAction);
						boolean bSomething = rn.getStatus() != RnStatus.STORNIERT;
						view.actions.rnExportAction.setEnabled(bSomething);
						view.actions.addPaymentAction.setEnabled(bSomething);
						view.actions.addExpenseAction.setEnabled(bSomething);
						view.actions.increaseLevelAction.setEnabled(bSomething);
						view.actions.stornoAction.setEnabled(bSomething);
					}
				} else if (t.contents instanceof Fall) {
					// Fall fall=(Fall)t.contents;
					manager.add(view.actions.editCaseAction);
				} else if (t.contents instanceof Patient) {
					manager.add(view.actions.patDetailAction);
				}
			}
		}
		
	}
	
}
