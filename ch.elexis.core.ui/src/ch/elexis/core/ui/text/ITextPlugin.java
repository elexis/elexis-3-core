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

package ch.elexis.core.ui.text;

import java.io.InputStream;

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.data.interfaces.text.ReplaceCallback;
import ch.elexis.core.ui.views.textsystem.TextTemplateView;

/**
 * Contract for embedding a text plugin Warning: Preliminary interface
 * 
 */
public interface ITextPlugin extends IExecutableExtension {
	
	public static enum PageFormat {
		USER, A4, A5
	};
	
	/** Return format of the page */
	public PageFormat getFormat();
	
	/** Set Format of the page */
	public void setFormat(PageFormat f);
	
	public static enum Parameter {
			NOUI, READ_ONLY
	};
	
	/**
	 * Pass parameters to the plugin instance. If the plugin considers the parameter depends on the
	 * specific implementations.
	 * 
	 * @param parameter
	 */
	public void setParameter(Parameter parameter);
	
	/**
	 * Create and return an SWT Coomposite that holds the editor
	 * 
	 * @param parent
	 *            parent component
	 * @param handler
	 *            Handler for saving the document
	 * @return
	 */
	public Composite createContainer(Composite parent, ICallback handler);
	
	/**
	 * The text component receives the focus
	 */
	public void setFocus();
	
	/**
	 * Text component is disposed. All ressources must be freed
	 */
	public void dispose();
	
	/**
	 * Show or hide the component specific menu bar
	 * 
	 * @param b
	 *            true: show
	 */
	public void showMenu(boolean b);
	
	/**
	 * Show or hide the component specific toolbar
	 */
	public void showToolbar(boolean b);
	
	/**
	 * Save contents on focus lost
	 * 
	 * @param bSave
	 *            true: yes, else no.
	 */
	public void setSaveOnFocusLost(boolean bSave);
	
	/**
	 * Create an empty document inside the container
	 * 
	 * @return
	 */
	public boolean createEmptyDocument();
	
	/**
	 * create a document from a byte array. The array contains the document in the (arbitrary)
	 * specific format of the text component. Prefarably, but not necessarily OpenDocumentFormat
	 * (odf)
	 * 
	 * @param bs
	 *            the byte array with the document in a fotmat the compnent can interpret
	 * @param asTemplate
	 *            tru if the byte array is a template.
	 * @return true on success
	 */
	public boolean loadFromByteArray(byte[] bs, boolean asTemplate);
	
	/**
	 * create a document from an InputStream that vontains the contents in the specific formar of
	 * the text component.
	 * 
	 * @return true on success
	 */
	public boolean loadFromStream(InputStream is, boolean asTemplate);
	
	/**
	 * Find a pattern (regular expression) in the document, and call ReplaceCallback with each
	 * match. Replace the found pattern with the replacment String received from ReplaceCallback.
	 * 
	 * @param pattern
	 *            a regular expression
	 * @param cb
	 *            a ReplaceCallback or null if no Replacement should be performed
	 * @return true if pattern was found at least once
	 */
	public boolean findOrReplace(String pattern, ReplaceCallback cb);
	
	/**
	 * Store the document into the byte array. The format can be any component specific type. The
	 * only requiremend is, that loadFromByteArray() must be able to recreate the document out of
	 * this byte array identically.
	 * 
	 * @return a Byte Array with the contents of the document.
	 */
	public byte[] storeToByteArray();
	
	/**
	 * Insert a table in the document
	 * 
	 * @param place
	 *            a regular expression in the document, that will be replaced by the table. Only the
	 *            first match will be used.
	 * @param properties
	 *            OR'ed property values
	 * @param contents
	 *            the contents of the table
	 * @param columnSizes
	 *            width of the columns or null: Autofit all columns.
	 * @return true on success.
	 */
	public abstract boolean insertTable(String place, int properties, String[][] contents,
		int[] columnSizes);
	
	// Properties for tables
	public final static int FIRST_ROW_IS_HEADER = 0x0001; // If set, the first row will be formatted
	// differently
	
