/*******************************************************************************
 * Copyright (c) 2007-2015, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     G. Weirich - initial API and implementation
 *     M. Descher - adapted to RBAC
 ******************************************************************************/
package ch.elexis.core.data.propertyTester;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.expressions.PropertyTester;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.ObjectEvaluatableACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.services.holder.AccessControlServiceHolder;

public class ACETester extends PropertyTester {

	@Override
	public boolean test(Object receiver, String property, Object[] args, Object expectedValue) {
		if ("ACE".equals(property)) {
			if (args.length > 0) {
				if (ContextServiceHolder.get().getActiveUser().isPresent()) {
					String right = (String) args[0];
					if (StringUtils.isNotBlank(right)) {
						// ch.elexis.core.tasks.model.ITask:READ
						// ch.elexis.core.model.IInvoice:CREATE
						String[] parts = right.split(":");
						if (parts.length == 2 && getRight(parts[1]) != null) {
							return AccessControlServiceHolder.get()
									.evaluate(new ObjectEvaluatableACE(parts[0].trim(), getRight(parts[1])));
						} else {
							return AccessControlServiceHolder.get().evaluate(EvACE.of(right));
						}
					}
				}
			}
		}

		return false;
	}

	private Right getRight(String string) {
		try {
			return Right.valueOf(string);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass()).warn("Could not parse right [" + string + "]");
		}
		return null;
	}

}
