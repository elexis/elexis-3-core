/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/
package ch.elexis.core.ui.dbcheck.external;

import org.eclipse.jface.viewers.LabelProvider;

public class ExtContributionsLabelProvider extends LabelProvider {
	
	@Override
	public String getText(Object element){
		String pack = element.getClass().getPackage().getName();
		String description = ((ExternalMaintenance) element).getMaintenanceDescription();
		return pack + ": " + description;
	}
	
}
