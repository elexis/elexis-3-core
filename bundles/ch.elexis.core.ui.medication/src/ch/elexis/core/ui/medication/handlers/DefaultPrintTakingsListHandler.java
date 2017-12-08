package ch.elexis.core.ui.medication.handlers;

import java.util.HashMap;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.services.IEvaluationService;

public class DefaultPrintTakingsListHandler extends AbstractHandler implements IHandler {
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		HashMap<String, String> parameterMap = new HashMap<>();
		parameterMap.put("ch.elexis.core.ui.medication.commandParameter.medication", "fix");
		IEvaluationService evaluationService = (IEvaluationService) HandlerUtil.getActiveSite(event)
			.getService(IEvaluationService.class);
		new PrintTakingsListHandler().execute(
			new ExecutionEvent(null, parameterMap, null, evaluationService.getCurrentState()));
		return null;
	}
	
}
