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
package ch.elexis.core.ui.constants;


/**
 * This class contains ID constants to be reference within the Eclipse UI. Each
 * of the constants in this class forms a contract to be fulfilled by any of the
 * available plugins. That is, there has to be respective instantiations on the
 * given extension points available for all IDs for the system to run.
 *
 * @since 3.0.0
 */
public class UiResourceConstants {

	public static final String PatientPerspektive_ID = "ch.elexis.PatientPerspective";
	
	public static final String LaborView_ID = "ch.elexis.Labor";
	public static final String PatientenListeView_ID = "ch.elexis.PatListView";
	public static final String PatientDetailView2_ID = "ch.elexis.PatDetail_v2";

}
