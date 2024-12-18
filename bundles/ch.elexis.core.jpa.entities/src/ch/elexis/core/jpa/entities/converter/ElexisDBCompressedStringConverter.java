/*******************************************************************************
 * Copyright (c) 2015 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.rgw.compress.CompEx;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

/**
 * Converts a string into a compressed array.
 *
 */
@Converter
public class ElexisDBCompressedStringConverter implements AttributeConverter<String, byte[]> {

	@Override
	public byte[] convertToDatabaseColumn(String objectValue) {
		if (objectValue == null)
			return new byte[0];
		return CompEx.Compress((String) objectValue, CompEx.ZIP);
	}

	@Override
	public String convertToEntityAttribute(byte[] dataValue) {
		if (dataValue == null || dataValue.length == 0)
			return StringUtils.EMPTY;
		try {
			byte[] exp = CompEx.expand(dataValue);
			return new String(exp, "UTF-8");
		} catch (Exception e) {
			e.printStackTrace();
			// If we face an error during un-compression we simply return an empty string
			// this should be better fixed in CompEx.expand
			return StringUtils.EMPTY;
		}
	}
}
