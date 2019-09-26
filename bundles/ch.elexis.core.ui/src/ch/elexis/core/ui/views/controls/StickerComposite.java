/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.views.controls;

import java.util.List;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.MenuAdapter;
import org.eclipse.swt.events.MenuEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.dialogs.AssignStickerDialog;
import ch.elexis.core.ui.util.CoreUiUtil;

public class StickerComposite extends Composite {
	
	private FormToolkit toolkit;
	private IPatient actPatient;
	
	public StickerComposite(Composite parent, int style, FormToolkit toolkit){
		super(parent, style);
		setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		ColumnLayout cwl = new ColumnLayout();
		cwl.maxNumColumns = 4;
		cwl.horizontalSpacing = 1;
		cwl.bottomMargin = 10;
		cwl.topMargin = 0;
		cwl.rightMargin = 1;
		cwl.leftMargin = 1;
		setLayout(cwl);
		setBackground(parent.getBackground());
		this.toolkit = toolkit;
		this.setVisible(false);
	}
	
	public static StickerComposite createWrappedStickerComposite(Composite parent, FormToolkit tk){
		Composite wrapper = new Composite(parent, SWT.NONE);
		TableWrapLayout wlayout = new TableWrapLayout();
		wlayout.bottomMargin = 0;
		wlayout.topMargin = 0;
		wrapper.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		wrapper.setLayout(wlayout);
		wrapper.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
		StickerComposite stickerComposite = new StickerComposite(wrapper, SWT.NONE, tk);
		stickerComposite.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
		return stickerComposite;
	}
	
	public void setPatient(IPatient p) {
		this.actPatient = p;
		for (Control cc : getChildren()) {
			cc.dispose();
		}
		this.setVisible(false);
		List<ISticker> etis = StickerServiceHolder.get().getStickers(actPatient);
		if (etis == null)
			return;
		if (etis.size() > 0) {
			this.setVisible(true);
			for (ISticker et : etis) {
				if (et != null) {
					Composite stickerForm = CoreUiUtil.createForm(this, et);
					if (toolkit != null) {
						toolkit.adapt(stickerForm);
						stickerForm.setBackground(CoreUiUtil.getColorForString(et.getBackground()));
					}
					Menu menu = new Menu(stickerForm);
					stickerForm.setMenu(menu);
					stickerForm.setLayoutData(new ColumnLayoutData());
					
					MenuItem miAdd = createMenuItemAdd(menu);
					final MenuItem miRemove = new MenuItem(menu, SWT.NONE);
					miRemove.setData("sticker", et);
					miRemove.setText("Sticker entfernen");
					miRemove.addSelectionListener(new SelectionAdapter() {
						
						@Override
						public void widgetSelected(SelectionEvent e){
							MenuItem mi = (MenuItem) e.getSource();
							ISticker et = (ISticker) mi.getData("sticker");
							StickerServiceHolder.get().removeSticker(et, actPatient);
							// refresh
							setPatient(actPatient);
							getParent().getParent().layout(true);
						}
						
					});
					menu.addMenuListener(new MenuAdapter() {
						
						@Override
						public void menuShown(MenuEvent e){
							miRemove.setEnabled(
								CoreHub.acl.request(AccessControlDefaults.KONTAKT_ETIKETTE));
							miAdd.setEnabled(
								CoreHub.acl.request(AccessControlDefaults.KONTAKT_ETIKETTE));
						}
					});
					
				}
			}
		} else {
			this.setVisible(true);
			Menu menu = new Menu(this);
		    setMenu(menu);
			createMenuItemAdd(menu);
		}
		layout();
	}

	private MenuItem createMenuItemAdd(Menu menu) {
		final MenuItem miAdd = new MenuItem(menu, SWT.NONE);
		miAdd.setText("Sticker hinzufügen");
		miAdd.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(SelectionEvent e){
				
				AssignStickerDialog assignStickerDialog =
						new AssignStickerDialog(getShell(), actPatient);
				if (assignStickerDialog.open() == MessageDialog.OK) {
					// refresh
					setPatient(actPatient);
					getParent().getParent().layout(true);
				}
			}
			
		});
		miAdd.setEnabled(
			CoreHub.acl.request(AccessControlDefaults.KONTAKT_ETIKETTE));
		return miAdd;
	}
}
