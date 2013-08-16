/*******************************************************************************
 * Copyright (c) 2013 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.data;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import ch.elexis.core.data.DBImage;
import ch.elexis.core.data.Sticker;
import ch.elexis.core.ui.UiDesk;

/**
 * This is a wrapper class for a {@link Sticker} element. It encapsulates
 * the basic core sticker and adds the respective graphic dependent elements.
 * 
 * @since 3.0.0
 */
public class UiSticker {
	private Sticker sticker;
	
	/**
	 * Instantiate a given {@link Sticker}
	 * @param sticker
	 */
	public UiSticker(Sticker sticker) {
		this.sticker = sticker;
	}
	
	public Image getImage(){
		Image ret = null;
		UiDBImage image = new UiDBImage(DBImage.load(sticker.get(Sticker.FLD_IMAGE_ID)));
		
		ret = UiDesk.getImageRegistry().get(image.getName());
		if(ret==null) {
			ret = image.getImageScaledTo(16, 16, false);
			if (ret!= null)
				UiDesk.getImageRegistry().put(image.getName(), ret);
		}
		
		return ret;
	}
	
	public void setImage(UiDBImage image){
		sticker.set(Sticker.FLD_IMAGE_ID, image.getId());
	}
	
	public Color getForeground(){
		return UiDesk.getColorFromRGB(sticker.getForeground());
	}
	
	public void setForeground(Color fg){
		if (fg != null) {
			sticker.setForeground(Sticker.FLD_FOREGROUND);
		}
	}
	
	public Color getBackground() {
		return UiDesk.getColorFromRGB(sticker.getBackground());
	}
		
	public void setBackground(Color bg){
		if (bg != null) {
			sticker.setBackground(UiDesk.createColor(bg.getRGB()));
		}
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
		lbl.setText(sticker.getLabel());
		lbl.setForeground(UiDesk.getColorFromRGB(sticker.getForeground()));
		lbl.setBackground(UiDesk.getColorFromRGB(sticker.getBackground()));
		return ret;
	}
	
}
