/*******************************************************************************
 * Copyright (c) 2006-2011, G. Weirich and Elexis
 * All rights reserved. This program and the accompanying materials
 * are made available under the terms of the Eclipse Public License v1.0
 * which accompanies this distribution, and is available at
 * http://www.eclipse.org/legal/epl-v10.html
 *
 * Contributors:
 *    G. Weirich - initial implementation
 *******************************************************************************/

package ch.elexis.core.ui.views;

import java.text.MessageFormat;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.action.Action;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.SashForm;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.IMemento;
import org.eclipse.ui.ISaveablePart2;
import org.eclipse.ui.IViewSite;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.forms.events.HyperlinkAdapter;
import org.eclipse.ui.forms.events.HyperlinkEvent;
import org.eclipse.ui.forms.widgets.Form;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.eclipse.ui.forms.widgets.Hyperlink;
import org.eclipse.ui.part.ViewPart;

import ch.elexis.admin.AccessControlDefaults;
import ch.elexis.core.constants.StringConstants;
import ch.elexis.core.data.activator.CoreHub;
import ch.elexis.core.data.events.ElexisEvent;
import ch.elexis.core.data.events.ElexisEventDispatcher;
import ch.elexis.core.data.events.ElexisEventListener;
import ch.elexis.core.data.util.Extensions;
import ch.elexis.core.model.ISticker;
import ch.elexis.core.ui.UiDesk;
import ch.elexis.core.ui.actions.GlobalActions;
import ch.elexis.core.ui.actions.GlobalEventDispatcher;
import ch.elexis.core.ui.actions.IActivationListener;
import ch.elexis.core.ui.constants.ExtensionPointConstantsUi;
import ch.elexis.core.ui.data.UiSticker;
import ch.elexis.core.ui.dialogs.AssignStickerDialog;
import ch.elexis.core.ui.dialogs.KontaktSelektor;
import ch.elexis.core.ui.events.ElexisUiEventListenerImpl;
import ch.elexis.core.ui.icons.ImageSize;
import ch.elexis.core.ui.icons.Images;
import ch.elexis.core.ui.text.EnhancedTextField;
import ch.elexis.core.ui.util.IKonsExtension;
import ch.elexis.core.ui.util.IKonsMakro;
import ch.elexis.core.ui.util.Log;
import ch.elexis.core.ui.util.SWTHelper;
import ch.elexis.core.ui.util.ViewMenus;
import ch.elexis.data.Anwender;
import ch.elexis.data.Artikel;
import ch.elexis.data.Fall;
import ch.elexis.data.Konsultation;
import ch.elexis.data.Mandant;
import ch.elexis.data.Patient;
import ch.elexis.data.Rechnungssteller;
import ch.elexis.data.Sticker;
import ch.rgw.tools.StringTool;
import ch.rgw.tools.TimeTool;
import ch.rgw.tools.VersionedResource;
import ch.rgw.tools.VersionedResource.ResourceItem;

/**
 * Behandlungseintrag, Diagnosen und Verrechnung Dg und Verrechnung können wie Drag&Drop aus den
 * entsprechenden Listen.Views auf die Felder gezogen werden.
 * 
 * @author gerry
 * 
 */
public class KonsDetailView extends ViewPart implements IActivationListener, ISaveablePart2 {
	private static final String NO_CONS_SELECTED = Messages.KonsDetailView_NoConsSelected; //$NON-NLS-1$
	public static final String ID = "ch.elexis.Konsdetail"; //$NON-NLS-1$
	public static final String CFG_VERTRELATION = "vertrelation"; //$NON-NLS-1$
	static Log log = Log.get("Detail"); //$NON-NLS-1$
	Hashtable<String, IKonsExtension> hXrefs;
	EnhancedTextField text;
	private Label lBeh, lVersion;
	Hyperlink hlMandant;
	Combo cbFall;
	private Konsultation actKons;
	FormToolkit tk;
	Form form;
	Patient actPat;
	Color defaultBackground;
	
