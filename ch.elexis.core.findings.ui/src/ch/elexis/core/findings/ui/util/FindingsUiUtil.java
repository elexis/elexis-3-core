package ch.elexis.core.findings.ui.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.IObservationLink.ObservationLinkType;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.ui.action.DateAction;
import ch.elexis.core.findings.ui.composites.CompositeGroup;
import ch.elexis.core.findings.ui.composites.ICompositeSaveable;
import ch.elexis.core.findings.util.commands.FindingDeleteCommand;
import ch.elexis.core.model.IPersistentObject;
import ch.elexis.core.ui.actions.CommentAction;
import ch.elexis.core.ui.util.SWTHelper;

public class FindingsUiUtil {
	
	public static void saveGroup(ICompositeSaveable iCompositeSaveable){
		for (ICompositeSaveable child : iCompositeSaveable.getChildReferences()) {
			if (child instanceof CompositeGroup) {
				saveGroup(child);
			} else {
				save(child);
			}
		}
		
		for (ICompositeSaveable child : iCompositeSaveable.getChildComponents()) {
			if (child instanceof CompositeGroup) {
				saveGroup(child);
			} else {
				save(child);
			}
		}
	}
	
	public static void save(ICompositeSaveable iCompositeSaveable){
		
		String text = iCompositeSaveable.getFieldTextValue();
		IFinding iFinding = iCompositeSaveable.getFinding();
		
		if (iFinding instanceof IObservation) {
			IObservation iObservation = (IObservation) iCompositeSaveable.getFinding();
			ObservationComponent obsComponent = iCompositeSaveable.getObservationComponent();
			ObservationType observationType = iCompositeSaveable.getObservationType();
			
			if (ObservationType.TEXT.equals(observationType)) {
				// text fields inside component
				if (obsComponent != null) {
					obsComponent.setStringValue(Optional.of(text));
				} else {
					iObservation.setStringValue(text);
				}
			} else if (ObservationType.NUMERIC.equals(observationType)) {
				try {
					if (obsComponent != null) {
						// numeric fields inside component
						BigDecimal number =
							NumberUtils.isNumber(text) ? new BigDecimal(text) : null;
						obsComponent.setNumericValue(
							number != null ? Optional.of(number) : Optional.empty());
						iObservation.updateComponent(obsComponent);
					} else {
						// numeric fields
						BigDecimal number =
							NumberUtils.isNumber(text) ? new BigDecimal(text) : null;
						iObservation.setNumericValue(number,
							iObservation.getNumericValueUnit().orElse(""));
					}
					
				} catch (NumberFormatException e) {
					LoggerFactory.getLogger(FindingsUiUtil.class)
						.warn("cannot save number illegal format", e);
				}
			}
		}
	}
	
	private static Optional<String> getComment(ICompositeSaveable iCompositeSaveable, boolean wrap,
		boolean emptyAllowed){
		// save text
		for (Action a : iCompositeSaveable.getToolbarActions()) {
			if (a instanceof CommentAction) {
				String comment = ((CommentAction) a).getComment();
				if (comment != null && (emptyAllowed || !StringUtils.isBlank(comment))) {
					if (wrap) {
						return Optional.of("[" + comment + "]");
					}
					return Optional.of(comment);
				}
			}
		}
		return Optional.empty();
	}
	
	public static IFinding saveObservation(IObservation iObservation,
		ICompositeSaveable iCompositeSaveable, LocalDateTime localDateTime){
		
		getComment(iCompositeSaveable, false, true)
			.ifPresent(item -> iObservation.setComment(item));
		
		for (ICompositeSaveable child : iCompositeSaveable.getChildReferences()) {
			child.saveContents(localDateTime);
		}
		
		iObservation.setText(iCompositeSaveable.getText());
		iObservation.setEffectiveTime(localDateTime);
		return iObservation;
	}
	
