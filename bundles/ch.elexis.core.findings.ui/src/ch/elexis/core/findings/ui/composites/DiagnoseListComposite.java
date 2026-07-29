/*******************************************************************************
 * Copyright (c) 2016-2022 MEDEVIT <office@medevit.at>.
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *     MEDEVIT <office@medevit.at> - initial API and implementation
 ******************************************************************************/
package ch.elexis.core.findings.ui.composites;

import java.lang.reflect.InvocationTargetException;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IMenuListener;
import org.eclipse.jface.action.IMenuManager;
import org.eclipse.jface.action.MenuManager;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.ProgressMonitorDialog;
import org.eclipse.jface.resource.ImageDescriptor;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.nebula.widgets.nattable.data.IColumnAccessor;
import org.eclipse.nebula.widgets.nattable.extension.glazedlists.GlazedListsDataProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.slf4j.LoggerFactory;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.findings.ICoding;
import ch.elexis.core.findings.ICondition;
import ch.elexis.core.findings.ICondition.ConditionCategory;
import ch.elexis.core.findings.ICondition.ConditionStatus;
import ch.elexis.core.findings.migration.IMigratorService;
import ch.elexis.core.findings.ui.dialogs.ConditionEditDialog;
import ch.elexis.core.findings.ui.services.CodingServiceComponent;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.lock.types.LockResponse;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.services.LocalConfigService;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.services.holder.ContextServiceHolder;
import ch.elexis.core.text.docx.util.TextUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.locks.LockResponseHelper;
import ch.elexis.core.ui.util.NatTableFactory;
import ch.elexis.core.ui.util.NatTableWrapper;
import ch.elexis.core.ui.util.NatTableWrapper.IDoubleClickListener;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.data.Patient;

/**
 * {@link Composite} implementation for managing the {@link ICondition} entries,
 * of a {@link Patient}.
 *
 * @author thomas
 *
 */
public class DiagnoseListComposite extends Composite {
	private NatTableWrapper natTableWrapper;
	private ToolBarManager toolbarManager;

	private ConditionEditDialog editDialog;

	private static final String PROPERTY_PRELOAD = "elexis.diagnose.preloadEditor"; //$NON-NLS-1$

	private static final int PRELOAD_DELAY_MILLIS = 1500;

	private EventList<ICondition> dataList = new BasicEventList<>();

