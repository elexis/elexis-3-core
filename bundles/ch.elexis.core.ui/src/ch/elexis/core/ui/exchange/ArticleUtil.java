package ch.elexis.core.ui.exchange;

import org.apache.commons.lang3.StringUtils;

import ch.elexis.data.Artikel;
import ch.rgw.tools.StringTool;

public class ArticleUtil {
	public static String getEan(Artikel article){
		String ret = article.getEAN();
		if (StringUtils.isBlank(ret)) {
			Object value = article.getExtInfoStoredObjectByKey("EAN");
			if (value instanceof String && ((String) value).length() > 11) {
				ret = (String) value;
			}
		}
		return ret;
	}
	
	public static String getPharmaCode(Artikel article){
		String ret = article.getPharmaCode();
		if (StringUtils.isBlank(ret)) {
			Object value = article.getExtInfoStoredObjectByKey("Pharmacode");
			if (value instanceof String && ((String) value).length() == 7) {
				ret = (String) value;
			}
		}
		return StringTool.pad(StringTool.LEFT, '0', ret, 7);
	}
}
