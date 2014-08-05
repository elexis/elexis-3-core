package ch.elexis.core.ui.util;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.MessageBox;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.views.codesystems.CodeSelectorFactory;
import ch.elexis.data.Artikel;
import ch.elexis.data.PersistentObject;
import ch.elexis.data.Query;

public class LagerartikelUtil {
	
	public static List<Artikel> getAllLagerartikel(){
		List<Artikel> ret = Artikel.getLagerartikel();
		// add articles which are not in the ARTIKEL db table ... by classname!?
		// put additional article class names in isAdditionalArticle
		// TODO move Lager information out of Artikel, solves this hack
		for (CodeSelectorFactory csf : getArticleCodeSelectorFactories()) {
			Class<? extends PersistentObject> elementClass = csf.getElementClass();
			
			if (elementClass != null && Artikel.class.isAssignableFrom(elementClass)
				&& isAdditionalArticle(elementClass)) {
				Query<Artikel> qbe = new Query<Artikel>(elementClass);
				qbe.add(Artikel.MINBESTAND, Query.GREATER, StringConstants.ZERO);
				qbe.or();
				qbe.add(Artikel.MAXBESTAND, Query.GREATER, StringConstants.ZERO);
				List<Artikel> l = qbe.execute();
				ret.addAll(l);
			}
		}
		
		return ret;
	}
	
	protected static boolean isAdditionalArticle(Class<? extends PersistentObject> elementClass){
		return elementClass.getSimpleName().equals("ArtikelstammItem");
	}
	
	private static List<CodeSelectorFactory> getArticleCodeSelectorFactories(){
		List<IConfigurationElement> list =
			Extensions.getExtensions(ExtensionPointConstantsUi.VERRECHNUNGSCODE);
		List<CodeSelectorFactory> csfList = new ArrayList<CodeSelectorFactory>();
		
		for (int i = 0; i < list.size(); i++) {
			IConfigurationElement ce = list.get(i);
			try {
				if (!"Artikel".equals(ce.getName())) {
					continue;
				}
				csfList.add((CodeSelectorFactory) ce
					.createExecutableExtension("CodeSelectorFactory"));
			} catch (Exception ex) {
				MessageBox mb = new MessageBox(UiDesk.getTopShell(), SWT.ICON_ERROR | SWT.OK);
				mb.setText(ch.elexis.core.ui.views.artikel.Messages.ArtikelView_errorCaption);
				mb.setMessage(ch.elexis.core.ui.views.artikel.Messages.ArtikelView_errorText
					+ ce.getName() + ":\n" //$NON-NLS-1$
					+ ex.getLocalizedMessage());
				mb.open();
			}
		}
		return csfList;
	}
}
