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

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.ToolBarManager;
import org.eclipse.jface.dialogs.Dialog;
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
import org.eclipse.swt.widgets.ToolBar;

import ca.odell.glazedlists.BasicEventList;
import ca.odell.glazedlists.EventList;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.service.LocalLockServiceHolder;
import ch.elexis.core.findings.IAllergyIntolerance;
import ch.elexis.core.findings.IAllergyIntolerance.AllergyIntoleranceCategory;
import ch.elexis.core.findings.ui.dialogs.AllergyIntoleranceEditDialog;
import ch.elexis.core.findings.ui.services.FindingsServiceComponent;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.locks.AcquireLockBlockingUi;
import ch.elexis.core.ui.locks.AcquireLockUi;
import ch.elexis.core.ui.locks.ILockHandler;
import ch.elexis.core.ui.util.NatTableFactory;
import ch.elexis.core.ui.util.NatTableWrapper;
import ch.elexis.core.ui.util.NatTableWrapper.IDoubleClickListener;
import ch.elexis.core.utils.CoreUtil;
import ch.elexis.data.Patient;

/**
 * {@link Composite} implementation for managing the {@link IAllergyIntolerance}
 * entries, of a {@link Patient}.
 *
 * @author thomas
 *
 */
public class AllergyIntoleranceListComposite extends Composite {
	private NatTableWrapper natTableWrapper;
	private ToolBarManager toolbarManager;

	private EventList<IAllergyIntolerance> dataList = new BasicEventList<>();

	@SuppressWarnings("deprecation")
	public AllergyIntoleranceListComposite(Composite parent, int style) {
		super(parent, style);
		setLayout(new GridLayout(1, false));

		natTableWrapper = NatTableFactory.createSingleColumnTable(this,
				new GlazedListsDataProvider<IAllergyIntolerance>(dataList, new IColumnAccessor<IAllergyIntolerance>() {
					@Override
					public int getColumnCount() {
						return 1;
					}

					@Override
					public Object getDataValue(IAllergyIntolerance condition, int columnIndex) {
						switch (columnIndex) {
						case 0:
							return getFormattedDescriptionText(condition);
						}
						return StringUtils.EMPTY;
					}

					private Object getFormattedDescriptionText(IAllergyIntolerance allergy) {
						StringBuilder text = new StringBuilder();

						StringBuilder contentText = new StringBuilder();
						// first display text
						Optional<String> allergyText = allergy.getText();
						allergyText.ifPresent(t -> {
							if (contentText.length() > 0) {
								contentText.append(StringUtils.LF);
							}
							contentText.append(t);
						});

						text.append(contentText.toString());

						return text.toString().replaceAll(StringUtils.LF, "<br/>");
					}

					@Override
					public void setDataValue(IAllergyIntolerance condition, int arg1, Object arg2) {
						// setting data values is not enabled here.
					}

				}), null);
		natTableWrapper.getNatTable().setLayoutData(new GridData(GridData.FILL_BOTH));
		natTableWrapper.addDoubleClickListener(new IDoubleClickListener() {
			@Override
			public void doubleClick(NatTableWrapper source, ISelection selection) {
				if (selection instanceof StructuredSelection && !selection.isEmpty()) {
					IAllergyIntolerance condition = (IAllergyIntolerance) ((StructuredSelection) selection)
							.getFirstElement();
					AcquireLockBlockingUi.aquireAndRun(condition, new ILockHandler() {
						@Override
						public void lockFailed() {
							// do nothing
						}

						@Override
						public void lockAcquired() {
							AllergyIntoleranceEditDialog dialog = new AllergyIntoleranceEditDialog(condition,
									Display.getDefault().getActiveShell());
							if (dialog.open() == Dialog.OK) {
								dialog.getAllergyIntolerance().ifPresent(c -> {
									source.getNatTable().refresh();
								});
							}
						}
					});
				}
			}
		});

		toolbarManager = new ToolBarManager();
		toolbarManager.add(new AddConditionAction());
		toolbarManager.add(new RemoveConditionAction());
		ToolBar toolbar = toolbarManager.createControl(this);
		toolbar.setLayoutData(new GridData(SWT.RIGHT, SWT.TOP, false, false));
		toolbar.setBackground(parent.getBackground());
	}

	public void setInput(List<IAllergyIntolerance> conditions) {
		dataList.clear();
		conditions.sort(new Comparator<IAllergyIntolerance>() {
			@Override
			public int compare(IAllergyIntolerance left, IAllergyIntolerance right) {
				Optional<LocalDate> lrecorded = left.getDateRecorded();
				Optional<LocalDate> rrecorded = right.getDateRecorded();
				if (lrecorded.isPresent() && rrecorded.isPresent()) {
					return rrecorded.get().compareTo(lrecorded.get());
				}
				return 0;
			}
		});
		dataList.addAll(conditions);
		natTableWrapper.getNatTable().refresh();
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

	private class AddConditionAction extends Action {

		@Override
		public ImageDescriptor getImageDescriptor() {
			return Images.IMG_NEW.getImageDescriptor();
		}

		@Override
		public String getText() {
			return "erstellen";
		}

		@Override
		public void run() {
			Patient selectedPatient = ElexisEventDispatcher.getSelectedPatient();
			if (selectedPatient != null) {
				AllergyIntoleranceEditDialog dialog = new AllergyIntoleranceEditDialog(
						AllergyIntoleranceCategory.UNKNOWN, getShell());
				if (dialog.open() == Dialog.OK) {
					dialog.getAllergyIntolerance().ifPresent(c -> {
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
			return "entfernen";
		}

		@Override
		public void run() {
			ISelection selection = natTableWrapper.getSelection();
			if (selection instanceof StructuredSelection && !selection.isEmpty()) {
				@SuppressWarnings("unchecked")
				List<IAllergyIntolerance> list = ((StructuredSelection) selection).toList();
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
