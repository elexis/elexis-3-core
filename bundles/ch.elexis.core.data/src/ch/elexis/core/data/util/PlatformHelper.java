/*******************************************************************************
 * Copyright (c) 2007-2009, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.data.util;

import java.io.File;
import java.net.URL;

import org.eclipse.core.runtime.FileLocator;
import org.eclipse.core.runtime.Platform;
import org.osgi.framework.Bundle;

/**
 * 
 * 
 * @deprecated moved to ch.elexis.core.utils
 *
 */
public class PlatformHelper {
	public static String getBasePath(String pluginID){
		try {
			URL url = Platform.getBundle(pluginID).getEntry("/");
			url = FileLocator.toFileURL(url);
			String bundleLocation = url.getPath();
			File file = new File(bundleLocation);
			bundleLocation = file.getAbsolutePath();
			return bundleLocation;
		} catch (Throwable throwable) {
			return "";
		}
	}
}
