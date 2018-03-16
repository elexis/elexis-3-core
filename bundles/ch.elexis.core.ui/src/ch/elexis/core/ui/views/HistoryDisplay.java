/*******************************************************************************
 * Copyright (c) 2006-2010, G. Weirich, D. Lutz, P. Schönbucher and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *    
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.util.ArrayList;

import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ControlAdapter;
import org.eclipse.swt.events.ControlEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.jdt.Nullable;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.HistoryLoader;
import ch.elexis.core.ui.actions.KonsFilter;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.views.controls.PagingComposite;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Patient;

/**
 * Anzeige der vergangenen Konsultationen. Es sollen einerseits "sofort" die letzten 3 oder 4 Kons
 * angezeigt werden, andererseits aber je nach Anforderung auch frühere nachgeladen werden. Dies ist
 * noch nicht korrekt implemetiert - aktuell werden immer alle Kons. geladen.
 * 
 * @author Gerry
 * 
 */
public class HistoryDisplay extends Composite implements BackgroundJobListener,
		ElexisEventListener {
	FormText text;
	ArrayList<Konsultation> lKons;
	private HistoryLoader loader;
	
	private final ScrolledComposite scrolledComposite;
	
	boolean multiline = false;
	
	private static final int PAGING_FETCHSIZE = 20;
	
	private PagingComposite pagingComposite;
	
	public HistoryDisplay(Composite parent, final IViewSite site){
		this(parent, site, false);
	}
	
	public HistoryDisplay(Composite parent, final IViewSite site, boolean multiline){
		super(parent, SWT.NONE);
		setLayout(SWTHelper.createGridLayout(true, 1));
		setLayoutData(new GridData(SWT.CENTER, SWT.TOP, true, true));
		pagingComposite = new PagingComposite(this, SWT.NONE) {
			@Override
			public void runPaging(){
				start();
			}
		};
		scrolledComposite = new ScrolledComposite(this, SWT.V_SCROLL | SWT.BORDER);
		scrolledComposite.setLayout(SWTHelper.createGridLayout(true, 1));
		scrolledComposite.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		
		this.multiline = multiline;
		lKons = new ArrayList<Konsultation>(20);
		
		text = UiDesk.getToolkit().createFormText(scrolledComposite, false);
		text.setWhitespaceNormalized(true);
		text.setColor(UiDesk.COL_BLUE, UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
		text.setColor(UiDesk.COL_GREEN, UiDesk.getColorRegistry().get(UiDesk.COL_LIGHTGREY));
		text.setColor(UiDesk.COL_DARKGREY, UiDesk.getColorRegistry().get(UiDesk.COL_DARKGREY));
		text.setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
		scrolledComposite.setContent(text);
		text.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(HyperlinkEvent e){
				String id = (String) e.getHref();
				Konsultation k = Konsultation.load(id);
				ElexisEventDispatcher.fireSelectionEvent(k);
			}
			
		});
		text.setText(Messages.HistoryDisplay_NoPatientSelected, false, false); //$NON-NLS-1$
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e){
				text.setSize(text.computeSize(scrolledComposite.getSize().x - 15, SWT.DEFAULT));
			}
			
		});
		ElexisEventDispatcher.getInstance().addListeners(this);
	}
	
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(this);
		scrolledComposite.dispose();
	}
	
	public void setFilter(KonsFilter f){
		stop();
		loader.setFilter(f);
	}
	
	public void start(){
		start(null);
	}
	
	public void start(KonsFilter f){
		stop();
		if (f == null) {
			loader = new HistoryLoader(new StringBuilder(), lKons, multiline,
				pagingComposite.getCurrentPage(), pagingComposite.getFetchSize());
		} else {
			// filter is set - no lazy loading
			pagingComposite.reset();
			loader = new HistoryLoader(new StringBuilder(), lKons, multiline);
		}
		loader.setFilter(f);
		loader.addListener(this);
		loader.schedule();
	}
	
	public void stop(){
		if (loader != null) {
			loader.removeListener(this);
			loader.cancel();
		}
	}
	
	public void load(Fall fall, boolean clear){
		if (clear) {
			lKons.clear();
		}
		if (fall != null) {
			Konsultation[] kons = fall.getBehandlungen(true);
			for (Konsultation k : kons) {
				lKons.add(k);
			}
		}
	}
	
	/**
	 * Loads all {@link Konsultation} for a {@link Patient}. If the {@link ElexisEvent} is null or
	 * the event is triggered by a {@link Patient}, all {@link Konsultation} will loaded instantly.
	 * 
	 * @param pat
	 * @param ev
	 */
	public void load(Patient pat, @Nullable ElexisEvent ev){
		if (ev == null || ev.getObject() instanceof Patient) {
			UiDesk.getDisplay().syncExec(new Runnable() {
				public void run(){
					if (!isDisposed()) {
						scrolledComposite.setOrigin(0, 0);
						if (lKons.size() > 0) {
							text.setText("wird geladen...", false, false);
							text.setSize(
								text.computeSize(scrolledComposite.getSize().x - 10, SWT.DEFAULT));
						}
					}
				}
			});
		}
		
		// lazy loading konsultations		
		if (pat != null) {
			lKons.clear();
			Fall[] faelle = pat.getFaelle();
			for (Fall f : faelle) {
				load(f, false);
			}
			pagingComposite.setup(1, lKons.size(), PAGING_FETCHSIZE);
		}
	}
	
	public void jobFinished(BackgroundJob j){
		UiDesk.getDisplay().asyncExec(new Runnable() {
			public void run(){
				String s = (String) loader.getData();
				
				// check if widget is valid
				if (!isDisposed()) {
					
					if (s != null) {
						int idxFrom = s.indexOf("<form>");
						int idxTo = s.indexOf("</form>");
						if (idxFrom != -1 && idxTo != -1) {
							s = s.substring(idxFrom + 6, s.indexOf("</form>"));
							text.setText(
								"<form>" + getDateFromToText()
								+ s + "</form>", true,
								true);
						}
					} else {
						text.setText(ElexisEventDispatcher.getSelectedPatient() != null ? ""
								: Messages.HistoryDisplay_NoPatientSelected,
							false, false);
					}
					
					text.setSize(text.computeSize(scrolledComposite.getSize().x - 10, SWT.DEFAULT));
				}
			}
			
			public String getDateFromToText(){
				if (loader.getlKons() != null && loader.getlKons().size() > 0) {
					Konsultation firstKons = loader.getlKons().get(loader.getlKons().size() - 1);
					Konsultation lastKons = loader.getlKons().get(0);
					String fromDate = firstKons != null ? firstKons.getDatum() : "-";
					String toDate = lastKons != null ? lastKons.getDatum() : "-";
					return "<p><span color=\"" + UiDesk.COL_DARKGREY + "\">von " + fromDate
						+ " bis " + toDate
						+ "</span></p>";
				}
				return "";
				
			}
		});
	}
	
	

	public void catchElexisEvent(ElexisEvent ev){
		UiDesk.asyncExec(new Runnable() {
			
			public void run(){
				if (text != null && (!text.isDisposed())) {
					text.setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
				}
			}
		});
	}
	
	private final ElexisEvent eetemplate = new ElexisEvent(null, null,
		ElexisEvent.EVENT_USER_CHANGED);
	
	public ElexisEvent getElexisEventFilter(){
		return eetemplate;
	}
}
