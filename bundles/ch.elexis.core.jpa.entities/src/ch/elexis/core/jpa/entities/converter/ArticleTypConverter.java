package ch.elexis.core.jpa.entities.converter;

import javax.persistence.AttributeConverter;
import javax.persistence.Converter;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.types.ArticleTyp;

@Converter
public class ArticleTypConverter implements AttributeConverter<ArticleTyp, String> {

	@Override
	public String convertToDatabaseColumn(ArticleTyp attribute){
		if (attribute != null) {
			return attribute.getCodeSystemName();
		}
		return null;
	}

	@Override
	public ArticleTyp convertToEntityAttribute(String dbData){
		if (StringUtils.isEmpty(dbData)) {
			return ArticleTyp.ARTIKEL;
		}
		return ArticleTyp.fromCodeSystemName(dbData);
	}
}
