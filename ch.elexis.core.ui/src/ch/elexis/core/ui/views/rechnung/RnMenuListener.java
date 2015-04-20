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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.Separator;

import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.data.Fall;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnung;
import ch.elexis.data.RnStatus;
import ch.rgw.tools.Tree;

public class RnMenuListener implements IMenuListener {
	
	RechnungsListeView view;
	private int generalStatus;
	
	RnMenuListener(RechnungsListeView view){
		this.view = view;
	}
	
	@SuppressWarnings("unchecked")
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
						enableStornoDependentFields(rn.getStatus() != RnStatus.STORNIERT);
					}
				} else if (t.contents instanceof Fall) {
					// Fall fall=(Fall)t.contents;
					manager.add(view.actions.editCaseAction);
				} else if (t.contents instanceof Patient) {
					manager.add(view.actions.patDetailAction);
				}
			} else {
				List<Rechnung> rechnungen = new ArrayList<Rechnung>();
				generalStatus = -1;
				boolean compatibleStatus = true;
				
				for (Object obj : o) {
					Tree treeElement = (Tree) obj;
					
					if (treeElement.contents instanceof Rechnung) {
						Rechnung rn = (Rechnung) treeElement.contents;
						compatibleStatus = isCompatible(rn.getStatus());
						
					} else if (treeElement.contents instanceof Fall) {
						Collection<Tree> fallRechnungen = treeElement.getChildren();
						for (Tree tRn : fallRechnungen) {
							Rechnung rn = (Rechnung) tRn.contents;
							compatibleStatus = isCompatible(rn.getStatus());
						}
					} else if (treeElement.contents instanceof Patient) {
						Collection<Tree> fallChilds = treeElement.getChildren();
						for (Tree fallTree : fallChilds) {
							Collection<Tree> fallRechnungen = fallTree.getChildren();
							for (Tree tRn : fallRechnungen) {
								Rechnung rn = (Rechnung) tRn.contents;
								compatibleStatus = isCompatible(rn.getStatus());
							}
						}
					}
				}
				
				// only show menu if status did match otherwise this could lead to irregularities of invoices   
				if (compatibleStatus) {
					if (generalStatus == RnStatus.FEHLERHAFT) {
						manager.add(view.actions.delRnAction);
						manager.add(view.actions.reactivateRnAction);
					} else {
						manager.add(view.actions.rnExportAction);
						manager.add(view.actions.addExpenseAction);
						manager.add(view.actions.increaseLevelAction);
						manager.add(new Separator());
						manager.add(view.actions.changeStatusAction);
						enableStornoDependentFields(generalStatus != RnStatus.STORNIERT);
					}
				}
			}
		}
	}
	
	/**
	 * check if status is the same than previous
	 * 
	 * @param status
	 * @return
	 */
	private boolean isCompatible(int status){
		// use for all non fehlerhaft or storno status 0
		if (status != RnStatus.FEHLERHAFT && status != RnStatus.STORNIERT) {
			status = 0;
		}
		
		if (generalStatus == -1 || generalStatus == status) {
			generalStatus = status;
			return true;
		} else {
			return false;
		}
	}
	
	private void enableStornoDependentFields(boolean enable){
		view.actions.rnExportAction.setEnabled(enable);
		view.actions.addPaymentAction.setEnabled(enable);
		view.actions.addExpenseAction.setEnabled(enable);
		view.actions.increaseLevelAction.setEnabled(enable);
		view.actions.stornoAction.setEnabled(enable);
	}
}