	private DiagnosenDisplay dd;
	private VerrechnungsDisplay vd;
	private Action versionBackAction, purgeAction, saveAction;
	Action versionFwdAction, assignStickerAction;
	int displayedVersion;
	Font emFont;
	Composite cDesc;
	Composite cEtiketten;
	private int[] sashWeights = null;
	private SashForm sash;
	
	private final ElexisEventListener eeli_pat = new ElexisUiEventListenerImpl(Patient.class) {
		@Override
		public void runInUi(ElexisEvent ev){
			setPatient((Patient) ev.getObject());
		};
	};
	
	private final ElexisEventListener eeli_user = new ElexisUiEventListenerImpl(Anwender.class,
		ElexisEvent.EVENT_USER_CHANGED) {
		@Override
		public void runInUi(ElexisEvent ev){
			adaptMenus();
		}
	};
	
	private final ElexisEventListener eeli_kons =
		new ElexisUiEventListenerImpl(Konsultation.class) {
			@Override
			public void runInUi(ElexisEvent ev){
				setKons((Konsultation) ev.getObject());
			}
		};
	
	private final ElexisEventListener eeli_fall = new ElexisUiEventListenerImpl(Fall.class,
		ElexisEvent.EVENT_RELOAD | ElexisEvent.EVENT_DELETE) {
		@Override
		public void runInUi(ElexisEvent ev){
			updateFallCombo();
		};
	};
	
	private final ElexisEventListener eeli_dateChange = new ElexisUiEventListenerImpl(
		Konsultation.class, ElexisEvent.EVENT_UPDATE) {
		@Override
		public void runInUi(ElexisEvent ev){
			setKons((Konsultation) ev.getObject());
		}
	};
	
	@Override
	public void saveState(IMemento memento){
		int[] w = sash.getWeights();
		memento.putString(CFG_VERTRELATION, Integer.toString(w[0]) + StringConstants.COMMA
			+ Integer.toString(w[1]));
		super.saveState(memento);
	}
	
