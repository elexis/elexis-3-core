package ch.elexis.core.ui.commands;

import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.swt.program.Program;

import ch.elexis.core.model.IArticle;
import ch.elexis.core.model.IPrescription;
import ch.elexis.core.services.holder.ContextServiceHolder;

public class OpenCompendiumHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		// get actual fix medication of the patient
		Optional<IPrescription> medication =
			ContextServiceHolder.get().getTyped(IPrescription.class);
		if (medication.isPresent()) {
			IArticle article = medication.get().getArticle();
			String gtin = article.getGtin();
			if (StringUtils.isNotBlank(gtin)) {
				String url =
					"http://www.compendium.ch/prod/gtin/" + article.getGtin(); //$NON-NLS-1$
				Program.launch(url);
				return null;
			} else if (StringUtils.isNotBlank(article.getName())) {
				// https://compendium.ch/search/BEXIN
				String url =
					"http://www.compendium.ch/search/products?q=" + article.getName().trim(); //$NON-NLS-1$
				Program.launch(url);
				return null;
			}
		}
		String url = "http://www.compendium.ch/search/de"; //$NON-NLS-1$
		Program.launch(url);
		return null;
	}
}
