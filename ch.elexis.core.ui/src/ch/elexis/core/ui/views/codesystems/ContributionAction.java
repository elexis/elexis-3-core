package ch.elexis.core.ui.views.codesystems;

import java.util.Collections;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.runtime.IConfigurationElement;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.LoggerFactory;

/**
 * An Action that encapsulates a command menu contribution. Is used by {@link CodeSelectorFactory}
 * to dynamically add contributions to popup menus.<br/>
 * <br/>
 * Selection is added to the {@link IEclipseContext} of the application under the name
 * <i>commandId.selection</i>, and should be removed by the handler.
 * 
 * @author thomas
 *
 */
public class ContributionAction extends Action {
	private String commandId;
	private String label;
	
	private Object[] selection;
	
	public ContributionAction(IConfigurationElement command){
		commandId = command.getAttribute("commandId");
		label = command.getAttribute("label");
	}
	
	public void setSelection(Object[] selection){
		this.selection = selection;
	}
	
	@Override
	public void run(){
		executeCommand();
	}
	
	public Object executeCommand(){
		try {
			ICommandService commandService =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			
			Command cmd = commandService.getCommand(commandId);
			if (selection != null) {
				PlatformUI.getWorkbench().getService(IEclipseContext.class)
					.set(commandId.concat(".selection"), new StructuredSelection(selection));
			}
			ExecutionEvent ee = new ExecutionEvent(cmd, Collections.EMPTY_MAP, null, null);
			return cmd.executeWithChecks(ee);
		} catch (Exception e) {
			LoggerFactory.getLogger(getClass())
				.error("cannot execute command with id: " + commandId, e);
		}
		return null;
	}
	
	@Override
	public String getText(){
		return label;
	}
	
	public boolean isValid(){
		return (commandId != null && !commandId.isEmpty()) && (label != null && !label.isEmpty());
	}
}
