package ch.elexis.core.findings.templates.ui.util;

import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.FindingsTemplates;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.templates.model.InputDataGroupComponent;
import ch.elexis.core.findings.templates.model.InputDataNumeric;
import ch.elexis.core.findings.templates.model.InputDataText;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.data.PersistentObject;

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

	public static String PS_GETOBSERVATIONSWITHCODE = "SELECT ID FROM ch_elexis_core_findings_observation WHERE deleted='0' AND content LIKE ?";

	public static List<IObservation> getAllObservationsWithCode(ICoding code) {
		PreparedStatement preparedStatement = PersistentObject.getDefaultConnection()
				.getPreparedStatement(PS_GETOBSERVATIONSWITHCODE);
		List<IObservation> ret = new ArrayList<>();
		try {
			preparedStatement.setString(1,
					"%\"system\":\"" + code.getSystem() + "\",\"code\":\"" + code.getCode() + "\"%");

			ResultSet results = preparedStatement.executeQuery();

			while ((results != null) && (results.next() == true)) {
				String obsId = results.getString(1);
				FindingsServiceHolder.findingsService.findById(obsId, IObservation.class, true)
						.ifPresent(o -> ret.add(o));
			}
		} catch (SQLException e) {
			LoggerFactory.getLogger(FindingsTemplateUtil.class).error("Could not load observation", e);
		} finally {
			PersistentObject.getDefaultConnection().releasePreparedStatement(preparedStatement);
		}
		return ret;
	}
}