	@Override
	public void createPartControl(final Composite p){
		setTitleImage(Images.IMG_VIEW_CONSULTATION_DETAIL.getImage());
		sash = new SashForm(p, SWT.VERTICAL);
		
		tk = UiDesk.getToolkit();
		form = tk.createForm(sash);
		form.getBody().setLayout(new GridLayout(1, true));
		form.setText(NO_CONS_SELECTED);
		cEtiketten = new Composite(form.getBody(), SWT.NONE);
		cEtiketten.setLayout(new RowLayout(SWT.HORIZONTAL));
		cEtiketten.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		cDesc = new Composite(form.getBody(), SWT.NONE);
		cDesc.setLayout(new RowLayout(SWT.HORIZONTAL));
		cDesc.setLayoutData(SWTHelper.getFillGridData(1, true, 1, false));
		lBeh = tk.createLabel(cDesc, NO_CONS_SELECTED);
		emFont = UiDesk.getFont("Helvetica", 11, SWT.BOLD); //$NON-NLS-1$
		lBeh.setFont(emFont);
		defaultBackground = p.getBackground();
		// lBeh.setBackground();
		hlMandant = tk.createHyperlink(cDesc, "--", SWT.NONE); //$NON-NLS-1$
		hlMandant.addHyperlinkListener(new HyperlinkAdapter() {
			
			@Override
			public void linkActivated(HyperlinkEvent e){
				KontaktSelektor ksl =
					new KontaktSelektor(getSite().getShell(), Mandant.class,
						Messages.KonsDetailView_SelectMandatorCaption, //$NON-NLS-1$
						Messages.KonsDetailView_SelectMandatorBody, new String[] {
							Mandant.FLD_SHORT_LABEL, Mandant.FLD_NAME1, Mandant.FLD_NAME2
						}); //$NON-NLS-1$
				if (ksl.open() == Dialog.OK) {
					actKons.setMandant((Mandant) ksl.getSelection());
					setKons(actKons);
				}
			}
			
		});
		hlMandant.setBackground(p.getBackground());
		
		cbFall = new Combo(form.getBody(), SWT.SINGLE);
		cbFall.addSelectionListener(new SelectionAdapter() {
			
			@Override
			public void widgetSelected(final SelectionEvent e){
				Fall[] faelle = (Fall[]) cbFall.getData();
				int i = cbFall.getSelectionIndex();
				if (i > -1 && i < faelle.length) {
					Fall nFall = faelle[i];
					
					Fall actFall = null;
					String fallId = "";
					if (actKons != null) {
						actFall = actKons.getFall();
						fallId = actFall.getId();
					}
					
					if (!nFall.getId().equals(fallId)) {
						if (!nFall.isOpen()) {
							SWTHelper.alert(Messages.KonsDetailView_CaseClosedCaption, //$NON-NLS-1$
								Messages.KonsDetailView_CaseClosedBody); //$NON-NLS-1$
						} else {
							MessageDialog msd =
								new MessageDialog(
									getViewSite().getShell(),
									Messages.KonsDetailView_ChangeCaseCaption, //$NON-NLS-1$
									Images.IMG_LOGO.getImage(ImageSize._75x66_TitleDialogIconSize),
									MessageFormat.format(
										Messages.KonsDetailView_ConfirmChangeConsToCase,
										new Object[] {
											actFall.getLabel(), nFall.getLabel()
										}), MessageDialog.QUESTION, new String[] {
										Messages.KonsDetailView_Yes, //$NON-NLS-1$
										Messages.KonsDetailView_No
									}, 0); //$NON-NLS-1$
							if (msd.open() == 0) {
								actKons.setFall(nFall);
								setKons(actKons);
							}
						}
					}
				}
			}
			
		});
		GridData gdFall = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		cbFall.setLayoutData(gdFall);
		
		lVersion = tk.createLabel(form.getBody(), Messages.KonsDetailView_actual); //$NON-NLS-1$
		GridData gdVer = new GridData(GridData.FILL_HORIZONTAL | GridData.GRAB_HORIZONTAL);
		lVersion.setLayoutData(gdVer);
		
		text = new EnhancedTextField(form.getBody());
		hXrefs = new Hashtable<String, IKonsExtension>();
		@SuppressWarnings("unchecked")
		List<IKonsExtension> xrefs =
			Extensions.getClasses(
				Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION),
				"KonsExtension", false); //$NON-NLS-1$ //$NON-NLS-2$
		for (IKonsExtension x : xrefs) {
			String provider = x.connect(text);
			hXrefs.put(provider, x);
		}
		text.setXrefHandlers(hXrefs);
		
		@SuppressWarnings("unchecked")
		List<IKonsMakro> makros =
			Extensions.getClasses(
				Extensions.getExtensions(ExtensionPointConstantsUi.KONSEXTENSION),
				"KonsMakro", false); //$NON-NLS-1$ //$NON-NLS-2$
		text.setExternalMakros(makros);
		
		GridData gd =
			new GridData(GridData.FILL_HORIZONTAL | GridData.FILL_VERTICAL | GridData.GRAB_VERTICAL
				| GridData.GRAB_HORIZONTAL);
		text.setLayoutData(gd);
		tk.adapt(text);
		SashForm bf = new SashForm(sash, SWT.HORIZONTAL);
		
		Composite botleft = tk.createComposite(bf);
		botleft.setLayout(new GridLayout(1, false));
		Composite botright = tk.createComposite(bf);
		botright.setLayout(new GridLayout(1, false));
		
