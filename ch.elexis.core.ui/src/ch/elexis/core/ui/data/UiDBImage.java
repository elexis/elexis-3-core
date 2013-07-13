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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;

import org.eclipse.swt.SWT;
import org.eclipse.swt.SWTException;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.DBImage;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.util.SWTHelper;

/**
 * This is a wrapper class for a {@link DBImage} element. It encapsulates
 * the basic core sticker and adds the respective graphic dependent elements.
 * 
 * @since 3.0.0
 */
public class UiDBImage  {
	private Logger log = LoggerFactory.getLogger(UiDBImage.class);
	private DBImage dbImage;
	
	public UiDBImage(DBImage dbImage){
		this.dbImage = dbImage;
	}
	
	public UiDBImage(String prefix, final String name, final InputStream source){
		this.dbImage = new DBImage(prefix, name);		
		try {
			ImageLoader iml = new ImageLoader();
			iml.load(source);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			iml.save(baos, SWT.IMAGE_PNG);
			dbImage.setBinary(DBImage.FLD_IMAGE, baos.toByteArray());
		} catch (IllegalArgumentException | SWTException e) {
			log.error("Error setting image object on DBImage "+dbImage.getLabel(), e);
		}
	}
	
	public Image getImage() {
		byte[] in = dbImage.getBinary(DBImage.FLD_IMAGE);
		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		try {
			ImageData idata = new ImageData(bais);
			Image ret = new Image(UiDesk.getDisplay(), idata);
			return ret;
		} catch (Exception ex) {
			SWTHelper.showError("Image Error", "Ungültiges Bild",
					"Das Bild ist ungültig " + ex.getMessage());
			return null;
		}
	}

	public Image getImageScaledTo(int width, int height, boolean bShrinkOnly) {
		byte[] in = dbImage.getBinary(DBImage.FLD_IMAGE);
		ByteArrayInputStream bais = new ByteArrayInputStream(in);
		try {
			ImageData idata = new ImageData(bais);
			if (idata.width != width || idata.height != height) {
				idata = idata.scaledTo(width, height);
			}
			Image ret = new Image(UiDesk.getDisplay(), idata);
			return ret;
		} catch (Exception ex) {
			SWTHelper.showError("Image Error", "Ungültiges Bild",
					"Das Bild ist ungültig " + ex.getMessage());
			return null;
		}

	}

	/**
	 * @see {@link DBImage#getId()}
	 */
	public String getId() {
		return dbImage.getId();
	}

	/**
	 * @see {@link DBImage#getName()}
	 */
	public String getName() {
		return dbImage.getName();
	}
}
