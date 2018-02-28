/*******************************************************************************
 * Copyright (c) 2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    M. Descher - initial implementation
 *******************************************************************************/
package ch.elexis.core.ui.icons;

import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.MissingResourceException;
import java.util.ResourceBundle;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.resource.ImageRegistry;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.widgets.Display;

import ch.elexis.core.jdt.NonNull;
import ch.elexis.core.ui.icons.urihandler.IconURLConnection;

/**
 * Central image repository. Supersedes the images managed in {@link Desk}. This image registry
 * takes its values from a properties file, namely <code>ch.elexis.iconset.properties</code>. This
 * properties file is to be provided, together with the respective icons by a contributing fragment.
 * See <code>ch.elexis.core.ui.icons</code> for the basic open source icons contribution.
 * 
 * @author M. Descher / MEDEVIT Austria
 */
public enum Images {
		/** The Elexis logo **/
		IMG_LOGO,
		/** Returning to some home place */
		IMG_HOME,
		/** An Address label */
		IMG_ADRESSETIKETTE,
		/** a label with patient data */
		IMG_PATIENTETIKETTE,
		/** a label with some identity number (e.g. for lab orders) */
		IMG_VERSIONEDETIKETTE,
		/** a bomb icon */
		IMG_BOMB,
		/** an organisation icon */
		IMG_ORGANISATION,
		/** an organisation add icon */
		IMG_ORGANISATION_ADD,
		/** a transparent and empty 16x16 image */
		IMG_EMPTY_TRANSPARENT,
		/** a telephone icon */
		IMG_TELEPHONE,
		/** a mobile phone */
		IMG_MOBILEPHONE,
		/** a fax */
		IMG_FAX,
		/** a mail icon */
		IMG_MAIL,
		/** a mail send icon */
		IMG_MAIL_SEND,
		/** a web icon */
		IMG_WEB,
		/** new comment */
		IMG_COMMENT_ADD,
		/** a contact detail icon */
		IMG_CONTACT_DETAIL,
		/** deleting items */
		IMG_DELETE,
		/** a male */
		IMG_MANN,
		/** a female */
		IMG_FRAU,
		/** a group of two persons */
		IMG_GROUP,
		/** a money icon */
		IMG_MONEY,
		/** a Very Important Person/Patient */
		IMG_VIP,
		/** a Very Important Person/Patient overlay Icon */
		IMG_VIP_OVERLAY,
		/** a printer */
		IMG_PRINTER,
			/** a big printer icon */
	IMG_PRINTER_BIG,
	/** a big printer icon */
	IMG_PRINT_FIX,
	/** a big printer icon */
	IMG_PRINT_RESERVE,
		/** a filter */
		IMG_FILTER,
		IMG_FOLDER,
		/** creating a new Object */
		IMG_NEW,
		/** importing items */
		IMG_IMPORT,
		/** exporting items */
		IMG_EXPORT, IMG_GOFURTHER,
		/** editing an item */
		IMG_EDIT,
		IMG_EDIT_DONE, IMG_EDIT_ABORT,
		/** warning */
		IMG_ACHTUNG,
		/** red bullet */
		IMG_BULLET_RED,
		/** green bullet */
		IMG_BULLET_GREEN,
		/** yellow bullet */
		IMG_BULLET_YELLOW,
		/** grey bullet */
		IMG_BULLET_GREY,
		/** ok */
		IMG_OK,
		/** tick */
		IMG_TICK,
		/** error */
		IMG_FEHLER,
		/** refresh/reload */
		IMG_REFRESH,
		/** wizard/doing things automagically */
		IMG_WIZARD,
		/** add something to an existing object */
		IMG_ADDITEM,
		/** remove something from an existing object */
		IMG_REMOVEITEM,
		/** excalamation mark red */
		IMG_AUSRUFEZ_ROT,
		/** exclamantion mark */
		IMG_AUSRUFEZ,
		/** computer network */
		IMG_NETWORK,
		/** a book */
		IMG_BOOK,
		/** a person */
		IMG_BOOKMARK_PENCIL, IMG_PERSON,
		/** a person with an OK mark */
		IMG_PERSON_OK,
		/** a person with an ADD sign */
		IMG_PERSON_ADD,
		/** a greyed out person */
		IMG_PERSON_GREY,
		/** a diskette symbol */
		IMG_DISK,
		/** a closed lock */
		IMG_LOCK_CLOSED,
		IMG_LOCK_CLOSED_YELLOW,
		IMG_LOCK_CLOSED_GREEN,
		IMG_LOCK_CLOSED_GREY,
		/** An opened lock */
		IMG_LOCK_OPEN,
		/** Clipboard symbol */
		IMG_CLIPBOARD,
		/** Arrow right */
		IMG_NEXT, IMG_NEXT_WO_SHADOW,
		/** Arrow left */
		IMG_PREVIOUS,
		/** Arrow up */
		IMG_ARROWUP,
		/** Arrow down */
		IMG_ARROWDOWN,
		/** Arrow down to rectangle */
		IMG_ARROWDOWNTORECT,
		/** Arrow to stop */
		IMG_ARROWSTOP, IMG_ARROWSTOP_WO_SHADOW, IMG_EYE_WO_SHADOW,
		/** undo */
		IMG_UNDO,
		/** a 8px pencil symbol */
		IMG_PENCIL_8PX,
		/** a pill symbol */
		IMG_PILL,
		/** a pill with an exclamation */
		IMG_PILL_EXCLAMATION_WO_SHADOW,
		/** a link symbol */
		IMG_LINK,
		/** Move to upper list */
		IMG_MOVETOUPPERLIST,
		/** Move to lower list */
		IMG_MOVETOLOWERLIST,
		/** clear input field */
		IMG_CLEAR,
		/** Perspective Konsultation */
		IMG_PERSPECTIVE_KONS,
		/** Perspective Contacts */
		IMG_PERSPECTIVE_CONTACTS,
		/** Perspective letters */
		IMG_PERSPECTIVE_LETTERS,
		/** Perspective leistungen */
		IMG_PERSPECTIVE_LEISTUNGEN,
		/** Perspective articles */
		IMG_PERSPECTIVE_ARTICLES,
		/** Perspective reminder */
		IMG_PERSPECTIVE_REMINDERS,
		/** Perspective bills */
		IMG_PERSPECTIVE_BILLS,
		/** Perspective blackboard */
		IMG_PERSPECTIVE_BBS,
		/** Perspective orders */
		IMG_PERSPECTIVE_ORDERS,
		/** a document of type text */
		IMG_DOCUMENT_TEXT,
		/** a multi document with clip */
		IMG_DOCUMENT_STAND_UP,
		/** a multi document of type text */
		IMG_DOCUMENT_STACK,
		/** a default document */
		IMG_DOCUMENT,
		/** add document */
		IMG_DOCUMENT_ADD,
		/** write document */
		IMG_DOCUMENT_WRITE,
		/** remove document */
		IMG_DOCUMENT_REMOVE, IMG_MENUBAR, IMG_TOOLBAR,
		/** recipe */
		IMG_RECIPE_FIX, IMG_RECIPE_RESERVE,
		/** a bill */
		IMG_BILL, IMG_VIEW_WORK_INCAPABLE, IMG_VIEW_CONSULTATION_DETAIL, IMG_VIEW_LABORATORY, IMG_VIEW_PATIENT_DETAIL, IMG_VIEW_RECIPES, IMG_DATABASE, IMG_CONFLICT, IMG_QUESTION_MARK, IMG_FLAG_AT, IMG_FLAG_DE, IMG_FLAG_CH, IMG_FLAG_FR, IMG_FLAG_IT, IMG_FLAG_FL,
		/** a generic group icon */
		IMG_CATEGORY_GROUP,
		/** a syringe, nozzle, injection icon */
		IMG_SYRINGE,
		/** small blocks */
		IMG_BLOCKS_SMALL,
		/** sync icon **/
		IMG_SYNC,
		/** a star icon **/
		IMG_STAR,
		/** a non-filed star icon **/
		IMG_STAR_EMPTY,
		/** stop icon **/
		IMG_STOP,
		/** covercard png **/
		IMG_COVERCARD,
		/** document copy png **/
		IMG_COPY, IMG_USER_SILHOUETTE, IMG_TABLE, IMG_NODE, IMG_NW_STATUS, IMG_GEAR,
		/** checkbox (checked) **/
		IMG_CHECKBOX,
		/** checkbox (unchecked) **/
		IMG_CHECKBOX_UNCHECKED,
		/** document stand/managemet **/
		IMG_DOC_STAND,
		/** system template **/
		IMG_DOC_SYS,
		/** jar icon **/
		IMG_JAR,
		/** fixmedi icon **/
		IMG_FIX_MEDI,
		/** reserve medi icon **/
		IMG_RESERVE_MEDI,
		/** symptomatic medi icon **/
		IMG_SYMPTOM_MEDI,
		/** need medi icon **/
		IMG_NEED_MEDI,
		/** sort after personal favorites **/
		IMG_SORT_STAR,
		/** daten gif **/
		IMG_DATA,
		/** edit a balance */
		IMG_BALANCE_EDIT,
		/** shopping cart **/
		IMG_CART,
		/** wizard for some day event **/
		IMG_WIZ_DAY, IMG_CALENDAR,
		IMG_COUNTER_STOP,
		IMG_USER_IDLE, 
		IMG_SYSTEM_MONITOR,
		IMG_BELL_EXCLAMATION,
		IMG_SORT_DATE,
		IMG_SORT_DATE_DESCENDING,
		IMG_CARDS;
		
