package ch.elexis.core.ui.documents.handler;

import java.util.Collections;
import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.commands.IHandler;
import org.eclipse.core.commands.NotEnabledException;
import org.eclipse.core.commands.NotHandledException;
import org.eclipse.core.commands.common.NotDefinedException;
import org.eclipse.core.expressions.EvaluationContext;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.ui.ISources;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.eclipse.ui.handlers.HandlerUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.model.IDocument;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.documents.service.DocumentStoreServiceHolder;

public class DocumentLocalEditHandler extends AbstractHandler implements IHandler {
	
	private static Logger logger = LoggerFactory.getLogger(DocumentLocalEditHandler.class);
	
	private static final String LOCAL_EDIT_START =
		"ch.elexis.core.ui.documents.commandLocalEditStart";
	private static final String LOCAL_EDIT_ABORT =
		"ch.elexis.core.ui.documents.commandLocalEditAbort";
	private static final String LOCAL_EDIT_DONE =
		"ch.elexis.core.ui.documents.commandLocalEditDone";
	private static final String LOCAL_EDIT_OVERVIEW =
		"ch.elexis.core.ui.documents.commandLocalEditOverview";
	
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException{
		
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		if (selection instanceof StructuredSelection
			&& !((StructuredSelection) selection).isEmpty()) {
			List<?> iDocuments = ((StructuredSelection) selection).toList();
			
			ICommandService commandService =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			for (Object document : iDocuments) {
				if (document instanceof IDocument) {
					switch (event.getCommand().getId()) {
					case LOCAL_EDIT_START:
						sendLocalEditEvent((IDocument) document, commandService
							.getCommand("ch.elexis.core.ui.command.startEditLocalDocument"));
						break;
					case LOCAL_EDIT_ABORT:
						sendLocalEditEvent((IDocument) document, commandService
							.getCommand("ch.elexis.core.ui.command.abortLocalDocument"));
						break;
					case LOCAL_EDIT_DONE:
						sendLocalEditEvent((IDocument) document, commandService
							.getCommand("ch.elexis.core.ui.command.endLocalDocument"));
						break;
					default:
						break;
					
					}
					sendReloadViewEvent(document);
				}
			}
		}
		if (LOCAL_EDIT_OVERVIEW.equals(event.getCommand().getId())) {
			ICommandService commandService =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			createEvent(commandService.getCommand("ch.elexis.core.ui.command.openLocalDocuments"),
				null);
			sendReloadViewEvent(null);
		}
		return null;
	}

	private void sendReloadViewEvent(Object document){
		ElexisEventDispatcher.getInstance().fire(new ElexisEvent(document,
			IDocument.class, ElexisEvent.EVENT_RELOAD, ElexisEvent.PRIORITY_NORMAL));
	}

	private void sendLocalEditEvent(IDocument document, Command command){
		Shell shell = PlatformUI.getWorkbench().getDisplay().getActiveShell();
		
			DocumentStoreServiceHolder.getService().getPersistenceObject(document).ifPresent(po -> {
				createEvent(command, po);
			});
	}

	private void createEvent(Command command, IPersistentObject po){
		EvaluationContext appContext = null;
		if (po != null) {
			appContext = new EvaluationContext(null, Collections.EMPTY_LIST);
			appContext.addVariable(ISources.ACTIVE_CURRENT_SELECTION_NAME,
				new StructuredSelection(po));
		}
		ExecutionEvent newEvent =
			new ExecutionEvent(command, Collections.EMPTY_MAP, this, appContext);
		try {
			command.executeWithChecks(newEvent);
		} catch (ExecutionException | NotDefinedException | NotEnabledException
				| NotHandledException e) {
			logger.error("cannot executre local edit event", e);
		}
	}
	
}