		dd = new DiagnosenDisplay(getSite().getPage(), botleft, SWT.NONE);
		dd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		vd = new VerrechnungsDisplay(getSite().getPage(), botright, SWT.NONE);
		vd.setLayoutData(SWTHelper.getFillGridData(1, true, 1, true));
		getSite()
			.registerContextMenu(ID + ".VerrechnungsDisplay", vd.contextMenuManager, vd.viewer);
		getSite().setSelectionProvider(vd.viewer);
		
		makeActions();
		ViewMenus menu = new ViewMenus(getViewSite());
		if (CoreHub.acl.request(AccessControlDefaults.AC_PURGE)) {
			menu.createMenu(versionFwdAction, versionBackAction, GlobalActions.neueKonsAction,
				GlobalActions.delKonsAction, GlobalActions.redateAction, assignStickerAction,
				purgeAction);
		} else {
			menu.createMenu(versionFwdAction, versionBackAction, GlobalActions.neueKonsAction,
				GlobalActions.delKonsAction, GlobalActions.redateAction, assignStickerAction);
		}
		sash.setWeights(sashWeights == null ? new int[] {
			80, 20
		} : sashWeights);
		
		menu.createToolbar(GlobalActions.neueKonsAction, saveAction);
		GlobalEventDispatcher.addActivationListener(this, this);
		text.connectGlobalActions(getViewSite());
		adaptMenus();
		setKons((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
	}
	
	@Override
	public void init(IViewSite site, IMemento memento) throws PartInitException{
		
		if (memento == null) {
			sashWeights = new int[] {
				80, 20
			};
		} else {
			String state = memento.getString(CFG_VERTRELATION);
			if (state == null) {
				state = "80,20"; //$NON-NLS-1$
			}
			String[] sw = state.split(StringConstants.COMMA);
			sashWeights = new int[] {
				Integer.parseInt(sw[0]), Integer.parseInt(sw[1])
			};
		}
		super.init(site, memento);
	}
	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.eclipse.ui.part.WorkbenchPart#dispose()
	 */
	@Override
	public void dispose(){
		GlobalEventDispatcher.removeActivationListener(this, this);
		text.disconnectGlobalActions(getViewSite());
		// emFont.dispose();
		super.dispose();
	}
	
	/** Aktuellen patient setzen */
	private synchronized void setPatient(Patient pat){
		if (pat != null && actPat != null) {
			if (pat.getId().equals(actPat.getId())) {
				if (!form.getText().equals(Messages.KonsDetailView_NoConsSelected)) {
					return;
				}
			}
		}
		for (Control cc : cEtiketten.getChildren()) {
			cc.dispose();
		}
		if (pat == null) {
			pat = ElexisEventDispatcher.getSelectedPatient();
		}
		actPat = pat;
		if (pat != null) {
			form.setText(pat.getPersonalia() + StringTool.space + "(" + pat.getAlter() + ")");
			List<ISticker> etis = pat.getStickers();
			if (etis != null && etis.size() > 0) {
				// Point size = form.getHead().getSize();
				for (ISticker et : etis) {
					if (et != null) {
						new UiSticker((Sticker) et).createForm(cEtiketten);
					}
				}
			}
			updateFallCombo();
		}
		form.layout();
	}
	
	private void updateFallCombo(){
		Patient pat = ElexisEventDispatcher.getSelectedPatient();
		if (pat != null) {
			Fall[] faelle = pat.getFaelle();
			cbFall.removeAll();
			cbFall.setData(faelle);
			for (Fall f : faelle) {
				cbFall.add(f.getLabel());
			}
		}
	}
	
	@Override
	public void setFocus(){
		text.setFocus();
	}
	
	/**
	 * Aktuelle Konsultation setzen.
	 */
	private synchronized void setKons(final Konsultation b){
		
		if (actKons != null && text.isDirty()) {
			actKons.updateEintrag(text.getContentsAsXML(), false);
		}
		
		if (b != null) {
			Fall act = b.getFall();
			setPatient(act.getPatient());
			setKonsText(b, b.getHeadVersion());
			
			Fall[] faelle = (Fall[]) cbFall.getData();
			for (int i = 0; i < faelle.length; i++) {
				if (faelle[i].getId().equals(act.getId())) {
					cbFall.select(i);
					break;
				}
			}
			cbFall.setEnabled(act.isOpen());
			Mandant m = b.getMandant();
			lBeh.setText(Messages.KonsDetailView_ConsOfDate + " " + b.getDatum()); //$NON-NLS-1$
			StringBuilder sb = new StringBuilder();
			if (m == null) {
				sb.append(Messages.KonsDetailView_NotYours); //$NON-NLS-1$
			} else {
				Rechnungssteller rs = m.getRechnungssteller();
				if (rs.getId().equals(m.getId())) {
					sb.append("(").append(m.getLabel()).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
				} else {
					sb.append("(").append(m.getLabel()).append("/").append( //$NON-NLS-1$ //$NON-NLS-2$
						rs.getLabel()).append(")"); //$NON-NLS-1$
				}
			}
			hlMandant.setText(sb.toString());
			hlMandant.setEnabled(CoreHub.acl.request(AccessControlDefaults.KONS_REASSIGN));
			dd.setDiagnosen(b);
			vd.setLeistungen(b);
			if (b.isEditable(false)) {
				text.setEnabled(true);
				text.setToolTipText("");
				lBeh.setForeground(UiDesk.getColor(UiDesk.COL_BLACK));
				lBeh.setBackground(defaultBackground);
			} else {
				text.setToolTipText("Konsultation geschlossen oder nicht von Ihnen");
				lBeh.setForeground(UiDesk.getColor(UiDesk.COL_GREY60));
				lBeh.setBackground(UiDesk.getColor(UiDesk.COL_GREY20));
			}
			
		} else {
			form.setText(NO_CONS_SELECTED);
			lBeh.setText("-"); //$NON-NLS-1$
			hlMandant.setText("--"); //$NON-NLS-1$
			hlMandant.setEnabled(false);
			lVersion.setText(""); //$NON-NLS-1$
			dd.clear();
			vd.clear();
			text.setText(""); //$NON-NLS-1$
			text.setEnabled(false);
		}
		actKons = b;
		cDesc.layout();
	}
	
	void setKonsText(final Konsultation b, final int version){
		String ntext = ""; //$NON-NLS-1$
		if ((version >= 0) && (version <= b.getHeadVersion())) {
			VersionedResource vr = b.getEintrag();
			ResourceItem entry = vr.getVersion(version);
			ntext = entry.data;
			StringBuilder sb = new StringBuilder();
			sb.append("rev. ").append(version).append(Messages.KonsDetailView_of).append( //$NON-NLS-1$ //$NON-NLS-2$
				new TimeTool(entry.timestamp).toString(TimeTool.FULL_GER))
				.append(" (").append(entry.remark).append(")"); //$NON-NLS-1$ //$NON-NLS-2$
			lVersion.setText(sb.toString());
		} else {
			lVersion.setText(""); //$NON-NLS-1$
		}
		text.setText(ntext);
		text.setKons(b);
		displayedVersion = version;
		versionBackAction.setEnabled(version != 0);
		versionFwdAction.setEnabled(version != b.getHeadVersion());
	}
	
	private void makeActions(){
		
		purgeAction = new Action(Messages.KonsDetailView_PurgeOldEntries) { //$NON-NLS-1$
			
				@Override
				public void run(){
					actKons.purgeEintrag();
					ElexisEventDispatcher.fireSelectionEvent(actKons);
				}
				
			};
		versionBackAction = new Action(Messages.KonsDetailView_PreviousEntry) { //$NON-NLS-1$
			
				@Override
				public void run(){
					if (actKons == null) {
						return;
					}
					if (MessageDialog.openConfirm(getViewSite().getShell(),
						Messages.KonsDetailView_ReplaceKonsTextCaption, //$NON-NLS-1$
						Messages.KonsDetailView_ReplaceKonsTextBody)) { //$NON-NLS-1$
						setKonsText(actKons, displayedVersion - 1);
						text.setDirty(true);
					}
				}
				
			};
		versionFwdAction = new Action(Messages.KonsDetailView_nextEntry) { //$NON-NLS-1$
				@Override
				public void run(){
					if (actKons == null) {
						return;
					}
					if (MessageDialog.openConfirm(getViewSite().getShell(),
						Messages.KonsDetailView_ReplaceKonsTextCaption, //$NON-NLS-1$
						Messages.KonsDetailView_ReplaceKonsTextBody2)) { //$NON-NLS-1$
						setKonsText(actKons, displayedVersion + 1);
						text.setDirty(true);
					}
				}
			};
		saveAction = new Action(Messages.KonsDetailView_SaveEntry) { //$NON-NLS-1$
				{
					setImageDescriptor(Images.IMG_DISK.getImageDescriptor());
					setToolTipText(Messages.KonsDetailView_SaveExplicit); //$NON-NLS-1$
				}
				
				@Override
				public void run(){
					save();
				}
			};
		
		versionFwdAction.setImageDescriptor(Images.IMG_NEXT.getImageDescriptor());
		versionBackAction.setImageDescriptor(Images.IMG_PREVIOUS.getImageDescriptor());
		purgeAction.setImageDescriptor(Images.IMG_DELETE.getImageDescriptor());
		assignStickerAction = new Action("Sticker...") {
			{
				setToolTipText("Der Konsultation einer Sticker zuweisen");
			}
			
			@Override
			public void run(){
				if (actKons != null) {
					AssignStickerDialog asd =
						new AssignStickerDialog(getViewSite().getShell(), actKons);
					asd.open();
				}
			}
		};
	}
	
	public void save(){
		if (actKons != null) {
			actKons.updateEintrag(text.getContentsAsXML(), false);
			log.log("saved.", Log.DEBUGMSG); //$NON-NLS-1$
			setKons(actKons);
		} else {
			log.log(getClass().getName() + " save() actKons == null", Log.WARNINGS);
		}
	}
	
	@Override
	public void activation(final boolean mode){
		if ((mode == false) && (text.isDirty())) {
			if (actKons != null) {
				actKons.updateEintrag(text.getContentsAsXML(), false);
				log.log("saved.", Log.DEBUGMSG); //$NON-NLS-1$
			}
			text.setDirty(false);
		} else {
			setKons((Konsultation) ElexisEventDispatcher.getSelected(Konsultation.class));
		}
		
	}
	
	@Override
	public void visible(final boolean mode){
		if (mode == true) {
			ElexisEventDispatcher.getInstance().addListeners(eeli_kons, eeli_pat, eeli_user,
				eeli_dateChange, eeli_fall);
			adaptMenus();
		} else {
			ElexisEventDispatcher.getInstance().removeListeners(eeli_kons, eeli_pat, eeli_user,
				eeli_dateChange, eeli_fall);
		}
		
	}
	
	public void adaptMenus(){
		vd.tVerr.getMenu().setEnabled(CoreHub.acl.request(AccessControlDefaults.LSTG_VERRECHNEN));
		GlobalActions.delKonsAction.setEnabled(CoreHub.acl
			.request(AccessControlDefaults.KONS_DELETE));
		GlobalActions.neueKonsAction.setEnabled(CoreHub.acl
			.request(AccessControlDefaults.KONS_CREATE));
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
	public void doSave(final IProgressMonitor monitor){ /* leer */
	}
	
	@Override
	public void doSaveAs(){ /* leer */
	}
	
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
	
	public void addToVerechnung(Artikel artikel){
		vd.addPersistentObject(artikel);
	}
	
}
