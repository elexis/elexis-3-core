/*******************************************************************************
 * Copyright (c) 2005-2010, G. Weirich and Elexis
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

import java.util.HashMap;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.action.IAction;
import org.eclipse.jface.viewers.StructuredViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.Kontakt;
import ch.elexis.core.data.Organisation;
import ch.elexis.core.data.PersistentObject;
import ch.elexis.core.data.Person;
import ch.elexis.core.data.Query;
import ch.elexis.core.data.admin.AccessControlDefaults;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.icons.Images;
import ch.elexis.core.ui.actions.FlatDataLoader;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.PersistentObjectLoader;
import ch.elexis.core.ui.actions.RestrictedAction;
import ch.elexis.core.ui.dialogs.GenericPrintDialog;
import ch.elexis.core.ui.dialogs.KontaktErfassenDialog;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.core.ui.util.viewers.CommonViewer;
import ch.elexis.core.ui.util.viewers.DefaultControlFieldProvider;
import ch.elexis.core.ui.util.viewers.DefaultLabelProvider;
import ch.elexis.core.ui.util.viewers.SimpleWidgetProvider;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer;
import ch.elexis.core.ui.util.viewers.ViewerConfigurer.ControlFieldListener;
import ch.rgw.tools.ExHandler;
import ch.rgw.tools.StringTool;

public class KontakteView extends ViewPart implements ControlFieldListener, ISaveablePart2 {
	public static final String ID = "ch.elexis.Kontakte"; //$NON-NLS-1$
	private CommonViewer cv;
	private ViewerConfigurer vc;
	IAction dupKontakt, delKontakt, createKontakt, printList;
	PersistentObjectLoader loader;
	
	private final String[] fields = {
		Kontakt.FLD_SHORT_LABEL + Query.EQUALS + Messages.getString("KontakteView.shortLabel"), //$NON-NLS-1$
		Kontakt.FLD_NAME1 + Query.EQUALS + Messages.getString("KontakteView.text1"), //$NON-NLS-1$
		Kontakt.FLD_NAME2 + Query.EQUALS + Messages.getString("KontakteView.text2"), //$NON-NLS-1$
		Kontakt.FLD_STREET + Query.EQUALS + Messages.getString("KontakteView.street"), //$NON-NLS-1$
		Kontakt.FLD_ZIP + Query.EQUALS + Messages.getString("KontakteView.zip"), //$NON-NLS-1$
		Kontakt.FLD_PLACE + Query.EQUALS + Messages.getString("KontakteView.place")}; //$NON-NLS-1$
	private ViewMenus menu;
	
	public KontakteView(){}
	
	@Override
	public void createPartControl(Composite parent){
		parent.setLayout(new FillLayout());
		cv = new CommonViewer();
		loader = new FlatDataLoader(cv, new Query<Kontakt>(Kontakt.class));
		loader.setOrderFields(new String[] {
			Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_STREET, Kontakt.FLD_PLACE
		});
		vc =
			new ViewerConfigurer(loader, new KontaktLabelProvider(),
				new DefaultControlFieldProvider(cv, fields),
				new ViewerConfigurer.DefaultButtonProvider(), new SimpleWidgetProvider(
					SimpleWidgetProvider.TYPE_LAZYLIST, SWT.MULTI, null));
		cv.create(vc, parent, SWT.NONE, getViewSite());
		makeActions();
		cv.setObjectCreateAction(getViewSite(), createKontakt);
		menu = new ViewMenus(getViewSite());
		menu.createViewerContextMenu(cv.getViewerWidget(), delKontakt, dupKontakt);
		menu.createMenu(printList);
		menu.createToolbar(printList);
		vc.getContentProvider().startListening();
		vc.getControlFieldProvider().addChangeListener(this);
		cv.addDoubleClickListener(new CommonViewer.DoubleClickListener() {
			public void doubleClicked(PersistentObject obj, CommonViewer cv){
				try {
					KontaktDetailView kdv =
						(KontaktDetailView) getSite().getPage().showView(KontaktDetailView.ID);
					kdv.kb.catchElexisEvent(new ElexisEvent(obj, obj.getClass(),
						ElexisEvent.EVENT_SELECTED));
				} catch (PartInitException e) {
					ExHandler.handle(e);
				}
				
			}
		});
	}
	
	public void dispose(){
		vc.getContentProvider().stopListening();
		vc.getControlFieldProvider().removeChangeListener(this);
		super.dispose();
	}
	
	@Override
	public void setFocus(){
		vc.getControlFieldProvider().setFocus();
	}
	
	public void changed(HashMap<String, String> values){
		ElexisEventDispatcher.clearSelection(Kontakt.class);
	}
	
	public void reorder(String field){
		loader.reorder(field);
	}
	
	/**
	 * ENTER has been pressed in the control fields, select the first listed patient
	 */
	// this is also implemented in PatientenListeView
	public void selected(){
		StructuredViewer viewer = cv.getViewerWidget();
		Object[] elements = cv.getConfigurer().getContentProvider().getElements(viewer.getInput());
		
		if (elements != null && elements.length > 0) {
			Object element = elements[0];
			/*
			 * just selecting the element in the viewer doesn't work if the control fields are not
			 * empty (i. e. the size of items changes): cv.setSelection(element, true); bug in
			 * TableViewer with style VIRTUAL? work-arount: just globally select the element without
			 * visual representation in the viewer
			 */
			if (element instanceof PersistentObject) {
				// globally select this object
				ElexisEventDispatcher.fireSelectionEvent((PersistentObject) element);
			}
		}
	}
	
	/*
	 * Die folgenden 6 Methoden implementieren das Interface ISaveablePart2 Wir benötigen das
	 * Interface nur, um das Schliessen einer View zu verhindern, wenn die Perspektive fixiert ist.
	 * Gibt es da keine einfachere Methode?
	 */
	public int promptToSaveOnClose(){
		return GlobalActions.fixLayoutAction.isChecked() ? ISaveablePart2.CANCEL
				: ISaveablePart2.NO;
	}
	
	public void doSave(IProgressMonitor monitor){ /* leer */
	}
	
	public void doSaveAs(){ /* leer */
	}
	
	public boolean isDirty(){
		return true;
	}
	
	public boolean isSaveAsAllowed(){
		return false;
	}
	
	public boolean isSaveOnCloseNeeded(){
		return true;
	}
	
	private void makeActions(){
		delKontakt =
			new RestrictedAction(AccessControlDefaults.KONTAKT_DELETE,
				Messages.getString("KontakteView.delete")) { //$NON-NLS-1$
				@Override
				public void doRun(){
					Object[] o = cv.getSelection();
					if (o != null) {
						Kontakt k = (Kontakt) o[0];
						
						if (SWTHelper.askYesNo("Wirklich löschen?", k.getLabel())) {
							k.delete();
							cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
						}
					}
				}
			};
		dupKontakt = new Action(Messages.getString("KontakteView.duplicate")) { //$NON-NLS-1$
				@Override
				public void run(){
					Object[] o = cv.getSelection();
					if (o != null) {
						Kontakt k = (Kontakt) o[0];
						Kontakt dup;
						if (k.istPerson()) {
							Person p = Person.load(k.getId());
							dup =
								new Person(p.getName(), p.getVorname(), p.getGeburtsdatum(),
									p.getGeschlecht());
						} else {
							Organisation org = Organisation.load(k.getId());
							dup =
								new Organisation(org.get(Organisation.FLD_NAME1),
									org.get(Organisation.FLD_NAME2));
						}
						dup.setAnschrift(k.getAnschrift());
						cv.getConfigurer().getControlFieldProvider().fireChangedEvent();
						// cv.getViewerWidget().refresh();
					}
				}
			};
		createKontakt = new Action(Messages.getString("KontakteView.create")) { //$NON-NLS-1$
				@Override
				public void run(){
					String[] flds = cv.getConfigurer().getControlFieldProvider().getValues();
					String[] predef = new String[] {
						flds[1], flds[2], StringConstants.EMPTY, flds[3], flds[4], flds[5]
					};
					KontaktErfassenDialog ked =
						new KontaktErfassenDialog(getViewSite().getShell(), predef);
					ked.open();
				}
			};
		
		printList = new Action("Markierte Adressen drucken") {
			{
				setImageDescriptor(Images.IMG_PRINTER.getImageDescriptor());
				setToolTipText("Die in der Liste markierten Kontakte als Tabelle ausdrucken");
			}
			
			public void run(){
				Object[] sel = cv.getSelection();
				String[][] adrs = new String[sel.length][];
				if (sel != null && sel.length > 0) {
					GenericPrintDialog gpl =
						new GenericPrintDialog(getViewSite().getShell(), "Adressliste",
							"Adressliste");
					gpl.create();
					for (int i = 0; i < sel.length; i++) {
						Kontakt k = (Kontakt) sel[i];
						String[] f =
							new String[] {
								Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_NAME3,
								Kontakt.FLD_STREET, Kontakt.FLD_ZIP, Kontakt.FLD_PLACE,
								Kontakt.FLD_PHONE1
							};
						String[] v = new String[f.length];
						k.get(f, v);
						adrs[i] = new String[4];
						adrs[i][0] =
							new StringBuilder(v[0]).append(StringConstants.SPACE).append(v[1])
								.append(StringConstants.SPACE).append(v[2]).toString();
						adrs[i][1] = v[3];
						adrs[i][2] =
							new StringBuilder(v[4]).append(StringConstants.SPACE).append(v[5])
								.toString();
						adrs[i][3] = v[6];
					}
					gpl.insertTable("[Liste]", adrs, null);
					gpl.open();
				}
			}
		};
	}
	
	class KontaktLabelProvider extends DefaultLabelProvider {
		
		@Override
		public String getText(Object element){
			String[] fields =
				new String[] {
					Kontakt.FLD_NAME1, Kontakt.FLD_NAME2, Kontakt.FLD_NAME3, Kontakt.FLD_STREET,
					Kontakt.FLD_ZIP, Kontakt.FLD_PLACE, Kontakt.FLD_PHONE1
				};
			String[] values = new String[fields.length];
			((Kontakt) element).get(fields, values);
			return StringTool.join(values, StringConstants.COMMA);
		}
		
		@Override
		public Image getColumnImage(Object element, int columnIndex){
			// TODO Auto-generated method stub
			return null;
		}
		
		/*
		 * @Override public String getColumnText(Object element, int columnIndex) { Kontakt
		 * k=(Kontakt)element; switch(columnIndex){ case 0: return k.get(Kontakt.FLD_NAME1); case 1:
		 * return k.get(Kontakt.FLD_NAME2); case 2: return k.get(Kontakt.FLD_NAME3); case 3: return
		 * k.get(Kontakt.FLD_STREET); case 4: return k.get(Kontakt.FLD_ZIP); case 5: return
		 * k.get(Kontakt.FLD_PLACE); case 6: return k.get(Kontakt.FLD_PHONE1); } return "?"; }
		 */
	}
}
