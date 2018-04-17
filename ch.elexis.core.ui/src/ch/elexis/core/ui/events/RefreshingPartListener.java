package ch.elexis.core.ui.events;

import org.eclipse.ui.IPartListener2;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.IWorkbenchPartReference;
import org.eclipse.ui.part.ViewPart;
import org.slf4j.LoggerFactory;

import ch.elexis.core.ui.views.IRefreshable;

/**
 * {@link IPartListener2} implementation that refreshed the provided {@link ViewPart} if a matching
 * partVisble event is fired by the workbench. Other {@link IPartListener2} methods than partVisble
 * are not overridden by default, and can be overridden by sub classes.
 * 
 * @author thomas
 *
 */
public class RefreshingPartListener implements IPartListener2 {
	
	private ViewPart part;

	public RefreshingPartListener(ViewPart part){
		this.part = part;
	}
	
	@Override
	public void partActivated(IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void partBroughtToTop(IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void partClosed(IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void partDeactivated(IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void partOpened(IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void partHidden(IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void partVisible(IWorkbenchPartReference partRef){
		if (isMatchingPart(partRef)) {
			if (part instanceof IRefreshable) {
				((IRefreshable) part).refresh();
			} else {
				LoggerFactory.getLogger(getClass())
					.warn("Could not refresh " + part + ", is not instance of IRefreshable");
			}
		}
	}
	
	@Override
	public void partInputChanged(IWorkbenchPartReference partRef){
		// TODO Auto-generated method stub
		
	}
	
	protected boolean isMatchingPart(IWorkbenchPartReference partRef) {
		IWorkbenchPart visiblePart = partRef.getPart(false);
		return part == visiblePart;
	}
}
