package ch.elexis.core.ui.e4.parts;

/**
 * Mixin interface to declare that the implementing part is refreshable. Parts implementing this may
 * use the command <code>ch.elexis.core.ui.command.part.refresh</code> to call for refresh
 */
public interface IRefreshablePart {
	
	public void refresh();
	
}
