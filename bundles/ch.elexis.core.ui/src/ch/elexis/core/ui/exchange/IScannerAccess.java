/*******************************************************************************
 * Copyright (c) 2007-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     G. Weirich - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.exchange;

import org.eclipse.swt.graphics.ImageData;

/**
 * Interface to specify a Java (SWT) connection to scanner devices e.g. via TWAIN or SANE
 * Implementing class must implement IScannerAccess and IScannerAccess.ISource.
 * 
 * It must return all available Sources on a call to getSources(), let the user configure a source
 * via configureSource(), and must aquire an image from the source on a call to aquire
 * 
 * @author gerry
 * 
 */
public interface IScannerAccess {
	
	/**
	 * Retrieve all available Sources (scanners, cameras etc)
	 * 
	 * @return an Array of SourceDefinitions, or null if there is no available Sources
	 */
	public ISource[] getSources();
	
	public ISource getDefaultSource();
	
	public void setDefaultSource(ISource src);
	
	/**
	 * let the user enter configuration details (e.g. resolution settings, color settings) with the
	 * scanner specific dialog
	 * 
	 * @param src
	 *            the scan source o configure
	 */
	public void configureSource(ISource src, Object configuration);
	
	/**
	 * Aquire an Image from the Scanner and create an SWT image from it. As every ISource has its
	 * own configuration stored, the TWAIN device will use the source configuration
	 * 
	 * @param src
	 *            the Scanner to use
	 * @return the image from the scanner
	 */
	public ImageData aquire(ISource src) throws Exception;
	
	/**
	 * An Image source
	 * 
	 */
	public interface ISource {
		/**
		 * Name of the source
		 * 
		 * @return a human readable name
		 */
		public String getName();
		
		/**
		 * A longer description
		 * 
		 * @return A longer description
		 */
		public String getDescription();
		
		/**
		 * Is the source ready at the moment?
		 * 
		 * @return true if it is ready
		 */
		public boolean isAvailable();
		
		/**
		 * The Source configuration (Color, Resolution, etc.)
		 * 
		 * @return an Object wit the configuration
		 */
		public Object getConfiguration();
		
	}
}