	@SuppressWarnings("deprecation")
	public DiagnoseListComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		natTableWrapper = NatTableFactory.createSingleColumnTable(this,
				new GlazedListsDataProvider<ICondition>(dataList, new IColumnAccessor<ICondition>() {
					@Override
					public int getColumnCount() {
						return 1;
					}

					@Override
					public Object getDataValue(ICondition condition, int columnIndex) {
						switch (columnIndex) {
						case 0:
							boolean useStructured = ConfigServiceHolder
									.getGlobal(IMigratorService.DIAGNOSE_SETTINGS_USE_STRUCTURED, false);
							boolean useAlternativeFormat = LocalConfigService
									.get(Preferences.P_TEXT_DIAGNOSE_EXPORT_WORD_FORMAT, false);

							if (useStructured && useAlternativeFormat) {
								String rawHtml = (String) getAlternativeFormattedText(condition);
								return TextUtil.sanitizeHtmlForNebula(rawHtml);
							} else {
								String rawHtml = (String) getStandardFormattedText(condition);
								return TextUtil.sanitizeHtmlForNebula(rawHtml);
							}
						}
						return StringUtils.EMPTY;
					}

					/**
					 * NEW: Alternative formatting (bold title, bulleted text, spacing)
					 */
					private Object getAlternativeFormattedText(ICondition condition) {
						StringBuilder text = new StringBuilder();

						text.append("<strong>");

						ConditionStatus status = condition.getStatus();
						text.append(status.getLocalized());
						StringBuilder secondLine = new StringBuilder();
						Optional<String> start = condition.getStart();
						if (start.isPresent() && StringUtils.isNotBlank(start.get())) {
							secondLine.append(start.get());
						}

						Optional<String> end = condition.getEnd();
						if (end.isPresent() && StringUtils.isNotBlank(end.get())) {
							secondLine.append(" - ").append(end.get());
						}

						List<ICoding> codings = condition.getCoding();
						if (codings != null && !codings.isEmpty()) {
							for (ICoding iCoding : codings) {
								secondLine.append(" [")
										.append(CodingServiceComponent.getService().getShortLabel(iCoding))
										.append("]");
							}
						}
						if (secondLine.length() > 0) {
							text.append("<br/>").append(secondLine);
						}
						text.append("</strong>");

						boolean hasText = condition.getText().isPresent()
								&& StringUtils.isNotBlank(condition.getText().get());
						boolean hasNotes = !condition.getNotes().isEmpty();

						if (hasText || hasNotes) {
							text.append("<p><br/>");
						}

						if (hasText) {
							text.append(TextUtil.blocksToNebulaBreaks(condition.getText().get()));
						}

						if (hasNotes) {
							for (String note : condition.getNotes()) {
								if (StringUtils.isNotBlank(note)) {
									for (String line : note.split("\\r?\\n")) {
										appendFormattedLine(text, line);
									}
								}
							}
						}

						return text.toString();
					}

					/**
					 * OLD / STANDARD layout (used when checkboxes are disabled)
					 */
					private Object getStandardFormattedText(ICondition condition) {
						StringBuilder contentText = new StringBuilder();
						Optional<String> conditionText = condition.getText();
						if (conditionText.isPresent() && StringUtils.isNotBlank(conditionText.get())) {
							contentText.append(
									TextUtil.stripInlineFormatting(TextUtil.blocksToNebulaBreaks(conditionText.get())));
						}
						// then display the coding
						List<ICoding> codings = condition.getCoding();
						if (codings != null && !codings.isEmpty()) {
							for (ICoding iCoding : codings) {
								if (contentText.length() > 0) {
									contentText.append(", ");
								}
								contentText.append("[")
										.append(CodingServiceComponent.getService().getShortLabel(iCoding))
										.append("] ");
							}
						}
						// add additional information before content
						StringBuilder text = new StringBuilder();
						text.append("<strong>");
						ConditionStatus status = condition.getStatus();
						text.append(status.getLocalized());
						Optional<String> start = condition.getStart();
						start.ifPresent(string -> text.append(" (").append(string).append(" - "));
						Optional<String> end = condition.getEnd();
						end.ifPresent(string -> text.append(string));
						start.ifPresent(string -> text.append(")"));

						List<String> notes = condition.getNotes();
						if (!notes.isEmpty()) {
							text.append(" (" + notes.size() + ")");
						}
						text.append("</strong>");
						if (contentText.length() > 0) {
							boolean multiLine = contentText.indexOf("<br") >= 0 || contentText.indexOf("<ul") >= 0
									|| contentText.indexOf("<ol") >= 0;
							text.append(multiLine ? "<br/>" : " ").append(contentText);
						}

						return text.toString();
					}

					@Override
					public void setDataValue(ICondition condition, int arg1, Object arg2) {
						// setting data values is not enabled here.
					}

				}), null);
		natTableWrapper.getNatTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		natTableWrapper.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(NatTableWrapper source, ISelection selection) {
				if (selection instanceof StructuredSelection && !selection.isEmpty()) {
					ICondition condition = (ICondition) ((StructuredSelection) selection).getFirstElement();
					if (editDialog != null && editDialog.isOpen()) {
						editDialog.getShell().setActive();
						return;
					}
					editCondition(condition, source);
				}
			}
		});

		final MenuManager mgr = new MenuManager();
		mgr.setRemoveAllWhenShown(true);
		mgr.addMenuListener(new ConditionsMenuListener());
		natTableWrapper.getNatTable().setMenu(mgr.createContextMenu(natTableWrapper.getNatTable()));

		toolbarManager = new ToolBarManager();
		toolbarManager.add(new AddConditionAction());
		toolbarManager.add(new RemoveConditionAction());
		ToolBar toolbar = toolbarManager.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		toolbar.setBackground(parent.getBackground());

		addDisposeListener(e -> disposeEditDialog());
	}

	/**
	 * Locks the condition, edits it, releases the lock. The lock is acquired exactly
	 * as {@code AcquireLockBlockingUi} does it blocking, with a cancelable progress
	 * dialog but that dialog is closed again before the editor opens, and the lock
	 * is only released once editing is done.
	 * <p>
	 * The order is what makes a kept editor possible at all. {@code AcquireLockBlockingUi}
	 * calls its handler while its progress dialog is still up, and that dialog is
	 * application modal and disposed right afterwards: an editor built next to it would
	 * be blocked by its modality, and one built below it would be disposed with it.
	 * Since {@link ICondition} is an {@code Identifiable}, it takes the overload without
	 * the standalone shortcut, so that progress dialog appears even without a lock server.
	 */
	private void editCondition(ICondition condition, NatTableWrapper source) {
		LockResponse lockResponse = acquireLockBlocking(condition);
		if (lockResponse == null) {
			return;
		}
		if (!lockResponse.isOk()) {
			LockResponseHelper.showInfo(lockResponse, condition, LoggerFactory.getLogger(getClass()));
			return;
		}
		try {
			if (openEditDialog(condition, condition.getCategory(), getShell()) == Dialog.OK && editDialog != null) {
				editDialog.getCondition().ifPresent(c -> {
					source.getNatTable().refresh();
				});
			}
		} finally {
			LocalLockServiceHolder.get().releaseLock(lockResponse.getLockInfo());
		}
	}

	private LockResponse acquireLockBlocking(ICondition condition) {
		LockResponse[] result = new LockResponse[1];
		try {
			new ProgressMonitorDialog(getShell()).run(true, true,
					monitor -> result[0] = LocalLockServiceHolder.get().acquireLockBlocking(condition, 30, monitor));
		} catch (InvocationTargetException | InterruptedException e) {
			LoggerFactory.getLogger(getClass()).warn("Exception during acquire lock.", e); //$NON-NLS-1$
			return null;
		}
		return result[0];
	}
	
	private int openEditDialog(ICondition condition, ConditionCategory category, Shell parentShell) {
		if (editDialog != null && !editDialog.canReuseFor(parentShell, category)) {
			disposeEditDialog();
		}
		if (editDialog == null) {
			editDialog = condition != null ? new ConditionEditDialog(condition, parentShell)
					: new ConditionEditDialog(category, parentShell);
			editDialog.setReusable(true);
		} else {
			editDialog.setCondition(condition);
		}
		return editDialog.open();
	}

	private void disposeEditDialog() {
		if (editDialog != null) {
			editDialog.dispose();
			editDialog = null;
		}
	}

	private void preloadEditDialog() {
		if (editDialog != null || isDisposed() || getShell() == null
				|| !Boolean.parseBoolean(System.getProperty(PROPERTY_PRELOAD, Boolean.TRUE.toString()))) {
			return;
		}
		getDisplay().timerExec(PRELOAD_DELAY_MILLIS, () -> {
			if (editDialog != null || isDisposed() || getShell() == null || getShell().isDisposed()) {
				return;
			}
			try {
				ConditionEditDialog preloaded = new ConditionEditDialog(ConditionCategory.PROBLEMLISTITEM, getShell());
				preloaded.setReusable(true);
				preloaded.preload();
				editDialog = preloaded;
			} catch (Exception e) {
				LoggerFactory.getLogger(getClass()).warn("Could not preload the condition dialog", e); //$NON-NLS-1$
				disposeEditDialog();
			}
		});
	}

	private static void appendFormattedLine(StringBuilder text, String line) {
		text.append(line.trim()).append("<br/>");
	}

	public void setInput(List<ICondition> conditions) {
		dataList.clear();
		conditions.sort(new Comparator<ICondition>() {
			@Override
			public int compare(ICondition left, ICondition right) {
				Optional<LocalDate> lrecorded = left.getDateRecorded();
				Optional<LocalDate> rrecorded = right.getDateRecorded();
				if (lrecorded.isPresent() && rrecorded.isPresent()) {
					return rrecorded.get().compareTo(lrecorded.get());
				} else {
					Optional<String> lstart = left.getStart();
					Optional<String> rstart = right.getStart();
					if (lstart.isPresent() && rstart.isPresent()) {
						return rstart.get().compareTo(lstart.get());
					}
				}
				return 0;
			}
		});
		dataList.addAll(conditions);
		natTableWrapper.getNatTable().refresh();

		preloadEditDialog();

		Display.getDefault().asyncExec(() -> {
			if (!isDisposed()) {
				Composite parent = getParent();
				while (parent != null) {
					if (parent instanceof ScrolledForm) {
						((ScrolledForm) parent).reflow(true);
						break;
					}
					parent = parent.getParent();
				}
			}
		});
	}

	@Override
	public Point computeSize(int wHint, int hHint, boolean changed) {
		Point ret = toolbarManager.getControl().computeSize(wHint, hHint);
		Point natRet = natTableWrapper.computeSize(wHint, hHint);
		ret.y += natRet.y;
		ret.x = natRet.x;
		return ret;
	}

	@Override
	public void setBackground(Color color) {
		super.setBackground(color);
		if (natTableWrapper != null && !natTableWrapper.isDisposed()) {
			natTableWrapper.getNatTable().setBackground(color);
		}
	}

	private class ConditionsMenuListener implements IMenuListener {

		@Override
		public void menuAboutToShow(IMenuManager manager) {
			ISelection currentSelection = natTableWrapper.getSelection();
			if (currentSelection instanceof StructuredSelection) {
				StructuredSelection sSelection = (StructuredSelection) currentSelection;
				if (sSelection.size() == 1) {
					ICondition selectedCondition = (ICondition) sSelection.getFirstElement();
					ConditionStatus selectionStatus = selectedCondition.getStatus();
					if (selectionStatus != ConditionStatus.ACTIVE) {
						manager.add(new ToggleStatusAction(selectedCondition, ConditionStatus.ACTIVE));
					}
					if (selectionStatus != ConditionStatus.RESOLVED) {
						manager.add(new ToggleStatusAction(selectedCondition, ConditionStatus.RESOLVED));
					}
					if (selectionStatus != ConditionStatus.RELAPSE) {
						manager.add(new ToggleStatusAction(selectedCondition, ConditionStatus.RELAPSE));
					}
					if (selectionStatus != ConditionStatus.REMISSION) {
						manager.add(new ToggleStatusAction(selectedCondition, ConditionStatus.REMISSION));
					}
				}
				if (!sSelection.isEmpty()) {
					manager.add(new RemoveConditionAction());
				}
			}
		}
	}

	private class ToggleStatusAction extends Action {

		private ConditionStatus status;
		private ICondition condition;

		public ToggleStatusAction(ICondition condition, ConditionStatus status) {
			this.status = status;
			this.condition = condition;
		}

		@Override
		public String getText() {
			return Messages.DiagnoseListComposite_StatusPrefix + StringUtils.SPACE + status.getLocalized();
		}

		@Override
		public void run() {
			AcquireLockUi.aquireAndRun(condition, new ILockHandler() {
				@Override
				public void lockFailed() {
				}

				@Override
				public void lockAcquired() {
					condition.setStatus(status);
					FindingsServiceComponent.getService().saveFinding(condition);
				}
			});

			natTableWrapper.getNatTable().refresh();
		}
	}

	private class AddConditionAction extends Action {

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_NEW.getImageDescriptor();
		}

		@Override
		public String getText() {
			return Messages.DiagnoseListComposite_Create;
		}

		@Override
		public void run() {
			IPatient selectedPatient = ContextServiceHolder.get().getActivePatient().orElse(null);
			if (selectedPatient != null) {
				if (editDialog != null && editDialog.isOpen()) {
					editDialog.getShell().setActive();
					return;
				}
				if (openEditDialog(null, ConditionCategory.PROBLEMLISTITEM, getShell()) == Dialog.OK
						&& editDialog != null) {
					editDialog.getCondition().ifPresent(c -> {
						c.setPatientId(selectedPatient.getId());
						FindingsServiceComponent.getService().saveFinding(c);
						// touch after creation
						LocalLockServiceHolder.get().acquireLock(c);
						dataList.add(c);
						natTableWrapper.getNatTable().refresh();
					});
				}
			}
		}
	}

	private class RemoveConditionAction extends Action {

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_DELETE.getImageDescriptor();
		}

		@Override
		public String getText() {
			return Messages.DiagnoseListComposite_Remove;
		}

		@Override
		public void run() {
			ISelection selection = natTableWrapper.getSelection();
			if (selection instanceof StructuredSelection && !selection.isEmpty()) {
				@SuppressWarnings("unchecked")
				List<ICondition> list = ((StructuredSelection) selection).toList();
				list.stream().forEach(c -> {
					AcquireLockUi.aquireAndRun(c, new ILockHandler() {
						@Override
						public void lockFailed() {
						}

						@Override
						public void lockAcquired() {
							FindingsServiceComponent.getService().deleteFinding(c);
							dataList.remove(c);
							natTableWrapper.getNatTable().refresh();
						}
					});
				});
			}
		}
	}

	private boolean macosx_swt_eventTimed = false;

	@Override
	public void redraw() {
		super.redraw();
		// MacOs specific redraw bug workaround since 3.9
		// https://redmine.medelexis.ch/issues/24604
		// https://github.com/eclipse-platform/eclipse.platform.swt/issues/415
		if (CoreUtil.isMac()) {
			if (macosx_swt_eventTimed) {
				return;
			}
			macosx_swt_eventTimed = true;
			getDisplay().timerExec(250, new Runnable() {
				@Override
				public void run() {
					natTableWrapper.getNatTable().redraw();
					macosx_swt_eventTimed = false;
				}
			});
		}
	}
}