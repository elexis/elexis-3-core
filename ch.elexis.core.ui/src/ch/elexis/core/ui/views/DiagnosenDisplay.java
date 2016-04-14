/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.DropTargetEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableItem;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.interfaces.IDiagnose;
import ch.elexis.core.data.interfaces.IVerrechenbar;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.CodeSelectorHandler;
import ch.elexis.core.ui.dialogs.FreeTextDiagnoseDialog;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.IUnlockable;
import ch.elexis.core.ui.util.PersistentObjectDragSource;
import ch.elexis.core.ui.util.PersistentObjectDragSource.ISelectionRenderer;
import ch.elexis.core.ui.util.PersistentObjectDropTarget;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.codesystems.DiagnosenView;
import ch.elexis.data.FreeTextDiagnose;
import ch.elexis.data.Konsultation;
import ch.elexis.data.PersistentObject;

public class DiagnosenDisplay extends Composite implements ISelectionRenderer, IUnlockable {
	Table tDg;
	private final Button addFreeTextBtn;
	private final Hyperlink hDg;
	private final PersistentObjectDropTarget dropTarget;

	private final ElexisEventListener eeli_update = new ElexisUiEventListenerImpl(
		Konsultation.class, ElexisEvent.EVENT_UPDATE) {
		@Override
		public void runInUi(ElexisEvent ev){
			Konsultation actKons =
				(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			if (actKons != null) {
				setDiagnosen(actKons);
			}
		}
	};
	
	public void setEnabled(boolean enabled) {
		addFreeTextBtn.setEnabled(enabled);
		hDg.setEnabled(enabled);
		super.setEnabled(enabled);
	};

	public DiagnosenDisplay(final IWorkbenchPage page, final Composite parent, final int style){
		super(parent, style);
		setLayout(new GridLayout(2, false));
		hDg =
			UiDesk.getToolkit()
				.createHyperlink(this, Messages.DiagnosenDisplay_Diagnoses, SWT.NONE); //$NON-NLS-1$
		hDg.setLayoutData(new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL));
		hDg.addHyperlinkListener(new HyperlinkAdapter() {
			@Override
			public void linkActivated(final HyperlinkEvent e){
				try {
					page.showView(DiagnosenView.ID);
					CodeSelectorHandler.getInstance().setCodeSelectorTarget(dropTarget);
				} catch (Exception ex) {
					ElexisStatus status =
						new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
							Messages.DiagnosenDisplay_ErrorStartingCodeSystem + ex.getMessage(),
							ex, ElexisStatus.LOG_ERRORS);
					StatusManager.getManager().handle(status, StatusManager.SHOW);
				}
			}
		});
		addFreeTextBtn = new Button(this, SWT.PUSH);
		addFreeTextBtn.setImage(Images.IMG_DOCUMENT_TEXT.getImage());
		addFreeTextBtn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e){
				FreeTextDiagnoseDialog ftDialog =
					new FreeTextDiagnoseDialog(Display.getDefault().getActiveShell());
				if (ftDialog.open() == Window.OK) {
					FreeTextDiagnose diag = new FreeTextDiagnose(ftDialog.getText(), true);
					Konsultation actKons =
						(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
					actKons.addDiagnose(diag);
					setDiagnosen(actKons);
				}
			}
		});

		tDg = UiDesk.getToolkit().createTable(this, SWT.SINGLE | SWT.WRAP);
		tDg.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 2, 1));
		tDg.setMenu(createDgMenu());

		// new PersistentObjectDragSource()
		dropTarget =
			new PersistentObjectDropTarget(Messages.DiagnosenDisplay_DiagnoseTarget, tDg,
				new DropReceiver()); //$NON-NLS-1$
		new PersistentObjectDragSource(tDg, this);

		ElexisEventDispatcher.getInstance().addListeners(eeli_update);
	}

	public void clear(){
		tDg.removeAll();
	}

	private final class DropReceiver implements PersistentObjectDropTarget.IReceiver {
		@Override
		public void dropped(final PersistentObject o, final DropTargetEvent ev){
			Konsultation actKons =
				(Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class);
			if (actKons == null) {
				SWTHelper.alert("Keine Konsultation ausgewählt",
					"Bitte wählen Sie zuerst eine Konsultation aus");
			} else {
				if (o instanceof IDiagnose) {
					actKons.addDiagnose((IDiagnose) o);
					setDiagnosen(actKons);
				}
			}
		}

		@Override
		public boolean accept(final PersistentObject o){
			if (o instanceof IVerrechenbar) {
				return true;
			}
			if (o instanceof IDiagnose) {
				return true;
			}
			return false;
		}
	}

	public void setDiagnosen(final Konsultation b){
		List<IDiagnose> dgl = b.getDiagnosen();
		tDg.removeAll();
		for (IDiagnose dg : dgl) {
			TableItem ti = new TableItem(tDg, SWT.WRAP);
			ti.setText(dg.getLabel());
			ti.setData(dg);
		}
		// tDg.setEnabled(b.getStatus()==RnStatus.NICHT_VON_HEUTE);
	}

	private Menu createDgMenu(){
		Menu ret = new Menu(tDg);
		MenuItem delDg = new MenuItem(ret, SWT.NONE);
		delDg.setText(Messages.DiagnosenDisplay_RemoveDiagnoses); //$NON-NLS-1$
		delDg.addSelectionListener(new delDgListener());
		return ret;
	}

	class delDgListener extends SelectionAdapter {
		@Override
		public void widgetSelected(final SelectionEvent e){
			int sel = tDg.getSelectionIndex();
			if (sel != -1) {
				TableItem ti = tDg.getItem(sel);
				((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class))
					.removeDiagnose((IDiagnose) ti.getData());
				tDg.remove(sel);
			}
			// setBehandlung(actBehandlung);
		}
	}

	@Override
	public List<PersistentObject> getSelection(){
		TableItem[] sel = tDg.getSelection();
		ArrayList<PersistentObject> ret = new ArrayList<PersistentObject>();
		if ((sel != null) && (sel.length > 0)) {
			for (TableItem ti : sel) {
				IDiagnose id = (IDiagnose) ti.getData();
				String clazz = id.getClass().getName();
				ret.add(CoreHub.poFactory.createFromString(clazz + "::" + id.getCode())); //$NON-NLS-1$
			}
		}
		return ret;
	}

	@Override
	public void setUnlocked(boolean unlocked) {
		setEnabled(unlocked);
		redraw();
	}
}
