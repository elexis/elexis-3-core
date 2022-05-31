/*******************************************************************************
 * Copyright (c) 2007-2010, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *
 *******************************************************************************/

package ch.elexis.core.ui.laboratory.views;

import java.util.logging.Level;

import javax.inject.Inject;
import javax.inject.Named;

import org.apache.commons.lang3.StringUtils;
import org.eclipse.e4.core.di.annotations.Optional;
import org.eclipse.e4.ui.model.application.ui.basic.MPart;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.CheckStateChangedEvent;
import org.eclipse.jface.viewers.CheckboxTableViewer;
import org.eclipse.jface.viewers.ICheckStateListener;
import org.eclipse.jface.viewers.IColorProvider;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.IStructuredContentProvider;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.ITableLabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.jface.viewers.ViewerComparator;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Table;
import org.eclipse.swt.widgets.TableColumn;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.Heartbeat;
import ch.elexis.core.data.events.Heartbeat.HeartListener;
import ch.elexis.core.data.interfaces.ILabResult;
import ch.elexis.core.model.LabResultConstants;
import ch.elexis.core.services.holder.ConfigServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.e4.util.CoreUiUtil;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.LabResult;
import ch.elexis.data.Patient;
import ch.rgw.tools.Log;

/**
 * This view displays all LabResults that are not marked as seen by the doctor.
 * One can mark them individually or globally as seen from this view.
 *
 * @author gerry
 *
 */
public class LabNotSeenView extends ViewPart implements HeartListener {
	public final static String ID = "ch.elexis.LabNotSeenView"; //$NON-NLS-1$
	CheckboxTableViewer tv;
	LabResult[] unseen = null;
	private long lastUpdate = 0;
	private Log log = Log.get(this.getClass().getName());
	private boolean inUpdate = false;
	private Table table;

	public static LabNotSeenComparator comparator;

	private static final String[] columnHeaders = { Messages.LabNotSeenView_patient, Messages.LabNotSeenView_parameter,
			Messages.LabNotSeenView_normRange, Messages.LabNotSeenView_date, Messages.LabNotSeenView_value };
	private static final int[] colWidths = new int[] { 250, 100, 60, 70, 50 };
	private IAction markAllAction, markPersonAction;

	public LabNotSeenView() {
	}

	@Override
	public void createPartControl(final Composite parent) {
		parent.setLayout(new FillLayout());
		table = new Table(parent, SWT.CHECK | SWT.V_SCROLL);
		comparator = new LabNotSeenComparator();
		for (int i = 0; i < columnHeaders.length; i++) {
			TableColumn tc = new TableColumn(table, SWT.NONE);
			tc.setText(columnHeaders[i]);
			tc.setWidth(colWidths[i]);
			tc.addSelectionListener(getSelectionAdapter(tc, i));
		}
		table.setHeaderVisible(true);
		table.setLinesVisible(true);
		tv = new CheckboxTableViewer(table);
		tv.setComparator(comparator);
		tv.setContentProvider(new LabNotSeenContentProvider());
		tv.setLabelProvider(new LabNotSeenLabelProvider());
		tv.setUseHashlookup(true);
		tv.addSelectionChangedListener(new ISelectionChangedListener() {

			public void selectionChanged(final SelectionChangedEvent event) {
				IStructuredSelection sel = (IStructuredSelection) event.getSelection();
				if (!sel.isEmpty()) {
					if (sel.getFirstElement() instanceof LabResult) {
						LabResult lr = (LabResult) sel.getFirstElement();
						ElexisEventDispatcher.fireSelectionEvent(lr.getPatient());
					}
				}

			}
		});
		tv.addCheckStateListener(new ICheckStateListener() {
			boolean bDaempfung;

			public void checkStateChanged(final CheckStateChangedEvent event) {
				if (bDaempfung == false) {
					bDaempfung = true;
					LabResult lr = (LabResult) event.getElement();
					boolean state = event.getChecked();
					if (state) {
						if (CoreHub.acl.request(AccessControlDefaults.LAB_SEEN)) {
							lr.removeFromUnseen();
						} else {
							tv.setChecked(lr, false);
						}
					} else {
						lr.addToUnseen();
					}
					bDaempfung = false;
				}
			}

		});
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		menu.createToolbar(markPersonAction, markAllAction);
		heartbeat();
		CoreHub.heart.addListener(this,
				ConfigServiceHolder.getUser(Preferences.LABSETTINGS_CFG_LABNEW_HEARTRATE, Heartbeat.FREQUENCY_HIGH));

		tv.setInput(this);

	}

