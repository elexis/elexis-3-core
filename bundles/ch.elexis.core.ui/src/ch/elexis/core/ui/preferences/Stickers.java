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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map.Entry;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.preference.PreferencePage;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchPreferencePage;

import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IDocumentLetter;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.model.IImage;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.holder.CoreModelServiceHolder;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.services.holder.StoreToStringServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.data.UiDBImage;
import ch.elexis.core.ui.dialogs.ImageChooser;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;

public class Stickers extends PreferencePage implements IWorkbenchPreferencePage {

	ComboViewer comboViewer;
	Canvas cImage, cFore, cBack;
	ISticker act;
	List<ISticker> lEtiketten;
	Button bNew, bRemove;
	Spinner spWert;
	HashMap<String, Button> classesCheck = new HashMap<>();

	void setSticker(ISticker et) {
		act = et;
		if (et == null) {
			cImage.setBackground(UiDesk.getColor(UiDesk.COL_WHITE));
			cFore.setBackground(UiDesk.getColor(UiDesk.COL_BLACK));
			cBack.setBackground(UiDesk.getColor(UiDesk.COL_LIGHTGREY));
			spWert.setSelection(0);
		} else {
			cFore.setBackground(CoreUiUtil.getColorForString(act.getForeground()));
			cBack.setBackground(CoreUiUtil.getColorForString(act.getBackground()));
			spWert.setSelection(act.getImportance());
			List<String> classes = StickerServiceHolder.get().getStickerClassLinksForSticker(act);
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
		comboViewer = new ComboViewer(ret, SWT.SIMPLE);
		comboViewer.setContentProvider(ArrayContentProvider.getInstance());
		comboViewer.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof ISticker) {
					return ((ISticker) element).getName();
				}
				return super.getText(element);
			}
		});
		comboViewer.getControl().setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		comboViewer.addSelectionChangedListener(e -> {
			if (!e.getStructuredSelection().isEmpty()) {
				setSticker((ISticker) e.getStructuredSelection().getFirstElement());
				bRemove.setEnabled(true);
			}
		});

		comboViewer.getCombo().addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				if (comboViewer.getCombo().getText().length() == 0) {
					bNew.setEnabled(false);
				} else {
					bNew.setEnabled(true);
				}

			}
		});
		comboViewer.setInput(lEtiketten);
		// new Label(ret,SWT.NONE).setText("Anzeige");
		Composite bottom = new Composite(ret, SWT.NONE);
		bottom.setLayout(new GridLayout(3, false));
		bNew = new Button(bottom, SWT.PUSH);
		bNew.setText(Messages.Stickers_NewSticker);
		bNew.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String name = comboViewer.getCombo().getText();
				ISticker sticker = CoreModelServiceHolder.get().create(ISticker.class);
				sticker.setName(name);
				sticker.setForeground("000000");
				sticker.setBackground("ffffff");
				CoreModelServiceHolder.get().save(sticker);
				lEtiketten.add(sticker);
				comboViewer.setInput(lEtiketten);
				comboViewer.setSelection(new StructuredSelection(sticker));
				setSticker(sticker);
			}
		});
		bRemove = new Button(bottom, SWT.PUSH);
		bRemove.setText(Messages.Stickers_DeleteSticker);
		bRemove.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = comboViewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty()) {
					ISticker eti = (ISticker) selection.getFirstElement();
					lEtiketten.remove(eti);
					comboViewer.setInput(lEtiketten);
					CoreModelServiceHolder.get().delete(eti);
					setSticker(null);
				}
			}
		});
		Group bottomRight = new Group(bottom, SWT.NONE);
		bottomRight.setLayoutData(SWTHelper.getFillGridData(1, true, 5, true));
		bottomRight.setLayout(new RowLayout(SWT.VERTICAL));
		bottomRight.setText(Messages.Stickers_useFor);
		addAssociateButton(Messages.Core_Patient, IPatient.class, bottomRight);
		addAssociateButton(Messages.Core_Consultation, IEncounter.class, bottomRight);
		addAssociateButton(Messages.Core_Case, ICoverage.class, bottomRight);
		addAssociateButton(Messages.Core_Document, IDocumentLetter.class, bottomRight);
		/*
		 * new Label(ret, SWT.SEPARATOR |
		 * SWT.HORIZONTAL).setLayoutData(SWTHelper.getFillGridData(2, false, 1, false));
		 */
		bottom.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cImage = new Canvas(bottom, SWT.BORDER);
		cImage.addPaintListener(new PaintListener() {

			@Override
			public void paintControl(PaintEvent e) {
				GC gc = e.gc;
				if (act != null) {
					Image img = act.getImage() != null ? CoreUiUtil.getImageAsIcon(act.getImage()) : null;
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
						UiDBImage dbImage = imc.getSelection();
						if (dbImage != null) {
							act.setImage(CoreModelServiceHolder.get().load(dbImage.getId(), IImage.class).get());
						} else {
							act.setImage(null);
						}
						CoreModelServiceHolder.get().save(act);
						setSticker(act);
					}
				}
			}
		});
		cFore = new Canvas(bottom, SWT.BORDER);
		GridData gdFore = new GridData(32, 16);
		cFore.setLayoutData(gdFore);
		Button bFore = new Button(bottom, SWT.PUSH);
		bFore.setText(Messages.Core_TextColour);
		bFore.addSelectionListener(new SelectionAdapter() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (act != null) {
					ColorDialog cd = new ColorDialog(getShell(), SWT.NONE);
					RGB rgb = cd.open();
					if (rgb != null) {
						act.setForeground(UiDesk.createColor(rgb));
						CoreModelServiceHolder.get().save(act);
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
						CoreModelServiceHolder.get().save(act);
					}
					setSticker(act);
				}
			}
		});
		spWert = new Spinner(bottom, SWT.NONE);
		spWert.setMaximum(999);
		spWert.setMinimum(0);
		spWert.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				if (act != null) {
					act.setImportance(spWert.getSelection());
					CoreModelServiceHolder.get().save(act);
				}
			}
		});
		new Label(bottom, SWT.NONE).setText(Messages.Stickers_ValueOfSticker);
		bNew.setEnabled(false);
		bRemove.setEnabled(false);
		return ret;
	}

	@Override
	public void init(IWorkbench workbench) {
		lEtiketten = CoreModelServiceHolder.get().getQuery(ISticker.class).execute();
		lEtiketten = new ArrayList<>(
				lEtiketten.stream().filter(et -> et.isVisible() && et.getImportance() < 1000).toList());
	}

	private void addAssociateButton(final String label, final Class<?> clazz, final Composite parent) {
		final Button button = new Button(parent, SWT.CHECK);
		button.setText(label);
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				IStructuredSelection selection = comboViewer.getStructuredSelection();
				if (selection != null && !selection.isEmpty()) {
					ISticker sticker = (ISticker) selection.getFirstElement();
					if (button.getSelection()) {
						StickerServiceHolder.get().setStickerAddableToClass(clazz, sticker);
					} else {
						StickerServiceHolder.get().removeStickerAddableToClass(clazz, sticker);
					}
				}
			}
		});
		classesCheck.put(StoreToStringServiceHolder.get().getTypeForModel(clazz), button);
	}
}
