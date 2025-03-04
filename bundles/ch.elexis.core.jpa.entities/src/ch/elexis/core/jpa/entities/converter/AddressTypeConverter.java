package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.types.AddressType;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class AddressTypeConverter implements AttributeConverter<AddressType, String> {

	@Override
	public String convertToDatabaseColumn(AddressType attribute) {
		if (attribute != null) {
			return Integer.toString(attribute.getValue());
		}
		return null;
	}

	@Override
	public AddressType convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return AddressType.PLACE_OF_RESIDENCE;
		}
		return AddressType.get(Integer.parseInt(dbData));
	}
}
