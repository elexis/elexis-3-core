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
package ch.elexis.core.ui.contacts.views;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.ILazyContentProvider;
import org.eclipse.jface.viewers.TableViewer;
import org.eclipse.jface.viewers.Viewer;
import org.eclipse.ui.part.ViewPart;
import org.eclipse.ui.progress.IProgressConstants;
import org.eclipse.ui.progress.IWorkbenchSiteProgressService;

import ch.elexis.core.ac.EvACE;
import ch.elexis.core.ac.Right;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.service.CoreModelServiceHolder;
import ch.elexis.core.l10n.Messages;
import ch.elexis.core.model.IPatient;
import ch.elexis.core.model.ModelPackage;
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.IQuery.COMPARATOR;
import ch.elexis.core.services.IQuery.ORDER;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewerContentProvider;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class PatListeContentProvider extends CommonViewerContentProvider implements ILazyContentProvider {
	private static final int QUERY_LIMIT = 500;

	Object[] pats;
	boolean bValid = false;
	boolean bUpdating = false;
	String[] orderLabels;
	String[] orderFields;
	String firstOrder;
	ViewPart site;

	public PatListeContentProvider(CommonViewer cv, String[] fieldsToOrder, ViewPart s) {
		super(cv);
		site = s;
		updateFields(fieldsToOrder);
	}

	/**
	 * Update the used fields to order the content.
	 *
	 * @param fieldsToOrder
	 * @since 3.0.0
	 */
	public void updateFields(String[] fieldsToOrder) {
		orderLabels = new String[fieldsToOrder.length];
		orderFields = new String[fieldsToOrder.length];
		for (int i = 0; i < fieldsToOrder.length; i++) {
			String[] def = fieldsToOrder[i].split(Query.EQUALS);
			orderFields[i] = def[0];
			orderLabels[i] = def.length > 1 ? def[1] : def[0];
		}
		firstOrder = orderFields[0];
	}

	@Override
	protected IQuery<?> getBaseQuery() {
		IQuery<IPatient> ret = CoreModelServiceHolder.get().getQuery(IPatient.class);
		if (!ignoreLimit) {
			ret.limit(QUERY_LIMIT);
		}
		return ret;
	}

	/**
	 * @since 3.2
	 */
	public void syncRefresh() {
		@SuppressWarnings("unchecked")
		IQuery<IPatient> patientQuery = (IQuery<IPatient>) getBaseQuery();
		// TODO implement as precondition?
		patientQuery.and(ModelPackage.Literals.ICONTACT__PATIENT, COMPARATOR.EQUALS, true);

		commonViewer.getConfigurer().getControlFieldProvider().setQuery(patientQuery);
		getQueryFilters().forEach(filter -> filter.apply(patientQuery));
		String[] actualOrder;
		int idx = StringTool.getIndex(orderFields, firstOrder);
		if ((idx == -1) || (idx == 0)) {
			actualOrder = orderFields;
		} else {
			actualOrder = new String[orderFields.length];
			int n = 0;
			int begin = idx;
			do {
				actualOrder[n++] = orderFields[idx++];
				if (idx >= orderFields.length) {
					idx = 0;
				}
			} while (idx != begin);
		}
		if (actualOrder != null && actualOrder.length > 0) {
			for (String order : actualOrder) {
				if (firstOrder.equals("code")) { //$NON-NLS-1$
					patientQuery.orderByLeftPadded(order, ORDER.ASC, firstOrder);
				} else {
					patientQuery.orderBy(order, ORDER.ASC);
				}
			}
		}
		List<IPatient> lPats = patientQuery.execute();
		pats = lPats.toArray(new Object[lPats.size()]);
		UiDesk.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				TableViewer tv = (TableViewer) commonViewer.getViewerWidget();
				if (tv != null && tv.getControl() != null && !tv.getControl().isDisposed()) {
					tv.setItemCount(pats.length);
					bValid = true;
					tv.refresh();
					commonViewer.resetScrollbarPosition(tv, ignoreLimit);
					commonViewer.setLimitReached(pats.length == QUERY_LIMIT, QUERY_LIMIT);
				}
				bUpdating = false;
			}
		});
	}

	@Override
	public Object[] getElements(Object inputElement) {
		if (bValid || bUpdating) {
			return pats;
		}

		if (!AccessControlServiceHolder.get().evaluate(EvACE.of(IPatient.class, Right.VIEW))) {
			return new Object[0];
		}

		Job job = new Job(Messages.PatListeContentProvider_LoadingPatients) {
			@Override
			protected IStatus run(IProgressMonitor monitor) {
				monitor.beginTask(Messages.PatListeContentProvider_LoadPatients, IProgressMonitor.UNKNOWN);
				// perform actual loading
				syncRefresh();
				monitor.done();
				return Status.OK_STATUS;
			}
		};
		job.setPriority(Job.SHORT);
		job.setUser(false);
		bUpdating = true;
		IWorkbenchSiteProgressService siteService = (IWorkbenchSiteProgressService) site.getSite()
				.getAdapter(IWorkbenchSiteProgressService.class);
		siteService.schedule(job, 0, true);

		job.setProperty(IProgressConstants.ICON_PROPERTY, Images.IMG_AUSRUFEZ_ROT.getImage());

		return pats;
	}

	@Override
	public void changed(HashMap<String, String> values) {
		super.setIgnoreLimit(false);
		bValid = false;
		// trigger loading pats
		getElements(null);
	}

	@Override
	protected void setIgnoreLimit(boolean value) {
		super.setIgnoreLimit(value);
		if (value) {
			bValid = false;
			// trigger loading pats
			getElements(null);
		}
	}

	@Override
	public void dispose() {
		stopListening();
	}

	@Override
	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
	}

	@Override
	public void reorder(String field) {
		int idx = StringTool.getIndex(orderFields, field);
		if (idx > -1) {
			firstOrder = orderFields[idx];
			changed(null);
		}

	}

	@Override
	public void selected() {
		// TODO Auto-generated method stub

	}

	@Override
	public void updateElement(int index) {
		if (!bValid) {
			getElements(commonViewer);
		}

		TableViewer tv = (TableViewer) commonViewer.getViewerWidget();
		if (pats.length > index) {
			tv.replace(pats[index], index);
		} else {
			Object elementAt = tv.getElementAt(index);
			if (elementAt != null) {
				tv.replace(StringConstants.DASH, index);
			}
		}
	}

	public void invalidate() {
		bValid = false;
	}

	/**
	 * Directly add an object to the content providers held array.
	 *
	 * @param newObject
	 * @return
	 * @see https://redmine.medelexis.ch/issues/5719 for use case
	 * @since 3.2
	 */
	void temporaryAddObject(Object newObject) {
		ArrayList<Object> temp = null;
		if (pats != null) {
			temp = new ArrayList<>(Arrays.asList(pats));
		} else {
			temp = new ArrayList<>();
		}
		temp.add(newObject);
		pats = temp.toArray();
		((TableViewer) commonViewer.getViewerWidget()).setItemCount(pats.length);
	}

	@Override
	public void init() {
		// TODO Auto-generated method stub

	}
}
