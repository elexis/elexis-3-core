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

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.ImageLoader;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.status.ElexisStatus;
import ch.elexis.core.exceptions.PersistenceException;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.Hub;
import ch.elexis.core.ui.util.SWTHelper;
import ch.rgw.tools.StringTool;

/**
 * A DBImage is an Image stored in the database and retrievable by its name
 * 
 * @author gerry
 * 
 */
public class DBImage extends PersistentObject {
	public static final String DEFAULT_PREFIX = "ch.elexis.images";
	public static final String FLD_PREFIX = "Prefix";
	private static final String FLD_TITLE = "Titel";
	public static final String DATE = "Datum";
	public static final String FLD_IMAGE = "Bild";
	public static final String DBVERSION = "1.0.0";
	public static final String TABLENAME = "DBIMAGE";
	
	static {
		addMapping(TABLENAME, DATE_COMPOUND, FLD_PREFIX, "Titel=Title", FLD_IMAGE);
	}
	
	@Override
	public String getLabel(){
		StringBuilder sb = new StringBuilder();
		synchronized (sb) {
			sb.append(get(DATE)).append(" - ").append(get(FLD_TITLE))
				.append(StringConstants.OPENBRACKET).append(get(FLD_PREFIX))
				.append(StringConstants.CLOSEBRACKET);
			return sb.toString();
		}
	}
	
	public String getName(){
		return get(FLD_TITLE);
	}
	
	public DBImage(String prefix, final String name, final InputStream source){
		ImageLoader iml = new ImageLoader();
		if (StringTool.isNothing(prefix)) {
			prefix = DEFAULT_PREFIX;
		}
		try {
			iml.load(source);
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			iml.save(baos, SWT.IMAGE_PNG);
			create(null);
			set(new String[] {
				FLD_PREFIX, FLD_TITLE
			}, prefix, name);
			setBinary(FLD_IMAGE, baos.toByteArray());
		} catch (Exception ex) {
			ElexisStatus status =
				new ElexisStatus(ElexisStatus.ERROR, Hub.PLUGIN_ID, ElexisStatus.CODE_NONE,
					"Image error: Das Bild konnte nicht geladen werden " + ex.getMessage(), ex);
			throw new PersistenceException(status);
		}
	}
	
	public Image getImage() {
		byte[] in = getBinary(FLD_IMAGE);
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
		byte[] in = getBinary(FLD_IMAGE);
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
	
	public static DBImage find(String prefix, String name){
		Query<DBImage> qbe = new Query<DBImage>(DBImage.class);
		if (StringTool.isNothing(prefix)) {
			prefix = DEFAULT_PREFIX;
		}
		qbe.add(FLD_PREFIX, Query.EQUALS, prefix);
		qbe.add(FLD_TITLE, Query.EQUALS, name);
		List<DBImage> ret = qbe.execute();
		if (ret != null && ret.size() > 0) {
			return ret.get(0);
		}
		return null;
	}
	
	public static DBImage load(String id){
		DBImage ret = new DBImage(id);
		if (!ret.exists()) {
			return null;
		}
		return ret;
	}
	
	@Override
	protected String getTableName(){
		return TABLENAME;
	}
	
	protected DBImage(String id){
		super(id);
	}
	
	protected DBImage(){}
	
}