	private Images(){}
	
	/**
	 * Returns an image. Clients do not need to dispose the image, it will be disposed
	 * automatically. Defaults to {@link ImageSize#_16x16_DefaultIconSize}
	 * 
	 * @return an {@link Image}
	 */
	public Image getImage(){
		return getImage(ImageSize._16x16_DefaultIconSize);
	}
	
	/**
	 * @return {@link ImageDescriptor} for the current image. Defaults to
	 *         {@link ImageSize#_16x16_DefaultIconSize}
	 */
	public ImageDescriptor getImageDescriptor(){
		return getImageDescriptor(ImageSize._16x16_DefaultIconSize);
	}
	
	/**
	 * Opportunistic lookup for a probably existing key.<br>
	 * There may exist keys within the <code>iconset.properties</code> file which are not managed by
	 * this Enumeration. This method allows an opportunistic lookup of such registered images.
	 * 
	 * @param iconKey
	 * @return <code>null</code> if no such image is existent
	 */
	public static Image lookupImage(String iconKey, ImageSize is){
		Image image = JFaceResources.getImageRegistry().get(iconKey + is.name);
		if (image == null) {
			boolean ret = addIconImageDescriptor(iconKey, is);
			if (!ret)
				return null;
			image = JFaceResources.getImageRegistry().get(iconKey);
		}
		return image;
	}
	
