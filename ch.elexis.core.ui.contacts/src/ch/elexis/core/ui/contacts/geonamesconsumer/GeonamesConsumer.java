/*******************************************************************************
 * Copyright (c) 2012 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 * 
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.ui.contacts.geonamesconsumer;

import at.medevit.elexis.geonames.interfaces.IGeonameService;

public class GeonamesConsumer {
	
	private static IGeonameService geonameService;
	
	public synchronized void setGeonameService(IGeonameService geonameService) {
		GeonamesConsumer.geonameService = geonameService;
		//System.out.println(geonameService.getClass().getName());
	}
	
	public synchronized void unsetGeonameService(IGeonameService geonameService) {
		if(GeonamesConsumer.geonameService == geonameService) {
			GeonamesConsumer.geonameService = null;
		}
	}

	public static IGeonameService getGeonameService(){
		return geonameService;
	}
}
