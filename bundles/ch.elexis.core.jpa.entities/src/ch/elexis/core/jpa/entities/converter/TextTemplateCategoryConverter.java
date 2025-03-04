package ch.elexis.core.jpa.entities.converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.types.TextTemplateCategory;
import jakarta.persistence.AttributeConverter;
import jakarta.persistence.Converter;

@Converter
public class TextTemplateCategoryConverter implements AttributeConverter<TextTemplateCategory, String> {

	@Override
	public String convertToDatabaseColumn(TextTemplateCategory attribute) {
		if (attribute != null) {
			return attribute.getLiteral();
		}
		return null;
	}

	@Override
	public TextTemplateCategory convertToEntityAttribute(String dbData) {
		if (StringUtils.isEmpty(dbData)) {
			return null;
		}
		return TextTemplateCategory.get(dbData);
	}
}