	private SelectionAdapter getSelectionAdapter(final TableColumn column, final int index) {
		SelectionAdapter selectionAdapter = new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				comparator.setColumn(index);
				tv.getTable().setSortDirection(comparator.getDirection());
				tv.getTable().setSortColumn(column);
				tv.refresh();

			}
		};
		return selectionAdapter;

	}

	@Override
	public void dispose() {
		CoreHub.heart.removeListener(this);
		super.dispose();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

	static class LabNotSeenLabelProvider extends LabelProvider implements ITableLabelProvider, IColorProvider {

		public Image getColumnImage(final Object element, final int columnIndex) {
			// TODO Auto-generated method stub
			return null;
		}

		public String getColumnText(final Object element, final int columnIndex) {
			if (element instanceof String) {
				return columnIndex == 0 ? (String) element : StringUtils.EMPTY;
			}
			LabResult lr = (LabResult) element;
			switch (columnIndex) {
			case 0:
				return lr.getPatient().getLabel();
			case 1:
				return lr.getItem().getName();
			case 2:
				Patient pat = lr.getPatient();
				if (pat.getGeschlecht().equalsIgnoreCase("m")) { //$NON-NLS-1$
					return lr.getItem().getReferenceMale();
				} else {
					return lr.getItem().getReferenceFemale();
				}
			case 3:
				return lr.getDate();
			case 4:
				return lr.getResult();
			}
			return "?"; //$NON-NLS-1$
		}

		public Color getBackground(final Object element) {
			// TODO Auto-generated method stub
			return null;
		}

		public Color getForeground(final Object element) {
			if (element instanceof String) {
				return UiDesk.getColor(UiDesk.COL_GREY);
			}
			LabResult lr = (LabResult) element;

			if (lr.isFlag(LabResultConstants.PATHOLOGIC)) {
				return UiDesk.getColor(UiDesk.COL_RED);
			} else {
				return UiDesk.getColor(UiDesk.COL_BLACK);
			}
		}

	}

	class LabNotSeenContentProvider implements IStructuredContentProvider {

		public Object[] getElements(final Object inputElement) {
			if (unseen == null) {
				return new Object[] { Messages.LabNotSeenView_loading };
			}
			return unseen;
		}

		public void dispose() { /* don't mind */
		}

		public void inputChanged(final Viewer viewer, final Object oldInput, final Object newInput) {
			// don't mind
		}

	}

	public void heartbeat() {
		long last = LabResult.getLastUpdateUnseen();
		if (lastUpdate != 0) {
			if (lastUpdate >= last) {
				log.log(Level.FINE, "Heartbeat unused"); //$NON-NLS-1$
				return;
			}
		}
		lastUpdate = last;
		log.log(Level.FINE, "Heartbeat used"); //$NON-NLS-1$
		unseen = LabResult.getUnseen().toArray(new LabResult[0]);
		UiDesk.getDisplay().syncExec(new Runnable() {

			public void run() {
				if (!inUpdate) {
					inUpdate = true;
					if (tv != null) {
						synchronized (tv.getControl()) {
							if (tv.getControl() != null && !tv.getControl().isDisposed()) {
								tv.refresh();
							}
						}
					}
					inUpdate = false;
				}
			}
		});

	}

	private void makeActions() {
		markAllAction = new RestrictedAction(AccessControlDefaults.LAB_SEEN, Messages.LabNotSeenView_markAll) { // $NON-NLS-1$
			{
				setToolTipText(Messages.LabNotSeenView_markAllToolTip); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_TICK.getImageDescriptor());
			}

			@Override
			public void doRun() {
				boolean openConfirm = MessageDialog.openConfirm(getViewSite().getShell(),
						Messages.LabNotSeenView_reallyMarkCaption, // $NON-NLS-1$
						Messages.LabNotSeenView_markAllToolTip);
				if (openConfirm) // $NON-NLS-1$
				{
					tv.setAllChecked(true);
					for (LabResult lr : LabResult.getUnseen()) {
						lr.removeFromUnseen();
					}
				}
			}

		};
		markPersonAction = new RestrictedAction(AccessControlDefaults.LAB_SEEN,
				Messages.LabNotSeenView_markAllofPatient) { // $NON-NLS-1$
			{
				setToolTipText(Messages.LabNotSeenView_markAllOfPatientToolTip); // $NON-NLS-1$
				setImageDescriptor(Images.IMG_PERSON_OK.getImageDescriptor());
			}

			@Override
			public void doRun() {
				Patient act = ElexisEventDispatcher.getSelectedPatient();
				for (LabResult lr : unseen) {
					if (lr.getPatient().equals(act)) {
						lr.removeFromUnseen();
						tv.setChecked(lr, true);
					}
				}
			}
		};
	}

	@Optional
	@Inject
	public void setFixLayout(MPart part, @Named(Preferences.USR_FIX_LAYOUT) boolean currentState) {
		CoreUiUtil.updateFixLayout(part, currentState);
	}

	public class LabNotSeenComparator extends ViewerComparator {

		private int propertyIndex;
		private boolean direction = true;

		public LabNotSeenComparator() {
			this.propertyIndex = 0;
		}

		public int getDirection() {
			return direction ? SWT.DOWN : SWT.UP;
		}

		public void setColumn(int column) {
			if (column == this.propertyIndex) {
				// Same column as last sort; toggle the direction
				direction = !direction;
			} else {
				// New column; do an ascending sort
				this.propertyIndex = column;
				direction = true;
			}
		}

		@Override
		public int compare(Viewer viewer, Object o1, Object o2) {
			if (o1 instanceof ILabResult && o2 instanceof ILabResult) {
				ILabResult l1 = (ILabResult) o1;
				ILabResult l2 = (ILabResult) o2;

				int rc = 0;

				switch (propertyIndex) {
				case 0:
					rc = l1.getLabOrder().getPatient().getLabel().compareTo(l2.getLabOrder().getPatient().getLabel());
					break;

				case 1:
					rc = l1.getLabOrder().getLabItem().getLabel()
							.compareToIgnoreCase(l2.getLabOrder().getLabItem().getLabel());
					break;

				case 3:
					rc = l1.getDate().compareTo(l2.getDate());
					break;

				case 4:
					rc = l1.getItem().getKuerzel().compareToIgnoreCase(l2.getItem().getKuerzel());
					break;
				}

				if (direction) {
					rc = -rc;
				}
				return rc;

			}
			return 0;

		}

	}

}
