package ch.elexis.core.findings.templates.ui.util;

import java.util.HashMap;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.InputDataText;
import ch.elexis.core.ui.icons.Images;

public class FindingsTemplateUtil {
	
	public static Image getImage(Object object){
		if (object instanceof FindingsTemplate) {
			FindingsTemplate findingsTemplate = (FindingsTemplate) object;
			if (findingsTemplate.getInputData() instanceof InputDataGroup) {
				return Images.IMG_DOCUMENT_STACK.getImage();
			} else if (findingsTemplate.getInputData() instanceof InputDataGroupComponent) {
				return Images.IMG_DOCUMENT_STAND_UP.getImage();
			} else if (findingsTemplate.getInputData() instanceof InputDataNumeric) {
				return Images.IMG_DOCUMENT.getImage();
			} else if (findingsTemplate.getInputData() instanceof InputDataText) {
				return Images.IMG_DOCUMENT.getImage();
			}
		} else if (object instanceof FindingsTemplates) {
			return Images.IMG_FOLDER.getImage();
		}
		return null;
	}
	
	public static void executeCommand(String commandId){
		try {
			ICommandService commandService =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			
			Command cmd = commandService.getCommand(commandId);
			ExecutionEvent ee = new ExecutionEvent(cmd, new HashMap<String, Object>(), null, null);
			cmd.executeWithChecks(ee);
		} catch (Exception e) {
			LoggerFactory.getLogger(FindingsTemplateUtil.class)
				.error("cannot execute command with id: " + commandId, e);
		}
	}
}