	// TODO make grid lines invisible for tables - Für die Rezept-Tabellen die Linien unsichtbar
	// machen
	// public final static int GRID_VISIBLE = 0x0002; // If set, Grid Lines will be visible
	// Nein, auf 0 setzen geht nicht, mit 1 oder 0 kommt lediglich die RP Erzeugung durcheinander
	// und frägt 2x nach der Adresse, mit 0 bringt sie keinen Inhalt,
	public final static int GRID_VISIBLE = 0x0002; // If set, Grid Lines will be visible
	public final static String ENTRY_TYPE_TABLE = "TypeTable";
	public final static String ENTRY_TYPE_TEXT = "TypeText";
	
	/**
	 * Position text into a rectangular area.
	 * 
	 * @param adjust
	 *            SWT.LEFT, SWT.RIGHT, SWT.CENTER
	 * @param x
	 *            ,y,w,h position and size of the rectangle relative to the page bounds. Measured as
	 *            millimeters. The effective position and size must match the given values at +-
	 *            2mm.
	 * @param text
	 *            the text to insert, which can contain '\n' that must be honored.
	 * @return an implementation specific cursor that allows a later insert at the same position
	 *         (mit insertText(Object,text,adjust)
	 * */
	public Object insertTextAt(int x, int y, int w, int h, String text, int adjust);
	
	/**
	 * Set font for all following operations (until the next call to setFont)
	 * 
	 * @param name
	 *            name of the font
	 * @param style
	 *            SWT.MIN, SWT.NORMAL, SWT.BOLD (thin, normal or bold)
	 * @param size
	 *            font height in Pt
	 * @return false on error. True on success, what might mean however, that not the specified font
	 *         but a similar font was set.
	 */
	public boolean setFont(String name, int style, float size);
	
	/**
	 * Set style for all following operations (until the next call to setFont or setStyle)
	 * 
	 * @param style
	 *            SWT.MIN, SWT.NORMAL, SWT.BOLD (thin, normal or bold)
	 * @return false on error. True on success
	 */
	public boolean setStyle(int style);
	
	/**
	 * Insert text at a position specified by a regular expression
	 * 
	 * @param marke
	 *            regular expression, that describes the insertion point. Only the first match will
	 *            be used, and the inserted Text will replace the found string.
	 * @param adjust
	 *            SWT.LEFT oder SWT.RIGHT
	 * @return An implementation specific cursor thar allows a later insert after that position
	 * */
	public Object insertText(String marke, String text, int adjust);
	
	/**
	 * Insert text at the position described by the implemetation specific cursor
	 * 
	 * @param pos
	 *            an implementation specific cursor
	 * @param adjust
	 *            SWT.LEFT, SWT.CENTER, SWT.RIGHT
	 * @return a cursor that can be used for a subsequent insert.
	 * */
	public Object insertText(Object pos, String text, int adjust);
	
	/**
	 * clear the document
	 * 
	 * @return
	 */
	public abstract boolean clear();
	
	/**
	 * Print the document
	 * 
	 * @param toPrinter
	 *            Name of the Printers or null (then to default printer)
	 * @param toTray
	 *            Name of the tray or null (then tray as defined y the driver or the template)
	 * @param waitUntilFinished
	 *            if true: return after the printJob ist finished
	 * @return true on success
	 */
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished);
	
	/**
	 * Default Mimettype of the documents that this implementation creates
	 */
	public String getMimeType();
	
	/**
	 * does the plugin want to output the document immediately after creating
	 * 
	 * @return true if output should start immerdiately, false if the user triggers output manually
	 */
	public boolean isDirectOutput();
	
	/**
	 * Callback interface for save operations
	 * 
	 */
	public interface ICallback {
		public void save();
		
		public boolean saveAs();
	}
	
	/**
	 * Does load printer and tray preferences if they are defined (can be done in
	 * {@link TextTemplateView}). Settings will be considered when
	 * {@link ITextPlugin#print(String, String, boolean)} is called or
	 * {@link ITextPlugin#createContainer(Composite, ICallback)} is executed (applied as default
	 * printer/tray).
	 * 
	 * If settings are {@code null} defaults will be applied.
	 * 
	 * @param template
	 *            name to identify the template
	 * @since 3.1
	 */
	public void initTemplatePrintSettings(String template);
}
