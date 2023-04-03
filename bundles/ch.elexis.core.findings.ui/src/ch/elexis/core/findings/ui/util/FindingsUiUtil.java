package ch.elexis.core.findings.ui.util;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.commands.Command;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.emf.common.util.EList;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.commands.ICommandService;
import org.slf4j.LoggerFactory;

import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.exceptions.ElexisException;
import ch.elexis.core.findings.IClinicalImpression;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.IFinding;
import ch.elexis.core.findings.IObservation;
import ch.elexis.core.findings.IObservation.ObservationType;
import ch.elexis.core.findings.IProcedureRequest;
import ch.elexis.core.findings.ObservationComponent;
import ch.elexis.core.findings.codes.CodingSystem;
import ch.elexis.core.findings.templates.model.FindingsTemplate;
import ch.elexis.core.findings.templates.model.InputData;
import ch.elexis.core.findings.templates.model.InputDataGroup;
import ch.elexis.core.findings.ui.action.DateAction;
import ch.elexis.core.findings.ui.composites.CompositeGroup;
import ch.elexis.core.findings.ui.composites.ICompositeSaveable;
import ch.elexis.core.findings.ui.services.CodingServiceComponent;
import ch.elexis.core.findings.ui.services.FindingsTemplateServiceComponent;
import ch.elexis.core.findings.util.commands.FindingDeleteCommand;
import ch.elexis.core.findings.util.commands.ILockingProvider;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.actions.CommentAction;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.util.SWTHelper;

public class FindingsUiUtil {

	private static String CFG_VISIBLE_CODINGS = "ch.elexis.core.findings/ui/visiblecodings";
	private static String CFG_SELECTED_CODINGS = "ch.elexis.core.findings/ui/selectedcodings";

	/**
	 * Get a list of all codes of the local code system.
	 *
	 * @return
	 */
	public static List<ICoding> getAvailableCodings() {
		List<ICoding> codes = CodingServiceComponent.getService()
				.getAvailableCodes(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem());
		codes.sort((a, b) -> {
			return a.getDisplay().compareTo(b.getDisplay());
		});
		return codes;
	}

	public static List<ICoding> loadVisibleCodings() {
		String visibleCodingsString = ConfigServiceHolder.getMandator(CFG_VISIBLE_CODINGS, null);
		List<ICoding> availableCodings = FindingsUiUtil.getAvailableCodings();
		if (visibleCodingsString != null) {
			List<ICoding> visible = new ArrayList<>();
			String[] parts = visibleCodingsString.split("\\|");
			for (String part : parts) {
				for (ICoding availableCoding : availableCodings) {
					if (availableCoding.getCode().equals(part)) {
						visible.add(availableCoding);
						break;
					}
				}
			}
			return visible;
		}
		return Collections.emptyList();
	}

	public static void saveVisibleCodings(List<ICoding> visible) {
		StringBuilder sb = new StringBuilder();
		for (ICoding iCoding : visible) {
			if (sb.length() > 0) {
				sb.append("|");
			}
			sb.append(iCoding.getCode());
		}
		ConfigServiceHolder.setMandator(CFG_VISIBLE_CODINGS, sb.toString());
	}

	public static List<ICoding> loadSelectedCodings() {
		String selectedCodingsString = ConfigServiceHolder.getMandator(CFG_SELECTED_CODINGS, null);
		if (selectedCodingsString != null) {
			List<ICoding> selected = new ArrayList<>();
			List<ICoding> availableCodings = FindingsUiUtil.getAvailableCodings();
			String[] parts = selectedCodingsString.split("\\|");
			for (String part : parts) {
				for (ICoding availableCoding : availableCodings) {
					if (availableCoding.getCode().equals(part)) {
						selected.add(availableCoding);
						break;
					}
				}
			}
			return selected;
		} else {
			return Collections.emptyList();
		}
	}

	public static void saveSelectedCodings(List<ICoding> selection) {
		StringBuilder sb = new StringBuilder();
		for (ICoding iCoding : selection) {
			if (sb.length() > 0) {
				sb.append("|");
			}
			sb.append(iCoding.getCode());
		}
		ConfigServiceHolder.setMandator(CFG_SELECTED_CODINGS, sb.toString());
	}

