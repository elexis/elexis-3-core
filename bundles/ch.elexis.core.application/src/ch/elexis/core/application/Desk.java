/*******************************************************************************
 * Copyright (c) 2013-2025 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.application;

import org.eclipse.equinox.app.IApplication;
import org.eclipse.equinox.app.IApplicationContext;

import ch.elexis.core.application.eedep.Application;
import ch.elexis.core.constants.ElexisSystemPropertyConstants;
import ch.elexis.core.ui.e4.util.CoreUiUtil;

public class Desk implements IApplication {

	private IApplication application;

	/**
	 * @since 3.0.0 log-in has been moved from ApplicationWorkbenchAdvisor to this
	 *        method
	 * @since 3.10 major refactorings to persistence startup
	 * @since 3.13 split into legacy and ee-dependent implementation
	 */
	@Override
	public Object start(IApplicationContext context) throws Exception {

		if (ElexisSystemPropertyConstants.IS_EE_DEPENDENT_OPERATION_MODE) {
			application = new Application();
		} else {
			application = new LegacyApplication();
		}

		CoreUiUtil.injectServices(application);

		return application.start(context);
	}

	protected void initIdentifiers() {

	}

	@Override
	public void stop() {
		application.stop();
	}
}
