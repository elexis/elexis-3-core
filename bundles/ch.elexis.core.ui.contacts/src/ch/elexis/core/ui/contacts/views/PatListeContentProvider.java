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
import java.util.stream.Collectors;
import java.util.stream.Stream;

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
import ch.elexis.core.services.IQuery;
import ch.elexis.core.services.holder.AccessControlServiceHolder;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.CommonViewerContentProvider;
import ch.elexis.data.Query;
import ch.rgw.tools.StringTool;

public class PatListeContentProvider extends CommonViewerContentProvider implements ILazyContentProvider {
	private static final int QUERY_LIMIT = 500;

	// @formatter:off
	private final static String BASEQUERY = "SELECT * FROM Kontakt "
			+ "WHERE (DELETED = 0 AND istPerson = 1) AND istPatient = 1";
	private static final String[] FILTER_FIELDS = {"PatientNr", "Bezeichnung1", "Bezeichnung2", "Geburtsdatum"};
	private static final String ORDER_BY_CLAUSE = " ORDER BY %s ASC";
	private static final String CAST_DECIMAL_TEMPLATE = "CAST(%s AS DECIMAL)";
	private static final String LIMIT_TEMPLATE = " LIMIT %s";
	private static final String LIKE_TEMPLATE = " AND %s LIKE '%s'";
	private static final String IS_TEMPLATE = " AND %s = '%s'";
	
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
		List<IPatient> lPats;
		lPats = (List<IPatient>) getPatientsList();
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
	
	/**
	 * TODO: pls fix hardcoding % idk man
	 * <p>
	 * Builds an sql query string which also applies filters based on input values.
	 * Executes the query and returns the results.
	 * </p>
	 * @return list of IPatients
	 * 
	 * @author mdedic
	 * @since 3.13
	 */
	private List<IPatient> getPatientsList() {
		String query = BASEQUERY;
		String[] filters = commonViewer.getConfigurer().getControlFieldProvider().getValues();
		    
		for (int i = 0; i < filters.length; i++) {
		    String string = filters[i];
		    if(string.length() > 0 && !string.isBlank()) {
		    	// if its dob filter, format it
		    	if(i == 3) {
		    		string = string.replaceAll("[^\\d]", "");
		    		string = stringToDate(string);
		    		string = "%" + string;
		    	}
		    	// append filter to the query
		    	// very bad hardcoded string ...
		    	// if 0 (=patientnr), then get the exact record instead of like
		    	if(i == 0) {
		    		query += String.format(IS_TEMPLATE, FILTER_FIELDS[i] , string);
		    	} else {
		    		query += String.format(LIKE_TEMPLATE, FILTER_FIELDS[i] , string + "%");
				}
			}
		}
		// find out which sort order was selected, then append order by to the query
		// if its code aka patientnr, then cast as decimal to it.
		if(firstOrder.equals(orderFields[0])) {
			String casted = String.format(CAST_DECIMAL_TEMPLATE, FILTER_FIELDS[0]);
			query += String.format(ORDER_BY_CLAUSE, casted);
		} else {
			for (int i = 1; i < FILTER_FIELDS.length; i++) {
				if(firstOrder.equals(orderFields[i])) {
					query += String.format(ORDER_BY_CLAUSE, FILTER_FIELDS[i]);
				}
			}
		}
		// mehr als 500 anzeigen
		if (!ignoreLimit) {
			query += String.format(LIMIT_TEMPLATE, QUERY_LIMIT);
		}
		System.out.println(query);
		Stream<IPatient> patientQuery = CoreModelServiceHolder.get().executeNativeQuery(query, IPatient.class);
		return patientQuery.collect(Collectors.toList());
	}

	/**
	 * Converts a date string to a fitting format for the database.
	 * This method also assumes the input is ddMMyyyy.
	 * db is yyyyMMdd so this method just formats ddMMyyyy so it works.
	 * 
	 * @param string
	 * @return string formatted for the database
	 * 
	 * @author mdedic
	 * @since 3.13
	 */
	private String stringToDate(String string) {
		if(string.length()>2) {
			StringBuilder sb = new StringBuilder(string.substring(0, 2));
			String mm = string.substring(2);
			mm = mm.substring(0, Math.min(mm.length(), 2));
			mm+="%";
			if(string.length() == 4) {
				return string;
			}
			sb.insert(0, mm);
			if(string.length()>4) {
				String yy = string.substring(4);
				yy+="%";
				sb.insert(0, yy);
			}
			string = sb.toString();
		}
		return string;
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