	/**
	 * Opportunistic lookup for a probably existing key.<br>
	 * 
	 * @param iconKey
	 * @param is
	 * @return
	 */
	public static ImageDescriptor lookupImageDescriptor(@NonNull String iconKey,
		@NonNull ImageSize is){
		return getImageDescriptor(iconKey, is);
	}
	
	/**
	 * Returns an image. Clients do not need to dispose the image, it will be disposed
	 * automatically.
	 * 
	 * @return an {@link Image}
	 */
	public synchronized Image getImage(ImageSize is){
		Image image = JFaceResources.getImageRegistry().get(this.name() + is.name);
		if (image == null) {
			addIconImageDescriptor(this.name(), is);
			image = JFaceResources.getImageRegistry().get(this.name() + is.name);
		}
		return image;
	}
	
	/**
	 * @return {@link ImageDescriptor} for the current image
	 */
	public ImageDescriptor getImageDescriptor(ImageSize is){
		return getImageDescriptor(this.name(), is);
	}
	
	/**
	 * 
	 * @return an {@link ImageDescriptor} of type URLImageDescriptor
	 * @since 3.3
	 */
	public ImageDescriptor getURLImageDescriptor(){
		try {
			URL imageDesciptorUrl = new URL(getIconURI());
			return ImageDescriptor.createFromURL(imageDesciptorUrl);
		} catch (MalformedURLException e) {
			e.printStackTrace();
		}
		return null;
	}
	
