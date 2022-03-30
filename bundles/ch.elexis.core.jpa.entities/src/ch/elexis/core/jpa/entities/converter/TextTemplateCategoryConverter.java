package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.types.TextTemplateCategory;

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
