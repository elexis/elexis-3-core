/*******************************************************************************
 * Copyright (c) 2005-2009, G. Weirich and Elexis
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

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.viewers.ISelectionChangedListener;
import org.eclipse.jface.viewers.SelectionChangedEvent;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.viewers.TreeViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.forms.widgets.FormText;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.ScrolledForm;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.LazyTreeLoader;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.TreeContentProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.data.BBSEntry;
import ch.elexis.data.Query;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.Tree;

/**
 * Bulletin Board System - ein Schwarzes Brett. Im Prinzip Erweiterung des Reminder-Konzepts zu
 * Threads ähnlich newsreader und Webforen. Farben können mit &lt;span
 * color="rot"&gt;...&lt;/span&gt; kontrolliert werden (bzw. "grün" und "blau")
 * 
 * @author gerry
 */
@Deprecated
public class BBSView extends ViewPart implements ISelectionChangedListener, ISaveablePart2 {
	public static final String ID = "ch.elexis.BBSView"; //$NON-NLS-1$
	private CommonViewer headlines;
	private ViewerConfigurer vc;
	private ScrolledForm form;
	private FormToolkit tk;
	private Query<BBSEntry> qbe;
	private LazyTreeLoader<BBSEntry> loader;
	private Label origin;
	private FormText msg;
	private Text input;
	
	@Override
	public void createPartControl(Composite parent){
		SashForm sash = new SashForm(parent, SWT.NONE);
		qbe = new Query<BBSEntry>(BBSEntry.class);
		loader = new LazyTreeLoader<BBSEntry>("BBS", qbe, "reference", new String[] { //$NON-NLS-1$ //$NON-NLS-2$
				"datum", "time", "Thema" //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
			});
		headlines = new CommonViewer();
		vc =
			new ViewerConfigurer(new TreeContentProvider(headlines, loader),
				new ViewerConfigurer.TreeLabelProvider(), new DefaultControlFieldProvider(
					headlines, new String[] {
						"Thema" //$NON-NLS-1$
					}), new NewThread(), new SimpleWidgetProvider(SimpleWidgetProvider.TYPE_TREE,
					SWT.NONE, null));
		headlines.create(vc, sash, SWT.NONE, getViewSite());
		
		tk = UiDesk.getToolkit();
		form = tk.createScrolledForm(sash);
		form.getBody().setLayout(new GridLayout(1, false));
		form.setText(Messages.BBSView_PleaseEnterSubject); //$NON-NLS-1$
		origin = tk.createLabel(form.getBody(), ""); //$NON-NLS-1$
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		origin.setLayoutData(gd);
		msg = tk.createFormText(form.getBody(), false);
		gd =
			new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL
				| GridData.FILL_VERTICAL);
		msg.setLayoutData(gd);
		msg.setColor(Messages.BBSView_rot, UiDesk.getColor(UiDesk.COL_RED)); //$NON-NLS-1$
		msg.setColor(Messages.BBSView_gruen, UiDesk.getColor(UiDesk.COL_GREEN)); //$NON-NLS-1$
		msg.setColor(Messages.BBSView_blau, UiDesk.getColor(UiDesk.COL_BLUE)); //$NON-NLS-1$
		input = tk.createText(form.getBody(), "", SWT.WRAP | SWT.MULTI | SWT.BORDER); //$NON-NLS-1$
		gd = new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL);
		input.setLayoutData(gd);
		Button send = tk.createButton(form.getBody(), Messages.BBSView_DoSend, SWT.PUSH); //$NON-NLS-1$
		send.addSelectionListener(new SelectionAdapter() {
			@SuppressWarnings("unchecked")
			@Override
			public void widgetSelected(SelectionEvent e){
				
				Object[] sel = headlines.getSelection();
				if (sel == null || sel.length == 0) {
					return;
				}
				Tree item = (Tree) sel[0];
				BBSEntry en = (BBSEntry) (item).contents;
				BBSEntry ne = new BBSEntry(en.getTopic(), CoreHub.getLoggedInContact(), en, input.getText());
				Tree child = item.add(ne);
				((TreeViewer) headlines.getViewerWidget()).add(sel[0], child);
				((TreeViewer) headlines.getViewerWidget()).setSelection(new StructuredSelection(
					child), true);
			}
			
		});
		headlines.getViewerWidget().addSelectionChangedListener(this);
		((TreeContentProvider) headlines.getConfigurer().getContentProvider()).startListening();
		setDisplay();
	}
	
	@Override
	public void setFocus(){
		// TODO Auto-generated method stub
		
	}
	
	/*
	 * (Kein Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose(){
		((TreeContentProvider) headlines.getConfigurer().getContentProvider()).stopListening();
		super.dispose();
	}
	
	@SuppressWarnings("unchecked")
	public void setDisplay(){
		Object[] sel = headlines.getSelection();
		if (sel == null || sel.length == 0) {
			form.setText(Messages.BBSView_14); //$NON-NLS-1$
			return;
		}
		BBSEntry en = ((Tree<BBSEntry>) sel[0]).contents;
		form.setText(en.getTopic());
		StringBuilder sb = new StringBuilder();
		sb.append(en.getAuthor().getLabel()).append(Messages.BBSView_15).append(en.getDate())
			.append( //$NON-NLS-1$
				Messages.BBSView_16).append(en.getTime()).append(Messages.BBSView_17); //$NON-NLS-1$ //$NON-NLS-2$
		origin.setText(sb.toString());
		try {
			msg.setText(Messages.BBSView_18 + en.getText() + Messages.BBSView_19, true, true); //$NON-NLS-1$ //$NON-NLS-2$
		} catch (Exception ex) {
			ExHandler.handle(ex);
			
		}
		input.setText(Messages.BBSView_20); //$NON-NLS-1$
	}
	
	class NewThread implements ViewerConfigurer.ButtonProvider {
		
		@Override
		public Button createButton(Composite parent){
			Button ret = new Button(parent, SWT.PUSH);
			ret.setText(Messages.BBSView_21); //$NON-NLS-1$
			ret.addSelectionListener(new SelectionAdapter() {
				
				@Override
				public void widgetSelected(SelectionEvent e){
					new BBSEntry(
						headlines.getConfigurer().getControlFieldProvider().getValues()[0],
						CoreHub.getLoggedInContact(), null, Messages.BBSView_22); //$NON-NLS-1$
					loader.invalidate();
					headlines.notify(CommonViewer.Message.update);
					setDisplay();
				}
				
			});
			return ret;
		}
		
		@Override
		public boolean isAlwaysEnabled(){
			return false;
		}
		
	}
	
	@Override
	public void selectionChanged(SelectionChangedEvent event){
		setDisplay();
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	@Override
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	@Override
	public void doSave(IProgressMonitor monitor){ /* leer */}
	
	@Override
	public void doSaveAs(){ /* leer */}
	
	@Override
	public boolean isDirty(){
		return true;
	}
	
	@Override
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	@Override
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
}
