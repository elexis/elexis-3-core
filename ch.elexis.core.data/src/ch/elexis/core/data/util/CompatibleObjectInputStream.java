/*******************************************************************************
 * Copyright (c) 2005-2013, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    MEDEVIT <office@medevit.at> - initial implementation
 *******************************************************************************/

package ch.elexis.core.data.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectStreamClass;

/**
 * For compatibility reasons only, avoids unfolding and class de-serialization errors in
 * {@link PersistentObject#fold}
 * 
 * @since 3.0.0
 */
public class CompatibleObjectInputStream extends ObjectInputStream {
	
	public CompatibleObjectInputStream(InputStream arg0) throws IOException{
		super(arg0);
	}
	
	@Override
	protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException,
		ClassNotFoundException{
		
		if (desc.getName().equals("ch.elexis.data.Kontakt$statL")) {
			return Thread.currentThread().getContextClassLoader()
				.loadClass("ch.elexis.core.data.Kontakt$statL");
		}
		
		if (desc.getName().equals("ch.elexis.util.MFUList$Entry")) {
			return Thread.currentThread().getContextClassLoader()
				.loadClass("ch.elexis.core.data.util.MFUList$Entry");
		}
		
		if (desc.getName().equals("ch.elexis.util.MFUList")) {
			return Thread.currentThread().getContextClassLoader()
				.loadClass("ch.elexis.core.data.util.MFUList");
		}
		
		if (desc.getName().equals("ch.rgw.tools.MFUList$Entry")) {
			return Thread.currentThread().getContextClassLoader()
				.loadClass("ch.elexis.core.data.util.MFUList$Entry");
		}
		
		if (desc.getName().equals("ch.rgw.tools.MFUList")) {
			return Thread.currentThread().getContextClassLoader()
				.loadClass("ch.elexis.core.data.util.MFUList");
		}
		
		if (desc.getName().equals("ch.elexis.admin.ACE")) {
			return Thread.currentThread().getContextClassLoader()
				.loadClass("ch.elexis.core.data.admin.ACE");
		}
		
		return super.resolveClass(desc);
	}
}
