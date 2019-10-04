/*******************************************************************************
 * Copyright (c) 2006-2009, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.actions;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.core.runtime.jobs.Job;
import org.eclipse.jface.viewers.IFilter;

import ch.elexis.core.data.service.ContextServiceHolder;
import ch.elexis.core.model.ICoverage;
import ch.elexis.core.model.IEncounter;
import ch.elexis.core.text.model.Samdas;
import ch.elexis.data.Konsultation;
import ch.rgw.tools.VersionedResource;

/**
 * Texte früherer Konsultationen asynchron nachladen.
 * 
 * @author Gerry
 * 
 */
public class HistoryLoader extends BackgroundJob {
	StringBuilder sb;
	private List<IEncounter> lKons;
	KonsFilter filter;
	IFilter globalFilter;
	private final int currentPage;
	private final int pageSize;
	boolean multiline = false;
	
	public void setFilter(final KonsFilter kf){
		filter = kf;
	}
	
	/*
	 * multine == true: show Konsultation text with newlines
	 */
	public HistoryLoader(final StringBuilder sb, final List<IEncounter> lKons){
		this(sb, lKons, false);
	}
	
	public HistoryLoader(final StringBuilder sb, final List<IEncounter> lKons,
		final boolean multiline){
		this(sb, lKons, multiline, 0, 0);
	}
	
	public HistoryLoader(final StringBuilder sb, final List<IEncounter> paramlKons,
		final boolean multiline, final int currentPage, final int pageSize){
		super(Messages.HistoryLoader_LoadKonsMessage); //$NON-NLS-1$
		this.sb = sb;
		this.lKons = new ArrayList<IEncounter>(paramlKons);
		this.multiline = multiline;
		this.setPriority(Job.DECORATE);
		this.setUser(false);
		this.currentPage = currentPage;
		this.pageSize = pageSize;
	}
	@Override
	public IStatus execute(final IProgressMonitor monitor){
		synchronized (lKons) {
			monitor.beginTask(Messages.HistoryLoader_LoadKonsMessage, lKons.size() + 100); //$NON-NLS-1$
			monitor.subTask(Messages.HistoryLoader_Sorting); //$NON-NLS-1$
			if (lKons.isEmpty()) {
				return Status.OK_STATUS;
			}
			
			Collections.sort(lKons, new Comparator<IEncounter>() {
				public int compare(final IEncounter o1, final IEncounter o2){
					if ((o1 == null) || (o2 == null)) {
						return 0;
					}
					
					int sort = o2.getDate().compareTo(o1.getDate());
					if (sort != 0) {
						return sort;
					}
					return Long.compare(o2.getLastupdate(), o1.getLastupdate());
				}
			});

			if (currentPage > 0 && pageSize > 0)
			{
				// lazy loading via pagination
				int fromIdx = (currentPage - 1) * pageSize;
				int toIdx = currentPage * pageSize;
				
				// upper limit corrections
				if (toIdx > lKons.size()) {
					toIdx = lKons.size();
					fromIdx = toIdx - pageSize;
				}
				// lower limit corrections
				if (fromIdx < 0) {
					fromIdx = 0;
				}
				lKons = new ArrayList<IEncounter>(
					fromIdx < toIdx ? lKons.subList(fromIdx, toIdx) : lKons);
			} else {
				lKons = new ArrayList<IEncounter>(lKons);
			}
			monitor.worked(50);
			
			ICoverage selectedFall = ContextServiceHolder.get().getActiveCoverage().orElse(null);
			Iterator<IEncounter> it = lKons.iterator();
			sb.append("<form>"); //$NON-NLS-1$
			//@TODO convert to jpa
			globalFilter = ObjectFilterRegistry.getInstance().getFilterFor(Konsultation.class);
			while (!monitor.isCanceled()) {
				if (!it.hasNext()) {
					sb.append("</form>"); //$NON-NLS-1$
					result = sb.toString();
					monitor.worked(1);
					monitor.done();
					return Status.OK_STATUS;
				}
				IEncounter k = it.next();
				if (filter != null) {
					if (filter.pass(k) == false) {
						continue;
					}
				}
				if (globalFilter != null) {
					if (globalFilter.select(k) == false) {
						continue;
					}
				}
				VersionedResource vr = k.getVersionedEntry();
				String s = vr.getHead();
				if (s != null) {
					if (s.startsWith("<")) { //$NON-NLS-1$
						Samdas samdas = new Samdas(s);
						s = samdas.getRecordText();
					}
					s = maskHTML(s);
					if (multiline) {
						// TODO use system line separator
						// replace Windows line separator
						s = s.replaceAll("\r\n", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
						// replace remaining "manual" line separators
						s = s.replaceAll("\n", "<br/>"); //$NON-NLS-1$ //$NON-NLS-2$
					}
					
				} else {
					s = ""; //$NON-NLS-1$
				}
				String label = maskHTML(k.getLabel());
				// make kons text grey if kons Fall is not the selected Fall
				if (selectedFall != null && !selectedFall.equals(k.getCoverage())) {
					sb.append("<p><a href=\"") //$NON-NLS-1$
						.append(maskHTML(k.getId())).append("\">") //$NON-NLS-1$
						.append(label).append("</a><br/>") //$NON-NLS-1$
						.append("<span color=\"gruen\">") //$NON-NLS-1$
						.append(maskHTML(k.getCoverage().getLabel()))
						.append("</span><br/><span color=\"gruen\">") //$NON-NLS-1$
						.append(s).append("</span></p>"); //$NON-NLS-1$
				} else {
					sb.append("<p><a href=\"") //$NON-NLS-1$
					.append(maskHTML(k.getId())).append("\">") //$NON-NLS-1$
					.append(label).append("</a><br/>") //$NON-NLS-1$
					.append("<span color=\"gruen\">") //$NON-NLS-1$
					.append(maskHTML(k.getCoverage().getLabel())).append("</span><br/>") //$NON-NLS-1$
					.append(s).append("</p>"); //$NON-NLS-1$
				}
				monitor.worked(1);
				
			}
			sb.setLength(0);
			monitor.done();
		}
		return Status.CANCEL_STATUS;
	}
	
	private String maskHTML(String input){
		String s = input.replaceAll("<", "&lt;"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll(">", "&gt;"); //$NON-NLS-1$ //$NON-NLS-2$
		s = s.replaceAll("&", "&amp;"); //$NON-NLS-1$ //$NON-NLS-2$
		return s;
	}
	
	@Override
	public int getSize(){
		return lKons.size();
	}
	
	public List<IEncounter> getlKons(){
		return lKons;
	}
	
}
