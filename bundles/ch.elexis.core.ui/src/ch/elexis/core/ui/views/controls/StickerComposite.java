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
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.ui.forms.widgets.ColumnLayout;
import org.eclipse.ui.forms.widgets.ColumnLayoutData;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.TableWrapData;
import org.eclipse.ui.forms.widgets.TableWrapLayout;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.UiSticker;
import ch.elexis.core.ui.dialogs.AssignStickerDialog;
import ch.elexis.data.Patient;
import ch.elexis.data.Sticker;

public class StickerComposite extends Composite {
	
	private FormToolkit toolkit;
	
	public StickerComposite(Composite parent, int style, FormToolkit toolkit){
		super(parent, style);
		setLayoutData(new TableWrapData(TableWrapData.FILL_GRAB, TableWrapData.TOP, 1, 1));
		ColumnLayout cwl = new ColumnLayout();
		cwl.maxNumColumns = 4;
		cwl.horizontalSpacing = 1;
		cwl.bottomMargin = 1;
		cwl.topMargin = 1;
		cwl.rightMargin = 1;
		cwl.leftMargin = 1;
		setLayout(cwl);
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
	
	public void setPatient(final Patient p){
		for (Control cc : getChildren()) {
			cc.dispose();
		}
		List<ISticker> etis = p.getStickers();
		if (etis == null)
			return;
		if (etis.size() > 0) {
			this.setVisible(true);
			for (ISticker et : etis) {
				if (et != null) {
					Composite stickerForm = createForm(this, et);
					Menu menu = new Menu(stickerForm);
					stickerForm.setMenu(menu);
					stickerForm.setLayoutData(new ColumnLayoutData());
					
					final MenuItem miAdd = new MenuItem(menu, SWT.NONE);
					miAdd.setText("Sticker hinzufügen");
					miAdd.addSelectionListener(new SelectionAdapter() {
						
						@Override
						public void widgetSelected(SelectionEvent e){
							
							AssignStickerDialog assignStickerDialog =
								new AssignStickerDialog(getShell(), p);
							if (assignStickerDialog.open() == MessageDialog.OK) {
								// refresh
								setPatient(p);
								getParent().getParent().layout(true);
							}
						}
						
					});
					final MenuItem miRemove = new MenuItem(menu, SWT.NONE);
					miRemove.setData("sticker", et);
					miRemove.setText("Sticker entfernen");
					miRemove.addSelectionListener(new SelectionAdapter() {
						
						@Override
						public void widgetSelected(SelectionEvent e){
							MenuItem mi = (MenuItem) e.getSource();
							ISticker et = (ISticker) mi.getData("sticker");
							p.removeSticker(et);
							// refresh
							setPatient(p);
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
			this.setVisible(false);
		}
		layout();
	}
	
	public Composite createForm(Composite parent, ISticker st){
		Composite ret = new Composite(parent, SWT.NONE);
		
		GridLayout gl = new GridLayout(2, false);
		gl.marginHeight = 2;
		ret.setLayout(gl);
		Image img = new UiSticker((Sticker) st).getImage();
		
		GridData gd1 = null;
		GridData gd2 = null;
		
		Composite cImg = new Composite(ret, SWT.NONE);	
		if (img != null) {
			cImg.setBackgroundImage(img);
			gd1 = new GridData(img.getBounds().width, img.getBounds().height);
			gd2 = new GridData(SWT.DEFAULT, img.getBounds().height);
		} else {
			cImg.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
			gd1 = new GridData(getFont().getFontData()[0].getHeight(), 8);
			gd2 = new GridData(SWT.DEFAULT, SWT.DEFAULT);
		}
		cImg.setLayoutData(gd1);
		Label lbl = new Label(ret, SWT.NONE);
		lbl.setLayoutData(gd2);
		lbl.setText(st.getLabel());
		if (toolkit != null)
			toolkit.adapt(ret);
		UiSticker stu = new UiSticker((Sticker) st);
		ret.setBackground(stu.getBackground());
		lbl.setForeground(stu.getForeground());
		lbl.setBackground(Display.getDefault().getSystemColor(SWT.COLOR_WIDGET_BACKGROUND));
		return ret;
	}
	
}
