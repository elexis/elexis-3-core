package ch.elexis.core.ui.stock.handler;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;

import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.stock.dialogs.ImportArticleDialog;

public class ArticleImportHandler extends AbstractHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		ImportArticleDialog importArticleDialog =
			new ImportArticleDialog(UiDesk.getDisplay().getActiveShell());
		importArticleDialog.open();
		return null;
	}
}
