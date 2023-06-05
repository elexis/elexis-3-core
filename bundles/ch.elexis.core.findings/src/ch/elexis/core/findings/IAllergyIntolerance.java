/*******************************************************************************
 * Copyright (c) 2016 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.findings;

import java.time.LocalDate;
import java.util.MissingResourceException;
import java.util.Optional;
import java.util.ResourceBundle;

public interface IAllergyIntolerance extends IFinding {
	public enum AllergyIntoleranceCategory {
		UNKNOWN("unknown"), FOOD("food"), MEDICATION("medication"), ENVIRONMENT("environment"), BIOLOGIC("biologic");

		private String code;

		private AllergyIntoleranceCategory(String code) {
			this.code = code;
		}

		public String getCode() {
			return code;
		}

		public String getLocalized() {
			try {
				String localized = ResourceBundle.getBundle(ch.elexis.core.l10n.Messages.BUNDLE_NAME)
						.getString(this.getClass().getSimpleName() + "_" + this.name());
				return localized;
			} catch (MissingResourceException e) {
				return this.toString();
			}
		}
	}

	/**
	 * Get the allergy intolerance category.
	 *
	 * @return
	 */
	public AllergyIntoleranceCategory getCategory();

	/**
	 * Get the date the {@link IAllergyIntolerance} was documented.
	 *
	 * @return
	 */
	public Optional<LocalDate> getDateRecorded();
}