	/**
	 * Returns all actions from the main toolbar
	 * 
	 * @param c
	 * @param iObservation
	 * @param horizontalGrap
	 * @return
	 */
	public static List<Action> createToolbarMainComponent(Composite c, IObservation iObservation,
		int horizontalGrap){
		
		Composite toolbarComposite = new Composite(c, SWT.NONE);
		toolbarComposite.setLayout(SWTHelper.createGridLayout(true, 2));
		toolbarComposite.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, true, false));
		
		List<Action> actions = new ArrayList<>();
		LocalDateTime currentDate = iObservation.getEffectiveTime().orElse(LocalDateTime.now());
		
		ToolBarManager menuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL);
		
		Action action = new DateAction(c.getShell(), currentDate, toolbarComposite);
		menuManager.add(action);
		menuManager.createControl(toolbarComposite)
			.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false, horizontalGrap, 1));
		actions.add(action);
		
		return actions;
	}
	
	/**
	 * Returns all actions from the sub toolbar
	 * 
	 * @param c
	 * @param iObservation
	 * @param horizontalGrap
	 * @return
	 */
	public static List<Action> createToolbarSubComponents(Composite c, IObservation iObservation,
		int horizontalGrap){
		
		List<Action> actions = new ArrayList<>();
		String comment = iObservation.getComment().orElse("");
		
		ToolBarManager menuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.NO_FOCUS);
		Action commentableAction = new CommentAction(c.getShell(), comment);
		menuManager.add(commentableAction);
		menuManager.createControl(c)
			.setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, horizontalGrap, 1));
		actions.add(commentableAction);
		
		return actions;
	}
	
	public static List<IObservation> getOberservationChildren(IObservation iObservation,
		List<IObservation> list, int maxDepth){
		if (maxDepth > 0) {
			List<IObservation> refChildrens =
				iObservation.getTargetObseravtions(ObservationLinkType.REF);
			list.add(iObservation);
			
			for (IObservation child : refChildrens) {
				getOberservationChildren(child, list, --maxDepth);
			}
		}
		return list;
	}
	
	public static void deleteObservation(IFinding iFinding) throws ElexisException{
		List<IObservation> observationChildrens =
			FindingsUiUtil
				.getOberservationChildren((IObservation) iFinding, new ArrayList<>(), 100);
		
		List<IFinding> lockedChildrens = new ArrayList<>();
		for (IObservation iObservation : observationChildrens) {
			if (!CoreHub.getLocalLockService().acquireLock((IPersistentObject) iObservation)
				.isOk()) {
				throw new ElexisException("Das Löschen ist nicht möglich, kein Lock erhalten.");
			}
			lockedChildrens.add(iObservation);
		}
		
		for (IFinding f : lockedChildrens) {
			new FindingDeleteCommand(f).execute();
			CoreHub.getLocalLockService().releaseLock((IPersistentObject) f);
		}
	}
	
	public static String getTypeAsText(IFinding iFinding){
		if (iFinding instanceof ICondition) {
			return "Problem";
		} else if (iFinding instanceof IClinicalImpression) {
			return "Beurteilung";
		} else if (iFinding instanceof IObservation) {
			return "Beobachtung";
		} else if (iFinding instanceof IProcedureRequest) {
			return "Prozedere";
		}
		return "";
	}
	
	public static Object executeCommand(String commandId, IFinding selection){
		try {
			ICommandService commandService =
				(ICommandService) PlatformUI.getWorkbench().getService(ICommandService.class);
			
			Command cmd = commandService.getCommand(commandId);
			if(selection != null) {
				PlatformUI.getWorkbench().getService(IEclipseContext.class)
					.set(commandId.concat(".selection"), new StructuredSelection(selection));
			}
			ExecutionEvent ee = new ExecutionEvent(cmd, Collections.EMPTY_MAP, null, null);
			return cmd.executeWithChecks(ee);
		} catch (Exception e) {
			LoggerFactory.getLogger(FindingsUiUtil.class)
				.error("cannot execute command with id: " + commandId, e);
		}
		return null;
	}
}
