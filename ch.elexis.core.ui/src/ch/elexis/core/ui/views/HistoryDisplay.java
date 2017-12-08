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
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.FormText;

import ch.elexis.core.constants.Preferences;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.BackgroundJob;
import ch.elexis.core.ui.actions.BackgroundJob.BackgroundJobListener;
import ch.elexis.core.ui.actions.HistoryLoader;
import ch.elexis.core.ui.actions.KonsFilter;
import ch.elexis.core.ui.icons.Images;
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
public class HistoryDisplay extends ScrolledComposite implements BackgroundJobListener,
		ElexisEventListener {
	FormText text;
	ArrayList<Konsultation> lKons;
	private HistoryLoader loader;
	private boolean bLock;
	HistoryDisplay self = this;
	
	boolean multiline = false;
	private int currentPage;
	
	private volatile boolean isLazyLoadingBusy;
	
	private static final int LAZY_LOADING_FETCHSIZE = 20;
	
	public HistoryDisplay(Composite parent, final IViewSite site){
		this(parent, site, false);
	}
	
	public HistoryDisplay(Composite parent, final IViewSite site, boolean multiline){
		super(parent, SWT.V_SCROLL | SWT.BORDER);
		currentPage = 0;
		this.multiline = multiline;
		lKons = new ArrayList<Konsultation>(20);
		text = UiDesk.getToolkit().createFormText(this, false);
		text.setWhitespaceNormalized(true);
		text.setColor(UiDesk.COL_BLUE, UiDesk.getColorRegistry().get(UiDesk.COL_BLUE));
		text.setColor(UiDesk.COL_GREEN, UiDesk.getColorRegistry().get(UiDesk.COL_LIGHTGREY));
		text.setFont(UiDesk.getFont(Preferences.USR_DEFAULTFONT));
		setContent(text);
		text.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(HyperlinkEvent e){
				String id = (String) e.getHref();
				if ("linkprevious".equals(id)) {
					doLazyLoading(currentPage - 1);
				} else if ("linknext".equals(id)) {
					doLazyLoading(currentPage + 1);
				}
				else {
					Konsultation k = Konsultation.load(id);
					ElexisEventDispatcher.fireSelectionEvent(k);
				}
			}
			
		});
		text.setText(Messages.HistoryDisplay_NoPatientSelected, false, false); //$NON-NLS-1$
		addControlListener(new ControlAdapter() {
			@Override
			public void controlResized(ControlEvent e){
				text.setSize(text.computeSize(self.getSize().x - 15, SWT.DEFAULT));
			}
			
		});
		ElexisEventDispatcher.getInstance().addListeners(this);
	}
	
	/**
	 * If page is greater then 0 lazy loading is activated
	 * 
	 * @param page
	 */
	private void doLazyLoading(int page){
		if (!isLazyLoadingBusy) {
			if (page > 0 && page <= getMaxPageSize()) {
				isLazyLoadingBusy = true;
				currentPage = page;
				start();
			}
		}
	}
	
	@Override
	public void dispose(){
		ElexisEventDispatcher.getInstance().removeListeners(this);
		super.dispose();
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
			loader = new HistoryLoader(new StringBuilder(), lKons, multiline, currentPage,
				LAZY_LOADING_FETCHSIZE);
		} else {
			// filter is set - no lazy loading
			currentPage = 0;
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
	
	public void load(Patient pat){
		// lazy loading konsultations
		currentPage = 0;
		isLazyLoadingBusy = false;
		
		if (pat != null) {
			lKons.clear();
			Fall[] faelle = pat.getFaelle();
			for (Fall f : faelle) {
				load(f, false);
			}
			
			// activate lazy loading
			if (lKons.size() > LAZY_LOADING_FETCHSIZE) {
				currentPage = 1;
			}
		}
		
		UiDesk.getDisplay().asyncExec(new Runnable() {
			public void run(){
				if (!isDisposed()) {
					setOrigin(0, 0);
					if (lKons.size() > 0) {
						text.setText("wird geladen...", false, false);
						text.setSize(text.computeSize(self.getSize().x - 10, SWT.DEFAULT));
					}
				}
			}
		});
		
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
							text.setImage("previous", Images.IMG_PREVIOUS.getImage());
							text.setImage("next", Images.IMG_NEXT.getImage());
							text.setText("<form>" + getPaginationField() + s + "</form>", true,
								true);
						}
					} else {
						text.setText(ElexisEventDispatcher.getSelectedPatient() != null ? ""
								: Messages.HistoryDisplay_NoPatientSelected,
							false, false);
					}
					
					text.setSize(text.computeSize(self.getSize().x - 10, SWT.DEFAULT));	
				}
				isLazyLoadingBusy = false;
			}

			private String getPaginationField(){
				return currentPage > 0
						? "<p><a href=\"linkprevious\"><img href=\"previous\"></img></a> ("
							+ currentPage + "/"
							+ getMaxPageSize()
							+ ") <a href=\"linknext\"><img href=\"next\"></img></a></p>"
						: "";
			}

		});
	}
	
	private int getMaxPageSize(){
		if (lKons != null) {
			return (int) Math.ceil((double) lKons.size() / LAZY_LOADING_FETCHSIZE);
		}
		return 0;
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
