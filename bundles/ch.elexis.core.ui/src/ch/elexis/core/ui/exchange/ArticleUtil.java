package ch.elexis.core.ui.exchange;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.core.model.IArticle;
import ch.rgw.tools.StringTool;

public class ArticleUtil {
	public static String getEan(IArticle article) {
		String ret = article.getGtin();
		if (StringUtils.isBlank(ret)) {
			Object value = article.getExtInfo("EAN"); //$NON-NLS-1$
			if (value instanceof String && ((String) value).length() > 11) {
				ret = (String) value;
			}
		}
		return ret;
	}

	public static String getPharmaCode(IArticle article) {
		String ret = StringUtils.EMPTY;
		try {
			Method method = article.getClass().getMethod("getPHAR"); //$NON-NLS-1$
			ret = (String) method.invoke(article);
		} catch (NoSuchMethodException | SecurityException | IllegalAccessException | IllegalArgumentException
				| InvocationTargetException e) {
			// ignore no pharmacode available ...
		}
		if (StringUtils.isBlank(ret)) {
			Object value = article.getExtInfo("Pharmacode"); //$NON-NLS-1$
			if (value instanceof String && ((String) value).length() == 7) {
				ret = (String) value;
			}
		}
		return StringTool.pad(StringTool.LEFT, '0', StringUtils.defaultString(ret), 7);
	}
}
