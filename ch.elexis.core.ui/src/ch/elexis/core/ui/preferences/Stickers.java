/*******************************************************************************
 * Copyright (c) 2008-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 * 
 *******************************************************************************/

package ch.elexis.core.ui.preferences;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.PaintEvent;
import org.eclipse.swt.events.PaintListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Canvas;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.data.Brief;
import ch.elexis.core.data.Fall;
import ch.elexis.core.data.Konsultation;
import ch.elexis.core.data.Patient;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.Sticker;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.UiSticker;
import ch.elexis.core.ui.dialogs.ImageChooser;
import ch.elexis.core.ui.util.SWTHelper;

public class Stickers extends PreferencePage implements
		IWorkbenchPreferencePage {

	Combo combo;
	Canvas cImage, cFore, cBack;
	Sticker act;
	List<Sticker> lEtiketten;
	Button bNew, bRemove;
	Spinner spWert;
	HashMap<String, Button> classesCheck = new HashMap<String, Button>();

	void setSticker(Sticker et) {
		act = et;
		if (et == null) {
			cImage.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			cFore.setBackground(UiDesk.getColor(UiDesk.COL_BLACK));
			cBack.setBackground(UiDesk.getColor(UiDesk.COL_LIGHTGREY));
			spWert.setSelection(0);
		} else {
			UiSticker uist = new UiSticker(et);
			cFore.setBackground(uist.getForeground());
			cBack.setBackground(uist.getBackground());
			spWert.setSelection(act.getWert());
			List<String> classes = et.getClassesForSticker();
			for (Entry<String, Button> e : classesCheck.entrySet()) {
				e.getValue().setSelection(false);
			}
			for (String st : classes) {
				Button b = classesCheck.get(st);
				if (b != null) {
					b.setSelection(true);
				}
			}
		}
		cImage.redraw();
		cFore.redraw();
		cBack.redraw();

	}

	@Override
	protected Control createContents(Composite parent) {
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout());
		ret.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		combo = new Combo(ret, SWT.SIMPLE);
		combo.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		combo.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = combo.getSelectionIndex();
				if (i > -1) {
					setSticker(lEtiketten.get(i));
					bRemove.setEnabled(true);
				}
			}

		});
		combo.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (combo.getText().length() == 0) {
					bNew.setEnabled(false);
				} else {
					bNew.setEnabled(true);
				}

			}
		});
		for (Sticker et : lEtiketten) {
			if (et.getVisibility())
				combo.add(et.getLabel());
		}
		// new Label(ret,SWT.NONE).setText("Anzeige");
		Composite bottom = new Composite(ret, SWT.NONE);
		bottom.setLayout(new GridLayout(3, false));
		bNew = new Button(bottom, SWT.PUSH);
		bNew.setText(Messages.Stickers_NewSticker);
		bNew.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String name = combo.getText();
				Sticker n = new Sticker(name, null, null);
				lEtiketten.add(n);
				combo.add(n.getLabel());
			}

		});
		bRemove = new Button(bottom, SWT.PUSH);
		bRemove.setText(Messages.Stickers_DeleteSticker);
		bRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int idx = combo.getSelectionIndex();
				if (idx > -1) {
					// String n=combo.getItem(idx);
					combo.remove(idx);
					Sticker eti = lEtiketten.get(idx);
					lEtiketten.remove(idx);
					eti.delete();
				}
			}
		});
		Group bottomRight = new Group(bottom, SWT.NONE);
		bottomRight.setLayoutData(SWTHelper.getFillGridData(1, true, 5, true));
		bottomRight.setLayout(new RowLayout(SWT.VERTICAL));
		bottomRight.setText(Messages.Stickers_useFor);
		addAssociateButton(Messages.Stickers_patient, Patient.class,
				bottomRight);
		addAssociateButton(Messages.Stickers_consultation, Konsultation.class,
				bottomRight);
		addAssociateButton(Messages.Stickers_case, Fall.class, bottomRight);
		addAssociateButton(Messages.Stickers_document, Brief.class, bottomRight);
		/*
		 * new Label(ret, SWT.SEPARATOR |
		 * SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, false, 1,
		 * false));
		 */
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cImage = new Canvas(bottom, SWT.BORDER);
		cImage.addPaintListener(new PaintListener() {

			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				if (act != null) {
					Image img = new UiSticker(act).getImage();
					if (img != null) {
						gc.drawImage(img, 0, 0);
						return;
					}
				}
				gc.setForeground(UiDesk.getColor(UiDesk.COL_GREY20));
				gc.fillRectangle(0, 0, 32, 32);
			}
		});
		GridData gdImage = new GridData(32, 32);
		cImage.setLayoutData(gdImage);
		Button bNewImage = new Button(bottom, SWT.PUSH);
		bNewImage.setText(Messages.Stickers_Image);
		bNewImage.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (act != null) {
					ImageChooser imc = new ImageChooser(getShell());
					if (imc.open() == Dialog.OK) {
						new UiSticker(act).setImage(imc.getSelection());
						setSticker(act);
					}
				}
			}
		});
		cFore = new Canvas(bottom, SWT.BORDER);
		GridData gdFore = new GridData(32, 16);
		cFore.setLayoutData(gdFore);
		Button bFore = new Button(bottom, SWT.PUSH);
		bFore.setText(Messages.Stickers_TextColor);
		bFore.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (act != null) {
					ColorDialog cd = new ColorDialog(getShell(), SWT.NONE);
					RGB rgb = cd.open();
					if (rgb != null) {
						act.setForeground(UiDesk.createColor(rgb));
					}
					setSticker(act);
				}
			}

		});
		cBack = new Canvas(bottom, SWT.BORDER);
		GridData gdBack = GridDataFactory.copyData(gdFore);
		cBack.setLayoutData(gdBack);
		Button bBack = new Button(bottom, SWT.PUSH);
		bBack.setText(Messages.Stickers_BackgroundColor);
		bBack.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (act != null) {
					ColorDialog cd = new ColorDialog(getShell(), SWT.NONE);
					RGB rgb = cd.open();
					if (rgb != null) {
						act.setBackground(UiDesk.createColor(rgb));
					}
					setSticker(act);
				}
			}

		});
		spWert = new Spinner(bottom, SWT.NONE);
		spWert.addModifyListener(new ModifyListener() {

			public void modifyText(ModifyEvent e) {
				if (act != null) {
					act.setWert(spWert.getSelection());
				}

			}
		});
		new Label(bottom, SWT.NONE).setText(Messages.Stickers_ValueOfSticker);
		bNew.setEnabled(false);
		bRemove.setEnabled(false);
		return ret;
	}

	public void init(IWorkbench workbench) {
		Query<Sticker> qbe = new Query<Sticker>(Sticker.class);
		lEtiketten = qbe.execute();
		if (lEtiketten != null) {

		} else {
			lEtiketten = new LinkedList<Sticker>();
		}

	}

	private void addAssociateButton(final String label, final Class<?> clazz,
			final Composite parent) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				int i = combo.getSelectionIndex();
				if (i > -1) {
					Sticker sticker = lEtiketten.get(i);

					if (button.getSelection()) {
						sticker.setClassForSticker(clazz);
					} else {
						sticker.removeClassForSticker(clazz);
					}
				}
			}
		});
		classesCheck.put(clazz.getName(), button);
	}
}
