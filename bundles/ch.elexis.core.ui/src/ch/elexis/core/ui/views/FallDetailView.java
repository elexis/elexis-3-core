/*******************************************************************************
 * Copyright (c) 2005-2012, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    M. Descher - fix for wrong case being set if eeli_fall listener not active
 * 
 *******************************************************************************/

package ch.elexis.core.ui.views;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.interfaces.IPersistentObject;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.locks.ToggleCurrentCaseLockHandler;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.data.Anwender;
import ch.elexis.data.Fall;

public class FallDetailView extends ViewPart implements ISaveablePart2 {
	public static final String ID = "ch.elexis.FallDetailView"; //$NON-NLS-1$
	FallDetailBlatt2 fdb;
	
	private final ElexisEventListener eeli_user = new ElexisUiEventListenerImpl(Anwender.class,
		ElexisEvent.EVENT_USER_CHANGED) {
		
		@Override
		public void runInUi(ElexisEvent ev){
			fdb.reloadBillingSystemsMenu();
		}
	};
	
	private final ElexisEventListener eeli_fall = new ElexisUiEventListenerImpl(Fall.class) {
		@Override
		public void runInUi(final ElexisEvent ev){
			Fall fall = (Fall) ev.getObject();
			Fall deselectedFall = null;
			switch (ev.getType()) {
			case ElexisEvent.EVENT_SELECTED:
				deselectedFall = fdb.getFall();
				fdb.setFall(fall);
				if (deselectedFall != null) {
					releaseAndRefreshLock(deselectedFall, ToggleCurrentCaseLockHandler.COMMAND_ID);
				}
				break;
			case ElexisEvent.EVENT_DESELECTED:
				deselectedFall = fdb.getFall();
				fdb.setFall(null);
				if (deselectedFall != null) {
					releaseAndRefreshLock(deselectedFall, ToggleCurrentCaseLockHandler.COMMAND_ID);
				}
				break;
			case ElexisEvent.EVENT_LOCK_AQUIRED:
			case ElexisEvent.EVENT_LOCK_RELEASED:
				if(fall.equals(fdb.getFall())) {
					fdb.setUnlocked(ev.getType()==ElexisEvent.EVENT_LOCK_AQUIRED);
				}
				break;
			default:
				break;
			}
		}
	};
	
	private void releaseAndRefreshLock(IPersistentObject object, String commandId){
		if (object != null && LocalLockServiceHolder.get().isLockedLocal(object)) {
			LocalLockServiceHolder.get().releaseLock(object);
		}
		ICommandService commandService =
			(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
		commandService.refreshElements(commandId, null);
	}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new GridLayout());
		fdb = new FallDetailBlatt2(parent);
		fdb.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		fdb.setUnlocked(false);
		ElexisEventDispatcher.getInstance().addListeners(eeli_fall, eeli_user);
	}
	
	@Override
	public void setFocus(){
		Fall f = (Fall) ElexisEventDispatcher.getSelected(Fall.class);
		fdb.setFall(f);
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(eeli_fall, eeli_user);
		super.dispose();
	}
	
	/***********************************************************************************************
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	@Override
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	@Override
	public void doSaveAs(){ /* leer */
	}
	
	@Override
	public boolean isDirty(){
		return true;
	}
	
	@Override
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	@Override
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	public Fall getActiveFall(){
		return fdb.getFall();
	}
}
