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
package ch.elexis.core.ui.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.data.Sticker;
import ch.elexis.core.icons.ImageSize;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.UiDesk;

/**
 * Eine Markierung für im Prinzip beliebige Objekte. Ein Objekt, das eine Etikette hat, kann diese
 * Etikette zur Darstellung verwenden
 * 
 * @author gerry
 * @since 3.0.0
 */
public class UiSticker extends Sticker {
	public static final String IMAGE_ID = "BildID";
	public static final String BACKGROUND = "bg";
	public static final String FOREGROUND = "vg";
	
	public UiSticker(String name, Color fg, Color bg){
		super(name);
		if (fg == null) {
			fg = UiDesk.getColor(UiDesk.COL_BLACK);
		}
		if (bg == null) {
			bg = UiDesk.getColor(UiDesk.COL_WHITE);
		}
		set(new String[] {
			FOREGROUND, BACKGROUND
		}, new String[] {
			UiDesk.createColor(fg.getRGB()), UiDesk.createColor(bg.getRGB())
		});
	}
	
	public Composite createForm(Composite parent){
		Composite ret = new Composite(parent, SWT.NONE);
		ret.setLayout(new GridLayout(2, false));
		Image img = getImage();
		
		GridData gd1 = null;
		GridData gd2 = null;

		Composite cImg = new Composite(ret, SWT.NONE);
		if (img != null) {
			cImg.setBackgroundImage(img);
			gd1 = new GridData(img.getBounds().width, img.getBounds().height);
			gd2 = new GridData(SWT.DEFAULT, img.getBounds().height);
		} else {
			gd1 = new GridData(10, 10);
			gd2 = new GridData(SWT.DEFAULT, SWT.DEFAULT);
		}
		cImg.setLayoutData(gd1);
		Label lbl = new Label(ret, SWT.NONE);
		lbl.setLayoutData(gd2);
		lbl.setText(getLabel());
		lbl.setForeground(getForeground());
		lbl.setBackground(getBackground());
		return ret;
	}
	
	public Image getImage(){
		Image ret = null;
		DBImage image = DBImage.load(get(IMAGE_ID));
		if (image != null) {
			ret = Images.lookupImage(image.getName(), ImageSize._16x16_DefaultIconSize);
		}
		return ret;
	}
	
	public void setImage(DBImage image){
		set(IMAGE_ID, image.getId());
	}
	
	public void setForeground(String fg){
		set(FOREGROUND, fg);
	}
	
	public void setForeground(Color fg){
		if (fg != null) {
			set(FOREGROUND, UiDesk.createColor(fg.getRGB()));
		}
	}
	
	public Color getForeground(){
		String vg = get(FOREGROUND);
		return UiDesk.getColorFromRGB(vg);
		
	}
	
	public void setBackground(String bg){
		set(BACKGROUND, bg);
	}
	
	public void setBackground(Color bg){
		if (bg != null) {
			set(BACKGROUND, UiDesk.createColor(bg.getRGB()));
		}
	}
	
	public void register(){
		UiDesk.getImageRegistry().put(get(Sticker.NAME), new DBImageDescriptor(get(Sticker.NAME)));
	}
	
	public Color getBackground(){
		String bg = get(BACKGROUND);
		return UiDesk.getColorFromRGB(bg);
	}

	
	public static UiSticker load(String id){
		UiSticker ret = new UiSticker(id);
		if (!ret.exists()) {
			return null;
		}
		return ret;
	}
	
	protected UiSticker(String id){
		super(id);
	}
	
	protected UiSticker(){}
	
}