	public static void saveGroup(ICompositeSaveable iCompositeSaveable) {
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

	public static void save(ICompositeSaveable iCompositeSaveable) {

		String text = iCompositeSaveable.getFieldTextValue();
		IFinding iFinding = iCompositeSaveable.getFinding();

		if (iFinding instanceof IObservation) {
			IObservation iObservation = (IObservation) iCompositeSaveable.getFinding();
			ObservationComponent obsComponent = iCompositeSaveable.getObservationComponent();
			ObservationType observationType = iCompositeSaveable.getObservationType();

			if (ObservationType.TEXT.equals(observationType)) {
				// text fields inside component
				if (obsComponent != null) {
					obsComponent.setStringValue(text);
				} else {
					iObservation.setStringValue(text);
				}
			} else if (ObservationType.NUMERIC.equals(observationType)) {
				try {
					if (obsComponent != null) {
						// numeric fields inside component
						BigDecimal number = NumberUtils.isNumber(text) ? new BigDecimal(text) : null;
						obsComponent.setNumericValue(number);
						iObservation.updateComponent(obsComponent);
					} else {
						// numeric fields
						BigDecimal number = NumberUtils.isNumber(text) ? new BigDecimal(text) : null;
						iObservation.setNumericValue(number,
								iObservation.getNumericValueUnit().orElse(StringUtils.EMPTY));
					}

				} catch (NumberFormatException e) {
					LoggerFactory.getLogger(FindingsUiUtil.class).warn("cannot save number illegal format", e);
				}
			} else if (ObservationType.BOOLEAN.equals(observationType)) {
				if (StringUtils.isNotBlank(text)) {
					iObservation.setBooleanValue(Boolean.valueOf(text));
				}
			} else if (ObservationType.DATE.equals(observationType)) {
				if (StringUtils.isNotBlank(text)) {
					LocalDateTime dateTime = LocalDateTime.parse(text);
					iObservation.setDateTimeValue(Date.from(dateTime.atZone(ZoneId.systemDefault()).toInstant()));
				}
			}
		}
	}

	private static Optional<String> getComment(ICompositeSaveable iCompositeSaveable, boolean wrap,
			boolean emptyAllowed) {
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

	public static IFinding saveObservation(IObservation iObservation, ICompositeSaveable iCompositeSaveable,
			LocalDateTime localDateTime) {

		getComment(iCompositeSaveable, false, true).ifPresent(item -> iObservation.setComment(item));

		for (ICompositeSaveable child : iCompositeSaveable.getChildReferences()) {
			child.saveContents(localDateTime);
		}

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
	public static List<Action> createToolbarMainComponent(Composite c, IObservation iObservation, int horizontalGrap) {

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
	public static List<Action> createToolbarSubComponents(Composite c, IObservation iObservation, int horizontalGrap) {

		List<Action> actions = new ArrayList<>();
		String comment = iObservation.getComment().orElse(StringUtils.EMPTY);

		ToolBarManager menuManager = new ToolBarManager(SWT.FLAT | SWT.HORIZONTAL | SWT.NO_FOCUS);
		Action commentableAction = new CommentAction(c.getShell(), comment);
		menuManager.add(commentableAction);
		menuManager.createControl(c).setLayoutData(new GridData(SWT.LEFT, SWT.TOP, true, false, horizontalGrap, 1));
		actions.add(commentableAction);

		return actions;
	}

	/**
	 * Delete the {@link IFinding}.
	 *
	 * @param iFinding
	 * @throws ElexisException
	 */
	public static void deleteFinding(IFinding iFinding) throws ElexisException {
		try {
			if (LocalLockServiceHolder.get().acquireLock(iFinding).isOk()) {
				new FindingDeleteCommand(iFinding, new ILockingProvider() {

					@Override
					public LockResponse releaseLock(Object object) {
						return LocalLockServiceHolder.get().releaseLock((IFinding) object);
					}

					@Override
					public LockResponse acquireLock(Object object) {
						return LocalLockServiceHolder.get().acquireLock((IFinding) object);
					}
				}).execute();
			}
		} catch (ElexisException e) {
			MessageDialog.openInformation(Display.getDefault().getActiveShell(), "Löschen",
					"Befund wurde nicht gelöscht. Der Befund ist auf einer anderen Station geöffnet.");
		} finally {
			LocalLockServiceHolder.get().releaseLock(iFinding);
		}
	}

	public static String getTypeAsText(IFinding iFinding) {
		if (iFinding instanceof ICondition) {
			return "Problem";
		} else if (iFinding instanceof IClinicalImpression) {
			return "Beurteilung";
		} else if (iFinding instanceof IObservation) {
			return "Beobachtung";
		} else if (iFinding instanceof IProcedureRequest) {
			return "Prozedere";
		}
		return StringUtils.EMPTY;
	}

	/**
	 * Execute the UI command found by the commandId, using the
	 * {@link ICommandService}.
	 *
	 * @param commandId
	 * @param selection
	 * @return
	 */
	public static Object executeCommand(String commandId, IFinding selection) {
		try {
			ICommandService commandService = (ICommandService) PlatformUI.getWorkbench()
					.getService(ICommandService.class);

			Command cmd = commandService.getCommand(commandId);
			if (selection != null) {
				CoreUiUtil.setCommandSelection(commandId, Collections.singletonList(selection));
			}
			ExecutionEvent ee = new ExecutionEvent(cmd, Collections.EMPTY_MAP, null, null);
			return cmd.executeWithChecks(ee);
		} catch (Exception e) {
			LoggerFactory.getLogger(FindingsUiUtil.class).error("cannot execute command with id: " + commandId, e);
		}
		return null;
	}

	public static boolean isCodingForGroup(ICoding c) {
		Optional<FindingsTemplate> template = FindingsTemplateServiceComponent.getService().getFindingsTemplate(c);
		if (template.isPresent()) {
			return template.get().getInputData() instanceof InputDataGroup;
		}
		return false;
	}

	public static List<ICoding> getCodesOfGroup(ICoding iCoding) {
		Optional<FindingsTemplate> template = FindingsTemplateServiceComponent.getService()
				.getFindingsTemplate(iCoding);
		if (template.isPresent()) {
			List<ICoding> ret = new ArrayList<>();
			InputData inputData = template.get().getInputData();
			if (inputData instanceof InputDataGroup) {
				ret.addAll(getCodesOfGroup((InputDataGroup) inputData));
			} else {
				ret.add(iCoding);
			}
			return ret;
		}
		return Collections.emptyList();
	}

	private static List<ICoding> getCodesOfGroup(InputDataGroup inputData) {
		List<ICoding> ret = new ArrayList<>();
		EList<FindingsTemplate> subTemplates = inputData.getFindingsTemplates();
		for (FindingsTemplate findingsTemplate : subTemplates) {
			if (findingsTemplate.getInputData() instanceof InputDataGroup) {
				ret.addAll(getCodesOfGroup((InputDataGroup) findingsTemplate.getInputData()));
			} else {
				CodingServiceComponent.getService()
						.getCode(CodingSystem.ELEXIS_LOCAL_CODESYSTEM.getSystem(), findingsTemplate.getTitle())
						.ifPresent(c -> ret.add(c));
			}
		}
		return ret;
	}
}
