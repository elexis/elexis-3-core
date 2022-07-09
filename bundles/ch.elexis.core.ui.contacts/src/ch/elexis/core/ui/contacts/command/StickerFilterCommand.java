package ch.elexis.core.ui.contacts.command;

import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;

import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.holder.StickerServiceHolder;
import ch.elexis.core.ui.contacts.views.PatientenListeView;
import ch.elexis.core.ui.dialogs.StickerSelectionDialog;
import ch.elexis.core.ui.util.viewers.CommonViewerContentProvider.QueryFilter;

public class StickerFilterCommand extends AbstractHandler implements IHandler {

	public static final String CMD_ID = "at.medevit.elexis.contacts.core.command.StickerFilter"; //$NON-NLS-1$
	public static final String STATE_ID = "org.eclipse.ui.commands.toggleState"; //$NON-NLS-1$

	private QueryFilter currentFilter;

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		boolean state = HandlerUtil.toggleCommandState(event.getCommand());
		PatientenListeView part = (PatientenListeView) HandlerUtil.getActivePart(event);

		if (state) {
			part.getContentProvider().removeQueryFilter(currentFilter);
			part.reload();
			currentFilter = null;
		} else {
			StickerSelectionDialog stickerDialog = new StickerSelectionDialog(HandlerUtil.getActiveShell(event),
					"Filtern nach Sticker", IPatient.class);
			if (stickerDialog.open() == StickerSelectionDialog.OK) {
				currentFilter = new StickerFilter(stickerDialog.getSelection());
				part.getContentProvider().addQueryFilter(currentFilter);
				part.reload();
			}
		}
		return null;
	}

	private class StickerFilter implements QueryFilter {

		private List<ISticker> filterStickers;

		public StickerFilter(List<ISticker> filterStickers) {
			this.filterStickers = filterStickers;
		}

		@Override
		public void apply(IQuery<?> query) {
			Set<String> ids = new HashSet<>();
			filterStickers.forEach(fs -> ids.addAll(StickerServiceHolder.get().getObjectsWithSticker(fs, IPatient.class)
					.stream().map(IPatient::getId).collect(Collectors.toList())));
			query.and("id", COMPARATOR.IN, ids); //$NON-NLS-1$
		}
	}
}
