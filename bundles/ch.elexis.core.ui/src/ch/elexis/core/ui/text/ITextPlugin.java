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

import org.eclipse.core.runtime.IExecutableExtension;
import org.eclipse.swt.widgets.Composite;

import ch.elexis.core.ui.views.textsystem.TextTemplateView;

/**
 * Contract for embedding a text plugin Warning: Preliminary interface
 *
 */
public interface ITextPlugin extends IExecutableExtension, ch.elexis.core.text.ITextPlugin
{

	/**
	 * Create and return an SWT Coomposite that holds the editor
	 *
	 * @param parent  parent component
	 * @param handler Handler for saving the document
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
	 * @param b true: show
	 */
	public void showMenu(boolean b);

	/**
	 * Show or hide the component specific toolbar
	 */
	public void showToolbar(boolean b);

	/**
	 * Save contents on focus lost
	 *
	 * @param bSave true: yes, else no.
	 */
	public void setSaveOnFocusLost(boolean bSave);

	/**
	 * Print the document
	 *
	 * @param toPrinter         Name of the Printers or null (then to default
	 *                          printer)
	 * @param toTray            Name of the tray or null (then tray as defined y the
	 *                          driver or the template)
	 * @param waitUntilFinished if true: return after the printJob ist finished
	 * @return true on success
	 */
	public boolean print(String toPrinter, String toTray, boolean waitUntilFinished);
	
	/**
	 * does the plugin want to output the document immediately after creating
	 *
	 * @return true if output should start immerdiately, false if the user triggers
	 *         output manually
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
	 * {@link ITextPlugin#createContainer(Composite, ICallback)} is executed
	 * (applied as default printer/tray).
	 *
	 * If settings are {@code null} defaults will be applied.
	 *
	 * @param template name to identify the template
	 * @since 3.1
	 */
	public void initTemplatePrintSettings(String template);
}
