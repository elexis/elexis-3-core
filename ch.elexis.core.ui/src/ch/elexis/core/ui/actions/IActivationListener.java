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
package ch.elexis.core.ui.actions;

import org.eclipse.ui.IPartListener2;

/**
 * Listen to a view being activated, related to {@link IPartListener2} used by
 * {@link GlobalEventDispatcher}
 * @since 3.0.0 extracted from GlobalEventDispatcher
 */
public interface IActivationListener {
	/**
	 * @see IPartListener2#partActivated(org.eclipse.ui.IWorkbenchPartReference)
	 * @param mode
	 */
	public void activation(boolean mode);

	/**
	 * @see IPartListener2#partVisible(org.eclipse.ui.IWorkbenchPartReference)
	 * @param mode
	 */
	public void visible(boolean mode);
}
