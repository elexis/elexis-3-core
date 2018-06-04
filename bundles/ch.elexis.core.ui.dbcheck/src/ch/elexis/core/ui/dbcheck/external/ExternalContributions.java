/*******************************************************************************
 * Copyright (c) 2011, MEDEVIT OG and MEDELEXIS AG
 * All rights reserved.
 *******************************************************************************/
package ch.elexis.core.ui.dbcheck.external;

import java.util.LinkedList;
import java.util.List;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Platform;
import org.eclipse.core.runtime.Status;
import org.eclipse.ui.statushandlers.StatusManager;

import ch.elexis.core.ui.dbcheck.Activator;

public class ExternalContributions {
	
	static List<ExternalMaintenance> ext = null;
	
	public static List<ExternalMaintenance> getExt(){
		if (ext == null) {
			ext = new LinkedList<ExternalMaintenance>();
			instantiate();
		}
		return ext;
	}
	
	private static void instantiate(){
		IConfigurationElement[] config =
			Platform.getExtensionRegistry().getConfigurationElementsFor(
				Activator.PLUGIN_ID + ".ExternalMaintenance");
		if (config.length == 0)
			return;
		for (IConfigurationElement e : config) {
			try {
				Object o = e.createExecutableExtension("MaintenanceCode");
				if (o instanceof ExternalMaintenance) {
					ext.add((ExternalMaintenance) o);
				}
			} catch (CoreException e1) {
				Status status =
					new Status(IStatus.WARNING, Activator.PLUGIN_ID, e1.getLocalizedMessage());
				StatusManager.getManager().handle(status, StatusManager.SHOW);
			}
			
		}
		
	}
	
// public static Composite getExternalContributions(Composite comp, final StyledText outputField){
// final ProgressMonitorDialog pmd = new ProgressMonitorDialog(null);
// IConfigurationElement[] config =
// Platform.getExtensionRegistry().getConfigurationElementsFor(
// Activator.PLUGIN_ID + ".ExternalMaintenance");
// try {
// for (IConfigurationElement e : config) {
// final Object o = e.createExecutableExtension("MaintenanceCode");
// if (o instanceof ExternalMaintenance) {
// Label lbl = new Label(comp, SWT.None);
// lbl.setText(((ExternalMaintenance) o).getMaintenanceDescription());
// Button b = new Button(comp, SWT.None);
// b.setText("RUN");
// final ExecExternalContribution irp =
// new ExecExternalContribution((ExternalMaintenance) o);
// b.addSelectionListener(new SelectionAdapter() {
//
// @Override
// public void widgetSelected(SelectionEvent e){
// try {
// pmd.run(false, false, irp);
// outputField.setText(irp.getOutput());
// } catch (InvocationTargetException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// } catch (InterruptedException e1) {
// // TODO Auto-generated catch block
// e1.printStackTrace();
// }
//
// }
//
// });
// }
// }
// } catch (CoreException ex) {
// System.out.println(ex.getMessage());
// }
//
// return null;
// }
	
}