	/**
	 * resolve the image
	 * 
	 * @param imageName
	 * @param is
	 * @return
	 */
	private static ImageDescriptor getImageDescriptor(String imageName, ImageSize is){
		ImageDescriptor id = null;
		id = JFaceResources.getImageRegistry().getDescriptor(imageName + is.name);
		if (id == null) {
			addIconImageDescriptor(imageName, is);
			id = JFaceResources.getImageRegistry().getDescriptor(imageName + is.name);
		}
		return id;
	}
	
	/**
	 * @return a string to be embedded as iconURI, see beta plugin process for an example
	 */
	public String getIconURI(){
		return "icon://" + name();
	}
	
	/**
	 * Get the Icon as {@link InputStream}; used by the {@link IconURLConnection}
	 * 
	 * @param is
	 * @return <code>null</code> if any error in resolving the image
	 * @throws IOException
	 */
	public InputStream getImageAsInputStream(ImageSize is) throws IOException{
		InputStream ret = null;
		
		ResourceBundle iconsetProperties = ResourceBundle.getBundle("iconset");
		String fileName = iconsetProperties.getString(this.name());
		URL url =
			FileLocator.find(Activator.getContext().getBundle(), new Path("icons/" + is.name + "/"
				+ fileName), null);
		ret = url.openConnection().getInputStream();
		
		return ret;
	}
	
	/**
	 * Add an image descriptor for a specific key and {@link IconSize} to the global
	 * {@link ImageRegistry}
	 * 
	 * @param name
	 * @param is
	 * @return <code>true</code> if successfully added, else <code>false</code>
	 */
	private static boolean addIconImageDescriptor(String name, ImageSize is){
		String fileName;
		try {
			ResourceBundle iconsetProperties = ResourceBundle.getBundle("iconset");
			fileName = iconsetProperties.getString(name);
		} catch (MissingResourceException | IllegalArgumentException e) {
			fileName = name;
		}
		
		Path path = new Path("icons/" + is.name + "/" + fileName);
		URL fileLocation = FileLocator.find(Activator.getContext().getBundle(), path, null);
		if (fileLocation == null)
			return false;
		ImageDescriptor id = ImageDescriptor.createFromURL(fileLocation);
		JFaceResources.getImageRegistry().put(name + is.name, id);
		
		return true;
	}
	
	/**
	 * Return a resized (software scaled) version of an image, image will not be e
	 * 
	 * @param image
	 *            the image to resize
	 * @param is
	 *            the target {@link ImageSize}
	 * @return
	 */
	public static Image resize(Image image, ImageSize is){
		return resize(image, is.width, is.height);
	}
	
	/**
	 * Return a resized (software scaled) version of an image, image will not be disposed
	 * 
	 * @param image
	 *            the image to resize
	 * @param width
	 * @param height
	 * @return
	 */
	public static Image resize(Image image, int width, int height){
		return new Image(Display.getDefault(), image.getImageData().scaledTo(width, height));
	}
	
}
