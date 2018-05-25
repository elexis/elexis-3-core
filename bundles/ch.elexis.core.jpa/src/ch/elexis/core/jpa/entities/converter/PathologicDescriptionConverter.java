package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.types.PathologicDescription;
import ch.elexis.core.types.PathologicDescription.Description;

@Converter
public class PathologicDescriptionConverter implements AttributeConverter<PathologicDescription, String> {

	@Override
	public String convertToDatabaseColumn(PathologicDescription attribute) {
		if (attribute != null) {
			return attribute.toString();
		}
		return null;
	}

	@Override
	public PathologicDescription convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return new PathologicDescription(Description.UNKNOWN);
		}
		return PathologicDescription.of(dbData);
	}
}
